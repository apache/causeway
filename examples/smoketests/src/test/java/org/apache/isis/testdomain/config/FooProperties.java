package org.apache.isis.testdomain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties("foo")
@Data
class FooProperties {

    private boolean flag = false;
    
}
