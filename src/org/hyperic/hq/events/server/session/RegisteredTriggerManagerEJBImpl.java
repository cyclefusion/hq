/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2004, 2005, 2006], Hyperic, Inc.
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

package org.hyperic.hq.events.server.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.hyperic.dao.DAOFactory;
import org.hyperic.hq.events.ext.RegisteredTriggerEvent;
import org.hyperic.hq.events.shared.RegisteredTriggerPK;
import org.hyperic.hq.events.shared.RegisteredTriggerValue;

/** 
 * The trigger manager.
 *
 * @ejb:bean name="RegisteredTriggerManager"
 *      jndi-name="ejb/events/RegisteredTriggerManager"
 *      local-jndi-name="LocalRegisteredTriggerManager"
 *      view-type="local"
 *      type="Stateless"
 * 
 * @ejb:transaction type="SUPPORTS"
 */

public class RegisteredTriggerManagerEJBImpl implements SessionBean {
    private TriggerDAO getTriggerDAO(){
        return DAOFactory.getDAOFactory().getTriggerDAO();
    }

    private AlertDefinitionDAO getAlertDefDAO(){
        return DAOFactory.getDAOFactory().getAlertDefDAO();
    }

    private RegisteredTrigger getRegisteredTrigger(Integer trigId) {
        return getTriggerDAO().findById(trigId);
    }
    
    /**
     * Get a collection of all triggers
     *
     * @ejb:transaction type="REQUIRED"
     * @ejb:interface-method
     */
    public Collection getAllTriggers() {
        Collection triggers;
        List triggerValues = new ArrayList();

        triggers = getTriggerDAO().findAll();

        for (Iterator i = triggers.iterator(); i.hasNext(); ) {
            RegisteredTrigger t = (RegisteredTrigger) i.next();
            
            triggerValues.add(t.getRegisteredTriggerValue());
        }

        return triggerValues;
    }

    /**
     * Create a new trigger
     *
     * @return a RegisteredTriggerValue 
     *
     * @ejb:transaction type="REQUIRED"
     * @ejb:interface-method
     */
    public RegisteredTriggerValue createTrigger(RegisteredTriggerValue val) {
        // XXX -- Things here aren't symmetrical.  The EventsBoss is currently
        // registering the trigger with the dispatcher, and updateTrigger()
        // is updating it with the dispatcher.  Seems like this should all
        // be done here in the manager
        return getTriggerDAO().create(val).getRegisteredTriggerValue();
    }

    /**
     * Update a trigger.
     *
     * @ejb:transaction type="REQUIRED"
     * @ejb:interface-method
     */
    public void updateTrigger(RegisteredTriggerValue val) {
        RegisteredTrigger t = getRegisteredTrigger(val.getId());

        t.setRegisteredTriggerValue(val);
        
        // Re-register the trigger with the dispatcher
        RegisteredTriggerNotifier.broadcast(RegisteredTriggerEvent.UPDATE, val);
    }

    /**
     * Delete a trigger.
     *
     * @ejb:interface-method
     * @ejb:transaction type="REQUIRED"
     */
    public void deleteTrigger(Integer trigId) {
        RegisteredTrigger t = getRegisteredTrigger(trigId);
        RegisteredTriggerValue val = t.getRegisteredTriggerValue();

        getTriggerDAO().remove(t);
        
        // Unregister the trigger with the dispatcher
        RegisteredTriggerNotifier.broadcast(RegisteredTriggerEvent.DELETE, val);
    }

    /**
     * Delete all triggers for an alert definition.
     *
     * @ejb:interface-method
     * @ejb:transaction type="REQUIRED"
     */
    public void deleteAlertDefinitionTriggers(Integer adId) {
        AlertDefinition def = getAlertDefDAO().findById(adId);
        Collection triggers = new ArrayList(def.getTriggers());
        RegisteredTriggerValue[] vals =
            new RegisteredTriggerValue[triggers.size()];

        int i = 0;
        for (Iterator it = triggers.iterator(); it.hasNext(); i++) {
            RegisteredTrigger t = (RegisteredTrigger) it.next();
            
            vals[i] = t.getRegisteredTriggerValue();
            getTriggerDAO().remove(t);
        }       

        // Unregister the trigger with the dispatcher
        RegisteredTriggerNotifier.broadcast(RegisteredTriggerEvent.DELETE, 
                                            vals);
    }

    public void ejbCreate() {}

    public void ejbRemove() {}

    public void ejbActivate() {}

    public void ejbPassivate() {}

    public void setSessionContext(SessionContext ctx) {}
}
