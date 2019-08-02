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
package org.apache.isis.unittestsupport.jmocking;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.auto.Auto;
import org.jmock.auto.Mock;

import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Allowing;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Checking;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.ExpectationsOn;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Ignoring;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Never;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.One;

class MyMockomatic {
    private final JUnitRuleMockery2 context;

    public MyMockomatic(final JUnitRuleMockery2 context) {
        this.context = context;
    }

    public List<Object> fillIn(final Object object, final List<Field> knownFields) {
        List<Object> mocks = new ArrayList<Object>();
        for (final Field field : knownFields) {
            autoMockIfAnnotated(object, field, mocks);
            autoInstantiateIfAnnotated(object, field);
        }
        return mocks;
    }

    private void autoMockIfAnnotated(final Object object,
            final Field field, List<Object> mocks) {
        if (!field.isAnnotationPresent(Mock.class)) {
            return;
        }
        final Object mock = context.mock(field.getType(), field.getName());
        setAutoField(field, object, mock, "auto-mock field " + field.getName());
        if(field.isAnnotationPresent(Ignoring.class)) {
            context.ignoring(mock);
        }
        if(field.isAnnotationPresent(Allowing.class)) {
            context.allowing(mock);
        }
        if(field.isAnnotationPresent(Never.class)) {
            context.never(mock);
        }
        if(field.isAnnotationPresent(One.class)) {
            context.oneOf(mock);
        }
        if(field.isAnnotationPresent(Checking.class)) {
            checking(field, mock);
        }
        mocks.add(mock);
    }

    private <T> void checking(final Field field, final T mock) {
        Checking checking = field.getAnnotation(Checking.class);
        @SuppressWarnings("unchecked")
        Class<? extends ExpectationsOn<T>> expectationsOnClass = (Class<? extends ExpectationsOn<T>>) checking.value();
        context.checking(mock, expectationsOnClass);
    }

    private void autoInstantiateIfAnnotated(final Object object,
            final Field field) {
        if (!field.isAnnotationPresent(Auto.class)) {
            return;
        }
        final Class<?> type = field.getType();
        if (type == States.class) {
            autoInstantiateStates(field, object);
        } else if (type == Sequence.class) {
            autoInstantiateSequence(field, object);
        } else {
            throw new IllegalStateException("cannot auto-instantiate field of type " + type.getName());
        }
    }

    private void autoInstantiateStates(final Field field, final Object object) {
        setAutoField(field, object, context.states(field.getName()), "auto-instantiate States field " + field.getName());
    }

    private void autoInstantiateSequence(final Field field, final Object object) {
        setAutoField(field, object, context.sequence(field.getName()), "auto-instantiate Sequence field " + field.getName());
    }

    private void setAutoField(final Field field, final Object object, final Object value, final String description) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("cannot " + description, e);
        }
    }

}