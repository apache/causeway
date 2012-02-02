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

package org.apache.isis.viewer.dnd.view.collection;

import java.util.Vector;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public class SimpleCollectionSorter implements CollectionSorter {

    @Override
    public void sort(final ObjectAdapter[] elements, final Comparator order, final boolean reverse) {
        if (order == null) {
            return;
        }

        final Vector<ObjectAdapter> sorted = new Vector<ObjectAdapter>(elements.length);
        outer: for (int j = 0; j < elements.length; j++) {
            final ObjectAdapter element = elements[j];
            order.init(element);
            int i = 0;
            for (final ObjectAdapter objectAdapter : sorted) {
                final ObjectAdapter sortedElement = objectAdapter;
                if (sortedElement != null && (order.compare(sortedElement) > 0 ^ reverse)) {
                    sorted.insertElementAt(element, i);
                    continue outer;
                }
                i++;
            }
            sorted.addElement(element);
        }
        sorted.copyInto(elements);
    }

}
