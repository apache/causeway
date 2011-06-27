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

package org.apache.isis.core.testsupport.jmock;

import org.jmock.Mockery;

/**
 * Adapter for {@link MockFixture}, removing some of the boilerplate.
 */
public abstract class MockFixtureAdapter<T> implements MockFixture<T> {

    private T object;

    /**
     * Assumes that the mock created is the one to be {@link #object() returned} (though subclass can override that if
     * need be).
     */
    protected T createMock(final MockFixture.Context fixtureContext, final Class<T> cls) {
        final Mockery mockery = fixtureContext.getMockery();
        final String name = fixtureContext.name();
        return object = name != null ? mockery.mock(cls, name) : mockery.mock(cls);
    }

    @Override
    public T object() {
        return object;
    }
}