/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.persistence.file;

import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.SimpleOid;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.ValueObjectExample;
import org.nakedobjects.object.collection.ArbitraryCollection;
import org.nakedobjects.object.value.Date;
import org.nakedobjects.object.value.FloatingPointNumber;
import org.nakedobjects.object.value.Label;
import org.nakedobjects.object.value.Logical;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.Option;
import org.nakedobjects.object.value.Percentage;
import org.nakedobjects.object.value.TextString;
import org.nakedobjects.object.value.Time;
import org.nakedobjects.object.value.TimeStamp;
import org.nakedobjects.object.value.URLString;
import org.nakedobjects.object.value.WholeNumber;

import org.apache.log4j.Logger;

public abstract class DataManagerTestCase extends NakedObjectTestCase {
	private static final Logger LOG = Logger.getLogger(DataManagerTestCase.class);
	
	protected DataManager manager;
	protected final int SIZE = 5;
	
	private SimpleOid oids[];
	private ObjectData data[];
	private ObjectData pattern;
	
	public DataManagerTestCase(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		LOG.debug("Test setup...");
		
		MockObjectManager objectManager = MockObjectManager.setup();
		objectManager.setupAddClass(Role.class);
		objectManager.setupAddClass(Team.class);
		objectManager.setupAddClass(ArbitraryCollection.class);
		objectManager.setupAddClass(ValueObjectExample.class);
		
		
		oids = new SimpleOid[SIZE];
		data = new ObjectData[SIZE];
		NakedClass type = NakedClassManager.getInstance().getNakedClass(Role.class.getName());
		pattern = new ObjectData(type, null);
		for (int i = 0; i < SIZE; i++) {
			oids[i] = new SimpleOid(i);
			data[i] = new ObjectData(type, oids[i]);
			manager.insert(data[i]);
		}
		
		assertEquals(SIZE, manager.numberOfInstances(pattern));
		for (int i = 0; i < SIZE; i++) {
			assertEquals(data[i], manager.loadData(oids[i]));
			assertTrue(manager.getInstances(pattern).contains(data[i]));
		}
		
		
		super.setUp();
		LOG.debug("Test starting...");
	}
	
	protected void tearDown() throws Exception {
		LOG.debug("...Test ended\n\n");
		super.tearDown();
	}
	
	public void testCreateOid() throws Exception {
		SimpleOid oid = manager.createOid();
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

	public void testInsertObjectWithFields() throws ObjectStoreException {
		ObjectData data = createData(Role.class, 99);
		data.set("Person", new SimpleOid(101));
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


	public void testInsertObjectWithEmptyOneToManyAssociations() throws ObjectStoreException {
		ObjectData data = createData(Team.class, 99);

		SimpleOid coid = new SimpleOid(103);
		data.initCollection(coid, "Members");
		
		manager.insert(data);

		ObjectData read = manager.loadObjectData(data.getOid());
		assertEquals(data.getOid(), read.getOid());
		assertEquals(data.getClassName(), read.getClassName());
		
		ReferenceVector c = read.elements("Members");
		assertNotNull(c);
		assertEquals(coid, c.getOid());
		assertEquals(0, c.size());
	}

	public void testInsertObjectWithOneToManyAssociations() throws ObjectStoreException {
		ObjectData data = createData(Team.class, 99);
		
		SimpleOid coid = new SimpleOid(103);
		data.initCollection(coid, "Members");
		SimpleOid oid[] = new SimpleOid[3];
		for (int i = 0; i < oid.length; i++) {
			oid[i] = new SimpleOid(104 + i);
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

	
	public void testInsertCollection() throws ObjectStoreException {
		CollectionData data = new CollectionData(NakedClassManager.getInstance().getNakedClass(ArbitraryCollection.class.getName()), new SimpleOid(200));
		
		SimpleOid oids[] = new SimpleOid[6];
		for (int i = 0; i < oids.length; i++) {
			oids[i] = new SimpleOid(103 + i);
			data.addElement(oids[i]);
		}		
		
		manager.insert(data);

		CollectionData read = manager.loadCollectionData(data.getOid());
		assertEquals(data.getOid(), read.getOid());
		assertEquals(data.getClassName(), read.getClassName());
		
		ReferenceVector c = read.references();
		assertEquals(oids.length, c.size());
		for (int i = 0; i < oids.length; i++) {
			assertEquals(oids[i], c.elementAt(i));
		}		
	}
		
	
	public void testRemove() throws Exception {
		SimpleOid oid = oids[2];
		manager.remove(oid);
		
		assertEquals(SIZE - 1, manager.numberOfInstances(pattern));
		
		ObjectDataVector instances = manager.getInstances(pattern);
		for (int i = 0; i < instances.size(); i++) {
			assertFalse(instances.element(i) == data[2]);
		}
		
		assertNull(manager.loadObjectData(oid));
	}

	public void testSaveObject() throws Exception {
		data[2].set("Person", new SimpleOid(231));
		data[2].set("Name", "Fred");
		manager.save(data[2]);
		
		assertTrue(manager.getInstances(pattern).contains(data[2]));
		ObjectData read = manager.loadObjectData(oids[2]);
		assertEquals(data[2], read);
		assertEquals(data[2].get("Name"), read.get("Name"));
		assertEquals(data[2].get("Person"), read.get("Person"));
	}
	
	public void testInsertValues() throws ObjectStoreException {
		NakedClass type = NakedClassManager.getInstance().getNakedClass(ValueObjectExample.class.getName());
		SimpleOid oid = new SimpleOid(99);
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
		
		TimeStamp timestamp1 = new TimeStamp();
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
		
		TimeStamp timestamp2 = new TimeStamp();
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
	

	public void testSaveValues() throws ObjectStoreException {
		NakedClass type = NakedClassManager.getInstance().getNakedClass(ValueObjectExample.class.getName());
		SimpleOid oid = new SimpleOid(99);
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
		
		TimeStamp timestamp1 = new TimeStamp();
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
		
		TimeStamp timestamp2 = new TimeStamp();
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
	
	
	private ObjectData createData(Class cls, long id) {
		NakedClass type = NakedClassManager.getInstance().getNakedClass(cls.getName());
		SimpleOid oid = new SimpleOid(id);
		return new ObjectData(type, oid);
		
	}
	
	
}
