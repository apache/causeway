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

package org.apache.isis.metamodel.facets.collections.clear;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.metamodel.commons.StringExtensions;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.MethodFinderUtils;
import org.apache.isis.metamodel.facets.MethodLiteralConstants;
import org.apache.isis.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.facets.collections.modify.CollectionClearFacet;

public class CollectionClearFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final Can<String> PREFIXES = Can.ofSingleton(MethodLiteralConstants.CLEAR_PREFIX);

    public CollectionClearFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        attachCollectionClearFacets(processMethodContext);

    }

    private void attachCollectionClearFacets(final ProcessMethodContext processMethodContext) {

        final Method getMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asJavaBaseName(getMethod.getName());

        final Class<?> cls = processMethodContext.getCls();
        final Method method = MethodFinderUtils.findMethod(cls, MethodLiteralConstants.CLEAR_PREFIX + capitalizedName, void.class, null);
        processMethodContext.removeMethod(method);

        final FacetHolder collection = processMethodContext.getFacetHolder();
        super.addFacet(createCollectionClearFacet(method, getMethod, collection));
    }

    private CollectionClearFacet createCollectionClearFacet(
            final Method clearMethodIfAny, final Method accessorMethod, final FacetHolder collection) {

        if (clearMethodIfAny != null) {
            return new CollectionClearFacetViaClearMethod(clearMethodIfAny, collection);
        } else {
            return new CollectionClearFacetViaAccessor(accessorMethod, collection);
        }
    }

}
