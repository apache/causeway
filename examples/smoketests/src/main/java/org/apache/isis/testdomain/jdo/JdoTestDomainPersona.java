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
package org.apache.isis.testdomain.jdo;

import java.util.HashSet;

import javax.inject.Inject;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.fixtures.api.PersonaWithBuilderScript;
import org.apache.isis.extensions.fixtures.fixturescripts.BuilderScriptAbstract;
import org.apache.isis.extensions.fixtures.fixturescripts.BuilderScriptWithResult;
import org.apache.isis.extensions.fixtures.fixturescripts.BuilderScriptWithoutResult;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.testdomain.ldap.LdapConstants;

import lombok.val;

public enum JdoTestDomainPersona 
implements PersonaWithBuilderScript<BuilderScriptAbstract<? extends Object>>  {

    PurgeAll {
        @Override
        public BuilderScriptAbstract<?> builder() {
            return new BuilderScriptWithoutResult() {

                @Override
                protected void execute(ExecutionContext ec) {

                    repository.allInstances(Inventory.class)
                    .forEach(repository::remove);

                    repository.allInstances(Book.class)
                    .forEach(repository::remove);

                    repository.allInstances(Product.class)
                    .forEach(repository::remove);

                }
                
                @Inject private RepositoryService repository;

            };
        }    
    },

    InventoryWith1Book {
        @Override
        public BuilderScriptAbstract<?> builder() {
            return new BuilderScriptWithResult<Inventory>() {

                private Inventory inventory;

                @Override
                protected Inventory buildResult(ExecutionContext ec) {

                    val products = new HashSet<Product>();

                    products.add(Book.of(
                            "Sample Book", "A sample book for testing.", 99.,
                            "Sample Author", "Sample ISBN", "Sample Publisher"));

                    inventory = Inventory.of("Sample Inventory", products);
                    repository.persist(inventory);
                    
                    return inventory;

                }
                
                @Inject private RepositoryService repository;

            };
        }    
    },
    
    SvenApplicationUser {
        @Override
        public BuilderScriptAbstract<?> builder() {
            return new BuilderScriptWithResult<Inventory>() {

                private Inventory inventory;

                @Override
                protected Inventory buildResult(ExecutionContext ec) {

                    val regularUserRoleName = securityConfig.getRegularUserRoleName();
                    val regularUserRole = applicationRoleRepository.findByName(regularUserRoleName);
                    val enabled = true;
                    val username = LdapConstants.SVEN_PRINCIPAL;
                    val svenUser = applicationUserRepository.findByUsername(username);
                    if(svenUser==null) {
                        applicationUserRepository
                        .newDelegateUser(username, regularUserRole, enabled);
                    } else {
                        applicationUserRepository.enable(svenUser);
                    }
                    
                    return inventory;

                }
                
                @Inject private ApplicationUserRepository applicationUserRepository;
                @Inject private ApplicationRoleRepository applicationRoleRepository;
                @Inject private SecurityModuleConfig securityConfig;

            };
        }    
        
        
    },


    ;


}
