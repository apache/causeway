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
package org.apache.causeway.core.runtimeservices.wrapper;

import java.lang.reflect.Method;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Data;

@Data
class MemberAndTarget {
    static MemberAndTarget notFound() {
        return new MemberAndTarget(Type.NONE, null, null, null, null);
    }

    static MemberAndTarget foundAction(final ObjectAction action, final ManagedObject target, final Method method) {
        return new MemberAndTarget(Type.ACTION, action, null, target, method);
    }

    static MemberAndTarget foundProperty(final OneToOneAssociation property, final ManagedObject target, final Method method) {
        return new MemberAndTarget(Type.PROPERTY, null, property, target, method);
    }

    public boolean isMemberFound() {
        return type != Type.NONE;
    }

    enum Type {
        ACTION,
        PROPERTY,
        NONE
    }

    private final Type type;
    /**
     * Populated if and only if {@link #type} is {@link Type#ACTION}.
     */
    private final ObjectAction action;
    /**
     * Populated if and only if {@link #type} is {@link Type#PROPERTY}.
     */
    private final OneToOneAssociation property;
    private final ManagedObject target;
    private final Method method;
}
