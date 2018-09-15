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

package org.apache.isis.core.metamodel.facets.object.callbacks;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.ImperativeFacetMulti;
import org.apache.isis.core.metamodel.spec.DomainModelException;
import org.apache.isis.core.metamodel.spec.Instance;

/**
 * A {@link Facet} that represents some type of lifecycle callback on the object
 * (eg about to be persisted).
 */
public interface CallbackFacet extends Facet, ImperativeFacetMulti {

    public void invoke(Instance object);

    public static final class Util {

        private Util() {
        }

        public static void callCallback(final Instance object, final Class<? extends Facet> cls) {
            final CallbackFacet facet = (CallbackFacet) object.getSpecification().getFacet(cls);
            if (facet != null) {
                try {
                    facet.invoke(object);
                } catch (final RuntimeException e) {
                    throw new DomainModelException("Callback failed.  Calling " + facet + " on " + object, e);
                }
            }
        }

    }

}
