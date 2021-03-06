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

package org.hyperic.hq.ui.action.resource.common.monitor.alerts.config;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hyperic.hq.appdef.shared.AppdefEntityID;
import org.hyperic.hq.bizapp.shared.EventsBoss;
import org.hyperic.hq.ui.Constants;
import org.hyperic.hq.ui.action.BaseAction;
import org.hyperic.hq.ui.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A class to enable or disable alerts for a resource and all its children
 */
public class AlertEnablerAction
    extends BaseAction {

    private EventsBoss eventsBoss;

    @Autowired
    public AlertEnablerAction(EventsBoss eventsBoss) {
        super();
        this.eventsBoss = eventsBoss;
    }

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        Integer sessionId = RequestUtils.getSessionId(request);
        AppdefEntityID eid = RequestUtils.getEntityId(request);
        String state = RequestUtils.getStringParameter(request, Constants.ALERT_STATE_PARAM);

        HashMap<String, Object> forwardParams = new HashMap<String, Object>(3);

        forwardParams.put(Constants.RESOURCE_PARAM, eid.getId());
        forwardParams.put(Constants.RESOURCE_TYPE_ID_PARAM, new Integer(eid.getType()));
        forwardParams.put(Constants.MODE_PARAM, Constants.MODE_LIST);
        boolean enabled = true;
        if (state.equals(Constants.MODE_DISABLED)) {
            enabled = false;
        }
        eventsBoss.activateAlertDefinitions(sessionId.intValue(), new AppdefEntityID[] { eid }, enabled);

        return this.returnSuccess(request, mapping, forwardParams, BaseAction.YES_RETURN_PATH);
    }
}
