/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.core.runtime.idstringifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.runtime.IsisModuleCoreRuntime;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * Convenience service that looks up (and caches) the {@link IdStringifier} available for a given value class, and
 * optionally the class of the owning entity.
 *
 * <p>
 *     This is intended for framework use, there is little reason to call it or override it.
 * </p>
 *
 * @since 2.x
 */
@Service
@Named(IsisModuleCoreRuntime.NAMESPACE + ".IdStringifierLookupService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor
public class IdStringifierLookupService {

    @Inject
    private final List<IdStringifier<?>> idStringifiers;
    private final Map<Class<?>, IdStringifier<?>> stringifierByClass = new ConcurrentHashMap<>();

    public <T> IdStringifier<T> lookupElseFail(Class<T> candidateValueClass) {
        val idStringifier = stringifierByClass.computeIfAbsent(candidateValueClass, aClass -> {
            for (val candidateStringifier : idStringifiers) {
                if (candidateStringifier.handles(candidateValueClass)) {
                    return candidateStringifier;
                }
            }
            return null;
        });
        return Optional.<IdStringifier<T>>ofNullable(_Casts.uncheckedCast(idStringifier))
                .orElseThrow(() -> new IllegalStateException(String.format("Could not locate an IdStringifier to handle '%s'", candidateValueClass)));
    }

    public <T> Optional<IdStringifier<T>> lookup(Class<T> candidateValueClass) {
        val idStringifier = stringifierByClass.computeIfAbsent(candidateValueClass, aClass -> {
            for (val candidateStringifier : idStringifiers) {
                if (candidateStringifier.handles(candidateValueClass)) {
                    return candidateStringifier;
                }
            }
            return null;
        });
        return Optional.ofNullable(_Casts.uncheckedCast(idStringifier));
    }
}
