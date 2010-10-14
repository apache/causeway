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

import java.util.Iterator;

import org.apache.isis.extensions.hibernate.objectstore.testdomain.BiDirectional;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.ManyToMany;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.OneToMany;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.OneToOne;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.SimpleObject;
import org.apache.isis.extensions.hibernate.objectstore.testdomain.SimpleSubClass;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;


public class PersistentSpecificationsTest extends ProxyJunit3TestCase {

    public void testNEED_TO_REINSTATE() {

    }

    public void xtestMapClasses() {
        // system.addSpecification(BiDirectional.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.TRUE);

        int count = 0;
        for (final Iterator<?> iter = classes.getPersistentClasses(); iter.hasNext();) {
            iter.next();
            count++;
        }
        assertTrue(classes.isPersistentClass(BiDirectional.class.getName()));
        assertTrue(classes.isPersistentClass(ManyToMany.class.getName()));
        assertTrue(classes.isPersistentClass(OneToMany.class.getName()));
        assertTrue(classes.isPersistentClass(OneToOne.class.getName()));
        assertEquals("classes count", 4, count);

        final PersistentSpecification bidirPc = classes.getPersistentClass(BiDirectional.class.getName());
        assertTrue("!assn secondOneToMany", !bidirPc.hasAssociation("secondonetomany"));
        assertTrue("assn oneToMany", bidirPc.hasAssociation("oneToMany"));
        assertTrue("assn manyToMany", bidirPc.hasAssociation("manyToMany"));
        assertTrue("assn oneToOne", bidirPc.hasAssociation("oneToOne"));

        final Association assnManyToMany = bidirPc.getAssociation("manyToMany");
        assertTrue("!many to many inverse", !assnManyToMany.isInverse());
        assertEquals("many to many type", ManyToMany.class.getName(), assnManyToMany.getPersistentClass().getName());

        final PersistentSpecification m2mPc = classes.getPersistentClass(ManyToMany.class.getName());
        assertTrue("assn many", m2mPc.hasAssociation("many"));
        final Association assnMany = m2mPc.getAssociation("many");
        assertTrue("many inverse", assnMany.isInverse());
        assertEquals("many type", BiDirectional.class.getName(), assnMany.getPersistentClass().getName());

    }

    public void xtestMapClassesNotBidirectional() {
        // system.addSpecification(BiDirectional.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.FALSE);
        final PersistentSpecification pc = classes.getPersistentClass(BiDirectional.class.getName());
        assertTrue("!assn secondOneToMany", !pc.hasAssociation("secondonetomany"));
        assertTrue("assn oneToMany", pc.hasAssociation("oneToMany"));
        assertTrue("!assn manyToMany", !pc.hasAssociation("manytomany"));
        assertTrue("!assn oneToOne", !pc.hasAssociation("onetoone"));
    }

    public void xtestMapSubClasses() {
        // system.addSpecification(SimpleSubClass.class.getName());
        final PersistentSpecifications classes = PersistentSpecifications.buildPersistentSpecifications(Boolean.TRUE);

        int count = 0;
        for (final Iterator<?> iter = classes.getPersistentClasses(); iter.hasNext();) {
            iter.next();
            count++;
        }
        assertEquals("classes count", 2, count);
        assertTrue(classes.isPersistentClass(SimpleSubClass.class.getName()));
        assertTrue(classes.isPersistentClass(SimpleObject.class.getName()));

        final PersistentSpecification pc = classes.getPersistentClass(SimpleObject.class.getName());
        final PersistentSpecification[] subclasses = pc.getSubClassesArray();
        assertEquals("subclasses size", 1, subclasses.length);
        assertEquals("subclass name", SimpleSubClass.class.getName(), subclasses[0].getName());
    }
}
