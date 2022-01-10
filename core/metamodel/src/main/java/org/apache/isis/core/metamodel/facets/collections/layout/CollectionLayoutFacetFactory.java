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
package org.apache.isis.core.metamodel.facets.collections.layout;


import java.util.Properties;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;
import org.apache.isis.core.metamodel.facets.members.render.RenderFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.datanucleus.util.StringUtils;


public class CollectionLayoutFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public CollectionLayoutFacetFactory() {
        super(FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetHolder holder = processMethodContext.getFacetHolder();

        Properties properties = processMethodContext.metadataProperties("collectionLayout");
        if(properties == null) {
            // alternate key
            properties = processMethodContext.metadataProperties("layout");
        }
        final CollectionLayout collectionLayout = Annotations.getAnnotation(processMethodContext.getMethod(), CollectionLayout.class);


        // cssClass
        CssClassFacet cssClassFacet = CssClassFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(cssClassFacet == null) {
            cssClassFacet = CssClassFacetForCollectionLayoutAnnotation.create(collectionLayout, holder);
        }
        FacetUtil.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet = DescribedAsFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(describedAsFacet == null) {
            describedAsFacet = DescribedAsFacetForCollectionLayoutAnnotation.create(collectionLayout, holder);
        }
        FacetUtil.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = HiddenFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(hiddenFacet == null) {
            hiddenFacet = HiddenFacetForCollectionLayoutAnnotation.create(collectionLayout, holder);
        }
        FacetUtil.addFacet(hiddenFacet);


        // defaultView
        DefaultViewFacet defaultViewFacet = DefaultViewFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(defaultViewFacet == null) {
            defaultViewFacet = DefaultViewFacetForCollectionLayoutAnnotation.create(collectionLayout, getConfiguration(), holder);
        }
        FacetUtil.addFacet(defaultViewFacet);
        

        // named
        NamedFacet namedFacet = NamedFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(namedFacet == null) {
            namedFacet = NamedFacetForCollectionLayoutAnnotation.create(collectionLayout, holder);
        }
        FacetUtil.addFacet(namedFacet);


        // paged
        PagedFacet pagedFacet = PagedFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(pagedFacet == null) {
            pagedFacet = PagedFacetForCollectionLayoutAnnotation.create(collectionLayout, holder);
        }
        FacetUtil.addFacet(pagedFacet);


        // renderType
        RenderFacet renderFacet = RenderFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(renderFacet == null) {
            renderFacet = RenderFacetForCollectionLayoutAnnotation.create(collectionLayout, holder);
        }
        FacetUtil.addFacet(renderFacet);


        // sortedBy
        SortedByFacet sortedByFacet = SortedByFacetOnCollectionFromLayoutProperties.create(properties, holder);
        if(sortedByFacet == null) {
            sortedByFacet = SortedByFacetForCollectionLayoutAnnotation.create(collectionLayout, holder);
        }
        FacetUtil.addFacet(sortedByFacet);

        // In preparation for v2 adding support for sequence in @CollectionLayout
        if (collectionLayout!=null
                && StringUtils.notEmpty(collectionLayout.sequence())
                && holder.getFacet(MemberOrderFacetAnnotation.class)==null) {
            FacetUtil.addFacet( new MemberOrderFacetAnnotation(
                    "",
                    collectionLayout.sequence(),
                    servicesInjector.lookupService(TranslationService.class),
                    holder));
        }
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {

        final FacetHolder holder = processMemberContext.getFacetHolder();

        Properties properties = processMemberContext.metadataProperties("collectionLayout");
        if(properties == null) {
            // alternate key
            properties = processMemberContext.metadataProperties("layout");
        }


        // cssClass
        CssClassFacet cssClassFacet = CssClassFacetOnCollectionFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet = DescribedAsFacetOnCollectionFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = HiddenFacetOnCollectionFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(hiddenFacet);


        // named
        NamedFacet namedFacet = NamedFacetOnCollectionFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(namedFacet);


        // paged
        PagedFacet pagedFacet = PagedFacetOnCollectionFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(pagedFacet);


        // renderType
        RenderFacet renderFacet = RenderFacetOnCollectionFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(renderFacet);


        // sortedBy
        SortedByFacet sortedByFacet = SortedByFacetOnCollectionFromLayoutProperties.create(properties, holder);
        FacetUtil.addFacet(sortedByFacet);


    }


}
