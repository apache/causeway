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
package org.apache.isis.extensions.commandlog.jdo;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.commandlog.jdo.entities.CommandJdo;
import org.apache.isis.extensions.commandlog.jdo.entities.CommandJdoRepository;
import org.apache.isis.extensions.commandlog.jdo.ui.CommandServiceMenu;
import org.apache.isis.extensions.commandlog.model.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.teardown.jdo.TeardownFixtureJdoAbstract;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // @DomainService's
        CommandJdoRepository.class
        , CommandServiceMenu.class

        // @Service's
        , CommandJdo.TableColumnOrderDefault.class

        // entities
        , CommandJdo.class
})
@ComponentScan(
        basePackageClasses= {
                IsisModuleExtCommandLogJdo.class
        })
public class IsisModuleExtCommandLogJdo
implements IsisModuleExtCommandLogApplib {

    public static final String NAMESPACE = "isis.ext.commandLog";

    /**
     * For tests that need to delete the command table first.
     * Should be run in the @Before of the test.
     */
    public FixtureScript getTeardownFixtureWillDelete() {
        return new TeardownFixtureJdoAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(CommandModel.class);
            }
        };
    }

}
