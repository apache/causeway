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


package org.apache.isis.extensions.hibernate.objectstore.tools.internal;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.OidAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.TimestampAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.UserAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor.VersionAccessor;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype.DateType;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype.TimeType;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.BiDirectional;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.ManyToMany;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.OneToMany;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.OneToOne;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.SimpleObject;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.SimpleSubClass;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;


public class Nof2HbmXmlTest extends ProxyJunit3TestCase {

    public void testNEED_TO_REINSTATE() {

    }

    public void xtestCreateDom() {
        // system.addSpecification(BiDirectional.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.TRUE);

        final Nof2HbmXml nof2HbmXml = new Nof2HbmXml();
        final Document biDirectionaldoc = nof2HbmXml.createDocument(classes.getPersistentClass(BiDirectional.class.getName()));
        assertEquals("-//Hibernate/Hibernate Mapping DTD 3.0//EN", biDirectionaldoc.getDocType().getPublicID());
        assertEquals("http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd", biDirectionaldoc.getDocType().getSystemID());

        final Element root = biDirectionaldoc.getRootElement();
        assertEquals("hibernate-mapping", root.getName());

        final List<?> rootElements = root.elements();
        assertEquals("root element count", 1, rootElements.size());
        final Element classNode = (Element) rootElements.get(0);
        assertEquals("class", classNode.getName());
        assertEquals(BiDirectional.class.getName(), classNode.attribute("name").getData());
        assertEquals("BIDIRECTIONAL", classNode.attribute("table").getData());

        checkIdVersionInfo(classNode, "PKbidirectionalID", "long", OidAccessor.class.getName());
        assertEquals("property element count", 3, classNode.elements("property").size());

        assertEquals("one-to-one element count", 1, classNode.elements("one-to-one").size());
        final Element oneToOne = classNode.element("one-to-one");
        assertEquals("oneToOne name", "oneToOne", oneToOne.attribute("name").getData());
        assertEquals("oneToOne class", OneToOne.class.getName(), oneToOne.attribute("class").getData());
        assertEquals("oneToOne property-ref", "one", oneToOne.attribute("property-ref").getData());

        assertEquals("bag element count", 1, classNode.elements("bag").size());

        final Element bag = classNode.element("bag");
        assertEquals("bag name", "manyToMany", bag.attribute("name").getData());
        assertEquals("bag table", "BIDIRECTIONAL_MANYTOMANY", bag.attribute("table").getData());
        assertNull("bag inverse", bag.attribute("inverse"));

        assertEquals("key", "FKbidirectional", bag.element("key").attribute("column").getData());

        final Element manyToMany = bag.element("many-to-many");
        assertEquals("many class", ManyToMany.class.getName(), manyToMany.attribute("class").getData());
        assertEquals("many column", "FKmanytomany", manyToMany.attribute("column").getData());

        assertEquals("many-to-one element count", 2, classNode.elements("many-to-one").size());

        final Element manyToOne = (Element) classNode.elements("many-to-one").get(0);
        assertEquals("manyToOne name", "oneToMany", manyToOne.attribute("name").getData());
        assertEquals("manyToOne column", "FKoneToMany", manyToOne.attribute("column").getData());
        assertEquals("manyToOne class", OneToMany.class.getName(), manyToOne.attribute("class").getData());

        final Element secondManyToOne = (Element) classNode.elements("many-to-one").get(1);
        assertEquals("secondManyToOne name", "secondOneToMany", secondManyToOne.attribute("name").getData());
        assertEquals("secondManyToOne column", "FKsecondOneToMany", secondManyToOne.attribute("column").getData());
        assertEquals("secondManyToOne class", OneToMany.class.getName(), secondManyToOne.attribute("class").getData());
    }

    private void checkIdVersionInfo(final Element classNode, final String pkid, final String idType, final String accessType) {
        assertEquals("id element count", 1, classNode.elements("id").size());
        final Element idNode = classNode.element("id");
        assertEquals("id name", "id", idNode.attribute("name").getData());
        assertEquals("id type", idType, idNode.attribute("type").getData());
        if (accessType == null) {
            assertNull("access", idNode.attribute("access"));
        } else {
            assertEquals("id access", accessType, idNode.attribute("access").getData());
        }
        assertEquals("id column", pkid, idNode.attribute("column").getData());
        assertEquals("generator class", "native", idNode.element("generator").attribute("class").getData());

        assertEquals("version element count", 1, classNode.elements("version").size());
        final Element versionNode = classNode.element("version");
        assertEquals("version name", "adapter_version", versionNode.attribute("name").getData());
        assertEquals("version type", "long", versionNode.attribute("type").getData());
        assertEquals("version access", VersionAccessor.class.getName(), versionNode.attribute("access").getData());
        assertEquals("version column", "version", versionNode.attribute("column").getData());

        final List<?> propertyElements = classNode.elements("property");

        final Element modifiedByNode = (Element) propertyElements.get(0);
        assertEquals("modifiedBy name", "adapter_modified_by", modifiedByNode.attribute("name").getData());
        assertEquals("modifiedBy type", "string", modifiedByNode.attribute("type").getData());
        assertEquals("modifiedBy access", UserAccessor.class.getName(), modifiedByNode.attribute("access").getData());
        assertEquals("modifiedBy column", "modified_by", modifiedByNode.attribute("column").getData());

        final Element modifiedOnNode = (Element) propertyElements.get(1);
        assertEquals("modifiedOn name", "adapter_modified_on", modifiedOnNode.attribute("name").getData());
        assertEquals("modifiedOn type", "timestamp", modifiedOnNode.attribute("type").getData());
        assertEquals("modifiedOn access", TimestampAccessor.class.getName(), modifiedOnNode.attribute("access").getData());
        assertEquals("modifiedOn column", "modified_on", modifiedOnNode.attribute("column").getData());
    }

