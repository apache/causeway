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
package org.apache.isis.extensions.secman.jdo.seed;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.extensions.fixtures.legacy.fixturescripts.FixtureScripts;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUserRepository;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.runtime.system.session.IsisSessionFactory;
import org.apache.isis.security.authentication.AuthenticationSession;
import org.springframework.core.annotation.Order;

@DomainService(
        nature = NatureOfService.DOMAIN
)
@Order(-1000)
public class SeedSecurityModuleService {

    @Inject FixtureScripts fixtureScripts;
    
    @PostConstruct
    public void init() {
       
     // fixtureScripts.runFixtureScript(new SeedUsersAndRolesFixtureScript(), null);
        
        IsisContext.getAuthenticationSession()
        .ifPresent(authenticationSession->{
        	System.out.println("!!! authenticationSession " + authenticationSession);
        	
        	IsisContext.compute(()->run(authenticationSession));
        	
        });
        
    }
    
    private String run(AuthenticationSession authenticationSession) {
        
        System.out.println("!!! wait for SeedSecurityModule to start");
        
        try {
            
            Thread.sleep(5000);
            
            System.out.println("!!! SeedSecurityModule starting");
            
            final IsisSessionFactory sf = IsisContext.getSessionFactory();
            IsisSession session = sf.openSession(authenticationSession);
            
            fixtureScripts.runFixtureScript(new SeedUsersAndRolesFixtureScript(), null);
            
            session.getCurrentTransaction().flush();
            
            
        } catch (Exception e) {
            e.printStackTrace();
            
            return "FAILED";
        }

        //verify
        
        final ApplicationUserRepository applicationUserRepository = 
                IsisContext.getServiceRegistry()
                .lookupServiceElseFail(ApplicationUserRepository.class);
        
        _NullSafe.stream(applicationUserRepository.allUsers())
        .forEach(user->{
            System.out.println("!!! user: " + user + " : " + user.getEncryptedPassword());    
        });
        
        System.out.println("!!! SeedSecurityModule done");
        
        return "OK";
        
    }
    

}
