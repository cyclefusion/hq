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

package org.hyperic.hq.product;

import java.io.File;
import java.util.Properties;

import org.hyperic.cm.filemonitor.FileMonitor;
import org.hyperic.cm.filemonitor.IFileMonitor;
import org.hyperic.cm.filemonitor.MonitorStatus;
import org.hyperic.hq.agent.AgentConfig;

public class ConfigTrackPluginManager extends TrackEventPluginManager {

    private static IFileMonitor fileMonitor = FileMonitor.getInstance();

     public ConfigTrackPluginManager() {
        super();        
        initMonitor();
    }

     public ConfigTrackPluginManager(Properties props) {
         super(props);
         initMonitor();
     }

    private void initMonitor() {
        // determine the data dir for tracking local data
        final String dataDir = getProperty(AgentConfig.PROP_DATADIR[0],
            AgentConfig.PROP_DATADIR[1]);
        final File f = new File(dataDir);

        // get property for max diff size
        fileMonitor.setAppDataDir(f.getAbsolutePath());
        final String maxDiffSize = getProperty("hq.plugins.configmon.maxdiff", "5012");
        long size = -1;
        try{
            size = Long.valueOf(maxDiffSize).longValue();
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        fileMonitor.setMaxDiffSize(size);

        // start the monitor, no events will be tracked until tracked folders are defined.
        if (!(MonitorStatus.STARTED.equals(fileMonitor.getStatus())))
            fileMonitor.start();
    }

    public String getName() {
        return ProductPlugin.TYPE_CONFIG_TRACK;
    }
    
    public IFileMonitor getFileMonitor(){
        return fileMonitor;
    }

    @Override
    public void shutdown() throws PluginException {
        fileMonitor.stop();
        fileMonitor = null;
        super.shutdown();
    }
    
    
}
