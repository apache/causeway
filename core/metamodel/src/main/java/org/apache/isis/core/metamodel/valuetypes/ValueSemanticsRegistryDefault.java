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
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.valuetypes.ValueSemanticsRegistry;

import lombok.RequiredArgsConstructor;

@Service
@Named("isis.metamodel.ValueSemanticsRegistryDefault")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ValueSemanticsRegistryDefault
implements ValueSemanticsRegistry {

    // managed by Spring
    private final List<ValueSemanticsProvider<?>> valueSemanticsProviders;

    @Override
    public boolean hasValueSemantics(final Class<?> valueType) {
        return streamValueSemantics(valueType).findAny().isPresent();
    }

    @Override
    public <T> Stream<ValueSemanticsProvider<T>> streamValueSemantics(final Class<T> valueType) {
        var resolvableType = ResolvableType
                .forClassWithGenerics(ValueSemanticsProvider.class, ClassUtils.resolvePrimitiveIfNecessary(valueType));
        return _NullSafe.stream(valueSemanticsProviders)
                .filter(resolvableType::isInstance)
                .map(provider->_Casts.<ValueSemanticsProvider<T>>uncheckedCast(provider));
    }

    @Override
    public <T> Can<ValueSemanticsProvider<T>> selectValueSemantics(final Class<T> valueType) {
        return streamValueSemantics(valueType)
                .collect(Can.toCan());
    }

    @Override
    public Stream<Class<?>> streamClassesWithValueSemantics() {
        return _NullSafe.stream(valueSemanticsProviders)
        .map(ValueSemanticsProvider::getCorrespondingClass);
    }

    // -- HELPER

    //FIXME[ISIS-2871] enums are the outliers here maybe custom bean factory to the rescue?
    // https://www.baeldung.com/spring-factorybean

    // managed by Isis
    //private Map<Class<?>, ValueSemanticsProvider<?>> enumSemantics = new LinkedHashMap<>();


//    final Can<ValueSemanticsProvider> valueSemantics =
//    Stream.<ValueSemanticsProvider>concat(
//            (Stream<? extends ValueSemanticsProvider>) valueSemanticsRegistry.streamValueSemantics(cls),
//            cls.isEnum()
//            ? Stream.of(EnumValueSemanticsAbstract
//                            .create(getTranslationService(),
//                                    processClassContext.getIntrospectionPolicy(),
//                                    cls))
//            : Stream.empty())
//    .collect(Can.toCan());



}