/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.specsupport.scenarios;

import java.util.Map;
import java.util.Set;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.unittestsupport.jmocking.Imposterisers;

/**
 * @deprecated - with no replacement
 */
@Deprecated
class DomainServiceProviderMockery implements DomainServiceProvider {

    private RepositoryService mockRepositoryService = null;
    private FactoryService mockFactoryService = null;
    private final Map<Class<?>, Object> mocks = _Maps.newHashMap();

    private Mockery context;

    private ScenarioExecution scenarioExecution;

    DomainServiceProviderMockery() {
        init();
    }

    private void init() {
        context = new Mockery() {{
            setImposteriser(Imposterisers.getDefault());
        }};
        mocks.clear();
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(Class<T> serviceClass) {
        Object mock = doGetService(serviceClass);

        if(FactoryService.class.isAssignableFrom(serviceClass)) {
            mockFactoryService = (FactoryService) mock;

            context.checking(new Expectations() {
                {
                    allowing(mockFactoryService).instantiate(with(Expectations.<Class<?>>anything()));
                    will(new Action() {

                        @Override
                        @SuppressWarnings("rawtypes")
                        public Object invoke(Invocation invocation) throws Throwable {
                            Class cls = (Class) invocation.getParameter(0);
                            return scenarioExecution.injectServices(cls.newInstance());
                        }

                        @Override
                        public void describeTo(Description description) {
                            description.appendText("newTransientInstance");
                        }
                    });
                }
            });
        }
        if(RepositoryService.class.isAssignableFrom(serviceClass)) {
            mockRepositoryService = (RepositoryService) mock;
            context.checking(new Expectations() {
                {
                    allowing(mockRepositoryService).persist(with(Expectations.<T>anything()));
                }
            });
        }
        return (T) mock;
    }

    protected <T> Object doGetService(final Class<T> serviceClass) {
        Object mock = mocks.get(serviceClass);
        if(mock == null) {
            mock = context.mock(serviceClass);
        }
        mocks.put(serviceClass, mock);
        return mock;
    }

    @Override
    public <T> void replaceService(final T original, final T replacement) {
        final Class<?> originalKey = keyFor(original);
        if(originalKey == null) {
            throw new IllegalArgumentException("Service to replace not found");
        }
        mocks.put(originalKey, replacement);
    }

    private Class<?> keyFor(Object original) {
        final Set<Map.Entry<Class<?>, Object>> entries = mocks.entrySet();
        for (Map.Entry<Class<?>, Object> entry : entries) {
            if(entry.getValue() == original) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Mockery mockery() {
        return context;
    }

    DomainServiceProviderMockery init(ScenarioExecution scenarioExecution) {
        this.scenarioExecution = scenarioExecution;
        return this;
    }

    /**
     * not API
     */
    void assertIsSatisfied() {
        mockery().assertIsSatisfied();
        // discard all existing mocks and mockery, to start again.
        init();
    }
}