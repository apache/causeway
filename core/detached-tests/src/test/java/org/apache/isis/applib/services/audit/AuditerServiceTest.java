package org.apache.isis.applib.services.audit;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.testdomain.jdo.Inventory;
import org.apache.isis.testdomain.jdo.JdoTestDomainIntegTest;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

class AuditerServiceTest extends JdoTestDomainIntegTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        
        // cleanup
        fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.PurgeAll.builder());
        
        // given
        inventory = fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.InventoryWith1Book.builder());
    }
    
    @Test
    void sampleInventoryShouldBeSetUp() {
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(1, inventory.getProducts().size());
    }
    
    
}
