package org.apache.isis.core.webserver;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.config.AppConfigLocator;
import org.apache.isis.config.IsisConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldAppConfigTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void test() {

        // when
        IsisConfiguration isisConfiguration = AppConfigLocator.getAppConfig().isisConfiguration();

        // then
        Assertions.assertNotNull(isisConfiguration);
        
        Map<String, String> config = isisConfiguration.asMap();
        Assertions.assertNotNull(config);
        assertThat(config).hasSize(1);
        assertThat(config.get("isis.appManifest")).isEqualTo(DummyAppManifest.class.getName());
    }

}
