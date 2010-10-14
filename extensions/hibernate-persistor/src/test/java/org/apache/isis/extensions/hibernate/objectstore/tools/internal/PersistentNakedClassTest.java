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

import junit.framework.TestCase;

import org.apache.isis.runtime.testsystem.TestSpecification;


public class PersistentSpecificationTest extends TestCase {
    private static String className = "myclass";
    private PersistentSpecification root;
    private PersistentSpecification pc;
    private PersistentSpecification pc2;
    private PersistentSpecification pc3;
    private TestSpecification spec;
    private TestSpecification spec2;
    private TestSpecification spec3;

    @Override
    public void setUp() {
        root = new PersistentSpecification();

        spec = new TestSpecification(className);
        pc = new PersistentSpecification(spec, root);

        spec2 = new TestSpecification(className + "2");
        pc2 = new PersistentSpecification(spec2, root);

        spec3 = new TestSpecification(className + "3");
        pc3 = new PersistentSpecification(spec3, pc2);
    }

    @Override
    public void tearDown() {
        root = pc = pc2 = pc3 = null;
        spec = spec2 = spec3 = null;
    }

    public void testBasic() {
        final PersistentSpecification pcDuplicate = new PersistentSpecification(spec, root);

        assertEquals(className, pc.getName());
        assertEquals(pc, pcDuplicate);
        assertEquals("name", className, pc.getName());
        assertFalse("pc3=pc", pc3.equals(pc));
        assertEquals("parent", pc2, pc3.getParent());

        final PersistentSpecification[] pcSubclasses = pc2.getSubClassesArray();
        assertEquals("subclasses size", 1, pcSubclasses.length);
        assertEquals("subclasses 0", pc3, pcSubclasses[0]);
    }

    public void testRemoveFromHierarchy() {
        // validate setup
        pc2.setParent(pc);
        assertEquals(pc, pc2.getParent());
        assertEquals(pc2, pc3.getParent());

        pc2.removeFromHierarchy();

        assertNull("pc2 parent", pc2.getParent());
        assertEquals("pc3 parent", pc, pc3.getParent());
        final PersistentSpecification[] pcSubclasses = pc.getSubClassesArray();
        assertEquals("subclasses size", 1, pcSubclasses.length);
        assertEquals("subclasses 0", pc3, pcSubclasses[0]);
    }

    public void testSetParent() {
        // validate setup
        assertEquals(pc2, pc3.getParent());
        assertEquals(0, pc.getSubClassesArray().length);

        pc3.setParent(pc);

        assertEquals("parent", pc, pc3.getParent());
        assertEquals("pc2 subclasses", 0, pc2.getSubClassesArray().length);

        final PersistentSpecification[] pcSubclasses = pc.getSubClassesArray();
        assertEquals("subclasses size", 1, pcSubclasses.length);
        assertEquals("subclasses 0", pc3, pcSubclasses[0]);
    }

    // public void testGetUniqueFields() {
    // ObjectField[] fields2 = new ObjectField[3];
    // ObjectField[] fields3 = new ObjectField[5];
    // for (int i = 0; i < fields2.length; i++) {
    // fields2[i] = fields3[i] = new TestValueAssociation(null, "field"+i, null);
    // }
    // for (int i = fields2.length; i < fields3.length; i++) {
    // fields3[i] = new TestValueAssociation(null, "field"+i, null);
    // }
    // spec2.setupFields(fields2);
    // spec3.setupFields(fields3);
    //		
    // ObjectField[] unique2 = pc2.getUniqueFields();
    // assertEquals("unique2 len", 3, unique2.length);
    // assertEquals("unique2", fields2, unique2);
    // ObjectField[] unique3 = pc3.getUniqueFields();
    // assertEquals("unique3 len", 2, unique3.length);
    // for (int i = 0; i < unique3.length; i++) {
    // assertEquals("unique3 item "+i, fields3[i+3], unique3[i]);
    // }
    // }

    // public void testGetUniqueAssociation() {
    // ObjectField[] fields2 = new ObjectField[3];
    // fields2[0] = new TestOneToOneAssociation(null, "field1", spec);
    // fields2[1] = new TestOneToOneAssociation(null, "field2", spec);
    // fields2[2] = new TestOneToOneAssociation(null, "field3", spec3);
    // spec2.setupFields(fields2);
    //		
    // assertNull("unique assn spec", pc2.getUniqueAssociation(className));
    // ObjectField unique = pc2.getUniqueAssociation(className+"3");
    // assertNotNull("unique assn spec3", unique);
    // assertEquals("unique", fields2[2], unique);
    // }
}
