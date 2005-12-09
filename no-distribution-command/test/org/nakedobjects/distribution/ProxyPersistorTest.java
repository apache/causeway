package org.nakedobjects.distribution;

import org.nakedobjects.distribution.dummy.DummyClientResultData;
import org.nakedobjects.distribution.dummy.DummyNullValue;
import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyObjectDataFactory;
import org.nakedobjects.distribution.dummy.DummyReferenceData;
import org.nakedobjects.distribution.dummy.DummyValueData;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.TypedNakedCollection;
import org.nakedobjects.object.Version;
import org.nakedobjects.object.defaults.NullDirtyObjectSet;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.object.security.NullSession;
import org.nakedobjects.object.transaction.TransactionException;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.easymock.MockControl;

import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.object.reflect.DummyVersion;
import test.org.nakedobjects.objects.bom.Movie;
import test.org.nakedobjects.objects.bom.Person;
import test.org.nakedobjects.objects.specification.TestAdapterFactory;
import test.org.nakedobjects.objects.specification.TestObjectFactory;
import test.org.nakedobjects.objects.specification.TestSpecificationLoader;
import test.org.nakedobjects.utility.configuration.TestConfiguration;


public class ProxyPersistorTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ProxyPersistorTest.class);
    }

    private Distribution distribution;
    private MockControl distributionControl;
    private ProxyPersistor persistor;
    private ObjectLoaderImpl loader;
    private Session session;

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        LogManager.getRootLogger().setLevel(Level.DEBUG);

        persistor = new ProxyPersistor();

        distributionControl = MockControl.createControl(Distribution.class);
        distributionControl.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        distribution = (Distribution) distributionControl.getMock();

        persistor.setConnection(distribution);
        ObjectEncoder encoder = new ObjectEncoder();
        encoder.setDataFactory(new DummyObjectDataFactory());

        persistor.setEncoder(encoder);

        persistor.setUpdateNotifier(new NullDirtyObjectSet());

        session = new NullSession();

        NakedObjectsClient nakedObjects = new NakedObjectsClient();

        loader = new ObjectLoaderImpl();
        loader.setAdapterFactory(new TestAdapterFactory());
        loader.setObjectFactory(new TestObjectFactory());

        nakedObjects.setObjectLoader(loader);

        nakedObjects.setConfiguration(new TestConfiguration());
        nakedObjects.setSpecificationLoader(new TestSpecificationLoader());
        nakedObjects.setObjectPersistor(persistor);
        nakedObjects.setSession(session);

        nakedObjects.init();

    }

    public void testMakePersistentOutsideTransaction() throws Exception {
        NakedObject transientObject = new DummyNakedObject();
        try {
            persistor.makePersistent(transientObject);
            fail();
        } catch (TransactionException e) {}
    }

    public void testClientSideActionWhereNothingDone() throws Exception {
        distributionControl.replay();

        persistor.startTransaction();
        persistor.endTransaction();

        distributionControl.verify();
    }

    public void testAllInstances() throws Exception {
        DummyObjectData instance1 = new DummyObjectData(new DummyOid(12), Movie.class.getName(), true, new DummyVersion(3));
        DummyValueData name = new DummyValueData("ET", "java.lang.String");
        instance1.setFieldContent(new Data[] { new DummyNullValue(Person.class.getName()), name });

        distribution.allInstances(session, Movie.class.getName(), false);
        distributionControl.setReturnValue(new ObjectData[] { instance1 });

        distributionControl.replay();

        TypedNakedCollection instances = persistor.allInstances(NakedObjects.getSpecificationLoader().loadSpecification(
                Movie.class), false);

        distributionControl.verify();

        assertEquals(1, instances.size());
        NakedObject object = instances.elementAt(0);
        Movie movie = (Movie) object.getObject();
        assertEquals("ET", movie.getName());
        assertEquals(new DummyOid(12), object.getOid());
        assertEquals(new DummyVersion(3), object.getVersion());
    }

    public void testResolveImmediately() throws Exception {
        DummyObjectData instance1 = new DummyObjectData(new DummyOid(5), Movie.class.getName(), true, new DummyVersion(3));
        DummyValueData name = new DummyValueData("ET", "java.lang.String");
        instance1.setFieldContent(new Data[] { new DummyNullValue(Person.class.getName()), name });

        DummyReferenceData ref = new DummyReferenceData(new DummyOid(5), Movie.class.getName(), null);
        distribution.resolveImmediately(session, ref);
        distributionControl.setReturnValue(instance1);

        distributionControl.replay();

        NakedObject object;
        object = loader.recreateAdapterForPersistent(new DummyOid(5), NakedObjects.getSpecificationLoader().loadSpecification(
                Movie.class));
        Movie movie = (Movie) object.getObject();
        assertEquals(null, movie.getName());

        persistor.resolveImmediately(object);

        distributionControl.verify();

        assertEquals("ET", movie.getName());
        assertEquals(new DummyOid(5), object.getOid());
        assertEquals(new DummyVersion(3), object.getVersion());
    }

    public void testHasInstances() throws Exception {
        distribution.hasInstances(session, Movie.class.getName());
        distributionControl.setReturnValue(true);
        distributionControl.setReturnValue(false);

        distributionControl.replay();

        NakedObjectSpecification type = NakedObjects.getSpecificationLoader().loadSpecification(Movie.class);
        assertTrue(persistor.hasInstances(type, false));
        assertFalse(persistor.hasInstances(type, false));

        distributionControl.verify();
    }

    public void testNumberOfInstances() throws Exception {
        distribution.numberOfInstances(session, Movie.class.getName());
        distributionControl.setReturnValue(10);
        distributionControl.setReturnValue(4);

        distributionControl.replay();

        NakedObjectSpecification type = NakedObjects.getSpecificationLoader().loadSpecification(Movie.class);
        assertEquals(10, persistor.numberOfInstances(type, false));
        assertEquals(4, persistor.numberOfInstances(type, false));

        distributionControl.verify();
    }

    public void testAllInstancesButNoneFound() throws Exception {
        distribution.allInstances(session, Movie.class.getName(), false);
        distributionControl.setReturnValue(new ObjectData[0]);

        distributionControl.replay();

        persistor.allInstances(NakedObjects.getSpecificationLoader().loadSpecification(Movie.class), false);

        distributionControl.verify();
    }

    public void testClientSideActionWhereObjectChanged() throws Exception {
        DummyObjectData expectedMovie = new DummyObjectData(new DummyOid(12), Movie.class.getName(), true, new DummyVersion(4));
        DummyValueData expectedMovieName = new DummyValueData("War of the Worlds", String.class.getName());
        DummyReferenceData expectedDirectorRef = new DummyReferenceData(new DummyOid(14), Person.class.getName(),
                new DummyVersion(8));
        expectedMovie.setFieldContent(new Data[] { expectedDirectorRef, expectedMovieName });

        DummyObjectData expectedDirector = new DummyObjectData(new DummyOid(14), Person.class.getName(), true,
                new DummyVersion(8));
        DummyValueData expectedDirectorName = new DummyValueData("Unknown", String.class.getName());
        expectedDirector.setFieldContent(new Data[] { expectedDirectorName });

        ObjectData[] changes = new ObjectData[] { expectedMovie, expectedDirector };
        distribution.executeClientAction(session, new ObjectData[0], changes, new ReferenceData[0]);
        Version[] versionUpdates = new Version[] { new DummyVersion(5), new DummyVersion(9) };
        distributionControl.setReturnValue(new DummyClientResultData(null, versionUpdates));
        distributionControl.replay();

        Person director = new Person();
        NakedObject directorAdapter = loader.createAdapterForTransient(director);
        directorAdapter.persistedAs(new DummyOid(14));
        directorAdapter.setOptimisticLock(new DummyVersion(8));

        Movie movie = new Movie();
        NakedObject movieAdapter = loader.createAdapterForTransient(movie);
        movieAdapter.persistedAs(new DummyOid(12));
        movieAdapter.setOptimisticLock(new DummyVersion(4));

        director.setName("Unknown");
        movie.setName("War of the Worlds");
        movie.setDirector(director);

        persistor.startTransaction();
        persistor.objectChanged(movieAdapter);
        persistor.objectChanged(directorAdapter);
        persistor.endTransaction();

        assertEquals(new DummyVersion(5), movieAdapter.getVersion());
        assertEquals(new DummyVersion(9), directorAdapter.getVersion());
        distributionControl.verify();
    }

    public void testClientSideActionWhereTransientObjectMadePersistent() throws Exception {
        DummyObjectData movieData = new DummyObjectData(null, Movie.class.getName(), true, null);
        NullData directorData = new DummyNullValue(Person.class.getName());
        DummyValueData nameData = new DummyValueData("Star Wars", String.class.getName());
        movieData.setFieldContent(new Data[] { directorData, nameData });

        DummyObjectData updateData = new DummyObjectData(new DummyOid(123), "type", true, new DummyVersion(456));

        distribution.executeClientAction(session, new ObjectData[] { movieData }, new ObjectData[0], new ReferenceData[0]);
        distributionControl.setReturnValue(new DummyClientResultData(new ObjectData[] { updateData }, null));
        distributionControl.replay();

        Movie movie = new Movie();
        movie.setName("Star Wars");

        NakedObject transientObject = loader.createAdapterForTransient(movie);
        assertEquals(null, transientObject.getOid());
        assertEquals(null, transientObject.getVersion());

        persistor.startTransaction();
        persistor.makePersistent(transientObject);
        persistor.endTransaction();

        assertEquals(new DummyOid(123), transientObject.getOid());
        assertEquals(new DummyVersion(456), transientObject.getVersion());

        distributionControl.verify();
    }

    public void testObjectChangedtOutsideTransaction() throws Exception {
        DummyNakedObject transientObject = new DummyNakedObject();
        transientObject.setupResolveState(ResolveState.RESOLVED);
        try {
            persistor.objectChanged(transientObject);
            fail();
        } catch (TransactionException e) {}
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */