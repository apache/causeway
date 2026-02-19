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
package org.apache.causeway.persistence.jpa.integration.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Persistence.Weaving.SafeguardMode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public final class JpaWeavingSafeguard {

    final CausewayConfiguration.Persistence.Weaving.SafeguardMode mode;

//debug
//    {
//        mode = SafeguardMode.REQUIRE_WEAVED;
//    }

    public void checkAll(final Iterable<Class<?>> entityTypes) {
        var cache = new Cache();

        for(final Class<?> entityType : entityTypes) {
            var chainOfInheritance = cache.streamTypeHierarchyEntries(entityType).collect(Collectors.toList());

            boolean isRemainingRequiredToBeEnhanced = mode == SafeguardMode.REQUIRE_WEAVED;
            var it = chainOfInheritance.iterator();
            while(it.hasNext()) {
                var next = it.next();
                if(isRemainingRequiredToBeEnhanced
                    && !next.isEnhanced()) {
                        fail(next, chainOfInheritance);
                }
                if(next.isEnhanced()) {
                    isRemainingRequiredToBeEnhanced = true;
                }
            }
        }

    }

    private void fail(final EnhancementDescriptor offender, final List<EnhancementDescriptor> chainOfInheritance) {
        var chainAsMultiline = " * " + chainOfInheritance.stream()
                .map(EnhancementDescriptor::toString)
                .collect(Collectors.joining("\n * "));

        switch (mode) {
            case LOG_ONLY:
                log.warn("found non-weaved\n {}\n in chain of inheritance\n{}",
                    offender.cls().getName(),
                    chainAsMultiline);
                break;
            case REQUIRE_WEAVED_WHEN_ANY_SUB_IS_WEAVED:
                throw _Exceptions.unrecoverable("found non-weaved\n %s\n in chain of inheritance\n%s",
                    offender.cls().getName(),
                    chainAsMultiline);
            case REQUIRE_WEAVED:
                throw _Exceptions.unrecoverable("while weaving is enforced, found non-weaved\n %s\n in chain of inheritance\n%s",
                    offender.cls().getName(),
                    chainAsMultiline);
        }
    }

    @RequiredArgsConstructor
    @Getter @Accessors(fluent = true)
    private final class EnhancementDescriptor{
    	
    	final Class<?> cls;
        final boolean isEnhanced;
    	
        @Override public final String toString() {
            return String.format("%s%s",
                isEnhanced
                    ? "[#]"
                    : "[-]",
                cls.getName());
        }
    }

    @RequiredArgsConstructor
    @Getter @Accessors(fluent = true)
    private final class Cache {

    	final Map<Class<?>, EnhancementDescriptor> descriptorsByClass;
    	final _ClassCache classCache;
    	
        Cache() {
            this(new HashMap<>(), _ClassCache.getInstance());
        }

        Stream<EnhancementDescriptor> streamTypeHierarchyEntries(final Class<?> cls) {
            return streamTypeHierarchy(cls)
                .map(type->
                    descriptorsByClass.computeIfAbsent(type, __->new EnhancementDescriptor(type, classCache.isByteCodeEnhanced(type))));
        }

        // -- HELPER

        private Stream<Class<?>> streamTypeHierarchy(final Class<?> cls) {
            return _Reflect.streamTypeHierarchy(cls, InterfacePolicy.EXCLUDE)
                .filter(type->!Object.class.equals(type));
        }

    }

}
