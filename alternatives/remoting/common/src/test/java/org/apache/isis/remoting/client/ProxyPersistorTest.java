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

package org.apache.isis.remoting.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ObjectList;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.metamodel.services.container.DomainObjectContainerDefault;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.remoting.client.persistence.ClientSideTransactionManager;
import org.apache.isis.remoting.client.persistence.PersistenceSessionProxy;
import org.apache.isis.remoting.data.Data;
import org.apache.isis.remoting.data.DummyEncodeableObjectData;
import org.apache.isis.remoting.data.DummyIdentityData;
import org.apache.isis.remoting.data.DummyNullValue;
import org.apache.isis.remoting.data.DummyObjectData;
import org.apache.isis.remoting.data.DummyReferenceData;
import org.apache.isis.remoting.data.common.IdentityData;
import org.apache.isis.remoting.data.common.NullData;
import org.apache.isis.remoting.data.common.ObjectData;
import org.apache.isis.remoting.data.common.ReferenceData;
import org.apache.isis.remoting.data.query.PersistenceQueryData;
import org.apache.isis.remoting.exchange.ExecuteClientActionRequest;
import org.apache.isis.remoting.exchange.ExecuteClientActionResponse;
import org.apache.isis.remoting.exchange.FindInstancesRequest;
import org.apache.isis.remoting.exchange.HasInstancesRequest;
import org.apache.isis.remoting.exchange.KnownObjectsRequest;
import org.apache.isis.remoting.exchange.ResolveObjectRequest;
import org.apache.isis.remoting.facade.ServerFacade;
import org.apache.isis.remoting.protocol.encoding.internal.ObjectEncoderDecoder;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSessionFactory;
import org.apache.isis.runtime.persistence.adapterfactory.AdapterFactory;
import org.apache.isis.runtime.persistence.adapterfactory.pojo.PojoAdapterFactory;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerDefault;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManagerExtended;
import org.apache.isis.runtime.persistence.internal.RuntimeContextFromSession;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactory;
import org.apache.isis.runtime.persistence.objectfactory.ObjectFactoryBasic;
import org.apache.isis.runtime.persistence.oidgenerator.OidGenerator;
import org.apache.isis.runtime.persistence.query.PersistenceQueryFindAllInstances;
import org.apache.isis.runtime.testdomain.Movie;
import org.apache.isis.runtime.testdomain.Person;
import org.apache.isis.runtime.testspec.MovieSpecification;
import org.apache.isis.runtime.testsystem.ProxyJunit4TestCase;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtime.testsystem.TestProxyOid;
import org.apache.isis.runtime.testsystem.TestProxyOidGenerator;
import org.apache.isis.runtime.testsystem.TestProxyVersion;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class ProxyPersistorTest extends ProxyJunit4TestCase {

    private final Mockery mockery = new JUnit4Mockery();

    private PersistenceSessionFactory mockPersistenceSessionFactory;
    private ServerFacade mockDistribution;
    private ObjectEncoderDecoder mockEncoder;

    private PersistenceSessionProxy persistenceSessionProxy;
    private ClientSideTransactionManager transactionManager;
    private AuthenticationSession session;

    private AdapterManagerExtended adapterManager;

    private AdapterFactory adapterFactory;
    private ObjectFactory objectFactory;
    private OidGenerator oidGenerator;

    @Before
    public void setUp() throws Exception {
        // createSystem();

        mockPersistenceSessionFactory = mockery.mock(PersistenceSessionFactory.class);
        mockDistribution = mockery.mock(ServerFacade.class);
        mockEncoder = mockery.mock(ObjectEncoderDecoder.class);

        adapterManager = new AdapterManagerDefault();
        adapterFactory = new PojoAdapterFactory();
        objectFactory = new ObjectFactoryBasic();
        oidGenerator = new TestProxyOidGenerator();

        RuntimeContextFromSession runtimeContext = new RuntimeContextFromSession();
        DomainObjectContainerDefault container = new DomainObjectContainerDefault();

        runtimeContext.injectInto(container);
        runtimeContext.setContainer(container);

        ServicesInjectorDefault servicesInjector = new ServicesInjectorDefault();
        servicesInjector.setContainer(container);

        persistenceSessionProxy =
            new PersistenceSessionProxy(mockPersistenceSessionFactory, adapterFactory, objectFactory, servicesInjector,
                oidGenerator, adapterManager, mockDistribution, mockEncoder);

        persistenceSessionProxy.setSpecificationLoader(system.getReflector());
        transactionManager =
            new ClientSideTransactionManager(adapterManager, persistenceSessionProxy, mockDistribution, mockEncoder);
        transactionManager.injectInto(persistenceSessionProxy);

        session = IsisContext.getAuthenticationSession();

        system.setPersistenceSession(persistenceSessionProxy);

        ignoreCallsToInitializeMocks();
        persistenceSessionProxy.open();
    }

    @After
    public void tearDown() throws Exception {
    }

    private static SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    @Test
    public void testClientSideMakesNoRemoteCallsWhenNoWorkNeeded() throws Exception {
        mockery.checking(new Expectations() {
            {
                never(mockDistribution);
            }
        });

        transactionManager.startTransaction();
        transactionManager.endTransaction();
    }

    @Ignore("need to get working after refactoring")
    @Test
    public void testFindInstances() throws Exception {

        // The remote interface is asked for instances, which are returned as data objects
        final DummyObjectData instanceData =
            new DummyObjectData(new TestProxyOid(12, true), Movie.class.getName(), true, new TestProxyVersion(3));

        // The data then needs to be decoded into the ObjectAdapter
        final TestProxyAdapter dummyObjectAdapter = new TestProxyAdapter();
        // new DummyOid(12, true), ResolveState.GHOST, "test");
        dummyObjectAdapter.setupObject(new Movie());
        dummyObjectAdapter.setupSpecification(new MovieSpecification());

        final PersistenceQueryData c = new TestCriteria();
        ObjectSpecification noSpec = getSpecificationLoader().loadSpecification(Movie.class);
        final PersistenceQueryFindAllInstances criteria = new PersistenceQueryFindAllInstances(noSpec);

        final FindInstancesRequest request = new FindInstancesRequest(session, c);

        mockery.checking(new Expectations() {
            {
                one(mockEncoder).decode(instanceData);
                will(returnValue(dummyObjectAdapter));

                one(mockDistribution).findInstances(request);
                will(returnValue(new ObjectData[] { instanceData }));

                one(mockEncoder).encodePersistenceQuery(criteria);
                will(returnValue(c));
            }
        });

        final ObjectAdapter instances = persistenceSessionProxy.findInstances(criteria);

        // the proxy should return one instance, which will be the dummy object created by the encoder's
        // restore call
        final ObjectList objects = (ObjectList) instances.getObject();
        assertEquals(1, objects.size());
        assertEquals(dummyObjectAdapter, objects.elements().nextElement());
    }

    @Test
    public void testResolveImmediatelyIgnoredWhenAlreadyResolving() throws Exception {

        final TestProxyAdapter object = new TestProxyAdapter();
        object.setupResolveState(ResolveState.RESOLVING);

        // implicit: expect no calls to encoder object
        mockery.checking(new Expectations() {
            {
                never(mockDistribution);
            }
        });

        persistenceSessionProxy.resolveImmediately(object);
    }

    @Ignore("need to get working after refactoring")
    @Test
    public void testResolveImmediately() throws Exception {

        final TestProxyAdapter object = new TestProxyAdapter();
        object.setupOid(new TestProxyOid(5));
        object.setupResolveState(ResolveState.GHOST);

        final IdentityData identityData = new DummyReferenceData();
        final ObjectData objectData = new DummyObjectData();
        final ResolveObjectRequest request = new ResolveObjectRequest(session, identityData);

        mockery.checking(new Expectations() {
            {
                // encoder used to create identity data for target object
                one(mockEncoder).encodeIdentityData(object);
                will(returnValue(identityData));

                // remote call asks for object's data
                one(mockDistribution).resolveImmediately(request);
                will(returnValue(objectData));

                // data decode expected
                one(mockEncoder).decode(objectData);
                will(returnValue(null));
            }
        });

        persistenceSessionProxy.resolveImmediately(object);

        /*
         * 
         * assertEquals("ET", movie.getName()); assertEquals(new DummyOid(5), object.getOid()); assertEquals(new
         * DummyVersion(3), object.getVersion());
         */
    }

    @Ignore("TODO")
    @Test
    public void testResolveField_TBI() {
    }

    @Ignore("need to get working after refactoring")
    @Test
    public void testHasInstances() throws Exception {

        final HasInstancesRequest request = new HasInstancesRequest(session, Movie.class.getName());
        mockery.checking(new Expectations() {
            {
                one(mockDistribution).hasInstances(request);
                will(returnValue(true));

                one(mockDistribution).hasInstances(request);
                will(returnValue(false));
            }
        });

        final ObjectSpecification type = getSpecificationLoader().loadSpecification(Movie.class);
        assertTrue(persistenceSessionProxy.hasInstances(type));
        assertFalse(persistenceSessionProxy.hasInstances(type));
    }

    public void testFindInstancesButNoneFound() throws Exception {

        // system.addSpecificationToLoader(new MovieSpecification());

        final PersistenceQueryData c = new TestCriteria();
        ObjectSpecification noSpec = getSpecificationLoader().loadSpecification(Movie.class);
        final PersistenceQueryFindAllInstances criteria = new PersistenceQueryFindAllInstances(noSpec);
        final FindInstancesRequest request = new FindInstancesRequest(session, c);

        mockery.checking(new Expectations() {
            {
                one(mockDistribution).findInstances(request);
                will(returnValue(new ObjectData[0]));

                one(mockEncoder).encodePersistenceQuery(criteria);
                will(returnValue(c));
            }
        });

        persistenceSessionProxy.findInstances(criteria);
    }

    @Test(expected = IllegalStateException.class)
    public void testClientSideActionThrowsExceptionWhenTransactionNotStarted() throws Exception {

        transactionManager.endTransaction();
    }

    @Test
    public void testClientSideActionWhereObjectDeleted() throws Exception {

        final ObjectAdapter movieAdapter = system.createPersistentTestObject();

        // test starts here
        mockery.checking(new Expectations() {
            {
                final DummyIdentityData identityOfObjectToDelete =
                    encoderShouldCreateIdentityDataForMovie(movieAdapter);
                distributionShouldExecuteClientActionForDeletedMovie(identityOfObjectToDelete);
            }

            private DummyIdentityData encoderShouldCreateIdentityDataForMovie(final ObjectAdapter movieAdapter) {
                final DummyIdentityData identityOfObjectToDelete = new DummyIdentityData();

                one(mockEncoder).encodeIdentityData(movieAdapter);
                will(returnValue(identityOfObjectToDelete));
                return identityOfObjectToDelete;
            }

            private void distributionShouldExecuteClientActionForDeletedMovie(
                final DummyIdentityData identityOfObjectToDelete) {
                final Version[] versionUpdates = new Version[] {};
                one(mockDistribution).executeClientAction(with(any(ExecuteClientActionRequest.class)));
                will(returnValue(new ExecuteClientActionResponse(new ObjectData[] {}, versionUpdates, new ObjectData[0])));
            }
        });

        // TODO: should look inside the request object and ensure:
        // with(equalTo(session)),
        // with(equalTo(new ReferenceData[] { identityOfObjectToDelete })),
        // with(equalTo(new int[] { ClientTransactionEvent.DELETE })),

        transactionManager.startTransaction();
        persistenceSessionProxy.destroyObject(movieAdapter);
        transactionManager.endTransaction();
        final List<ObjectAdapter> allDisposedObjects = IsisContext.getUpdateNotifier().getDisposedObjects();

        assertFalse(allDisposedObjects.isEmpty());
        assertEquals(movieAdapter, allDisposedObjects.get(0));
    }

    @Test
    public void testClientSideActionWhereObjectChanged() throws Exception {

        final TestProxyAdapter directorAdapter = new TestProxyAdapter();
        directorAdapter.setupResolveState(ResolveState.RESOLVED);

        final TestProxyAdapter movieAdapter = new TestProxyAdapter();
        movieAdapter.setupResolveState(ResolveState.RESOLVED);

        mockery.checking(new Expectations() {
            {
                final DummyObjectData movieData = encoderShouldCreateGraphForChangedMovie(movieAdapter);
                final DummyObjectData directorData = encoderShouldCreateGraphForChangedDirector(directorAdapter);
                distributionShouldExecuteClientActionForBothChangedObjects(movieData, directorData);
            }

            private DummyObjectData encoderShouldCreateGraphForChangedMovie(final TestProxyAdapter movieAdapter) {
                final DummyObjectData movieData =
                    new DummyObjectData(new TestProxyOid(12, true), Movie.class.getName(), true,
                        new TestProxyVersion(4));
                final DummyEncodeableObjectData expectedMovieName =
                    new DummyEncodeableObjectData("War of the Worlds", String.class.getName());
                final DummyReferenceData expectedDirectorRef =
                    new DummyReferenceData(new TestProxyOid(14, true), Person.class.getName(), new TestProxyVersion(8));
                movieData.setFieldContent(new Data[] { expectedDirectorRef, expectedMovieName });

                one(mockEncoder).encodeGraphForChangedObject(movieAdapter, new KnownObjectsRequest());
                will(returnValue(movieData));
                return movieData;
            }

            private DummyObjectData encoderShouldCreateGraphForChangedDirector(final TestProxyAdapter directorAdapter) {
                final DummyObjectData directorData =
                    new DummyObjectData(new TestProxyOid(14, true), Person.class.getName(), true, new TestProxyVersion(
                        8));
                final DummyEncodeableObjectData expectedDirectorName =
                    new DummyEncodeableObjectData("Unknown", String.class.getName());
                directorData.setFieldContent(new Data[] { expectedDirectorName });

                one(mockEncoder).encodeGraphForChangedObject(directorAdapter, new KnownObjectsRequest());
                will(returnValue(directorData));
                return directorData;
            }

            private void distributionShouldExecuteClientActionForBothChangedObjects(final DummyObjectData movieData,
                final DummyObjectData directorData) {
                // final ObjectData[] changes = new ObjectData[] { movieData, directorData };
                // final int[] types = new int[] { ClientTransactionEvent.CHANGE, ClientTransactionEvent.CHANGE };

                one(mockDistribution).executeClientAction(with(any(ExecuteClientActionRequest.class)));

                final Version[] versionUpdates = new Version[] { new TestProxyVersion(5), new TestProxyVersion(9) };
                will(returnValue(new ExecuteClientActionResponse(new ObjectData[] { movieData, directorData },
                    versionUpdates, new ObjectData[0])));
            }
        });
        // TODO: should look inside the request object and ensure:
        // with(equalTo(session)),
        // with(equalTo(changes)),
        // with(equalTo(types)),

        transactionManager.startTransaction();
        persistenceSessionProxy.objectChanged(movieAdapter);
        persistenceSessionProxy.objectChanged(directorAdapter);
        transactionManager.endTransaction();

        assertEquals(new TestProxyVersion(5), movieAdapter.getVersion());
        assertEquals(new TestProxyVersion(9), directorAdapter.getVersion());
    }

    @Test
    public void testClientSideActionWhereTransientObjectMadePersistent() throws Exception {

        final ObjectAdapter transientObject = system.createTransientTestObject();

        final TestProxyOid previousOid = (TestProxyOid) transientObject.getOid();
        final DummyObjectData movieData = new DummyObjectData(previousOid, Movie.class.getName(), true, null);
        final NullData directorData = new DummyNullValue(Person.class.getName());
        final DummyEncodeableObjectData nameData = new DummyEncodeableObjectData("Star Wars", String.class.getName());
        movieData.setFieldContent(new Data[] { directorData, nameData });

        mockery.checking(new Expectations() {
            {
                // this returns results data with new oid and version
                final TestProxyOid newOid = new TestProxyOid(123, true);
                newOid.setupPrevious(previousOid);
                final DummyReferenceData updateData = new DummyReferenceData(newOid, "type", new TestProxyVersion(456));

                // the server is called with data (movieData) for the object to be persisted
                one(mockDistribution).executeClientAction(with(any(ExecuteClientActionRequest.class)));

                will(returnValue(new ExecuteClientActionResponse(new ReferenceData[] { updateData }, null,
                    new ObjectData[0])));
            }

        });
        // TODO: should look inside the request object and ensure:
        // with(equalTo(session)),
        // with(equalTo(new ReferenceData[] { movieData })),
        // with(equalTo(new int[] { ClientTransactionEvent.ADD })),

        getAdapterManager().adapterFor(transientObject.getObject());

        // client needs to encode the object's transient aspects
        mockery.checking(new Expectations() {
            {
                one(mockEncoder).encodeMakePersistentGraph(with(equalTo(transientObject)),
                    with(any(KnownObjectsRequest.class)));
                will(returnValue(movieData));
            }
        });

        transactionManager.startTransaction();
        persistenceSessionProxy.makePersistent(transientObject);
        transactionManager.endTransaction();
    }

    // /////////////////////////////
    // helpers
    // /////////////////////////////

    private void ignoreCallsToInitializeMocks() {
        mockery.checking(new Expectations() {
            {
                ignoring(mockDistribution).init();
            }
        });
    }

    private void ignoreCallsToDistribution() {
        mockery.checking(new Expectations() {
            {
                ignoring(mockDistribution);
            }
        });
    }

}

class TestCriteria implements PersistenceQueryData {
    private static final long serialVersionUID = 1L;

    @Override
    public Class getPersistenceQueryClass() {
        return null;
    }

    public boolean includeSubclasses() {
        return false;
    }

    @Override
    public String getType() {
        return null;
    }
}
