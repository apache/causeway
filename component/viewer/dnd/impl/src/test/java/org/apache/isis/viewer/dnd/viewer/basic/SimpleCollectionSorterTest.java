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

package org.apache.isis.viewer.dnd.viewer.basic;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.dnd.view.collection.CollectionSorter;
import org.apache.isis.viewer.dnd.view.collection.SimpleCollectionSorter;
import org.apache.isis.viewer.dnd.view.collection.TitleComparator;

public class SimpleCollectionSorterTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Test
    public void testSortByTitle() {
        final ObjectAdapter[] instances = new ObjectAdapter[] { adapterWithTitle("one"), adapterWithTitle("two"), adapterWithTitle("three"), adapterWithTitle("four"), };

        final SimpleCollectionSorter sorter = new SimpleCollectionSorter();
        sorter.sort(instances, new TitleComparator(), CollectionSorter.NORMAL);

        assertEquals("four", instances[0].titleString());
        assertEquals("one", instances[1].titleString());
        assertEquals("three", instances[2].titleString());
        assertEquals("two", instances[3].titleString());
    }

    @Test
    public void testSortByTitleReversed() {
        final ObjectAdapter[] instances = new ObjectAdapter[] { adapterWithTitle("one"), adapterWithTitle("two"), adapterWithTitle("three"), adapterWithTitle("four"), };

        final SimpleCollectionSorter sorter = new SimpleCollectionSorter();
        sorter.sort(instances, new TitleComparator(), CollectionSorter.REVERSED);

        assertEquals("two", instances[0].titleString());
        assertEquals("three", instances[1].titleString());
        assertEquals("one", instances[2].titleString());
        assertEquals("four", instances[3].titleString());
    }

    private ObjectAdapter adapterWithTitle(final String title) {
        final ObjectAdapter adapter = context.mock(ObjectAdapter.class, title);
        context.checking(new Expectations() {
            {
                allowing(adapter).titleString();
                will(returnValue(title));
            }
        });
        return adapter;
    }
}
