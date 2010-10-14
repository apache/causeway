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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.query;

import java.io.Serializable;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.type.Type;

class IndexedParameter implements Parameter, Serializable {
    private static final long serialVersionUID = 1L;
    private final int index;
    private transient Object value;
    private final Type type;

    public IndexedParameter(final int index, final Object value, final Type type) {
        super();
        this.index = index;
        this.value = value;
        this.type = type;
    }

    public void setParameterInto(final Query query) {
        if (type.equals(Hibernate.OBJECT)) {
            query.setEntity(index, value);
        } else if (type.equals(QueryPlaceholder.DETERMINE)) {
            query.setParameter(index, value);
        } else {
            query.setParameter(index, value, type);
        }
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

}

