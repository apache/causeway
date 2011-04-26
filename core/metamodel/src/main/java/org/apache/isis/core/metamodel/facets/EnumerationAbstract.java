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

package org.apache.isis.core.metamodel.facets;

public abstract class EnumerationAbstract implements Enumeration {

    private final int num;
    private final String nameInCode;
    private final String friendlyName;

    protected EnumerationAbstract(final int num, final String nameInCode, final String friendlyName) {
        this.num = num;
        this.nameInCode = nameInCode;
        this.friendlyName = friendlyName;
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public String getNameInCode() {
        return nameInCode;
    }

    @Override
    public String getFriendlyName() {
        return friendlyName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + num;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EnumerationAbstract other = (EnumerationAbstract) obj;
        if (num != other.num) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getNameInCode();
    }

}
