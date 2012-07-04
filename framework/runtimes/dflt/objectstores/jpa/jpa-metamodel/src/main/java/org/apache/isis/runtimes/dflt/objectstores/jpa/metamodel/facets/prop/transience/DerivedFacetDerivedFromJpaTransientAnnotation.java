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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.transience;

import javax.persistence.Transient;

import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacetAbstract;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;


/**
 * Derived from being {@link Transient}.
 */
public class DerivedFacetDerivedFromJpaTransientAnnotation extends
		NotPersistedFacetAbstract {

    public DerivedFacetDerivedFromJpaTransientAnnotation(
            final FacetHolder holder) {
        super(holder);
    }

    /**
     * Always returns <i>Derived</i>.
     */
    public String disables(final UsabilityContext<? extends UsabilityEvent> ic) {
        return "JPA transient";
    }

}
