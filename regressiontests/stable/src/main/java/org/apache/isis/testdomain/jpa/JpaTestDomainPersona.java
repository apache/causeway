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
package org.apache.isis.testdomain.jpa;

import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserStatus;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;
import org.apache.isis.testdomain.jpa.entities.JpaProduct;
import org.apache.isis.testdomain.ldap.LdapConstants;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithResult;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithoutResult;
import org.apache.isis.testing.fixtures.applib.personas.PersonaWithBuilderScript;

import lombok.val;

public enum JpaTestDomainPersona
implements PersonaWithBuilderScript<BuilderScriptAbstract<?>>  {

    PurgeAll {
        @Override
        public BuilderScriptWithoutResult builder() {
            return new BuilderScriptWithoutResult() {

                @Override
                protected void execute(final ExecutionContext ec) {

                    repository.allInstances(JpaInventory.class)
                    .forEach(repository::remove);

                    repository.allInstances(JpaBook.class)
                    .forEach(repository::remove);

                    repository.allInstances(JpaProduct.class)
                    .forEach(repository::remove);

                }

                @Inject private RepositoryService repository;

            };
        }
    },

    InventoryWith1Book {
        @Override
        public BuilderScriptWithResult<JpaInventory> builder() {
            return new BuilderScriptWithResult<JpaInventory>() {

                @Override
                protected JpaInventory buildResult(final ExecutionContext ec) {

                    val products = new TreeSet<JpaProduct>();

                    products.add(JpaBook.fromDto(BookDto.sample()));

                    val inventory = new JpaInventory("Sample Inventory", products);
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
