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
 *
 */
package org.apache.causeway.regressiontests.layouts.integtest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.causeway.persistence.commons.CausewayModulePersistenceCommons;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.CausewayModuleApplibMixins;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.regressiontests.layouts.integtest.model.Counter;
import org.apache.causeway.regressiontests.layouts.integtest.model.LayoutTestDomainModel;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;
import org.apache.causeway.viewer.wicket.applib.CausewayModuleViewerWicketApplibMixins;

@SpringBootTest(
        classes = Layout_Counter_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
public class Layout_Counter_IntegTest extends CausewayIntegrationTestAbstract {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleApplibMixins.class,
            CausewayModuleViewerWicketApplibMixins.class,
            CausewayModuleCoreRuntimeServices.class,
            CausewayModulePersistenceCommons.class,
            CausewayModuleSecurityBypass.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.UseLog4j2Test)
    })
    @ComponentScan(basePackageClasses = {AppManifest.class, LayoutTestDomainModel.class})
    public static class AppManifest {

        @Bean
        @Singleton
        public PlatformTransactionManager platformTransactionManager() {
            return new AbstractPlatformTransactionManager() {

                @Override
                protected Object doGetTransaction() throws TransactionException {
                    return null;
                }

                @Override
                protected void doBegin(final Object transaction, final TransactionDefinition definition) throws TransactionException {

                }

                @Override
                protected void doCommit(final DefaultTransactionStatus status) throws TransactionException {

                }

                @Override
                protected void doRollback(final DefaultTransactionStatus status) throws TransactionException {

                }
            };
        }

    }

    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    Bookmark target1;

    @BeforeEach
    void beforeEach() {
        interactionService.nextInteraction();

        Optional<Bookmark> bookmark = bookmarkService.bookmarkFor(newCounter("counter-1"));
        target1 = bookmark.orElseThrow();

        interactionService.nextInteraction();

    }

    protected Counter newCounter(final String name) {
        return Counter.builder().name(name).build();
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionNoPosition() {

        // given
        var objectSpecification = specificationLoader.loadSpecification(Counter.class);

        // when
        var action = lookupAction("actionNoPosition");

        // then
        assertThat(action).isNotNull();
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.FALLBACK))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo(""))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).isNull())
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionPositionBelow() {

        // given
        var action = lookupAction("actionPositionBelow");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo(""))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).isNull())
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionPositionPanel() {

        // given
        var action = lookupAction("actionPositionPanel");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo(""))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).isNull())
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionDetailsFieldSetNoPosition() {

        // given
        var action = lookupAction("actionDetailsFieldSetNoPosition");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.FALLBACK))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("1"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).isNotNull())
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionDetailsFieldSetPositionBelow() {

        // given
        var action = lookupAction("actionDetailsFieldSetPositionBelow");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("2"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).isNotNull())
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionDetailsFieldSetPositionPanel() {

        // given
        var action = lookupAction("actionDetailsFieldSetPositionPanel");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("3"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).isNotNull())
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionDetailsFieldSetPositionPanelDropdown() {

        // given
        var action = lookupAction("actionDetailsFieldSetPositionPanelDropdown");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL_DROPDOWN));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("4"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).isNotNull())
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionEmptyFieldSetNoPosition() {

        // given
        var action = lookupAction("actionEmptyFieldSetNoPosition");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.FALLBACK))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("1"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("empty"))
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionEmptyFieldSetPositionBelow() {

        // given
        var action = lookupAction("actionEmptyFieldSetPositionBelow");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("2"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("empty"))
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionEmptyFieldSetPositionPanel() {

        // given
        var action = lookupAction("actionEmptyFieldSetPositionPanel");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("3"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("empty"))
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionEmptyFieldSetPositionPanelDropdown() {

        // given
        var action = lookupAction("actionEmptyFieldSetPositionPanelDropdown");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL_DROPDOWN));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("4"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("empty"))
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyNoPosition() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyNoPosition");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.FALLBACK))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo(""))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("name"))  // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyBelow() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyBelow");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo(""))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("name"))  // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyPanel() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyPanel");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo(""))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("name"))  // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyPanelDropdown() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyPanelDropdown");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL_DROPDOWN));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo(""))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("name"))  // TODO: ?? strange, because 'name' is not a fieldset; should be 'details' ??
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyAndDetailsFieldSetNoPosition() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyAndDetailsFieldSetNoPosition");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.FALLBACK))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("1"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNameAndDetailsFieldSetPropertyBelow() {

        // given
        var action = lookupAction("actionAssociatedWithNameAndDetailsFieldSetPropertyBelow");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("2"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyAndDetailsFieldSetPanel() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyAndDetailsFieldSetPanel");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("3"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))    // because "name" is in this fieldSet
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyAndDetailsFieldSetPanelDropdown() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyAndDetailsFieldSetPanelDropdown");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL_DROPDOWN));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("4"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("details"))    // because "name" is in this fieldSet
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyButEmptyFieldSetNoPosition() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyButEmptyFieldSetNoPosition");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.FALLBACK))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("1"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("empty"))    // overrides the 'associateWith' ???
        ;
    }

    // @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void actionAssociatedWithNamePropertyAndSequenceNoPosition() {

        // given
        var action = lookupAction("actionAssociatedWithNamePropertyAndSequenceNoPosition");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.FALLBACK))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.BELOW));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("1"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo("name"))  // TODO: ?? strange, because 'name' is not a fieldset
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void openRestApi() {

        // given
        var action = lookupAction("openRestApi");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL_DROPDOWN));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("750.1"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo(LayoutConstants.FieldSetId.METADATA))
        ;
    }

    @Disabled // TODO: reinstate back in CAUSEWAY-3655
    @Test
    void clearHints() {

        // given
        var action = lookupAction("clearHints");

        // when, then
        /* not used
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());
        */

        var actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL));

        var layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("400.1"))
        ;

        var layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo(LayoutConstants.FieldSetId.METADATA))
        ;
    }

    private ObjectAction lookupAction(final String id) {
        var objectSpecification = specificationLoader.loadSpecification(Counter.class);
        List<ObjectAction> objectActions = objectSpecification.streamAnyActions(MixedIn.INCLUDED).collect(Collectors.toList());
        return objectSpecification.streamAnyActions(MixedIn.INCLUDED).filter(x -> x.getId().equals(id)).findFirst().orElseThrow();
    }

    /* not used
    private void extracted(final Class<?> cls) {
        LogicalType logicalType = metaModelService.lookupLogicalTypeByClass(cls).orElseThrow();
        MetamodelDto metamodelDto = metaModelService.exportMetaModel(Config.builder().build().withNamespacePrefix("layouts.test."));
        Map<String, DomainClassDto> metaModelDtoById = metamodelDto.getDomainClassDto().stream().collect(Collectors.toMap(DomainClassDto::getId, Function.identity()));
        DomainClassDto domainClassDto = metaModelDtoById.get(cls.getCanonicalName());
        Map<String, Action> actionById = domainClassDto.getActions().getAct().stream().collect(Collectors.toMap(Action::getId, Function.identity()));
        List<org.apache.causeway.schema.metamodel.v2.Facet> facets = actionById.get("updateNameUsingDeclaredAction").getFacets().getFacet();
        Map<String, org.apache.causeway.schema.metamodel.v2.Facet> facetById = facets.stream().collect(Collectors.toMap(org.apache.causeway.schema.metamodel.v2.Facet::getId, Function.identity()));
        Map<String, String> facetAttrByName = facetById.get(LayoutGroupFacet.class.getCanonicalName()).getAttr().stream().collect(Collectors.toMap(FacetAttr::getName, FacetAttr::getValue));
        facetAttrByName.get("Name");
    } */

    @Inject InteractionService interactionService;
    @Inject MetaModelService metaModelService;
    @Inject SpecificationLoader specificationLoader;
    @Inject BookmarkService bookmarkService;

    @Inject CausewayBeanTypeRegistry causewayBeanTypeRegistry;

}
