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

package org.hyperic.hq.authz.server.session;

import org.hyperic.dao.DAOFactory;
import org.hyperic.hq.authz.shared.AuthzConstants;
import org.hyperic.hq.authz.shared.ResourceTypeValue;
import org.hyperic.hq.authz.shared.ResourceValue;
import org.hyperic.hq.dao.HibernateDAO;

public class ResourceTypeDAO extends HibernateDAO
{
    public ResourceTypeDAO(DAOFactory f) {
        super(ResourceType.class, f);
    }

    public ResourceType create(AuthzSubject creator,
                               ResourceTypeValue createInfo)
    {
        ResourceType resType = new ResourceType(createInfo);
        save(resType);

        // We have to create a new resource
        ResourceValue resValue = new ResourceValue();

        ResourceType typeResType = findByName(AuthzConstants.typeResourceTypeName); 
            
        if (typeResType == null) {
            throw new IllegalArgumentException("Resource Type not found: " +
                                               AuthzConstants.typeResourceTypeName);
        }
        resValue.setResourceTypeValue(typeResType.getResourceTypeValue());

        resValue.setInstanceId(resType.getId());
        resValue.setName(resType.getName());
        Resource resource = 
            DAOFactory.getDAOFactory().getResourceDAO().create(creator, resValue);
        resType.setResource(resource);

        ResourceGroup authzGroup = 
            DAOFactory.getDAOFactory().getResourceGroupDAO()
            .findByName(AuthzConstants.authzResourceGroupName);
        if (authzGroup == null) {
            throw new IllegalArgumentException("Resource Group not found: " +
                                               AuthzConstants.authzResourceGroupName);
        }
        authzGroup.addResource(resource);

        return resType;
    }

    public ResourceType findById(Integer id) {
        return (ResourceType) super.findById(id);
    }

    public void save(ResourceType entity) {
        super.save(entity);
    }

    public void remove(AuthzSubject whoami, ResourceType entity) {
        // XXX need to check against owner
        super.remove(entity);
    }

    public ResourceType findByName(String name)
    {
        String sql = "from ResourceType where name=?";
        return (ResourceType)getSession().createQuery(sql)
            .setString(0, name)
            .setCacheable(true)
            .setCacheRegion("ResourceType.findByName")
            .uniqueResult();
    }
}
