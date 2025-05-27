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
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import org.apache.causeway.applib.CausewayModuleApplibMixins;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.layout.LayoutConstants;
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
import org.apache.causeway.persistence.commons.CausewayModulePersistenceCommons;
import org.apache.causeway.regressiontests.layouts.integtest.model.LayoutTestDomainModel;
import org.apache.causeway.regressiontests.layouts.integtest.model.SimpleObject;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;
import org.apache.causeway.viewer.wicket.applib.CausewayModuleViewerWicketApplibMixins;

@SpringBootTest(
        classes = Layout_SimpleObject_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
public class Layout_SimpleObject_IntegTest extends CausewayIntegrationTestAbstract {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleApplibMixins.class,
            CausewayModuleViewerWicketApplibMixins.class,
            CausewayModuleCoreRuntimeServices.class,
            CausewayModulePersistenceCommons.class,
            CausewayModuleSecurityBypass.class,
    })
    @ComponentScan(basePackageClasses = {AppManifest.class, LayoutTestDomainModel.class})
    public static class AppManifest {

    }

    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
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
        var objectSpecification = specificationLoader.loadSpecification(SimpleObject.class);
        /* not used
        List<ObjectAction> objectActions = objectSpecification.streamAnyActions(MixedIn.INCLUDED).collect(Collectors.toList());
        */
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
