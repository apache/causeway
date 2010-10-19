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


package org.apache.isis.common.jmock;

import java.lang.reflect.Field;

import org.jmock.Mockery;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class JMockRule implements org.junit.rules.MethodRule {

    /**
     * Only populated if provided in the constructor; otherwise will search reflectively.
     */
    private Mockery context;

    /**
     * Will search for {@link Mockery context} reflectively.
     */
    public JMockRule() {
    }

    /**
     * Specify the {@link Mockery context} to verify (as opposed to searching for it
     * reflectively).
     */
    public JMockRule(Mockery context) {
        this.context = context;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method,
            final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                base.evaluate();
                verifyMocks();
            }

            private void verifyMocks() {
                final Mockery context = contextToVerifyIfAny();
                if(context!=null) {
                    context.assertIsSatisfied();
                } else {
                    throw new AssertionError("could not locate JMock context");
                }
            }

            private Mockery contextToVerifyIfAny() {
                final Mockery context = JMockRule.this.context;
                if (context!=null) {
                    return context;
                }
                return findContextReflectivelyIfAny();
            }

            private Mockery findContextReflectivelyIfAny() {
                Class<?> clsToSearch = method.getMethod()
                        .getDeclaringClass();
                do {
                    final Mockery mockery = findContextIfAny(clsToSearch, target);
                    if (mockery != null) {
                        return mockery;
                    }
                    clsToSearch = clsToSearch.getSuperclass();
                } while (clsToSearch != Object.class);
                return null;
            }

            private Mockery findContextIfAny(final Class<?> cls, final Object target) {
                final Field[] fields = cls.getDeclaredFields();
                for (Field field : fields) {
                    final Class<?> type = field.getType();
                    if (Mockery.class.isAssignableFrom(type)) {
                        try {
                            field.setAccessible(true);
                            return (Mockery) field.get(target);
                        } catch (IllegalArgumentException e) {
                            continue;
                        } catch (IllegalAccessException e) {
                            continue;
                        }
                    }
                }
                return null;
            }
        };
    }

}
