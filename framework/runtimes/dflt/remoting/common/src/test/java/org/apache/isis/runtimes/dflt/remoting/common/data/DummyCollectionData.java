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

package org.apache.isis.runtimes.dflt.remoting.common.data;

import java.util.Arrays;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.CollectionData;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ReferenceData;

public class DummyCollectionData extends DummyReferenceData implements CollectionData {

    private static final long serialVersionUID = 1L;

    private final ReferenceData[] elements;
    private final String elementType;

    public DummyCollectionData(final Oid oid, final String collectionType, final String elementType, final ReferenceData[] elements, final Version version) {
        super(oid, collectionType, version);
        this.elementType = elementType;
        this.elements = elements;
    }

    @Override
    public ReferenceData[] getElements() {
        return elements;
    }

    @Override
    public String getElementype() {
        return elementType;
    }

    @Override
    public boolean hasAllElements() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((elementType == null) ? 0 : elementType.hashCode());
        result = prime * result + Arrays.hashCode(elements);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DummyCollectionData other = (DummyCollectionData) obj;
        if (elementType == null) {
            if (other.elementType != null) {
                return false;
            }
        } else if (!elementType.equals(other.elementType)) {
            return false;
        }
        if (!Arrays.equals(elements, other.elements)) {
            return false;
        }
        return true;
    }

}
