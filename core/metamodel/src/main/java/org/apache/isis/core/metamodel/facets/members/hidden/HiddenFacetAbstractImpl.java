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

package org.apache.isis.core.metamodel.facets.members.hidden;

import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public abstract class HiddenFacetAbstractImpl extends HiddenFacetAbstract {

    public HiddenFacetAbstractImpl(final Class<? extends Facet> facetType, final When when, Where where, final FacetHolder holder) {
        super(facetType, when, where, holder);
    }

    /**
     * For testing only.
     */
    public HiddenFacetAbstractImpl(final When when, Where where, final FacetHolder holder) {
        super(HiddenFacetAbstractImpl.class, when, where, holder);
    }

    @Override
    public String hiddenReason(final ObjectAdapter targetAdapter, Where whereContext) {
        if(!where().includes(whereContext)) {
            return null;
        }
        
        if (when() == When.ALWAYS) {
            return "Always hidden";
        }
        if (when() == When.NEVER) {
            return null;
        }

        // remaining tests depend on target in question.
        if (targetAdapter == null) {
            return null;
        }

        if (when() == When.UNTIL_PERSISTED) {
            return targetAdapter.isTransient() ? "Hidden until persisted" : null;
        } else if (when() == When.ONCE_PERSISTED) {
            return targetAdapter.representsPersistent() ? "Hidden once persisted" : null;
        }
        return null;
    }

}
