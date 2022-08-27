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
package org.apache.isis.core.metamodel.object;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleRenderRequest;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _InternalTitleUtil {

    // -- TITLE SUPPORT

    String titleString(@NonNull final TitleRenderRequest titleRenderRequest) {

        val managedObject = titleRenderRequest.getObject();

        if(!ManagedObjects.isSpecified(managedObject)) {
            return "unspecified object";
        }

        return managedObject.getSpecification().isNonScalar()
            ? collectionTitleString(
                    managedObject,
                    managedObject.getSpecification().getFacet(CollectionFacet.class))
            : objectTitleString(titleRenderRequest)
                .trim();
    }

    String htmlString(
            final @Nullable ManagedObject adapter,
            final @Nullable ObjectFeature feature) {

        if(!ManagedObjects.isSpecified(adapter)) {
            return "";
        }

        val spec = adapter.getSpecification();
        val valueFacet = spec.valueFacet().orElse(null);

        if(valueFacet==null) {
            return String.format("missing ValueFacet %s", spec.getCorrespondingClass());
        }

        @SuppressWarnings("unchecked")
        val renderer = (Renderer<Object>) valueFacet.selectRendererForFeature(feature).orElse(null);
        if(renderer==null) {
            return String.format("missing Renderer %s", spec.getCorrespondingClass());
        }

        return renderer.htmlPresentation(valueFacet.createValueSemanticsContext(feature), adapter.getPojo());
    }

    // -- HELPER

    private String objectTitleString(@NonNull final TitleRenderRequest titleRenderRequest) {
        val managedObject = titleRenderRequest.getObject();
        if (managedObject.getPojo() instanceof String) {
            return (String) managedObject.getPojo();
        }
        val spec = managedObject.getSpecification();
        return Optional.ofNullable(spec.getTitle(titleRenderRequest))
                .orElseGet(()->getDefaultTitle(managedObject));
    }

    private String collectionTitleString(final ManagedObject managedObject, final CollectionFacet facet) {
        final int size = facet.size(managedObject);
        val elementSpec = managedObject.getElementSpecification().orElse(null);
        if (elementSpec == null
                || elementSpec.getFullIdentifier().equals(Object.class.getName())) {
            switch (size) {
            case -1:
                return "Objects";
            case 0:
                return "No objects";
            case 1:
                return "1 object";
            default:
                return size + " objects";
            }
        } else {
            switch (size) {
            case -1:
                return elementSpec.getPluralName();
            case 0:
                return "No " + elementSpec.getPluralName();
            case 1:
                return "1 " + elementSpec.getSingularName();
            default:
                return size + " " + elementSpec.getPluralName();
            }
        }
    }

    private String getDefaultTitle(final ManagedObject managedObject) {
        return "A" + (" " + managedObject.getSpecification().getSingularName()).toLowerCase();
    }
}