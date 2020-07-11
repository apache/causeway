package org.apache.isis.testdomain.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.apache.isis.testdomain.model.stereotypes.MyService;

@Configuration
@ComponentScan(
        basePackageClasses= {               
                MyService.class
        })
public class Configuration_usingStereotypes {

}
