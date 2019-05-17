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
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.security.authentication.bypass.AuthenticatorBypass;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.security.authorization.bypass.AuthorizorBypass;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;
import org.apache.isis.core.security.authorization.standard.Authorizor;
import org.apache.isis.viewer.wicket.viewer.IsisWicketModule;

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
    IsisWicketModule.class
})
public class HelloWorldAppManifest {
    
    /**
    * The standard authentication manager, configured with the 'bypass' authenticator 
    * (allows all requests through).
    * <p>
    * integration tests ignore appManifest for authentication and authorization.
    */
   @Bean @Singleton
   public AuthenticationManager authenticationManagerWithBypass() {
       final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard();
       authenticationManager.addAuthenticator(new AuthenticatorBypass());
       return authenticationManager;
   }
   
   @Bean @Singleton
   public AuthorizationManager authorizationManagerWithBypass() {
       final AuthorizationManagerStandard authorizationManager = new AuthorizationManagerStandard() {
           {
               authorizor = new AuthorizorBypass();
           }  
       };
       return authorizationManager;
   }
   
   @Bean @Singleton
   public Authorizor autorizor() {
       return new AuthorizorBypass();
   }
   
   
   @Bean @Singleton
   public WebAppConfigBean webAppConfigBean() {
       return WebAppConfigBean.builder()
               .menubarsLayoutXml(new ClassPathResource("menubars.layout.xml", this.getClass()))
               .build();
   }

}
