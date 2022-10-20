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
package org.apache.causeway.core.metamodel.facets.object.title.parser;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.title.TitleFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.val;

public final class TitleFacetFromValueFacet
extends FacetAbstract
implements TitleFacet {

    private final @NonNull ValueFacet<?> valueFacet;

    public static TitleFacetFromValueFacet create(final ValueFacet<?> valueFacet, final FacetHolder holder) {
        return new TitleFacetFromValueFacet(valueFacet, holder);
    }

    private TitleFacetFromValueFacet(final ValueFacet<?> valueFacet, final FacetHolder holder) {
        // facets from the title() method have higher precedence
        super(TitleFacet.class, holder, Precedence.LOW);
        this.valueFacet = valueFacet;
    }

    @Override
    public String title(final TitleRenderRequest renderRequest) {
        if (renderRequest == null) {
            return null;
        }
        final Object pojo = renderRequest.getObject().getPojo();
        if (pojo == null) {
            return null;
        }

        // support for qualified value semantics, requires a 'where' context, that is,
        // what property, collection, action return or action param this is to be rendered for ...
        if(renderRequest.getFeature()!=null) {
            switch(renderRequest.getFeature().getFeatureType()) {
            case PROPERTY: {
                val prop = (OneToOneAssociation)renderRequest.getFeature();
                final Renderer renderer = valueFacet
                        .selectRendererForPropertyElseFallback(prop);
                return renderer
                        .titlePresentation(valueFacet
                                .createValueSemanticsContext(prop), pojo);
            }
            case COLLECTION: {
                val coll = (OneToManyAssociation)renderRequest.getFeature();
                final Renderer renderer = valueFacet
                        .selectRendererForCollectionElseFallback(coll);
                return renderer
                        .titlePresentation(valueFacet
                                .createValueSemanticsContext(coll), pojo);
            }
            case ACTION_PARAMETER_SINGULAR:
            case ACTION_PARAMETER_PLURAL:{
                val param = (ObjectActionParameter)renderRequest.getFeature();
                final Renderer renderer = valueFacet
                        .selectRendererForParameterElseFallback(param);
                return renderer.titlePresentation(valueFacet.createValueSemanticsContext(param), pojo);
            }
            default:
                // fall through
            }
        }

        // fall back to default value semantics ...

        val featureId = getFacetHolder().getFeatureIdentifier();
        val feature = getSpecificationLoader().loadFeature(featureId).orElse(null);

        return valueFacet.selectDefaultRenderer()
        .map(renderer->(Renderer) renderer)
        .map(renderer->renderer.titlePresentation(valueFacet.createValueSemanticsContext(feature), pojo))
        .orElseGet(()->String.format("Value type %s has no value semantics for title rendering.",
                renderRequest.getObject().getSpecification().getCorrespondingClass()));

    }

    @Override
    public boolean semanticEquals(final @NonNull Facet other) {
        return other instanceof TitleFacetFromValueFacet
                ? true //TODO this.valueFacet.semanticEquals(((TitleFacetUsingValueFacet)other).valueFacet)
                : false;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        valueFacet.visitAttributes(visitor);
    }


}
