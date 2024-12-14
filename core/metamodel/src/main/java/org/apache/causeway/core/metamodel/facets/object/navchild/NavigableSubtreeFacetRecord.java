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
import java.util.stream.Stream;

import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.extern.log4j.Log4j2;

@Log4j2
record NavigableSubtreeFacetRecord (
    Can<MethodHandle> subNodesMethodHandles, 
    FacetHolder facetHolder) 
implements NavigableSubtreeFacet {
    
    @Override
    public Class<? extends Facet> facetType() {
        return NavigableSubtreeFacet.class;
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
    public final int childCountOf(final Object node) {
        return subNodesMethodHandles.stream()
            .mapToInt(mh->{
                try {
                    return _NullSafe.sizeAutodetect(mh.invoke(node));
                } catch (Throwable e) {
                    log.error("failed to invoke subNodesMethodHandle {}",
                            mh.toString(), e);
                    return 0;
                }
            })
            .sum();
    }

    @Override
    public final Stream<Object> childrenOf(final Object node) {
        return subNodesMethodHandles.stream()
            .flatMap(mh->{
                try {
                    return _NullSafe.streamAutodetect(mh.invoke(node));
                } catch (Throwable e) {
                    throw _Exceptions.unrecoverable(e);
                }
            });
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        visitor.accept("facet", ClassUtils.getShortName(getClass()));
        visitor.accept("precedence", getPrecedence().name());
        visitor.accept("subNodesMethodHandles", subNodesMethodHandles.map(MethodHandle::toString));
    }
    
}

