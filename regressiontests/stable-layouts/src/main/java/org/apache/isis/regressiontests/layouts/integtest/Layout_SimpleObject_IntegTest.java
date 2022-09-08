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
package org.apache.isis.regressiontests.layouts.integtest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.applib.IsisModuleApplibMixins;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.layout.LayoutConstants;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.metamodel.Config;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.regressiontests.layouts.integtest.model.LayoutTestDomainModel;
import org.apache.isis.regressiontests.layouts.integtest.model.SimpleObject;
import org.apache.isis.schema.metamodel.v2.Action;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.schema.metamodel.v2.FacetAttr;
import org.apache.isis.schema.metamodel.v2.MetamodelDto;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;
import org.apache.isis.viewer.wicket.applib.IsisModuleViewerWicketApplibMixins;

import lombok.val;

@SpringBootTest(
        classes = Layout_SimpleObject_IntegTest.AppManifest.class
)
@ActiveProfiles("test")
public class Layout_SimpleObject_IntegTest extends IsisIntegrationTestAbstract {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            IsisModuleApplibMixins.class,
            IsisModuleViewerWicketApplibMixins.class,
            IsisModuleCoreRuntimeServices.class,
            IsisModuleSecurityBypass.class,
    })
    @PropertySources({
            @PropertySource(IsisPresets.UseLog4j2Test)
    })
    @ComponentScan(basePackageClasses = {AppManifest.class, LayoutTestDomainModel.class})
    public static class AppManifest {


    }

    @BeforeAll
    static void beforeAll() {
        IsisPresets.forcePrototyping();
    }


    @Test
    void openRestApi() {

        // given
        val action = lookupAction("openRestApi");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        val actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL_DROPDOWN));

        val layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("750.1"))
        ;

        val layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo(LayoutConstants.FieldSetId.METADATA))
        ;
    }

    @Test
    void clearHints() {

        // given
        val action = lookupAction("clearHints");

        // when, then
        List<Facet> facets = action.getFacetHolder().streamFacets().collect(Collectors.toList());

        val actionPositionFacet = action.getFacet(ActionPositionFacet.class);
        assertThat(actionPositionFacet)
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(ActionPositionFacet::position).isEqualTo(ActionLayout.Position.PANEL));

        val layoutOrderFacet = action.getFacet(LayoutOrderFacet.class);
        assertThat(layoutOrderFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getPrecedence).isEqualTo(Facet.Precedence.DEFAULT))
                .satisfies(f -> assertThat(f).extracting(LayoutOrderFacet::getSequence).isEqualTo("400.1"))
        ;

        val layoutGroupFacet = action.getFacet(LayoutGroupFacet.class);
        assertThat(layoutGroupFacet)
                .satisfies(f -> assertThat(f).extracting(LayoutGroupFacet::getGroupId).isEqualTo(LayoutConstants.FieldSetId.METADATA))
        ;
    }

    private ObjectAction lookupAction(final String id) {
        val objectSpecification = specificationLoader.loadSpecification(SimpleObject.class);
        List<ObjectAction> objectActions = objectSpecification.streamAnyActions(MixedIn.INCLUDED).collect(Collectors.toList());
        return objectSpecification.streamAnyActions(MixedIn.INCLUDED).filter(x -> x.getId().equals(id)).findFirst().orElseThrow();
    }


    private void extracted(final Class<?> cls) {
        LogicalType logicalType = metaModelService.lookupLogicalTypeByClass(cls).orElseThrow();
        MetamodelDto metamodelDto = metaModelService.exportMetaModel(Config.builder().build().withNamespacePrefix("layouts.test."));
        Map<String, DomainClassDto> metaModelDtoById = metamodelDto.getDomainClassDto().stream().collect(Collectors.toMap(DomainClassDto::getId, Function.identity()));
        DomainClassDto domainClassDto = metaModelDtoById.get(cls.getCanonicalName());
        Map<String, Action> actionById = domainClassDto.getActions().getAct().stream().collect(Collectors.toMap(Action::getId, Function.identity()));
        List<org.apache.isis.schema.metamodel.v2.Facet> facets = actionById.get("updateNameUsingDeclaredAction").getFacets().getFacet();
        Map<String, org.apache.isis.schema.metamodel.v2.Facet> facetById = facets.stream().collect(Collectors.toMap(org.apache.isis.schema.metamodel.v2.Facet::getId, Function.identity()));
        Map<String, String> facetAttrByName = facetById.get(LayoutGroupFacet.class.getCanonicalName()).getAttr().stream().collect(Collectors.toMap(FacetAttr::getName, FacetAttr::getValue));
        facetAttrByName.get("Name");
    }


    @Inject InteractionService interactionService;
    @Inject MetaModelService metaModelService;
    @Inject SpecificationLoader specificationLoader;
    @Inject BookmarkService bookmarkService;

    @Inject IsisBeanTypeRegistry isisBeanTypeRegistry;

}
