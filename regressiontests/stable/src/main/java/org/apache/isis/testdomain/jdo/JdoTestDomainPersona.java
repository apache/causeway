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
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testdomain.ldap.LdapConstants;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithResult;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithoutResult;
import org.apache.isis.testing.fixtures.applib.personas.PersonaWithBuilderScript;

import lombok.val;

public enum JdoTestDomainPersona
implements PersonaWithBuilderScript<BuilderScriptAbstract<?>>  {

    PurgeAll {
        @Override
        public BuilderScriptWithoutResult builder() {
            return new BuilderScriptWithoutResult() {

                @Override
                protected void execute(final ExecutionContext ec) {

                    repository.allInstances(JdoInventory.class)
                    .forEach(repository::remove);

                    repository.allInstances(JdoBook.class)
                    .forEach(repository::remove);

                    repository.allInstances(JdoProduct.class)
                    .forEach(repository::remove);

                }

                @Inject private RepositoryService repository;

            };
        }
    },

    InventoryWith1Book {
        @Override
        public BuilderScriptWithResult<JdoInventory> builder() {
            return new BuilderScriptWithResult<JdoInventory>() {

                @Override
                protected JdoInventory buildResult(final ExecutionContext ec) {

                    val products = new HashSet<JdoProduct>();

                    products.add(JdoBook.fromDto(BookDto.sample()));

                    val inventory = JdoInventory.of("Sample Inventory", products);
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
            return new BuilderScriptWithoutResult() {

                @Override
                protected void execute(final ExecutionContext ec) {

                    val regularUserRoleName = isisConfig.getExtensions().getSecman().getSeed().getRegularUser().getRoleName();
                    val regularUserRole = applicationRoleRepository.findByName(regularUserRoleName).orElse(null);
                    val username = LdapConstants.SVEN_PRINCIPAL;
                    ApplicationUser svenUser = applicationUserRepository.findByUsername(username).orElse(null);
                    if(svenUser==null) {
                        svenUser = applicationUserRepository
                                .newDelegateUser(username, ApplicationUserStatus.UNLOCKED);
                        applicationRoleRepository.addRoleToUser(regularUserRole, svenUser);

                    } else {
                        applicationUserRepository.enable(svenUser);
                    }

                }

                @Inject private ApplicationUserRepository applicationUserRepository;
                @Inject private ApplicationRoleRepository applicationRoleRepository;
                @Inject private IsisConfiguration isisConfig;

            };
        }


    },


    ;


}
