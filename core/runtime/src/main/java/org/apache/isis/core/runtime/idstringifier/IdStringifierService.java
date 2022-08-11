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

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.ValueSemantics;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.IsisModuleCoreRuntime;

import lombok.NonNull;
import lombok.val;

/**
 * Convenience service that looks up (and caches) the {@link IdStringifier}
 * available for a given value class, and optionally the class of the owning entity.
 * <p>
 * This is intended for framework use, there is little reason to call it or override it.
 *
 * @implNote yet does not support per member ValueSemantics selection;
 *      future work would look for {@link ValueSemantics} annotations on primary key members and
 *      would then honor {@link ValueSemantics#provider()} attribute,
 *      to narrow the {@link IdStringifier} search
 *
 * @since 2.0
 */
@Service
@Named(IsisModuleCoreRuntime.NAMESPACE + ".IdStringifierService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class IdStringifierService {

    private final Can<IdStringifier<?>> idStringifiers;
    private final Map<Class<?>, IdStringifier<?>> stringifierByClass = new ConcurrentHashMap<>();

    @Inject
    public IdStringifierService(
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

    public <T> String enstringPrimaryKey(final @NonNull Class<T> primaryKeyType, final @NonNull Object primaryKey) {
        val idStringifier = lookupElseFail(ClassUtils.resolvePrimitiveIfNecessary(primaryKeyType));
        return idStringifier.enstring(_Casts.uncheckedCast(primaryKey));
    }

    public <T> T destringPrimaryKey(
            final @NonNull Class<T> primaryKeyType,
            final @NonNull Class<?> entityClass,
            final @NonNull String stringifiedId) {
        val idStringifier = lookupElseFail(ClassUtils.resolvePrimitiveIfNecessary(primaryKeyType));
        @SuppressWarnings("unchecked")
        val primaryKey = _Casts.castTo(IdStringifier.SupportingTargetEntityClass.class, idStringifier)
                .map(stringifier->stringifier.destring(stringifiedId, entityClass))
                .orElseGet(()->idStringifier.destring(stringifiedId));
        return _Casts.uncheckedCast(primaryKey);
    }

    // -- HELPER

    private <T> IdStringifier<T> lookupElseFail(final Class<T> candidateValueClass) {
        return lookup(candidateValueClass)
            .orElseThrow(() -> _Exceptions.noSuchElement(
                    "Could not locate an IdStringifier to handle '%s'",
                    candidateValueClass));
    }

    private <T> Optional<IdStringifier<T>> lookup(final Class<T> candidateValueClass) {
        val idStringifier = stringifierByClass.computeIfAbsent(candidateValueClass, aClass -> {
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
