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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class ObjectAssociations {

    private ObjectAssociations() {
    }

    @Deprecated
    public static Map<String, List<ObjectAssociation>> groupByMemberOrderName(List<ObjectAssociation> associations) {
        return ObjectAssociation.Util.groupByMemberOrderName(associations);
    }

    @Deprecated
    public static Function<ObjectAssociation, String> toName() {
        return ObjectAssociation.Functions.toName();
    }

    @Deprecated
    public static Function<ObjectAssociation, String> toId() {
        return ObjectAssociation.Functions.toId();
    }

    @Deprecated
    public static Predicate<ObjectAssociation> being(final Contributed contributed) {
        return ObjectAssociation.Predicates.being(contributed);
    }

    @Deprecated
    public static Function<String, OneToOneAssociation> fromId(final ObjectSpecification objSpec) {
        return OneToOneAssociation.Functions.fromId(objSpec);
    }


}
