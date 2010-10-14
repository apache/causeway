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


package org.apache.isis.extensions.hibernate.objectstore.tools.internal;

import org.apache.isis.metamodel.spec.feature.ObjectAssociation;


/**
 * Association from one class to another (one to one, many to one, many to many) which is mapped as "inverse".
 */
public class Association {
    private final PersistentSpecification persistentClass;
    private final ObjectAssociation field;
    private final boolean inverse;

    public Association(final PersistentSpecification persistentClass, final ObjectAssociation field, final boolean inverse) {
        this.persistentClass = persistentClass;
        this.field = field;
        this.inverse = inverse;
    }

    public ObjectAssociation getField() {
        return field;
    }

    public boolean isInverse() {
        return inverse;
    }

    public PersistentSpecification getPersistentClass() {
        return persistentClass;
    }
}
