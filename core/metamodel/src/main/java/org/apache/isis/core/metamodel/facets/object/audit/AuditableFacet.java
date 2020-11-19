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
package org.apache.isis.core.metamodel.facets.object.audit;


import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.val;


/**
 * Corresponds to annotating the class with the {@Code @DomainObject(auditing=ENABLED)} annotation.
 */
public interface AuditableFacet extends Facet {

    /**
     * Indicates that the object to which this {@link Facet} is
     * attached should <i>not</i> be treated as being audited.
     *
     * <p>
     * Exists to allow implementations that configure auditing for all objects, but which
     * can then be disabled for selected objects (eg using {@link Audited#disabled()} ).
     */
    public boolean isDisabled();
    
    public static boolean isAuditingEnabled(final FacetHolder facetHolder) {
        if(facetHolder==null) {
            return false;
        }
        val auditableFacet = facetHolder.getFacet(AuditableFacet.class);
        if(auditableFacet == null || auditableFacet.isDisabled()) {
            return false;
        }
        return true;
    }

}
