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

package org.apache.isis.core.metamodel.facets.object.publishedobject;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.val;

/**
 * Indicates that changes to an object's properties are to be published has, specifying the means by which
 * a canonical event representing these changes should be created.
 */
public interface PublishedObjectFacet extends Facet {

    public static boolean isChangingEntitiesDispatchingEnabled(final FacetHolder facetHolder) {
        if(facetHolder==null) {
            return false;
        }
        val publishedObjectFacet = facetHolder.getFacet(PublishedObjectFacet.class);
        return publishedObjectFacet!=null;
    }
    
}
