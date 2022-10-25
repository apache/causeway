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

package org.apache.causeway.core.metamodel.services.idstringifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet.PrimaryKeyType;

import lombok.val;

/**
 * Convenience service that looks up (and caches) the {@link IdStringifier}
 * available for a given value class, and optionally the class of the owning entity.
 * <p>
 * This is intended for framework use, there is little reason to call it or override it.
 *
 * @since 2.0
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".IdStringifierLookupService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class IdStringifierLookupService {

    private final Can<IdStringifier<?>> idStringifiers;
    private final Map<Class<?>, IdStringifier<?>> stringifierByClass = new ConcurrentHashMap<>();

    @Inject
    public IdStringifierLookupService(
            final List<IdStringifier<?>> idStringifiers,
            final Optional<IdStringifier<Serializable>> idStringifierForSerializableIfAny) {
        // IdStringifierForSerializable is enforced to go last, so any custom IdStringifier(s)
        // that do not explicitly specify an @Order/@Precedence go earlier
        idStringifierForSerializableIfAny
        .ifPresent(idStringifierForSerializable->{
            idStringifiers.removeIf(idStringifier->idStringifierForSerializable.getClass()
                    .equals(idStringifier.getClass()));
            idStringifiers.add(idStringifierForSerializable); // put last
        });
        this.idStringifiers = Can.ofCollection(idStringifiers);
    }

    public <T> PrimaryKeyType<T> primaryKeyTypeFor(
            final Class<?> entityClass, final Class<T> primaryKeyType) {
        return PrimaryKeyType.getInstance(entityClass,
                this::lookupIdStringifierElseFail,
                primaryKeyType);
    }

    public <T> IdStringifier<T> lookupIdStringifierElseFail(final Class<T> candidateValueClass) {
        return lookupIdStringifier(candidateValueClass)
            .orElseThrow(() -> _Exceptions.noSuchElement(
                    "Could not locate an IdStringifier to handle '%s'",
                    candidateValueClass));
    }

    public <T> Optional<IdStringifier<T>> lookupIdStringifier(final Class<T> candidateValueClass) {
        val idStringifier = stringifierByClass.computeIfAbsent(
                ClassUtils.resolvePrimitiveIfNecessary(candidateValueClass), aClass -> {
            for (val candidateStringifier : idStringifiers) {
                if (candidateStringifier.getCorrespondingClass().isAssignableFrom(candidateValueClass)) {
                    return candidateStringifier;
                }
            }
            return null;
        });
        return Optional.ofNullable(_Casts.uncheckedCast(idStringifier));
    }

}
