/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.testdomain.jdo;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import org.apache.isis.applib.services.fixturespec.FixtureScriptsSpecificationProvider;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.beans.WebAppConfigBean;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.runtime.authorization.standard.AuthorizationManagerStandard;
import org.apache.isis.core.security.authentication.bypass.AuthenticatorBypass;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.standard.AuthenticationManagerStandard;
import org.apache.isis.core.security.authorization.bypass.AuthorizorBypass;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;
import org.apache.isis.core.security.authorization.standard.Authorizor;

@Configuration
//@PropertySource("classpath:/org/apache/isis/testdomain/jdo/isis-non-changing.properties")
@PropertySource("file:src/test/java/org/apache/isis/testdomain/jdo/isis-non-changing.properties")
public class JdoTestDomainModule {
    
    @Bean @Produces @Singleton
    public IsisConfiguration getConfig() {
        return _Config.getConfiguration();
    }
    
    /**
    * The standard authentication manager, configured with the 'bypass' authenticator 
    * (allows all requests through).
    * <p>
    * integration tests ignore appManifest for authentication and authorization.
    */
   @Bean @Produces @Singleton
   public AuthenticationManager authenticationManagerWithBypass() {
       final AuthenticationManagerStandard authenticationManager = new AuthenticationManagerStandard();
       authenticationManager.addAuthenticator(new AuthenticatorBypass());
       return authenticationManager;
   }
   
   @Bean @Produces @Singleton
   public AuthorizationManager authorizationManagerWithBypass() {
       final AuthorizationManagerStandard authorizationManager = new AuthorizationManagerStandard() {
           {
               authorizor = new AuthorizorBypass();
           }  
       };
       return authorizationManager;
   }
   
   @Bean @Produces @Singleton
   public Authorizor autorizor() {
       return new AuthorizorBypass();
   }
   
   
   @Bean @Produces @Singleton
   public WebAppConfigBean webAppConfigBean() {
       return WebAppConfigBean.builder()
               //.menubarsLayoutXml(new ClassPathResource(path, clazz))
               .build();
   }
   
   //@Bean @Produces @Singleton // as used in simple-app
   public FixtureScriptsSpecificationProvider fixtureScriptsSpecificationProvider() {
       return null;
   }
    
}
