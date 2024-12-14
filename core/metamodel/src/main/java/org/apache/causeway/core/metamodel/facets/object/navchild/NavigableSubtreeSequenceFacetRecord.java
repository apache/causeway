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
package org.apache.causeway.core.metamodel.facets.object.navchild;

import java.lang.invoke.MethodHandle;
import java.util.function.BiConsumer;

import org.springframework.util.ClassUtils;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

record NavigableSubtreeSequenceFacetRecord(
    String sequence,
    MethodHandle methodHandle, 
    FacetHolder facetHolder) 
implements NavigableSubtreeSequenceFacet {
    
    @Override
    public Class<? extends Facet> facetType() {
        return NavigableSubtreeSequenceFacet.class;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.DEFAULT;
    }
    
    @Override
    public FacetHolder getFacetHolder() {
        return facetHolder;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("facet", ClassUtils.getShortName(getClass()));
        visitor.accept("precedence", getPrecedence().name());
        visitor.accept("sequence", sequence);
        visitor.accept("methodHandle", methodHandle);
    }

}
