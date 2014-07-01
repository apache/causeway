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

package org.apache.isis.objectstore.xml.internal.data;

import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.version.Version;

/**
 * A logical collection of elements of a specified type
 */
public class CollectionData extends Data {
    private final ListOfRootOid elements = new ListOfRootOid();

    public CollectionData(final RootOid oid, final Version version) {
        super(oid, version);
    }

    public void addElement(final RootOid elementOid) {
        elements.add(elementOid);
    }

    public void removeElement(final RootOid elementOid) {
        elements.remove(elementOid);
    }

    public ListOfRootOid references() {
        return elements;
    }

    @Override
    public String toString() {
        return "CollectionData[type=" + getObjectSpecId() + ",elements=" + elements + "]";
    }
}
