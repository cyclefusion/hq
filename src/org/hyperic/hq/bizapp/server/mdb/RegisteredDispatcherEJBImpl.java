/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004-2008], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.bizapp.server.mdb;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperic.hq.bizapp.server.trigger.conditional.MultiConditionTrigger;
import org.hyperic.hq.common.SystemException;
import org.hyperic.hq.common.util.Messenger;
import org.hyperic.hq.events.AbstractEvent;
import org.hyperic.hq.events.EventConstants;
import org.hyperic.hq.events.FlushStateEvent;
import org.hyperic.hq.events.TriggerInterface;
import org.hyperic.hq.events.ext.RegisteredTriggers;
import org.hyperic.hq.events.server.session.EventProcessingTxnWrapperEJBImpl;
import org.hyperic.hq.events.shared.EventProcessingTxnWrapperLocal;


/** The RegisteredDispatcher Message-Drive Bean registers Triggers and
 * dispatches events to them
 * <p>
 *
 * </p>
 * @ejb:bean name="RegisteredDispatcher"
 *      jndi-name="ejb/event/RegisteredDispatcher"
 *      local-jndi-name="LocalRegisteredDispatcher"
 *      transaction-type="Container"
 *      acknowledge-mode="Auto-acknowledge"
 *      destination-type="javax.jms.Topic"
 *
 * @ejb:transaction type="REQUIRED"
 *
 * @jboss:destination-jndi-name name="topic/eventsTopic"
 */
