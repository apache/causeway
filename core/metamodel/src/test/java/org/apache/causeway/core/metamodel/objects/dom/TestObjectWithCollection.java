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
package org.apache.causeway.core.metamodel.objects.dom;

import java.util.Vector;

public class TestObjectWithCollection extends RuntimeTestPojo {

    private final Vector<Object> arrayList;
    private final boolean throwException;

    public TestObjectWithCollection(final Vector<Object> arrayList, final boolean throwException) {
        this.arrayList = arrayList;
        this.throwException = throwException;
    }

    public Object getList() {
        throwException();
        return arrayList;
    }

    public void addToList(final Object object) {
        throwException();
        arrayList.add(object);
    }

    private void throwException() {
        if (throwException) {
            throw new Error("cause invocation failure");
        }
    }

    public void removeFromList(final Object object) {
        throwException();
        arrayList.remove(object);
    }

    public void clearList() {
        throwException();
        arrayList.clear();
    }

    public String validateAddToList(final Object object) {
        throwException();
        if (object instanceof TestObjectWithCollection) {
            return "can't add this type of object";
        } else {
            return null;
        }
    }

    public String validateRemoveFromList(final Object object) {
        throwException();
        if (object instanceof TestObjectWithCollection) {
            return "can't remove this type of object";
        } else {
            return null;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((arrayList == null) ? 0 : arrayList.hashCode());
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
        final TestObjectWithCollection other = (TestObjectWithCollection) obj;
        if (arrayList == null) {
            if (other.arrayList != null) {
                return false;
            }
        } else if (!arrayList.equals(other.arrayList)) {
            return false;
        }
        return true;
    }

}
