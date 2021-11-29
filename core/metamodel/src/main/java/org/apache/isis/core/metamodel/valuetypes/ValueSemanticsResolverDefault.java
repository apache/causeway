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
package org.apache.isis.core.metamodel.valuetypes;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.applib.value.semantics.ValueSemanticsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.valuesemantics.EnumValueSemanticsAbstract;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@Named("isis.metamodel.ValueSemanticsResolverDefault")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ValueSemanticsResolverDefault
implements ValueSemanticsResolver {

    // managed by Spring
    private final List<ValueSemanticsProvider<?>> valueSemanticsProviders;
    private final TranslationService translationService;

    @Override
    public boolean hasValueSemantics(final Class<?> valueType) {
        return streamValueSemantics(valueType).findAny().isPresent();
    }

    @Override
    public <T> Stream<ValueSemanticsProvider<T>> streamValueSemantics(final Class<T> _valueType) {
        final var valueType = ClassUtils.resolvePrimitiveIfNecessary(_valueType);
//        val resolvableType = ResolvableType
//                .forClassWithGenerics(ValueSemanticsProvider.class, valueType);
        return Stream.<ValueSemanticsProvider<T>>concat(

                _NullSafe.stream(valueSemanticsProviders)
                //.filter(resolvableType::isInstance) //does not work for eg. TreeNode<?> ... Spring believes there is a wildcard mismatch
                .filter(vs->vs.getCorrespondingClass().isAssignableFrom(valueType))
                .map(provider->_Casts.<ValueSemanticsProvider<T>>uncheckedCast(provider)),

                // if we have an Enum, append default Enum semantics to the stream,
                // as these are not yet managed by Spring
                valueType.isEnum()
                    ? Stream.of(getDefaultEnumSemantics(_valueType))
                    : Stream.empty());
    }

    @Override
    public <T> Can<ValueSemanticsProvider<T>> selectValueSemantics(
            final @NonNull Identifier featureIdentifier,
            final Class<T> valueType) {
        //FIXME[ISIS-2877] honor customizations
        return streamValueSemantics(valueType)
                .collect(Can.toCan());
    }

    @Override
    public Stream<Class<?>> streamClassesWithValueSemantics() {
        return _NullSafe.stream(valueSemanticsProviders)
        .map(ValueSemanticsProvider::getCorrespondingClass);
    }

    // -- HELPER

    // managed by Isis
    @SuppressWarnings("rawtypes")
    private Map<Class<?>, ValueSemanticsProvider> enumSemantics = _Maps.newConcurrentHashMap();

    @SuppressWarnings("unchecked")
    public <T> ValueSemanticsProvider<T> getDefaultEnumSemantics(final Class<T> enumType) {
        return enumSemantics.computeIfAbsent(enumType, t->
                EnumValueSemanticsAbstract
                  .create(
                          translationService,
                          // in order to simplify matters, we just assume this for enums
                          IntrospectionPolicy.ENCAPSULATION_ENABLED,
                  enumType));
    }

}