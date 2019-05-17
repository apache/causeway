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
package domainapp.application;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.io.ClassPathResource;

import org.apache.isis.config.Presets;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.security.shiro.IsisSecurityBootUsingShiro;
import org.apache.isis.viewer.wicket.viewer.IsisWebWicketBoot;

/**
 * Makes the integral parts of the 'hello world' web application.
 */
@Configuration
@PropertySources({
    @PropertySource("classpath:/domainapp/application/isis-non-changing.properties"),
    @PropertySource(name=Presets.H2InMemory, factory = Presets.Factory.class, value = { "" }),
    @PropertySource(name=Presets.NoTranslations, factory = Presets.Factory.class, value = { "" }),
})
@Import({
    IsisWebWicketBoot.class,
    IsisSecurityBootUsingShiro.class
})
public class HelloWorldAppManifest {
    
   @Bean @Singleton
   public WebAppConfigBean webAppConfigBean() {
       return WebAppConfigBean.builder()
               .menubarsLayoutXml(new ClassPathResource("menubars.layout.xml", this.getClass()))
               .build();
   }

}
