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
package org.apache.isis.core.runtimeservices.menubars.bootstrap3;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.menu.MenuBarsLoaderService;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;
import org.apache.isis.core.metamodel.facetapi.Facet.Precedence;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.runtimeservices.RuntimeServicesTestAbstract;
import org.apache.isis.core.runtimeservices.menubars.MenuBarsLoaderServiceDefault;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

class MenuBarsServiceBS3Test
extends RuntimeServicesTestAbstract {

    private MenuBarsServiceBS3 menuBarsService;
    private MenuBarsLoaderServiceDefault menuBarsLoaderService;
    private LayoutService layoutService;

    @Override
    protected void onSetUp(MetaModelContext_forTestingBuilder mmcBuilder) {
        mmcBuilder.singleton(new Bar()); // install the menu-entry contributing domain service
    }

    @Override
    protected void afterSetUp() {

        getConfiguration().getCore().getMetaModel().getIntrospector().setValidateIncrementally(false);

        layoutService = getServiceRegistry().lookupServiceElseFail(LayoutService.class);
        menuBarsService = (MenuBarsServiceBS3) getServiceRegistry().lookupServiceElseFail(MenuBarsService.class);
        menuBarsLoaderService = (MenuBarsLoaderServiceDefault) getServiceRegistry().lookupServiceElseFail(MenuBarsLoaderService.class);

        // double check, we are all set
        assertNotNull(getSpecificationLoader().loadSpecification(Bar.class));

        assertTrue(getServiceRegistry().streamRegisteredBeans()
                .anyMatch(bean->bean.getBeanClass().equals(Bar.class)));

        assertTrue(getMetaModelContext().streamServiceAdapters()
                .anyMatch(domainObject->domainObject.getSpecification().getCorrespondingClass().equals(Bar.class)));
    }

    @Test
    void actionNamedFacet() {
        val serviceSpec = getSpecificationLoader().loadSpecification(Bar.class);
        val objectAction = serviceSpec.getAction("createSimpleObject").orElse(null);
        assertNotNull(objectAction);
        assertEquals("Create Simple Object", objectAction.getStaticFriendlyName().orElse(null));
    }

    @Test
    void roundtrip() {
        val menuBars = menuBarsService.menuBars();
        assertNotNull(menuBars);
        assertEquals(1L, menuBars.stream().count());

        val layoutData = menuBars.stream().findFirst().get();
        assertEquals("Create Simple Object", layoutData.getNamed());
        assertEquals(null, layoutData.getNamedEscaped()); // deprecated: always escape

        val xml = layoutService.toMenuBarsXml(MenuBarsService.Type.DEFAULT);

        // after round-trip
        val menuBars2 = menuBarsLoaderService.loadMenuBars(xml);
        assertNotNull(menuBars2);
        assertEquals(1L, menuBars2.stream().count());

        val layoutData2 = menuBars2.stream().findFirst().get();
        assertEquals("Create Simple Object", layoutData2.getNamed());
        assertEquals(null, layoutData2.getNamedEscaped()); // deprecated: always escape
    }

    @Test
    void customNamed() {

        val customNamed = "Hello";
        val xml = sampleMenuBarsXmlWithCustomName(customNamed);

        // create menubars-xml from scratch (annotations and fallback naming only)
        val menuBars = menuBarsLoaderService.loadMenuBars(xml);
        assertNotNull(menuBars);
        assertEquals(1L, menuBars.stream().count());

        val layoutData = menuBars.stream().findFirst().get();
        assertEquals(customNamed, layoutData.getNamed());
        assertEquals(null, layoutData.getNamedEscaped()); // deprecated: always escape

        getSpecificationLoader().disposeMetaModel();

        // load the modified menubars.xml with the MenuBarsLoaderService
        super.menubarsLayoutXmlResourceRef.set(new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8)));

        getSpecificationLoader().createMetaModel();

        // verify service-action's member named facet was installed when loading menubars from XML

        val serviceSpec = getSpecificationLoader().specForTypeElseFail(Bar.class);
        val objectAction = serviceSpec.getAction("createSimpleObject").orElse(null);
        assertNotNull(objectAction);

        assertEquals(customNamed, objectAction.getStaticFriendlyName().orElse(null));

        // trigger a menubars.xml reload, which installs more facets while purging old ones
        // verify however, that the number of facets stays constant

        menuBarsService.menuBars(); // trigger reload (MenuBarsService for testing is setup the support reloading)

        assertEquals(customNamed, objectAction.getStaticFriendlyName().orElse(null));

        val facetRanking = objectAction.getFacetRanking(MemberNamedFacet.class).orElse(null);
        assertNotNull(facetRanking);

        // XML layout facets are installed at precedence HIGH
        val xmlFacetRank = facetRanking.getRankLowerOrEqualTo(MemberNamedFacet.class, Precedence.HIGH);

        // verify rank did not grow with latest menubars.xml reload
        assertEquals(1, xmlFacetRank.size());

        // verify winning facet is the same object as the last one added from latest menubars.xml reload,
        // to make sure we are not feed the winner from an outdated cache
        assertSame(facetRanking.getWinnerNonEvent(MemberNamedFacet.class).get(), xmlFacetRank.getLastOrFail());

    }

    // -- HELPER

    private String sampleMenuBarsXmlWithCustomName(final String customNamed) {
        val xml = layoutService.toMenuBarsXml(MenuBarsService.Type.DEFAULT)
                    .replace(
                            "<cpt:named>Create Simple Object</cpt:named>",
                            "<cpt:named>"+customNamed+"</cpt:named>");
        //System.out.println(xml);
        return xml;
    }

}
