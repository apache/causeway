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

import java.util.List;

import org.apache.isis.applib.annotation.CollectionLayout;
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
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;


public class CollectionLayoutFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public CollectionLayoutFacetFactory() {
        super(FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetHolder holder = processMethodContext.getFacetHolder();

        final List<CollectionLayout> collectionLayouts = Annotations.getAnnotations(processMethodContext.getMethod(), CollectionLayout.class);


        // cssClass
        CssClassFacet cssClassFacet = CssClassFacetForCollectionLayoutAnnotation.create(collectionLayouts, holder);
        FacetUtil.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet =
                DescribedAsFacetForCollectionLayoutAnnotation.create(collectionLayouts, holder);
        FacetUtil.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = HiddenFacetForCollectionLayoutAnnotation.create(collectionLayouts, holder);
        FacetUtil.addFacet(hiddenFacet);


        // defaultView
        DefaultViewFacet defaultViewFacet =
                DefaultViewFacetForCollectionLayoutAnnotation.create(collectionLayouts, getConfiguration(), holder);
        FacetUtil.addFacet(defaultViewFacet);


        // named
        NamedFacet namedFacet = NamedFacetForCollectionLayoutAnnotation.create(collectionLayouts, holder);
        FacetUtil.addFacet(namedFacet);


        // paged
        PagedFacet pagedFacet = PagedFacetForCollectionLayoutAnnotation.create(collectionLayouts, holder);
        FacetUtil.addFacet(pagedFacet);


        // sortedBy
        SortedByFacet sortedByFacet = SortedByFacetForCollectionLayoutAnnotation.create(collectionLayouts, holder);
        FacetUtil.addFacet(sortedByFacet);

    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {

        // cssClass
        CssClassFacet cssClassFacet = null;
        FacetUtil.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet = null;
        FacetUtil.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = null;
        FacetUtil.addFacet(hiddenFacet);


        // named
        NamedFacet namedFacet = null;
        FacetUtil.addFacet(namedFacet);


        // paged
        PagedFacet pagedFacet = null;
        FacetUtil.addFacet(pagedFacet);


        // sortedBy
        SortedByFacet sortedByFacet = null;
        FacetUtil.addFacet(sortedByFacet);


    }


}
