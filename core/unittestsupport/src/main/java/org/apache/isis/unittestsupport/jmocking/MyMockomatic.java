/**
Copyright (c) 2000-2007, jMock.org
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of
conditions and the following disclaimer. Redistributions in binary form must reproduce
the above copyright notice, this list of conditions and the following disclaimer in
the documentation and/or other materials provided with the distribution.

Neither the name of jMock nor the names of its contributors may be used to endorse
or promote products derived from this software without specific prior written
permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
DAMAGE.
 */
package org.apache.isis.unittestsupport.jmocking;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Allowing;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Checking;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.ExpectationsOn;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Ignoring;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.Never;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2.One;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.auto.Auto;
import org.jmock.auto.Mock;

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