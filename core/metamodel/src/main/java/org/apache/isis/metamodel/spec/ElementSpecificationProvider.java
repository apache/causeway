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
package org.apache.isis.metamodel.spec;

import org.apache.isis.metamodel.facets.actcoll.typeof.ElementSpecificationProviderFromTypeOfFacet;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;

/**
 * A mechanism to provide the {@link ObjectSpecification type} of a
 * (stand-alone) collection.
 *
 * <p>
 * Introduced to decouple the <tt>facets</tt> package from the <tt>spec</tt>
 * package.
 */
public interface ElementSpecificationProvider {

    public ObjectSpecification getElementType();
    
    /** for convenience */
    public static ElementSpecificationProvider of(final TypeOfFacet typeOfFacet) {
        if (typeOfFacet == null) {
            return null;
        }
        final ObjectSpecification spec = typeOfFacet.valueSpec();
        return new ElementSpecificationProviderFromTypeOfFacet(spec);
    }
}
