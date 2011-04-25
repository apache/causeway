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

/**
 * A subinterface of {@link Fixture} representing a mock collaborator to be setup.
 */
public interface MockFixture<T> extends Fixture<MockFixture.Context> {

    public interface Context extends Fixture.Context {

        FixtureMockery getMockery();

        /**
         * Name to use for mock, if any.
         * 
         * <p>
         * If <tt>null</tt>, then uses the default name.
         */
        String name();
    }

    public static class Builder<T extends MockFixture<?>> {

        private final FixtureMockery mockery;
        private T fixture;
        private String name;

        public Builder(final FixtureMockery mockery, final Class<T> cls) {
            this.mockery = mockery;
            try {
                fixture = cls.newInstance();
            } catch (final InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (final IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }

        }

        public Builder<T> named(final String name) {
            this.name = name;
            return this;
        }

        public T build() {
            final Context context = new Context() {

                @Override
                public FixtureMockery getMockery() {
                    return mockery;
                }

                @Override
                public String name() {
                    return name;
                }
            };
            fixture.setUp(context);
            return fixture;
        }
    }

    T object();

}
