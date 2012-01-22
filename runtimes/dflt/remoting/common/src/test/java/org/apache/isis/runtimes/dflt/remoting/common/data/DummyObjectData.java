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

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.runtimes.dflt.remoting.common.data.common.ObjectData;

public class DummyObjectData extends DummyReferenceData implements ObjectData {

    private static final long serialVersionUID = 1L;

    private final boolean hasCompleteData;
    private Data[] fieldContent;

    public DummyObjectData(final Oid oid, final String type, final boolean hasCompleteData, final Version version) {
        super(oid, type, version);
        this.hasCompleteData = hasCompleteData;
    }

    public DummyObjectData() {
        this(null, "", false, null);
    }

    @Override
    public Data[] getFieldContent() {
        return fieldContent;
    }

    @Override
    public boolean hasCompleteData() {
        return hasCompleteData;
    }

    @Override
    public void setFieldContent(final Data[] fieldContent) {
        this.fieldContent = fieldContent;
    }

    public void setFieldContent(final int i, final DummyReferenceData reference) {
        fieldContent[i] = reference;
    }

    /*
     * public String toString() { ToString str = new ToString(this);
     * toString(str); return str.toString(); }
     */
    @Override
    protected void toString(final ToString str) {
        super.toString(str);
        str.append("resolved", hasCompleteData);
        str.append("fields", fieldContent == null ? 0 : fieldContent.length);
        /*
         * if(fieldContent == null) { str.append("fields", "none"); } else { for
         * (int i = 0; i < fieldContent.length; i++) { str.append("field" + i +
         * ": " + fieldContent[i].); }
         */
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
        final DummyObjectData other = (DummyObjectData) obj;
        if (!Arrays.equals(fieldContent, other.fieldContent)) {
            return false;
        }
        if (hasCompleteData != other.hasCompleteData) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fieldContent);
        result = prime * result + (hasCompleteData ? 1231 : 1237);
        return result;
    }

}
