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
package org.apache.isis.extensions.commandlog.impl;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;
import org.apache.isis.extensions.commandlog.impl.ui.CommandServiceMenu;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;
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
})
@ComponentScan(
        basePackageClasses= {
                IsisModuleExtCommandLogImpl.class
        })
public class IsisModuleExtCommandLogImpl implements ModuleWithFixtures {

    public abstract static class TitleUiEvent<S>
            extends org.apache.isis.applib.events.ui.TitleUiEvent<S> { }

    public abstract static class IconUiEvent<S>
            extends org.apache.isis.applib.events.ui.IconUiEvent<S> { }

    public abstract static class CssClassUiEvent<S>
            extends org.apache.isis.applib.events.ui.CssClassUiEvent<S> { }

    public abstract static class LayoutUiEvent<S>
            extends org.apache.isis.applib.events.ui.LayoutUiEvent<S> { }

    public abstract static class ActionDomainEvent<S>
            extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> { }

    public abstract static class CollectionDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> { }

    public abstract static class PropertyDomainEvent<S,T>
            extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> { }

    @Override
    public FixtureScript getTeardownFixture() {
        // can't delete from CommandJdo, is searched for during teardown (IsisSession#close)
        return FixtureScript.NOOP;
    }

    /**
     * For tests that need to delete the command table first.
     * Should be run in the @Before of the test.
     */
    public FixtureScript getTeardownFixtureWillDelete() {
        return new TeardownFixtureJdoAbstract() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                deleteFrom(CommandJdo.class);
            }
        };
    }

}
