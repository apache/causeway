package org.nakedobjects.distribution;

import org.nakedobjects.distribution.dummy.DummyNullValue;
import org.nakedobjects.distribution.dummy.DummyObjectData;
import org.nakedobjects.distribution.dummy.DummyObjectDataFactory;
import org.nakedobjects.distribution.dummy.DummyReferenceData;
import org.nakedobjects.distribution.dummy.DummyValueData;
import org.nakedobjects.object.ConcurrencyException;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.loader.ObjectLoaderImpl;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.object.security.NullSession;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.defaults.MockObjectPersistor;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.object.reflect.DummyVersion;
import test.org.nakedobjects.objects.bom.Movie;
import test.org.nakedobjects.objects.bom.Person;
import test.org.nakedobjects.objects.specification.TestAdapterFactory;
import test.org.nakedobjects.objects.specification.TestObjectFactory;
import test.org.nakedobjects.objects.specification.TestSpecificationLoader;
import test.org.nakedobjects.utility.configuration.TestConfiguration;

public class ServerDistributionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ServerDistributionTest.class);
    }

    private ServerDistribution distribution;
    private MockObjectPersistor persistor;
    private NullSession session;
    private ObjectLoaderImpl loader;
    private SingleResponseUpdateNotifier updates;

    protected void setUp() throws Exception {
        LogManager.getRootLogger().setLevel(Level.OFF);
        
        persistor = new MockObjectPersistor();
        
        ObjectEncoder encoder = new ObjectEncoder();
        encoder.setDataFactory(new DummyObjectDataFactory());
        
        distribution = new ServerDistribution();
        distribution.setEncoder(encoder);

        updates = new SingleResponseUpdateNotifier();
        distribution.setUpdateNotifier(updates);

        
        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        

        session = new NullSession();

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

    public void testExecuteClientActionWithNoWork() {
        ClientActionResultData result = distribution.executeClientAction(session, new ObjectData[0], new ObjectData[0], new ReferenceData[0]);
        
        assertEquals(0, result.getPersisted().length);
        assertEquals(0, result.getChanged().length);

        persistor.assertAction(0, "start transaction");
        persistor.assertAction(1, "end transaction");
    }

    public void testExecuteClientAction() {
/*        Movie movie = new Movie();
        movie.setName("ET");
        NakedObject adapter = NakedObjects.getObjectLoader().createAdapterForTransient(movie);
   */
        
        DummyObjectData movieData = new DummyObjectData(null, Movie.class.getName(), true, null);
        DummyValueData name = new DummyValueData("ET", "java.lang.String");
        movieData.setFieldContent(new Data[] { new DummyNullValue(Person.class.getName()), name });

        persistor.setupMakePersistentOid(new DummyOid(3));
        persistor.setupMakePersistentVersion(new DummyVersion(7));
        
        ClientActionResultData result = distribution.executeClientAction(new NullSession(), new ObjectData[] {movieData}, new ObjectData[0], new ReferenceData[0]);
        
        assertEquals(1, result.getPersisted().length);
        assertEquals(0, result.getChanged().length);
        
        persistor.assertAction(0, "start transaction");
        persistor.assertAction(2, "end transaction");

        assertEquals(new DummyOid(3), result.getPersisted()[0].getOid());
        assertEquals(new DummyVersion(7), result.getPersisted()[0].getVersion());
    }
    
    public void testSetAssociationWithNonCurrentVersion() {
        Movie movie = new Movie();
        NakedObject movieAdapter = loader.createAdapterForTransient(movie);
        movieAdapter.persistedAs(new DummyOid(1));
        
        Person person = new Person();
        NakedObject personAdapter = loader.createAdapterForTransient(person);
        personAdapter.persistedAs(new DummyOid(3));

        persistor.setupAddObject(movieAdapter);
        persistor.setupAddObject(personAdapter);
        
        DummyReferenceData movieData = new DummyReferenceData(new DummyOid(1), Movie.class.getName(), new DummyVersion(2));
        DummyReferenceData personData = new DummyReferenceData(new DummyOid(3), Person.class.getName(), new DummyVersion(4));
        
        movieAdapter.setOptimisticLock(new DummyVersion(1));
        personAdapter.setOptimisticLock(new DummyVersion(4));
        try {
            distribution.setAssociation(session, "director", movieData, personData);
            fail();
        } catch (ConcurrencyException expected) {
        }

        movieAdapter.setOptimisticLock(new DummyVersion(2));
        personAdapter.setOptimisticLock(new DummyVersion(3));
        try {
            distribution.setAssociation(session, "director", movieData, personData);
            fail();
        } catch (ConcurrencyException expected) {
        }
    }
    
    public void testSetAssociation() {
        Movie movie = new Movie();
        NakedObject movieAdapter = loader.createAdapterForTransient(movie);
        movieAdapter.persistedAs(new DummyOid(1));
        movieAdapter.setOptimisticLock(new DummyVersion(2));

        Person person = new Person();
        NakedObject personAdapter = loader.createAdapterForTransient(person);
        personAdapter.persistedAs(new DummyOid(3));
        personAdapter.setOptimisticLock(new DummyVersion(4));

        assertNull(movie.getDirector());
        
        persistor.setupAddObject(movieAdapter);
        persistor.setupAddObject(personAdapter);
        
        updates.addDirty(movieAdapter);
        
        DummyReferenceData movieData = new DummyReferenceData(new DummyOid(1), Movie.class.getName(), new DummyVersion(2));
        DummyReferenceData personData = new DummyReferenceData(new DummyOid(3), Person.class.getName(), new DummyVersion(4));
        ObjectData[] updates = distribution.setAssociation(session, "director", movieData, personData);
        
        assertEquals(person, movie.getDirector());
        
        assertEquals(1, updates.length);
        assertEquals(new DummyOid(1), updates[0].getOid());
/*
        DummyObjectData update = new DummyObjectData(new DummyOid(1), Movie.class.getName(), true, new DummyVersion(1));
        Data field1 = new DummyNullValue(Person.class.getName());
        Data field2 = new DummyValueData("", String.class.getName());
        update.setFieldContent(new Data[] {field1, field2});
        assertEquals(update, updates[0]);
   */
        }
    

    public void testSetValue() {
        Movie movie = new Movie();
        NakedObject movieAdapter = loader.createAdapterForTransient(movie);
        movieAdapter.persistedAs(new DummyOid(1));
        movieAdapter.setOptimisticLock(new DummyVersion(2));

        assertNull(movie.getName());
        
        persistor.setupAddObject(movieAdapter);
        
        updates.addDirty(movieAdapter);
        
        DummyReferenceData movieData = new DummyReferenceData(new DummyOid(1), Movie.class.getName(), new DummyVersion(2));
        ObjectData[] updates = distribution.setValue(session, "name", movieData, "Alan Parker");
        
        assertEquals("Alan Parker", movie.getName());
        
        assertEquals(1, updates.length);
        assertEquals(new DummyOid(1), updates[0].getOid());
        }
    

    public void testClearAssociation() {
        Movie movie = new Movie();
        NakedObject movieAdapter = loader.createAdapterForTransient(movie);
        movieAdapter.persistedAs(new DummyOid(1));
        movieAdapter.setOptimisticLock(new DummyVersion(2));

        Person person = new Person();
        NakedObject personAdapter = loader.createAdapterForTransient(person);
        personAdapter.persistedAs(new DummyOid(3));
        personAdapter.setOptimisticLock(new DummyVersion(4));

        movie.setDirector(person);
        assertEquals(person, movie.getDirector());
        
        persistor.setupAddObject(movieAdapter);
        persistor.setupAddObject(personAdapter);
        
        updates.addDirty(movieAdapter);
        
        DummyReferenceData movieData = new DummyReferenceData(new DummyOid(1), Movie.class.getName(), new DummyVersion(2));
        DummyReferenceData personData = new DummyReferenceData(new DummyOid(3), Person.class.getName(), new DummyVersion(4));
        ObjectData[] updates = distribution.clearAssociation(session, "director", movieData, personData);
        
        assertNull(movie.getDirector());
        
        assertEquals(1, updates.length);
        assertEquals(new DummyOid(1), updates[0].getOid());
/*
        DummyObjectData update = new DummyObjectData(new DummyOid(1), Movie.class.getName(), true, new DummyVersion(1));
        Data field1 = new DummyNullValue(Person.class.getName());
        Data field2 = new DummyValueData("", String.class.getName());
        update.setFieldContent(new Data[] {field1, field2});
        assertEquals(update, updates[0]);
   */
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