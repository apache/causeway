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


package org.apache.isis.metamodel.adapter;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.spec.ObjectSpecification;


public class ObjectList extends AbstractList<ObjectAdapter> {
    
    private final List<ObjectAdapter> instances;
    private final ObjectSpecification instanceSpecification;

    public ObjectList(final ObjectSpecification instanceSpecification, final ObjectAdapter[] instances) {
        this.instanceSpecification = instanceSpecification;
        this.instances = Collections.unmodifiableList(Arrays.asList(instances));
    }

    /**
     * Required implementation of {@link AbstractList}.
     */
    @Override
    public ObjectAdapter get(int index) {
        return instances.get(index);
    }

    /**
     * Required implementation of {@link AbstractList}.
     */
    @Override
    public int size() {
        return instances.size();
    }


    /**
     * @deprecated - use {@link #iterator()}.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public Enumeration<ObjectAdapter> elements() {
        return new IteratorEnumeration(iterator());
    }

    public ObjectSpecification getElementSpecification() {
        return instanceSpecification;
    }


    public String titleString() {
        return instanceSpecification.getPluralName() + ", " + size();
    }

    @Override
    public String toString() {
        final ToString s = new ToString(this);
        s.append("elements", instanceSpecification.getFullName());

        // title
        String title;
        try {
            title = "'" + this.titleString() + "'";
        } catch (final NullPointerException e) {
            title = "none";
        }
        s.append("title", title);

        s.append("vector", instances);

        return s.toString();
    }
    
}

