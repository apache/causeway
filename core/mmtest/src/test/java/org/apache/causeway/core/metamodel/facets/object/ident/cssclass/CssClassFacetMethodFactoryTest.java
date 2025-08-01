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
package org.apache.causeway.core.metamodel.facets.object.ident.cssclass;

import org.junit.jupiter.api.Test;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.ObjectSupportMethod;
import org.apache.causeway.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.causeway.core.metamodel.facets.object.support.ObjectSupportFacetFactoryTestAbstract;

class CssClassFacetMethodFactoryTest
extends ObjectSupportFacetFactoryTestAbstract {

    @Test
    void iconNameMethodPickedUpOnClassAndMethodRemoved() {
        @SuppressWarnings("unused")
        class Customer {
            public String cssClass() { return null; }
        }
        assertPicksUp(1, facetFactory, Customer.class, ObjectSupportMethod.CSS_CLASS, CssClassFacet.class);
    }

}
