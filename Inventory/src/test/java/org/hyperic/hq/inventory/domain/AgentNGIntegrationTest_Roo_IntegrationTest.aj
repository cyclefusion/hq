// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.hyperic.hq.inventory.domain;

import org.hyperic.hq.inventory.domain.AgentNGDataOnDemand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect AgentNGIntegrationTest_Roo_IntegrationTest {
    
    declare @type: AgentNGIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: AgentNGIntegrationTest: @ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml");
    
    declare @type: AgentNGIntegrationTest: @Transactional;
    
    @Autowired
    private AgentNGDataOnDemand AgentNGIntegrationTest.dod;
    
    @Test
    public void AgentNGIntegrationTest.testCountAgentNGs() {
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", dod.getRandomAgentNG());
        long count = org.hyperic.hq.inventory.domain.AgentNG.countAgentNGs();
        org.junit.Assert.assertTrue("Counter for 'AgentNG' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void AgentNGIntegrationTest.testFindAgentNG() {
        org.hyperic.hq.inventory.domain.AgentNG obj = dod.getRandomAgentNG();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to provide an identifier", id);
        obj = org.hyperic.hq.inventory.domain.AgentNG.findAgentNG(id);
        org.junit.Assert.assertNotNull("Find method for 'AgentNG' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'AgentNG' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void AgentNGIntegrationTest.testFindAllAgentNGs() {
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", dod.getRandomAgentNG());
        long count = org.hyperic.hq.inventory.domain.AgentNG.countAgentNGs();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'AgentNG', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<org.hyperic.hq.inventory.domain.AgentNG> result = org.hyperic.hq.inventory.domain.AgentNG.findAllAgentNGs();
        org.junit.Assert.assertNotNull("Find all method for 'AgentNG' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'AgentNG' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void AgentNGIntegrationTest.testFindAgentNGEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", dod.getRandomAgentNG());
        long count = org.hyperic.hq.inventory.domain.AgentNG.countAgentNGs();
        if (count > 20) count = 20;
        java.util.List<org.hyperic.hq.inventory.domain.AgentNG> result = org.hyperic.hq.inventory.domain.AgentNG.findAgentNGEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'AgentNG' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'AgentNG' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void AgentNGIntegrationTest.testFlush() {
        org.hyperic.hq.inventory.domain.AgentNG obj = dod.getRandomAgentNG();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to provide an identifier", id);
        obj = org.hyperic.hq.inventory.domain.AgentNG.findAgentNG(id);
        org.junit.Assert.assertNotNull("Find method for 'AgentNG' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyAgentNG(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'AgentNG' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void AgentNGIntegrationTest.testMerge() {
        org.hyperic.hq.inventory.domain.AgentNG obj = dod.getRandomAgentNG();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to provide an identifier", id);
        obj = org.hyperic.hq.inventory.domain.AgentNG.findAgentNG(id);
        boolean modified =  dod.modifyAgentNG(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        org.hyperic.hq.inventory.domain.AgentNG merged = (org.hyperic.hq.inventory.domain.AgentNG) obj.merge();
        obj.flush();
        org.junit.Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        org.junit.Assert.assertTrue("Version for 'AgentNG' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void AgentNGIntegrationTest.testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", dod.getRandomAgentNG());
        org.hyperic.hq.inventory.domain.AgentNG obj = dod.getNewTransientAgentNG(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'AgentNG' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'AgentNG' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void AgentNGIntegrationTest.testRemove() {
        org.hyperic.hq.inventory.domain.AgentNG obj = dod.getRandomAgentNG();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'AgentNG' failed to provide an identifier", id);
        obj = org.hyperic.hq.inventory.domain.AgentNG.findAgentNG(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'AgentNG' with identifier '" + id + "'", org.hyperic.hq.inventory.domain.AgentNG.findAgentNG(id));
    }
    
}
