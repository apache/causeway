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
package org.apache.causeway.viewer.graphql.model.util;

import lombok.experimental.UtilityClass;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

@UtilityClass
public final class TypeNames {

    public static String objectTypeNameFor(ObjectSpecification objectSpecification) {
        return sanitized(objectSpecification.getLogicalTypeName());
    }

    public static String metaTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__meta";
    }

    public static String mutationsTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__mutations";
    }

    public static String inputTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__input";
    }

    public static String invokeTypeNameFor(ObjectMember objectMember) {
        return sanitized(objectMember.getFeatureIdentifier().getFullIdentityString()) + "__invoke";
    }

    private static String sanitized(final String name) {
        return name.replace('.', '_');
    }

}
