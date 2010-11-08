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


package org.apache.isis.extensions.dnd.viewer.basic;

import junit.framework.TestCase;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.extensions.dnd.view.collection.CollectionSorter;
import org.apache.isis.extensions.dnd.view.collection.SimpleCollectionSorter;
import org.apache.isis.extensions.dnd.view.collection.TitleComparator;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;


public class SimpleCollectionSorterTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(SimpleCollectionSorterTest.class);
    }

    public void testSortByTitle() {
        final ObjectAdapter[] instances = new ObjectAdapter[] { object("one"), object("two"), object("three"), object("four"), };

        final SimpleCollectionSorter sorter = new SimpleCollectionSorter();
        sorter.sort(instances, new TitleComparator(), CollectionSorter.NORMAL);

        assertEquals("four", instances[0].titleString());
        assertEquals("one", instances[1].titleString());
        assertEquals("three", instances[2].titleString());
        assertEquals("two", instances[3].titleString());
    }

    public void testSortByTitleReversed() {
        final ObjectAdapter[] instances = new ObjectAdapter[] { object("one"), object("two"), object("three"), object("four"), };

        final SimpleCollectionSorter sorter = new SimpleCollectionSorter();
        sorter.sort(instances, new TitleComparator(), CollectionSorter.REVERSED);

        assertEquals("two", instances[0].titleString());
        assertEquals("three", instances[1].titleString());
        assertEquals("one", instances[2].titleString());
        assertEquals("four", instances[3].titleString());
    }

    private ObjectAdapter object(final String string) {
        final TestProxyAdapter object = new TestProxyAdapter();
        object.setupTitleString(string);
        return object;
    }
}
