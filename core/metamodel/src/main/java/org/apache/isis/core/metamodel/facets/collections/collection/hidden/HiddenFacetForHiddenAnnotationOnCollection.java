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

package org.apache.isis.core.metamodel.facets.collections.collection.hidden;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.hidden.HiddenFacetAbstractImpl;

/**
 * @deprecated
 */
@Deprecated
public class HiddenFacetForHiddenAnnotationOnCollection extends HiddenFacetAbstractImpl {

    public static HiddenFacet create(final Hidden hiddenAnnotation, final FacetHolder facetHolder) {
        if (hiddenAnnotation == null) {
            return null;
        }
        return new HiddenFacetForHiddenAnnotationOnCollection(hiddenAnnotation.when(), hiddenAnnotation.where(), facetHolder);
    }

    private HiddenFacetForHiddenAnnotationOnCollection(final When when, Where where, final FacetHolder holder) {
        super(HiddenFacetForHiddenAnnotationOnCollection.class, when, where, holder);
    }


}