    public void xtestOneToOneInverse() {
        // system.addSpecification(BiDirectional.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.TRUE);

        final Nof2HbmXml nof2HbmXml = new Nof2HbmXml();
        final Document doc = nof2HbmXml.createDocument(classes.getPersistentClass(OneToOne.class.getName()));

        final Element manyToOne = doc.getRootElement().element("class").element("many-to-one");
        assertEquals("name", "one", manyToOne.attribute("name").getData());
        assertEquals("class", BiDirectional.class.getName(), manyToOne.attribute("class").getData());
        assertEquals("unique", "true", manyToOne.attribute("unique").getData());
        assertEquals("column", "FKone", manyToOne.attribute("column").getData());
    }

    public void xtestManyToManyInverse() {
        // system.addSpecification(BiDirectional.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.TRUE);

        final Nof2HbmXml nof2HbmXml = new Nof2HbmXml();
        final Document doc = nof2HbmXml.createDocument(classes.getPersistentClass(ManyToMany.class.getName()));

        final Element bag = doc.getRootElement().element("class").element("bag");
        assertEquals("bag name", "many", bag.attribute("name").getData());
        assertEquals("bag table", "BIDIRECTIONAL_MANYTOMANY", bag.attribute("table").getData());
        assertEquals("bag inverse", "true", bag.attribute("inverse").getData());

        assertEquals("key", "FKmanytomany", bag.element("key").attribute("column").getData());

        final Element manyToMany = bag.element("many-to-many");
        assertEquals("many class", BiDirectional.class.getName(), manyToMany.attribute("class").getData());
        assertEquals("many column", "FKbidirectional", manyToMany.attribute("column").getData());
    }

    public void xtestSubClassedObject() {
        // system.addSpecification(SimpleSubClass.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.TRUE);

        final Nof2HbmXml nof2HbmXml = new Nof2HbmXml();
        final Document subClassDoc = nof2HbmXml.createDocument(classes.getPersistentClass(SimpleObject.class.getName()));

        final Element classNode = subClassDoc.getRootElement().element("class");
        assertNotNull("class node", classNode);
        assertEquals(SimpleObject.class.getName(), classNode.attribute("name").getData());
        assertEquals("SIMPLEOBJECT", classNode.attribute("table").getData());
        assertEquals("org.apache.isis.nos.store.hibernate.testobjects.SimpleObject", classNode.attribute("discriminator-value")
                .getData());

        checkIdVersionInfo(classNode, "PKsimpleobjectID", "java.lang.Long", null);

        assertEquals("property element count", 7, classNode.elements("property").size());

        final Element titleStringProperty = (Element) classNode.elements("property").get(2);
        assertEquals("title", titleStringProperty.attribute("column").getData());
        assertEquals("string", titleStringProperty.attribute("type").getData());
        assertEquals("title", titleStringProperty.attribute("name").getData());

        final Element stringProperty = (Element) classNode.elements("property").get(3);
        assertEquals("string", stringProperty.attribute("column").getData());
        assertEquals(String.class.getName(), stringProperty.attribute("type").getData());
        // assertEquals("org.apache.isis.persistence.hibernate.property.ObjectPropertyAccessor",
        // stringProperty.attribute("access").getData());
        assertEquals("string", stringProperty.attribute("name").getData());

        final Element someDateProperty = (Element) classNode.elements("property").get(4);
        assertEquals("someDate", someDateProperty.attribute("column").getData());
        assertEquals(DateType.class.getName(), someDateProperty.attribute("type").getData());
        assertNull("access", someDateProperty.attribute("access"));
        assertEquals("someDate", someDateProperty.attribute("name").getData());

        final Element someTimeProperty = (Element) classNode.elements("property").get(5);
        assertEquals("someTime", someTimeProperty.attribute("column").getData());
        assertEquals(TimeType.class.getName(), someTimeProperty.attribute("type").getData());
        assertNull("access", someTimeProperty.attribute("access"));
        assertEquals("someTime", someTimeProperty.attribute("name").getData());

        final Element longFieldProperty = (Element) classNode.elements("property").get(6);
        assertEquals("longField", longFieldProperty.attribute("column").getData());
        assertEquals("long", longFieldProperty.attribute("type").getData());
        assertNull("access", longFieldProperty.attribute("access"));
        assertEquals("longField", longFieldProperty.attribute("name").getData());
    }

    public void xtestSubClass() {
        // system.addSpecification(SimpleSubClass.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.TRUE);

        final Nof2HbmXml nof2HbmXml = new Nof2HbmXml();
        final Document subClassDoc = nof2HbmXml.createDocument(classes.getPersistentClass(SimpleSubClass.class.getName()));

        final Element subclass = subClassDoc.getRootElement().element("subclass");
        assertNotNull("subclass", subclass);
        assertEquals(SimpleSubClass.class.getName(), subclass.attribute("name").getData());
        assertEquals(SimpleObject.class.getName(), subclass.attribute("extends").getData());
        assertEquals("org.apache.isis.nos.store.hibernate.testobjects.SimpleSubClass", subclass.attribute("discriminator-value")
                .getData());

        assertEquals("property element count", 1, subclass.elements("property").size());
        final Element property = subclass.element("property");
        assertEquals("uniqueString", property.attribute("column").getData());
        assertEquals(String.class.getName(), property.attribute("type").getData());
        assertNull("access", property.attribute("access"));
        assertEquals("uniqueString", property.attribute("name").getData());
    }

}
