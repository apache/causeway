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

package junit;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperObject;
import org.apache.isis.core.integtestsupport.legacy.ConfigDir;
import org.apache.isis.core.integtestsupport.legacy.IsisTestRunner;
import org.apache.isis.core.integtestsupport.legacy.Service;
import org.apache.isis.core.integtestsupport.legacy.Services;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntityRepository;
import org.apache.isis.core.wrapper.WrapperFactoryDefault;

@RunWith(IsisTestRunner.class)
@ConfigDir("../../viewer/dnd-tck/src/main/resources")
@Services({ @Service(PrimitiveValuedEntityRepository.class), @Service(WrapperFactoryDefault.class) })
public abstract class AbstractTest {

    private DomainObjectContainer domainObjectContainer;
    private WrapperFactory wrapperFactory;

    /**
     * The {@link WrapperFactory#wrap(Object) wrapped} equivalent of the
     * {@link #setFoobarRepository(PrimitiveValuedEntityRepository) injected}
     * {@link PrimitiveValuedEntityRepository}.
     */
    protected PrimitiveValuedEntityRepository primitivesEntityRepository;

    @Before
    public void wrapInjectedServices() throws Exception {
        primitivesEntityRepository = wrapped(primitivesEntityRepository);
    }

    @Before
    public void setUp() throws Exception {
    }

    protected <T> T wrapped(final T obj) {
        return wrapperFactory.wrap(obj);
    }

    @SuppressWarnings("unchecked")
    protected <T> T unwrapped(final T obj) {
        if (obj instanceof WrapperObject) {
            final WrapperObject wrapperObject = (WrapperObject) obj;
            return (T) wrapperObject.wrapped();
        }
        return obj;
    }

    @After
    public void tearDown() throws Exception {
    }

    // //////////////////////////////////////////////////////
    // Injected.
    // //////////////////////////////////////////////////////

    protected WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }

    public void setWrapperFactory(final WrapperFactory wrapperFactory) {
        this.wrapperFactory = wrapperFactory;
    }

    protected DomainObjectContainer getDomainObjectContainer() {
        return domainObjectContainer;
    }

    public void setDomainObjectContainer(final DomainObjectContainer domainObjectContainer) {
        this.domainObjectContainer = domainObjectContainer;
    }

    public void setFoobarRepository(final PrimitiveValuedEntityRepository toDoItemRepository) {
        this.primitivesEntityRepository = toDoItemRepository;
    }

}
