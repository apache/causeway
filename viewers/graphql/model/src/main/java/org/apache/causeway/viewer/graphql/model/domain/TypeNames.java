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
package org.apache.causeway.viewer.graphql.model.domain;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TypeNames {

    public static String objectTypeNameFor(ObjectSpecification objectSpecification) {
        return sanitized(objectSpecification.getLogicalTypeName());
    }

    public static String metaTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__gqlv_meta";
    }

    public static String inputTypeNameFor(ObjectSpecification objectSpecification) {
        return objectTypeNameFor(objectSpecification) + "__gqlv_input";
    }

    public static String actionTypeNameFor(ObjectSpecification owningType, ObjectAction objectAction) {
        return objectTypeNameFor(owningType) + "__" + objectAction.getId() + "__gqlv_action";
    }

    public static String actionParamsTypeNameFor(ObjectSpecification owningType, ObjectAction objectAction) {
        return objectTypeNameFor(owningType) + "__" + objectAction.getId() + "__gqlv_action_params";
    }

    public static String actionParamTypeNameFor(ObjectSpecification owningType, ObjectActionParameter objectActionParameter) {
        return objectTypeNameFor(owningType) + "__" + objectActionParameter.getAction().getId() + "__" + objectActionParameter.getId() + "__gqlv_action_parameter";
    }

    public static String propertyTypeNameFor(ObjectSpecification owningType, OneToOneAssociation oneToOneAssociation) {
        return objectTypeNameFor(owningType) + "__" + oneToOneAssociation.getId() + "__gqlv_property";
    }

    public static String collectionTypeNameFor(ObjectSpecification owningType, OneToManyAssociation objectMember) {
        return objectTypeNameFor(owningType) + "__" + objectMember.getId() + "__gqlv_collection";
    }

    private static String sanitized(final String name) {
        return name.replace('.', '_').replace("#", "__").replace("()","");
    }

}
