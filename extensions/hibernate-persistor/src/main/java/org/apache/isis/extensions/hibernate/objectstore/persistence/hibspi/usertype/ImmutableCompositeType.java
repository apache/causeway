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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.usertype;

import java.io.Serializable;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.usertype.CompositeUserType;


/**
 * Base class for immutable Hibernate composite types.
 */
public abstract class ImmutableCompositeType extends ImmutableType implements CompositeUserType {

    public Object assemble(final Serializable cached, final SessionImplementor session, final Object owner) {
        return cached;
    }

    public Serializable disassemble(final Object value, final SessionImplementor session) {
        return (Serializable) value;
    }

    public Object replace(final Object original, final Object target, final SessionImplementor session, final Object owner) {
        return original;
    }

}
