package org.nakedobjects.persistence.file;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.persistence.ObjectManagerException;
import org.nakedobjects.object.persistence.defaults.SerialOid;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class XmlDataManagerTest extends TestCase {
	private static final Logger LOG = Logger.getLogger(XmlDataManagerTest.class);
	
	protected XmlDataManager manager;
	protected final int SIZE = 5;
	
	private SerialOid oids[];
	private ObjectData data[];
	private ObjectData pattern;
	
	public static void main(String[] args) {
 		junit.textui.TestRunner.run(XmlDataManagerTest.class);
	}

	protected void setUp() throws Exception {		
		LogManager.getLoggerRepository().setThreshold(Level.OFF);
		
		new NakedObjectsClient().setConfiguration(new Configuration());
 		XmlDataManager.clearTestDirectory();
		manager = new XmlDataManager("tmp/tests");

		//MockObjectManager.setup();
        //NakedObjectSpecificationLoaderImpl specificationLoader = new NakedObjectSpecificationLoaderImpl();
        //NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        //specificationLoader.setReflectorFactory(new DummyReflectorFactory());

		MockNakedObjectSpecificationLoader specificationLoader = new MockNakedObjectSpecificationLoader();
		new NakedObjectsClient().setSpecificationLoader(specificationLoader);
        
        //ConfigurationFactory.setConfiguration(new Configuration());
        
		oids = new SerialOid[SIZE];
		data = new ObjectData[SIZE];
//		NakedObjectSpecification type = specificationLoader.loadSpecification(Role.class.getName());
		NakedObjectSpecification type = new DummyNakedObjectSpecification();
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);
        specificationLoader.addSpecification(type);

        pattern = new ObjectData(type, null);
		for (int i = 0; i < SIZE; i++) {
			oids[i] = new SerialOid(i);
			data[i] = new ObjectData(type, oids[i]);
			manager.insert(data[i]);
		}
	/*	
		assertEquals(SIZE, manager.numberOfInstances(pattern));
		for (int i = 0; i < SIZE; i++) {
			assertEquals(data[i], manager.loadData(oids[i]));
			assertTrue(manager.getInstances(pattern).contains(data[i]));
		}
		*/
		super.setUp();
		LOG.debug("test starting...");
	}
	
	protected void tearDown() throws Exception {
		LOG.debug("...Test ended\n\n");
		super.tearDown();
	}
	
	public void testCreateOid() throws Exception {
		SerialOid oid = manager.createOid();
		long start = oid.getSerialNo();
		long next = start +1;
		for (int i = 0; i < 3; i++) {
			oid = manager.createOid();
			assertEquals(next++, oid.getSerialNo());
		}
	}

	public void testNextId() throws Exception {
		long first = manager.nextId();
		assertEquals(first + 1, manager.nextId());
		assertEquals(first + 2, manager.nextId());
		assertEquals(first + 3, manager.nextId());
	}

	public void testInsertObjectWithFields() throws ObjectManagerException {
		ObjectData data = createData(Role.class, 99);
		data.set("Person", new SerialOid(101));
		assertNotNull(data.get("Person"));
		data.set("Name", "Harry");
		assertNotNull(data.get("Name"));
		
		manager.insert(data);

		ObjectData read = manager.loadObjectData(data.getOid());
		assertEquals(data.getOid(), read.getOid());
		assertEquals(data.getClassName(), read.getClassName());
		
		assertEquals(data.get("Person"), read.get("Person"));
		assertEquals(data.get("Name"), read.get("Name"));
	}


	public void testInsertObjectWithEmptyOneToManyAssociations() throws ObjectManagerException {
		ObjectData data = createData(Team.class, 99);

		SerialOid coid = new SerialOid(103);
		data.initCollection(coid, "Members");
		
		manager.insert(data);

		ObjectData read = manager.loadObjectData(data.getOid());
		assertEquals(data.getOid(), read.getOid());
		assertEquals(data.getClassName(), read.getClassName());
		
		ReferenceVector c = read.elements("Members");
		assertNull(c);
	}

	public void testInsertObjectWithOneToManyAssociations() throws ObjectManagerException {
		ObjectData data = createData(Team.class, 99);
		
		SerialOid coid = new SerialOid(103);
		data.initCollection(coid, "Members");
		SerialOid oid[] = new SerialOid[3];
		for (int i = 0; i < oid.length; i++) {
			oid[i] = new SerialOid(104 + i);
			data.addElement("Members", oid[i]);
		}
		manager.insert(data);

		ObjectData read = manager.loadObjectData(data.getOid());
		assertEquals(data.getOid(), read.getOid());
		assertEquals(data.getClassName(), read.getClassName());
		
		ReferenceVector c = read.elements("Members");
		for (int i = 0; i < oid.length; i++) {
			assertEquals(oid[i], c.elementAt(i));
		}		
	}
	
	public void testNumberOfInstances() {
	    assertEquals(SIZE, manager.numberOfInstances(pattern));
	}

	public void testRemove() throws Exception {
		SerialOid oid = oids[2];
		manager.remove(oid);
		
		assertEquals(SIZE - 1, manager.numberOfInstances(pattern));
		
		ObjectDataVector instances = manager.getInstances(pattern);
		for (int i = 0; i < instances.size(); i++) {
			assertFalse(instances.element(i) == data[2]);
		}
		
		assertNull(manager.loadObjectData(oid));
	}

	public void testSaveObject() throws Exception {
		data[2].set("Person", new SerialOid(231));
		data[2].set("Name", "Fred");
		manager.save(data[2]);
		
		assertTrue(manager.getInstances(pattern).contains(data[2]));
		ObjectData read = manager.loadObjectData(oids[2]);
		assertEquals(data[2], read);
		assertEquals(data[2].get("Name"), read.get("Name"));
		assertEquals(data[2].get("Person"), read.get("Person"));
	}
	/*
	public void xxxtestInsertValues() throws ObjectStoreException {
		NakedObjectSpecification type = NakedObjects.getSpecificationLoader().loadSpecification(ValueObjectExample.class.getName());
		SerialOid oid = new SerialOid(99);
		ObjectData data =  new ObjectData(type, oid);


		Date date1 = new Date();
		date1.add(1,2,3);
		data.saveValue("Date", date1);
		
		FloatingPointNumber floatingPoint1 = new FloatingPointNumber();
		floatingPoint1.setValue(3.145);
		data.saveValue("Floating Point", floatingPoint1);
		
		Label label1 = new Label();
		label1.setValue("Labelled");
		data.saveValue("Label", label1);
		
		Logical logical1 = new Logical();
		logical1.setValue(true);
		data.saveValue("Logical", logical1);
		
		Money money1 = new Money();
		money1.setValue(1233.45);
		data.saveValue("Money", money1);
		
		Option option1 = new Option(new String[] {"Fred", "Sam", "joe"}, 1);
		data.saveValue("Option", option1);
		
		Percentage percentage1 = new Percentage();
		percentage1.setValue(95);
		data.saveValue("Percentage", percentage1);
		
		TextString textString1 = new TextString("Fred");
		data.saveValue("Text String", textString1);
		
		DateTime timestamp1 = new DateTime();
		timestamp1.add(1,2,3);
		data.saveValue("Time Stamp", timestamp1);

		Time time1 = new Time();
		time1.add(1,30);
		data.saveValue("Time", time1);

		URLString urlString1 = new URLString("http://nakedobjects.org/");
		data.saveValue("Url String", urlString1);

		WholeNumber number1 = new WholeNumber();
		number1.setValue(435422);
		data.saveValue("Whole Number", number1);
		
		
		manager.insert(data);
		
		
		
		ObjectData object = manager.loadObjectData(oid);

		Date date2 = new Date();
		object.restoreValue("Date", date2);
		assertEquals(date1, date2);

		FloatingPointNumber floatingPoint2 = new FloatingPointNumber();
		object.restoreValue("Floating Point", floatingPoint2);
		assertEquals(floatingPoint1, floatingPoint2);

		Label label2 = new Label();
		object.restoreValue("Label", label2);
		assertEquals(label1, label2);

		Logical logical2 = new Logical();
		object.restoreValue("Logical", logical2);
		assertEquals(logical1, logical2);
		
		Money money2 = new Money();
		object.restoreValue("Money", money2);
		assertEquals(money1, money2);

		Option option2 = new Option(new String [] {"Fred", "Sam", "joe"});
		object.restoreValue("Option", option2);
		assertEquals(option1, option2);
		
		Percentage percentage2 = new Percentage();
		object.restoreValue("Percentage", percentage2);
		assertEquals(percentage1, percentage2);

		Time time2 = new Time();
		object.restoreValue("Time", time2);
		assertEquals(time1, time2);
		
		DateTime timestamp2 = new DateTime();
		object.restoreValue("Time Stamp", timestamp2);
		assertEquals(timestamp1, timestamp2);
		
		TextString textString2 = new TextString();
		object.restoreValue("Text String", textString2);
		assertEquals(textString1, textString2);
		
		URLString urlString2 = new URLString();
		object.restoreValue("Url String", urlString2);
		assertEquals(urlString1, urlString2);
	
		WholeNumber number2 = new WholeNumber();
		object.restoreValue("Whole Number", number2);
		assertEquals(number1, number2);
	}

	public void xxxtestSaveValues() throws ObjectStoreException {
		NakedObjectSpecification type = NakedObjects.getSpecificationLoader().loadSpecification(ValueObjectExample.class.getName());
		SerialOid oid = new SerialOid(99);
		ObjectData data =  new ObjectData(type, oid);

		manager.insert(data);
		
		
		Date date1 = new Date();
		date1.add(1,2,3);
		data.saveValue("Date", date1);
		
		FloatingPointNumber floatingPoint1 = new FloatingPointNumber();
		floatingPoint1.setValue(3.145);
		data.saveValue("Floating Point", floatingPoint1);
		
		Label label1 = new Label();
		label1.setValue("Labelled");
		data.saveValue("Label", label1);
		
		Logical logical1 = new Logical();
		logical1.setValue(true);
		data.saveValue("Logical", logical1);
		
		Money money1 = new Money();
		money1.setValue(1233.45);
		data.saveValue("Money", money1);
		
		Option option1 = new Option(new String[] {"Fred", "Sam", "joe"}, 1);
		data.saveValue("Option", option1);
		
		Percentage percentage1 = new Percentage();
		percentage1.setValue(95);
		data.saveValue("Percentage", percentage1);
		
		TextString textString1 = new TextString("Fred");
		data.saveValue("Text String", textString1);
		
		DateTime timestamp1 = new DateTime();
		timestamp1.add(1,2,3);
		data.saveValue("Time Stamp", timestamp1);

		Time time1 = new Time();
		time1.add(1,30);
		data.saveValue("Time", time1);

		URLString urlString1 = new URLString("http://nakedobjects.org/");
		data.saveValue("Url String", urlString1);

		WholeNumber number1 = new WholeNumber();
		number1.setValue(435422);
		data.saveValue("Whole Number", number1);
		
		
		manager.save(data);
		
		
		
		ObjectData object = manager.loadObjectData(oid);

		Date date2 = new Date();
		object.restoreValue("Date", date2);
		assertEquals(date1, date2);

		FloatingPointNumber floatingPoint2 = new FloatingPointNumber();
		object.restoreValue("Floating Point", floatingPoint2);
		assertEquals(floatingPoint1, floatingPoint2);

		Label label2 = new Label();
		object.restoreValue("Label", label2);
		assertEquals(label1, label2);

		Logical logical2 = new Logical();
		object.restoreValue("Logical", logical2);
		assertEquals(logical1, logical2);
		
		Money money2 = new Money();
		object.restoreValue("Money", money2);
		assertEquals(money1, money2);

		Option option2 = new Option(new String [] {"Fred", "Sam", "joe"});
		object.restoreValue("Option", option2);
		assertEquals(option1, option2);
		
		Percentage percentage2 = new Percentage();
		object.restoreValue("Percentage", percentage2);
		assertEquals(percentage1, percentage2);

		Time time2 = new Time();
		object.restoreValue("Time", time2);
		assertEquals(time1, time2);
		
		DateTime timestamp2 = new DateTime();
		object.restoreValue("Time Stamp", timestamp2);
		assertEquals(timestamp1, timestamp2);
		
		TextString textString2 = new TextString();
		object.restoreValue("Text String", textString2);
		assertEquals(textString1, textString2);
		
		URLString urlString2 = new URLString();
		object.restoreValue("Url String", urlString2);
		assertEquals(urlString1, urlString2);
		
		WholeNumber number2 = new WholeNumber();
		object.restoreValue("Whole Number", number2);
		assertEquals(number1, number2);

		
		
		
	}
	
	*/
	
	private ObjectData createData(Class cls, long id) {
	    
		NakedObjectSpecification type = NakedObjects.getSpecificationLoader().loadSpecification(cls.getName());
		SerialOid oid = new SerialOid(id);
		return new ObjectData(type, oid);
		
	}
	
	
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/
