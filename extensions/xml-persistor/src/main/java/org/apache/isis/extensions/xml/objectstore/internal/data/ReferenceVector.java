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


package org.apache.isis.extensions.xml.objectstore.internal.data;

import java.util.Vector;

import org.apache.isis.commons.lang.ToString;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;


public class ReferenceVector {
    private final Vector elements = new Vector();

    public void add(final SerialOid oid) {
        elements.addElement(oid);
    }

    public void remove(final SerialOid oid) {
        elements.removeElement(oid);
    }

    public int size() {
        return elements.size();
    }

    public SerialOid elementAt(final int index) {
        return (SerialOid) elements.elementAt(index);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ReferenceVector) {
            return ((ReferenceVector) obj).elements.equals(elements);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int h = 17;
        h = 37 * h + elements.hashCode();
        return h;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("refs", elements);
        return str.toString();
    }
}
