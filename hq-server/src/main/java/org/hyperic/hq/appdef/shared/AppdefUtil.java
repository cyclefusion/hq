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

package org.hyperic.hq.appdef.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hyperic.hq.appdef.server.session.Server;
import org.hyperic.hq.authz.shared.AuthzConstants;
import org.hyperic.hq.inventory.domain.Resource;
import org.hyperic.hq.inventory.domain.ResourceGroup;
import org.hyperic.hq.inventory.domain.ResourceType;
import org.hyperic.hq.reference.RelationshipTypes;

/** AppdefUtil - utility methods for appdef entities and
* brethren.
*
* <a href="mailto:desmond@covalent.net">desmond</a>
* */

public class AppdefUtil {


    /** Translate appdefTypeId to authz resource type string.
     * @param appdef type id
     * @return authz resource type string
     * @throws InvalidAppdefTypeException
     */
    public static String appdefTypeIdToAuthzTypeStr (int adTypeId)
        throws InvalidAppdefTypeException {
        
        switch (adTypeId) {
        case AppdefEntityConstants.APPDEF_TYPE_PLATFORM:
            return AuthzConstants.platformResType;
        case AppdefEntityConstants.APPDEF_TYPE_SERVER:
            return AuthzConstants.serverResType;
        case AppdefEntityConstants.APPDEF_TYPE_SERVICE:
            return AuthzConstants.serviceResType;
        case AppdefEntityConstants.APPDEF_TYPE_APPLICATION:
            return AuthzConstants.applicationResType;
        case AppdefEntityConstants.APPDEF_TYPE_GROUP:
            return AuthzConstants.groupResType;
        default:
            throw new InvalidAppdefTypeException("No authz resource type "
                    + "String provisioned for appdef type argument.");
        }
    }

    /** Transform an authz resource type name into an appdef type id.
     * @param authz resource name
     * @return AppdefEntity type id
     * @throws InvalidAppdefTypeException
     */
    public static int resNameToAppdefTypeId (String resName)
        throws InvalidAppdefTypeException {

        if ( resName.equals(AuthzConstants.platformResType))
            return AppdefEntityConstants.APPDEF_TYPE_PLATFORM;
        if ( resName.equals(AuthzConstants.serverResType))
            return AppdefEntityConstants.APPDEF_TYPE_SERVER;
        if ( resName.equals(AuthzConstants.serviceResType))
            return AppdefEntityConstants.APPDEF_TYPE_SERVICE;
        if ( resName.equals(AuthzConstants.applicationResType))
            return AppdefEntityConstants.APPDEF_TYPE_APPLICATION;
        if ( resName.equals(AuthzConstants.groupResType))
            return AppdefEntityConstants.APPDEF_TYPE_GROUP;

        throw new InvalidAppdefTypeException("No appdef entity type "+
            "provisioned for authz resource name argument: " + resName);
    }

    /**
     * Runtime scans are supported servers that were not autodiscovered
     * themselves, as well as servers that were autodiscovered but
     * whose services are not automanaged (if they were, it would mean
     * that the server that autodiscovered them is managing its services,
     * so runtime scans should not be run for this server).
     * @return true if runtime scans could possibly be enabled for the
     * server, false otherwise.
     */
    public static boolean areRuntimeScansEnabled ( Server server ) {
        return ( server.isRuntimeAutodiscovery() &&
                 ( !server.isWasAutodiscovered() ||
                   ( server.isWasAutodiscovered() &&
                     !server.isServicesAutomanaged()) ));
    }
    
    public static AppdefEntityID newAppdefEntityId(Resource rv) {   
        ResourceType resType = rv.getType();
         
        if (resType == null) {
                throw new IllegalArgumentException(rv.getName() + 
                    " does not have a Resource Type");
        }
        int entityID = rv.getId();
            
        return new AppdefEntityID(getAppdefType(resType), entityID);
    }
    
    public static int getAppdefType(ResourceType rv) {
        if(!(rv.getResourceTypesTo(RelationshipTypes.PLATFORM).isEmpty())) {
            return AppdefEntityConstants.APPDEF_TYPE_PLATFORM;
        }
        if(!(rv.getResourceTypesTo(RelationshipTypes.SERVER).isEmpty()) || !(rv.getResourceTypesTo(RelationshipTypes.VIRTUAL).isEmpty())) {
            return AppdefEntityConstants.APPDEF_TYPE_SERVER;
        }
        if(!(rv.getResourceTypesTo(RelationshipTypes.SERVICE)).isEmpty()) {
            return AppdefEntityConstants.APPDEF_TYPE_SERVICE;
        }
        if(rv.getName().equals(AppdefEntityConstants.APPDEF_NAME_APPLICATION)){
            return AppdefEntityConstants.APPDEF_TYPE_APPLICATION;
        }
    
        int[] groupTypes = AppdefEntityConstants.getAppdefGroupTypes();
        for(int i=0;i< groupTypes.length;i++) {
            if(rv.getName().equals(AppdefEntityConstants.getAppdefGroupTypeName(groupTypes[i]))) {
                return AppdefEntityConstants.APPDEF_TYPE_GROUP;
            }
        }
    
        throw new IllegalArgumentException(rv.getName() + 
            " is not a valid Appdef Resource Type");
    }
    
    public static AppdefEntityID newAppdefEntityId(ResourceType resourceType) {
        return new AppdefEntityID(getAppdefType(resourceType), resourceType.getId());
    }
    
    public static Map groupByAppdefType(AppdefEntityID[] ids) {
        HashMap m = new HashMap();
        for (int i = 0; i < ids.length; i++) {
            Integer type = new Integer(ids[i].getType());
            ArrayList idList = (ArrayList) m.get(type);
            if (idList == null) {
                idList = new ArrayList();
                m.put(type, idList);
            }
            idList.add(ids[i].getId());
        }
        return m;
    }

    private AppdefUtil () {}
}
