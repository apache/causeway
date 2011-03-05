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

package org.apache.isis.alternatives.objectstore.xml.internal.data.xml;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.isis.alternatives.objectstore.xml.internal.clock.DefaultClock;
import org.apache.isis.alternatives.objectstore.xml.internal.data.ObjectData;
import org.apache.isis.alternatives.objectstore.xml.internal.data.ReferenceVector;
import org.apache.isis.alternatives.objectstore.xml.internal.data.Role;
import org.apache.isis.alternatives.objectstore.xml.internal.data.Team;
import org.apache.isis.alternatives.objectstore.xml.internal.version.FileVersion;
import org.apache.isis.core.commons.xml.XmlFile;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtimes.dflt.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtimes.dflt.runtime.transaction.ObjectPersistenceException;

public class XmlDataManagerTest extends ProxyJunit3TestCase {
    protected XmlDataManager manager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        FileVersion.setClock(new DefaultClock());

        clearTestDirectory();
        String charset = XmlFileUtil.lookupCharset(system.getConfiguration());
        manager = new XmlDataManager(new XmlFile(charset, "tmp/tests"));
    }

    @Override
    protected void tearDown() throws Exception {
        system.shutdown();
    }

    protected static void clearTestDirectory() {
        final File directory = new File("tmp" + File.separator + "tests");
        final String[] files = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(final File arg0, final String name) {
                return name.endsWith(".xml");
            }
        });

        if (files != null) {
            for (int f = 0; f < files.length; f++) {
                new File(directory, files[f]).delete();
            }
        }
    }

    public void testWriteReadTypeOidAndVersion() {
        final ObjectData data = createData(Role.class, 99, new FileVersion("user", 19));
        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getOid());

        assertEquals(data.getOid(), read.getOid());
        assertEquals(data.getTypeName(), read.getTypeName());
        assertEquals(data.getVersion(), read.getVersion());
    }

    public void testNextId() throws Exception {
        final long first = manager.nextId();
        assertEquals(first + 1, manager.nextId());
        assertEquals(first + 2, manager.nextId());
        assertEquals(first + 3, manager.nextId());
    }

    public void testInsertObjectWithFields() throws ObjectPersistenceException {
        final ObjectData data = createData(Role.class, 99, new FileVersion("user", 13));
        data.set("Person", SerialOid.createPersistent(101));
        assertNotNull(data.get("Person"));
        data.set("Name", "Harry");
        assertNotNull(data.get("Name"));

        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getOid());
        assertEquals(data.getOid(), read.getOid());
        assertEquals(data.getTypeName(), read.getTypeName());

        assertEquals(data.get("Person"), read.get("Person"));
        assertEquals(data.get("Name"), read.get("Name"));
    }

    public void testInsertObjectWithEmptyOneToManyAssociations() throws ObjectPersistenceException {
        final ObjectData data = createData(Team.class, 99, new FileVersion("user", 13));

        data.initCollection("Members");

        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getOid());
        assertEquals(data.getOid(), read.getOid());
        assertEquals(data.getTypeName(), read.getTypeName());

        final ReferenceVector c = read.elements("Members");
        assertNull(c);
    }

    public void testInsertObjectWithOneToManyAssociations() throws ObjectPersistenceException {
        final ObjectData data = createData(Team.class, 99, new FileVersion("user", 13));

        data.initCollection("Members");
        final SerialOid oid[] = new SerialOid[3];
        for (int i = 0; i < oid.length; i++) {
            oid[i] = SerialOid.createPersistent(104 + i);
            data.addElement("Members", oid[i]);
        }
        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getOid());
        assertEquals(data.getOid(), read.getOid());
        assertEquals(data.getTypeName(), read.getTypeName());

        final ReferenceVector c = read.elements("Members");
        for (int i = 0; i < oid.length; i++) {
            assertEquals(oid[i], c.elementAt(i));
        }
    }

    /*
     * public void xxxtestInsertValues() throws ObjectStoreException { ObjectSpecification type =
     * Isis.getSpecificationLoader().loadSpecification(ValueObjectExample.class.getName()); SerialOid oid = new
     * SerialOid(99); ObjectData data = new ObjectData(type, oid);
     * 
     * 
     * Date date1 = new Date(); date1.add(1,2,3); data.saveValue("Date", date1);
     * 
     * FloatingPointNumber floatingPoint1 = new FloatingPointNumber(); floatingPoint1.setValue(3.145);
     * data.saveValue("Floating Point", floatingPoint1);
     * 
     * Label label1 = new Label(); label1.setValue("Labelled"); data.saveValue("Label", label1);
     * 
     * Logical logical1 = new Logical(); logical1.setValue(true); data.saveValue("Logical", logical1);
     * 
     * Money money1 = new Money(); money1.setValue(1233.45); data.saveValue("Money", money1);
     * 
     * Option option1 = new Option(new String[] {"Fred", "Sam", "joe"}, 1); data.saveValue("Option", option1);
     * 
     * Percentage percentage1 = new Percentage(); percentage1.setValue(95); data.saveValue("Percentage", percentage1);
     * 
     * TextString textString1 = new TextString("Fred"); data.saveValue("Text String", textString1);
     * 
     * DateTime timestamp1 = new DateTime(); timestamp1.add(1,2,3); data.saveValue("Time Stamp", timestamp1);
     * 
     * Time time1 = new Time(); time1.add(1,30); data.saveValue("Time", time1);
     * 
     * URLString urlString1 = new URLString("http://isis.apache.org/"); data.saveValue("Url String", urlString1);
     * 
     * WholeNumber number1 = new WholeNumber(); number1.setValue(435422); data.saveValue("Whole Number", number1);
     * 
     * 
     * manager.insert(data);
     * 
     * 
     * 
     * ObjectData object = manager.loadObjectData(oid);
     * 
     * Date date2 = new Date(); object.restoreValue("Date", date2); assertEquals(date1, date2);
     * 
     * FloatingPointNumber floatingPoint2 = new FloatingPointNumber(); object.restoreValue("Floating Point",
     * floatingPoint2); assertEquals(floatingPoint1, floatingPoint2);
     * 
     * Label label2 = new Label(); object.restoreValue("Label", label2); assertEquals(label1, label2);
     * 
     * Logical logical2 = new Logical(); object.restoreValue("Logical", logical2); assertEquals(logical1, logical2);
     * 
     * Money money2 = new Money(); object.restoreValue("Money", money2); assertEquals(money1, money2);
     * 
     * Option option2 = new Option(new String [] {"Fred", "Sam", "joe"}); object.restoreValue("Option", option2);
     * assertEquals(option1, option2);
     * 
     * Percentage percentage2 = new Percentage(); object.restoreValue("Percentage", percentage2);
     * assertEquals(percentage1, percentage2);
     * 
     * Time time2 = new Time(); object.restoreValue("Time", time2); assertEquals(time1, time2);
     * 
     * DateTime timestamp2 = new DateTime(); object.restoreValue("Time Stamp", timestamp2); assertEquals(timestamp1,
     * timestamp2);
     * 
     * TextString textString2 = new TextString(); object.restoreValue("Text String", textString2);
     * assertEquals(textString1, textString2);
     * 
     * URLString urlString2 = new URLString(); object.restoreValue("Url String", urlString2); assertEquals(urlString1,
     * urlString2);
     * 
     * WholeNumber number2 = new WholeNumber(); object.restoreValue("Whole Number", number2); assertEquals(number1,
     * number2); }
     * 
     * public void xxxtestSaveValues() throws ObjectStoreException { ObjectSpecification type =
     * Isis.getSpecificationLoader().loadSpecification(ValueObjectExample.class.getName()); SerialOid oid = new
     * SerialOid(99); ObjectData data = new ObjectData(type, oid);
     * 
     * manager.insert(data);
     * 
     * 
     * Date date1 = new Date(); date1.add(1,2,3); data.saveValue("Date", date1);
     * 
     * FloatingPointNumber floatingPoint1 = new FloatingPointNumber(); floatingPoint1.setValue(3.145);
     * data.saveValue("Floating Point", floatingPoint1);
     * 
     * Label label1 = new Label(); label1.setValue("Labelled"); data.saveValue("Label", label1);
     * 
     * Logical logical1 = new Logical(); logical1.setValue(true); data.saveValue("Logical", logical1);
     * 
     * Money money1 = new Money(); money1.setValue(1233.45); data.saveValue("Money", money1);
     * 
     * Option option1 = new Option(new String[] {"Fred", "Sam", "joe"}, 1); data.saveValue("Option", option1);
     * 
     * Percentage percentage1 = new Percentage(); percentage1.setValue(95); data.saveValue("Percentage", percentage1);
     * 
     * TextString textString1 = new TextString("Fred"); data.saveValue("Text String", textString1);
     * 
     * DateTime timestamp1 = new DateTime(); timestamp1.add(1,2,3); data.saveValue("Time Stamp", timestamp1);
     * 
     * Time time1 = new Time(); time1.add(1,30); data.saveValue("Time", time1);
     * 
     * URLString urlString1 = new URLString("http://isis.apache.org/"); data.saveValue("Url String", urlString1);
     * 
     * WholeNumber number1 = new WholeNumber(); number1.setValue(435422); data.saveValue("Whole Number", number1);
     * 
     * 
     * manager.save(data);
     * 
     * 
     * 
     * ObjectData object = manager.loadObjectData(oid);
     * 
     * Date date2 = new Date(); object.restoreValue("Date", date2); assertEquals(date1, date2);
     * 
     * FloatingPointNumber floatingPoint2 = new FloatingPointNumber(); object.restoreValue("Floating Point",
     * floatingPoint2); assertEquals(floatingPoint1, floatingPoint2);
     * 
     * Label label2 = new Label(); object.restoreValue("Label", label2); assertEquals(label1, label2);
     * 
     * Logical logical2 = new Logical(); object.restoreValue("Logical", logical2); assertEquals(logical1, logical2);
     * 
     * Money money2 = new Money(); object.restoreValue("Money", money2); assertEquals(money1, money2);
     * 
     * Option option2 = new Option(new String [] {"Fred", "Sam", "joe"}); object.restoreValue("Option", option2);
     * assertEquals(option1, option2);
     * 
     * Percentage percentage2 = new Percentage(); object.restoreValue("Percentage", percentage2);
     * assertEquals(percentage1, percentage2);
     * 
     * Time time2 = new Time(); object.restoreValue("Time", time2); assertEquals(time1, time2);
     * 
     * DateTime timestamp2 = new DateTime(); object.restoreValue("Time Stamp", timestamp2); assertEquals(timestamp1,
     * timestamp2);
     * 
     * TextString textString2 = new TextString(); object.restoreValue("Text String", textString2);
     * assertEquals(textString1, textString2);
     * 
     * URLString urlString2 = new URLString(); object.restoreValue("Url String", urlString2); assertEquals(urlString1,
     * urlString2);
     * 
     * WholeNumber number2 = new WholeNumber(); object.restoreValue("Whole Number", number2); assertEquals(number1,
     * number2);
     * 
     * }
     */

    private ObjectData createData(final Class<?> type, final long id, final FileVersion version) {

        final ObjectSpecification noSpec = IsisContext.getSpecificationLoader().loadSpecification(type);
        final SerialOid oid = SerialOid.createPersistent(id);
        return new ObjectData(noSpec, oid, version);

    }

}
