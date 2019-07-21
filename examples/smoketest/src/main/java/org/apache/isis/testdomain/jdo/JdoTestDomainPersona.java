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
import java.util.Set;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.fixtures.api.PersonaWithBuilderScript;
import org.apache.isis.extensions.fixtures.fixturescripts.BuilderScriptAbstract;
import org.apache.isis.runtime.system.context.IsisContext;

public enum JdoTestDomainPersona 
implements PersonaWithBuilderScript<BuilderScriptAbstract<Inventory>>  {
    
    PurgeAll {
        @Override
        public BuilderScriptAbstract<Inventory> builder() {
            return new BuilderScriptAbstract<Inventory>() {

                @Override
                protected void execute(ExecutionContext ec) {
                    
                    //XXX lombok issue, cannot use val here (https://github.com/rzwitserloot/lombok/issues/434)
                    RepositoryService repository = IsisContext.getServiceRegistry()
                            .lookupServiceElseFail(RepositoryService.class);
                    
                    repository.allInstances(Inventory.class)
                    .forEach(repository::remove);
                    
                    repository.allInstances(Book.class)
                    .forEach(repository::remove);
                    
                    repository.allInstances(Product.class)
                    .forEach(repository::remove);
                    
                }

                @Override
                public Inventory getObject() {
                    return null;
                }
                
            };
        }    
    },
    
    InventoryWith1Book {
        @Override
        public BuilderScriptAbstract<Inventory> builder() {
            return new BuilderScriptAbstract<Inventory>() {

                private Inventory inventory;
                
                @Override
                protected void execute(ExecutionContext ec) {
                    
                    //don't use lombok's val here (https://github.com/rzwitserloot/lombok/issues/434)
                    RepositoryService repository = IsisContext.getServiceRegistry()
                            .lookupServiceElseFail(RepositoryService.class);
                    
                    Set<Product> products = new HashSet<>();
                    
                    products.add(Book.of(
                            "Sample Book", "A sample book for testing.", 99.,
                            "Sample Author", "Sample ISBN", "Sample Publisher"));
                    
                    inventory = Inventory.of("Sample Inventory", products);
                    repository.persist(inventory);
                    
                }

                @Override
                public Inventory getObject() {
                    return inventory;
                }
                
            };
        }    
    }
    
    ;

    
    
}
