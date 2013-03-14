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

package org.apache.isis.objectstore.xml.internal.data.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.xml.XmlFile;
import org.apache.isis.core.integtestsupport.IsisSystemWithFixtures;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.tck.dom.xmlos.Role;
import org.apache.isis.core.tck.dom.xmlos.Team;
import org.apache.isis.core.tck.dom.xmlos.TeamDomainRepository;
import org.apache.isis.objectstore.xml.XmlPersistenceMechanismInstaller;
import org.apache.isis.objectstore.xml.internal.clock.DefaultClock;
import org.apache.isis.objectstore.xml.internal.data.ListOfRootOid;
import org.apache.isis.objectstore.xml.internal.data.ObjectData;
import org.apache.isis.objectstore.xml.internal.version.FileVersion;

public class XmlDataManagerTest {
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(new XmlPersistenceMechanismInstaller())
        .withServices(new TeamDomainRepository())
        .build();

    protected XmlDataManager manager;

    @Before
    public void setUp() throws Exception {
        FileVersion.setClock(new DefaultClock());

        clearTestDirectory();
        final String charset = Utils.lookupCharset(iswf.getIsisSystem().getConfiguration());
        manager = new XmlDataManager(new XmlFile(charset, "tmp/tests"));
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
            for (final String file : files) {
                new File(directory, file).delete();
            }
        }
    }

    @Test
    public void testWriteReadTypeOidAndVersion() {
        final ObjectData data = createData(Role.class, 99, FileVersion.create("user", 19));
        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getRootOid());

        assertEquals(data.getRootOid(), read.getRootOid());
        assertEquals(data.getObjectSpecId(), read.getObjectSpecId());
        assertEquals(data.getVersion(), read.getVersion());
    }

    @Test
    public void testNextId() throws Exception {
        final long first = manager.nextId();
        assertEquals(first + 1, manager.nextId());
        assertEquals(first + 2, manager.nextId());
        assertEquals(first + 3, manager.nextId());
    }

    @Test
    public void testInsertObjectWithFields() throws ObjectPersistenceException {
        final ObjectData data = createData(Role.class, 99, FileVersion.create("user", 13));
        data.set("Person", RootOidDefault.create(ObjectSpecId.of("RLE"), ""+101));
        assertNotNull(data.get("Person"));
        data.set("Name", "Harry");
        assertNotNull(data.get("Name"));

        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getRootOid());
        assertEquals(data.getRootOid(), read.getRootOid());
        assertEquals(data.getObjectSpecId(), read.getObjectSpecId());

        assertEquals(data.get("Person"), read.get("Person"));
        assertEquals(data.get("Name"), read.get("Name"));
    }

    @Test
    public void testInsertObjectWithEmptyOneToManyAssociations() throws ObjectPersistenceException {
        final ObjectData data = createData(Team.class, 99, FileVersion.create("user", 13));

        data.initCollection("Members");

        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getRootOid());
        assertEquals(data.getRootOid(), read.getRootOid());
        assertEquals(data.getObjectSpecId(), read.getObjectSpecId());

        final ListOfRootOid c = read.elements("Members");
        assertNull(c);
    }

    @Test
    public void testInsertObjectWithOneToManyAssociations() throws ObjectPersistenceException {
        final ObjectData data = createData(Team.class, 99, FileVersion.create("user", 13));

        data.initCollection("Members");
        final RootOidDefault oid[] = new RootOidDefault[3];
        for (int i = 0; i < oid.length; i++) {
            oid[i] = RootOidDefault.create(ObjectSpecId.of("TEA"), ""+ (104 + i));
            data.addElement("Members", oid[i]);
        }
        manager.insertObject(data);

        final ObjectData read = (ObjectData) manager.loadData(data.getRootOid());
        assertEquals(data.getRootOid(), read.getRootOid());
        assertEquals(data.getObjectSpecId(), read.getObjectSpecId());

        final ListOfRootOid c = read.elements("Members");
        for (int i = 0; i < oid.length; i++) {
            assertEquals(oid[i], c.elementAt(i));
        }
    }


    private ObjectData createData(final Class<?> type, final long id, final Version version) {

        final ObjectSpecification objSpec = IsisContext.getSpecificationLoader().loadSpecification(type);
        final ObjectSpecId objectSpecId = objSpec.getSpecId();
        final RootOidDefault oid = RootOidDefault.create(objectSpecId, ""+id);
        return new ObjectData(oid, version);
    }
    /*
     * public void xxxtestInsertValues() throws ObjectStoreException {
     * ObjectSpecification type =
     * Isis.getSpecificationLoader().loadSpecification
     * (ValueObjectExample.class.getName()); SerialOid oid = new SerialOid(99);
     * ObjectData data = new ObjectData(type, oid);
     * 
     * 
     * Date date1 = new Date(); date1.add(1,2,3); data.saveValue("Date", date1);
     * 
     * FloatingPointNumber floatingPoint1 = new FloatingPointNumber();
     * floatingPoint1.setValue(3.145); data.saveValue("Floating Point",
     * floatingPoint1);
     * 
     * Label label1 = new Label(); label1.setValue("Labelled");
     * data.saveValue("Label", label1);
     * 
     * Logical logical1 = new Logical(); logical1.setValue(true);
     * data.saveValue("Logical", logical1);
     * 
     * Money money1 = new Money(); money1.setValue(1233.45);
     * data.saveValue("Money", money1);
     * 
     * Option option1 = new Option(new String[] {"Fred", "Sam", "joe"}, 1);
     * data.saveValue("Option", option1);
     * 
     * Percentage percentage1 = new Percentage(); percentage1.setValue(95);
     * data.saveValue("Percentage", percentage1);
     * 
     * TextString textString1 = new TextString("Fred");
     * data.saveValue("Text String", textString1);
     * 
     * DateTime timestamp1 = new DateTime(); timestamp1.add(1,2,3);
     * data.saveValue("Time Stamp", timestamp1);
     * 
     * Time time1 = new Time(); time1.add(1,30); data.saveValue("Time", time1);
     * 
     * URLString urlString1 = new URLString("http://isis.apache.org/");
     * data.saveValue("Url String", urlString1);
     * 
     * WholeNumber number1 = new WholeNumber(); number1.setValue(435422);
     * data.saveValue("Whole Number", number1);
     * 
     * 
     * manager.insert(data);
     * 
     * 
     * 
     * ObjectData object = manager.loadObjectData(oid);
     * 
     * Date date2 = new Date(); object.restoreValue("Date", date2);
     * assertEquals(date1, date2);
     * 
     * FloatingPointNumber floatingPoint2 = new FloatingPointNumber();
     * object.restoreValue("Floating Point", floatingPoint2);
     * assertEquals(floatingPoint1, floatingPoint2);
     * 
     * Label label2 = new Label(); object.restoreValue("Label", label2);
     * assertEquals(label1, label2);
     * 
     * Logical logical2 = new Logical(); object.restoreValue("Logical",
     * logical2); assertEquals(logical1, logical2);
     * 
     * Money money2 = new Money(); object.restoreValue("Money", money2);
     * assertEquals(money1, money2);
     * 
     * Option option2 = new Option(new String [] {"Fred", "Sam", "joe"});
     * object.restoreValue("Option", option2); assertEquals(option1, option2);
     * 
     * Percentage percentage2 = new Percentage();
     * object.restoreValue("Percentage", percentage2); assertEquals(percentage1,
     * percentage2);
     * 
     * Time time2 = new Time(); object.restoreValue("Time", time2);
     * assertEquals(time1, time2);
     * 
     * DateTime timestamp2 = new DateTime(); object.restoreValue("Time Stamp",
     * timestamp2); assertEquals(timestamp1, timestamp2);
     * 
     * TextString textString2 = new TextString();
     * object.restoreValue("Text String", textString2);
     * assertEquals(textString1, textString2);
     * 
     * URLString urlString2 = new URLString(); object.restoreValue("Url String",
     * urlString2); assertEquals(urlString1, urlString2);
     * 
     * WholeNumber number2 = new WholeNumber();
     * object.restoreValue("Whole Number", number2); assertEquals(number1,
     * number2); }
     * 
     * public void xxxtestSaveValues() throws ObjectStoreException {
     * ObjectSpecification type =
     * Isis.getSpecificationLoader().loadSpecification
     * (ValueObjectExample.class.getName()); SerialOid oid = new SerialOid(99);
     * ObjectData data = new ObjectData(type, oid);
     * 
     * manager.insert(data);
     * 
     * 
     * Date date1 = new Date(); date1.add(1,2,3); data.saveValue("Date", date1);
     * 
     * FloatingPointNumber floatingPoint1 = new FloatingPointNumber();
     * floatingPoint1.setValue(3.145); data.saveValue("Floating Point",
     * floatingPoint1);
     * 
     * Label label1 = new Label(); label1.setValue("Labelled");
     * data.saveValue("Label", label1);
     * 
     * Logical logical1 = new Logical(); logical1.setValue(true);
     * data.saveValue("Logical", logical1);
     * 
     * Money money1 = new Money(); money1.setValue(1233.45);
     * data.saveValue("Money", money1);
     * 
     * Option option1 = new Option(new String[] {"Fred", "Sam", "joe"}, 1);
     * data.saveValue("Option", option1);
     * 
     * Percentage percentage1 = new Percentage(); percentage1.setValue(95);
     * data.saveValue("Percentage", percentage1);
     * 
     * TextString textString1 = new TextString("Fred");
     * data.saveValue("Text String", textString1);
     * 
     * DateTime timestamp1 = new DateTime(); timestamp1.add(1,2,3);
     * data.saveValue("Time Stamp", timestamp1);
     * 
     * Time time1 = new Time(); time1.add(1,30); data.saveValue("Time", time1);
     * 
     * URLString urlString1 = new URLString("http://isis.apache.org/");
     * data.saveValue("Url String", urlString1);
     * 
     * WholeNumber number1 = new WholeNumber(); number1.setValue(435422);
     * data.saveValue("Whole Number", number1);
     * 
     * 
     * manager.save(data);
     * 
     * 
     * 
     * ObjectData object = manager.loadObjectData(oid);
     * 
     * Date date2 = new Date(); object.restoreValue("Date", date2);
     * assertEquals(date1, date2);
     * 
     * FloatingPointNumber floatingPoint2 = new FloatingPointNumber();
     * object.restoreValue("Floating Point", floatingPoint2);
     * assertEquals(floatingPoint1, floatingPoint2);
     * 
     * Label label2 = new Label(); object.restoreValue("Label", label2);
     * assertEquals(label1, label2);
     * 
     * Logical logical2 = new Logical(); object.restoreValue("Logical",
     * logical2); assertEquals(logical1, logical2);
     * 
     * Money money2 = new Money(); object.restoreValue("Money", money2);
     * assertEquals(money1, money2);
     * 
     * Option option2 = new Option(new String [] {"Fred", "Sam", "joe"});
     * object.restoreValue("Option", option2); assertEquals(option1, option2);
     * 
     * Percentage percentage2 = new Percentage();
     * object.restoreValue("Percentage", percentage2); assertEquals(percentage1,
     * percentage2);
     * 
     * Time time2 = new Time(); object.restoreValue("Time", time2);
     * assertEquals(time1, time2);
     * 
     * DateTime timestamp2 = new DateTime(); object.restoreValue("Time Stamp",
     * timestamp2); assertEquals(timestamp1, timestamp2);
     * 
     * TextString textString2 = new TextString();
     * object.restoreValue("Text String", textString2);
     * assertEquals(textString1, textString2);
     * 
     * URLString urlString2 = new URLString(); object.restoreValue("Url String",
     * urlString2); assertEquals(urlString1, urlString2);
     * 
     * WholeNumber number2 = new WholeNumber();
     * object.restoreValue("Whole Number", number2); assertEquals(number1,
     * number2);
     * 
     * }
     */

}
