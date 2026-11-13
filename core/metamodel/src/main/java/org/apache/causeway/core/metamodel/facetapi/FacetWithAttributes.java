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
package org.apache.causeway.core.metamodel.facetapi;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.services.metamodel.Config;

/**
 * For serialization of metamodel into XML,
 * see {@link org.apache.causeway.applib.services.metamodel.MetaModelService#exportMetaModel(Config)}.
 */
public interface FacetWithAttributes {

    default void visitAttributes(final BiConsumer<String, Object> visitor) {
    	FacetUtil.visitAttributes((Facet)this, visitor);
    }
    
    /**
     * Marker interface used within {@link FacetUtil#visitAttributes()}.
     */
    public static interface HidingOrShowing {
    }

    /**
     * Marker interface used within {@link FacetUtil#visitAttributes()}.
     */
    public static interface DisablingOrEnabling {
    }

    /**
     * Marker interface used within {@link FacetUtil#visitAttributes()}.
     */
    public static interface Validating {
    }
}
