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

package org.apache.isis.metamodel.facetapi;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.isis.metamodel.util.snapshot.XmlSchema.ExtensionData;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FacetUtil {


    public static void addOrReplaceFacet(final Facet facet) {
        if (facet == null) {
            return;
        }
        val facetHolder = facet.getFacetHolder();
        facetHolder.addOrReplaceFacet(facet);
    }

    /**
     * Attaches the {@link Facet} to its {@link Facet#getFacetHolder() facet
     * holder}.
     *
     * @return <tt>true</tt> if a non-<tt>null</tt> facet was added,
     *         <tt>false</tt> otherwise.
     */
    public static boolean addFacet(final Facet facet) {
        if (facet == null) {
            return false;
        }
        facet.getFacetHolder().addFacet(facet);
        return true;
    }

    /**
     * Attaches each {@link Facet} to its {@link Facet#getFacetHolder() facet
     * holder}.
     *
     * @return <tt>true</tt> if any facets were added, <tt>false</tt> otherwise.
     */
    public static boolean addFacets(final List<Facet> facetList) {
        boolean addedFacets = false;
        for (val facet : facetList) {
            addedFacets = addFacet(facet) | addedFacets;
        }
        return addedFacets;
    }

    public static <T extends Facet> ExtensionData<T> getFacetsByType(final FacetHolder facetHolder) {

        return new ExtensionData<T>() {

            @Override
            public int size() {
                return facetHolder.getFacetCount();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void visit(BiConsumer<Class<T>, T> elementConsumer) {
                facetHolder.streamFacets()
                .forEach(facet->elementConsumer.accept((Class<T>)facet.facetType(), (T)facet));
            }

        };
    }

    public static void copyFacets(final FacetHolder source, final FacetHolder target) {
        source.streamFacets()
        .forEach(target::addFacet);
    }

}
