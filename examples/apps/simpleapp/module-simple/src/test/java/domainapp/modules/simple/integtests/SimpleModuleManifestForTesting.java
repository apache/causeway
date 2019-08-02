/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package domainapp.modules.simple.integtests;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.isis.config.Presets;
import org.apache.isis.config.beans.IsisBeanScanInterceptorForSpring;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.extensions.fixtures.IsisBootFixtures;
import org.apache.isis.jdo.IsisBootDataNucleus;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.security.IsisBootSecurityBypass;

import domainapp.modules.simple.SimpleModule;


/**
 * Makes the integral parts of the 'module-simple' integration tests.
 */
@Configuration
@PropertySources({
	//@PropertySource("classpath:/domainapp/application/manifest/isis-non-changing.properties"),
	@PropertySource(name=Presets.HsqlDbInMemory, factory = Presets.Factory.class, value = { "" }),
    @PropertySource(name=Presets.DataNucleusAutoCreate, factory = Presets.Factory.class, value = { "" }),
})
@Import({
    IsisBoot.class,
    IsisBootSecurityBypass.class,
    IsisBootDataNucleus.class,
    IsisBootFixtures.class
})
@ComponentScan(
        basePackageClasses= {
                SimpleModule.class
        },
        includeFilters= {
                @Filter(type = FilterType.CUSTOM, classes= {IsisBeanScanInterceptorForSpring.class})
        })
public class SimpleModuleManifestForTesting {
    
   @Bean @Singleton
   public WebAppConfigBean webAppConfigBean() {
       return WebAppConfigBean.builder()
               .build();
   }
   
}
