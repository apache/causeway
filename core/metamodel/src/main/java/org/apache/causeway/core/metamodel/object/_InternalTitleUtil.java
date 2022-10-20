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
package org.apache.causeway.core.metamodel.object;

import java.util.Optional;

import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.facets.object.title.TitleRenderRequest;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _InternalTitleUtil {

    // -- TITLE SUPPORT

    String titleString(@NonNull final TitleRenderRequest titleRenderRequest) {

        val managedObject = titleRenderRequest.getObject();

        if(managedObject.getSpecialization().isUnspecified()) {
            return managedObject.getTitle();
        }

        return managedObject.getSpecification().isSingular()
            ? objectTitleString(titleRenderRequest)
                    .trim()
            : collectionTitleString(
                    managedObject,
                    managedObject.getSpecification().getFacet(CollectionFacet.class));
    }

    // -- HELPER

    String abbreviated(final String str, final int maxLength, final String suffix) {
        return str.length() < maxLength
                ? str
                : str.substring(0, maxLength - 3) + suffix;
    }

    private String objectTitleString(@NonNull final TitleRenderRequest titleRenderRequest) {
        val managedObject = titleRenderRequest.getObject();

        //TODO we have value-semantics now, don't skip it for strings
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