package domainapp.application;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.core.commons.config.AppConfigLocator;
import org.apache.isis.core.commons.config.IsisConfiguration;

class HelloWorldAppConfigTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void test() {
        
        IsisConfiguration isisConfiguration = AppConfigLocator.getAppConfig().isisConfiguration();
        Assertions.assertNotNull(isisConfiguration);
        
        Map<String, String> config = isisConfiguration.asMap();
        Assertions.assertNotNull(config);
        Assertions.assertTrue(config.size()>20);
    }

}