public class RegisteredDispatcherEJBImpl 
    implements MessageDrivenBean, MessageListener 
{
    private final Log log =
        LogFactory.getLog(RegisteredDispatcherEJBImpl.class);
    
    
    /**
     * Dispatch the event to interested triggers.
     * 
     * @param event The event.
     * @param visitedMCTriggers The set of visited multicondition triggers 
     *                          that will be updated if a trigger of this type 
     *                          processes this event.
     * @param txnWrapper The event processing txn wrapper.                       
     */
    private void dispatchEvent(AbstractEvent event, 
                               Set visitedMCTriggers, 
                               EventProcessingTxnWrapperLocal txnWrapper) 
        throws InterruptedException {        
        // Get interested triggers
        Collection triggers =
            RegisteredTriggers.getInterestedTriggers(event);
        
        //log.debug("There are " + triggers.size() + " registered for event");

        // Dispatch to each trigger
        for (Iterator i = triggers.iterator(); i.hasNext(); ) {
            TriggerInterface trigger = (TriggerInterface) i.next();
            try {
                updateVisitedMCTriggersSet(visitedMCTriggers, trigger);                
                txnWrapper.processEvent(trigger, event);
            } catch (SystemException e) {
                log.error("Event processing failed for trigger id="+
                           trigger.getId(), e);
            }
        }            
        
    }

    private void updateVisitedMCTriggersSet(Set visitedMCTriggers,
                                            TriggerInterface trigger) 
        throws InterruptedException {
        
        if (trigger instanceof MultiConditionTrigger) {
            boolean firstTimeVisited = visitedMCTriggers.add(trigger);

            try {
                if (firstTimeVisited) {
                    ((MultiConditionTrigger)trigger).acquireSharedLock();                        
                }                            
            } catch (InterruptedException e) {
                // failed to acquire shared lock - we will not visit this trigger
                visitedMCTriggers.remove(trigger);
                // reset the interrupted state
                Thread.currentThread().interrupt();
                throw e;
            }
        }
    } 
    
    /**
     * The onMessage method
     */
    public void onMessage(Message inMessage) {
        if (!(inMessage instanceof ObjectMessage)) {
            return;
        }
        
        // Just to be safe, start with a fresh queue.
        Messenger.resetThreadLocalQueue();
        final Set visitedMCTriggers = new HashSet();
        
        EventProcessingTxnWrapperLocal txnWrapper = 
            EventProcessingTxnWrapperEJBImpl.getOne();

        try {
            ObjectMessage om = (ObjectMessage) inMessage;
            Object obj = om.getObject();
                       
            if (obj instanceof AbstractEvent) {
                AbstractEvent event = (AbstractEvent) obj;
                dispatchEvent(event, visitedMCTriggers, txnWrapper);
            } else if (obj instanceof Collection) {
                Collection events = (Collection) obj;
                for (Iterator it = events.iterator(); it.hasNext(); ) {
                    AbstractEvent event = (AbstractEvent) it.next();
                    dispatchEvent(event, visitedMCTriggers, txnWrapper);
                }
            }
        } catch (JMSException e) {
            log.error("Cannot open message object", e);
        } catch (InterruptedException e) {
            log.info("Thread was interrupted while processing events.");
        } finally {
            try {
                flushStateForVisitedMCTriggers(visitedMCTriggers, txnWrapper);
            } catch (Exception e) {
                log.error("Failed to flush state for multi condition trigger", e);
            }
            
            dispatchEnqueuedEvents();
        }
    }
    
    private void flushStateForVisitedMCTriggers(Set visitedMCTriggers, 
                                    EventProcessingTxnWrapperLocal txnWrapper)  
        throws InterruptedException {
        
        if (visitedMCTriggers.isEmpty()) {
            return;
        }        
        
        try {
            FlushStateEvent event = new FlushStateEvent();

            for (Iterator it = visitedMCTriggers.iterator(); it.hasNext();) {
                MultiConditionTrigger trigger = (MultiConditionTrigger) it.next();
                it.remove();

                if (trigger.triggeringConditionsFulfilled()) {
                    try {   
                        tryFlushState(event, trigger, txnWrapper);    
                    } catch (SystemException e) {
                        // Continue flushing the other triggers. The transaction 
                        // wrapping the current trigger flush will be rolled back.
                        log.error("Failed to flush state for multi " +
                        		  "condition trigger id="+trigger.getId(), e);                        
                    }
                } else {
                    trigger.releaseSharedLock();
                }
            }

        } finally {
            // The visitedMCTriggers list should always be empty, but release 
            // shared locks in case it's not. This is very important. If the 
            // shared lock isn't released then the multicondition trigger 
            // may never fire again until the server is rebooted!
            if (!visitedMCTriggers.isEmpty()) {
                for (Iterator it = visitedMCTriggers.iterator(); it.hasNext();) {
                    MultiConditionTrigger trigger = (MultiConditionTrigger) it.next();
                    it.remove();
                    trigger.releaseSharedLock();
                }
            }
        }
        
    }

    /**
     * Flush the multi condition trigger state if the current thread is able 
     * to upgrade the shared locked to an exclusive lock.
     * 
     * @param event The flush event.
     * @param trigger The multi condition trigger. 
     * @throws InterruptedException if the current thread is interrupted. If this 
     *                              happens, then all locks on the trigger are 
     *                              guaranteed to have been released.
     * @param txnWrapper The event processing txn wrapper.
     * @throws SystemException if trigger state flushing fails.
     */
    private void tryFlushState(FlushStateEvent event, 
                               MultiConditionTrigger trigger,
                               EventProcessingTxnWrapperLocal txnWrapper) 
        throws InterruptedException, SystemException {
        
        boolean lockAcquired = false;

        try {
            lockAcquired = trigger.upgradeSharedLockToExclusiveLock();

            if (lockAcquired) {
                txnWrapper.processEvent(trigger, event);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There must be more interesting events "+
                            "to multicondition alert with trigger id="+
                            trigger.getId()+" since we failed to upgrade "+
                            "shared lock on flushing state. Current shared " +
                            "lock holders: "+trigger.getCurrentSharedLockHolders());    
                }                            
            }                        
        } finally {
            if (lockAcquired) {
                trigger.releaseExclusiveLock();
            }
        }
    }
    
    private void dispatchEnqueuedEvents() {
        List enqueuedEvents = Messenger.drainEnqueuedMessages();
        
        if (enqueuedEvents.isEmpty()) {
            return;
        }

        Messenger sender = new Messenger();
        sender.publishMessage(EventConstants.EVENTS_TOPIC, 
                              (Serializable)enqueuedEvents);
    }
            
    /**
     * @ejb:create-method
     */
    public void ejbCreate() {}
    public void ejbPostCreate() {}
    public void ejbActivate() {}
    public void ejbPassivate() {}

    /**
     * @ejb:remove-method
     */
    public void ejbRemove() {}

    public void setMessageDrivenContext(MessageDrivenContext ctx) {}
}
