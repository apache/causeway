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

    public static String objectTypeFieldNameFor(
            final ObjectSpecification objectSpecification) {
        return sanitized(objectSpecification.getLogicalTypeName());
    }

    public static String objectTypeNameFor(
            final ObjectSpecification objectSpecification,
            final SchemaType schemaType) {
        return schemaType.name().toLowerCase() + "__" + sanitized(objectSpecification.getLogicalTypeName());
    }

    public static String metaTypeNameFor(
            final ObjectSpecification objectSpecification,
            final SchemaType schemaType) {
        return objectTypeNameFor(objectSpecification, schemaType) + "__gqlv_meta";
    }

    public static String inputTypeNameFor(
            final ObjectSpecification objectSpecification,
            final SchemaType schemaType) {
        return objectTypeNameFor(objectSpecification, schemaType) + "__gqlv_input";
    }

    public static String enumTypeNameFor(
            final ObjectSpecification objectSpec,
            final SchemaType schemaType) {
        return objectTypeNameFor(objectSpec, schemaType) + "__gqlv_enum";
    }

    public static String actionTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction objectAction,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + objectAction.getId() + "__gqlv_action";
    }

    public static String actionInvokeTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction objectAction,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + objectAction.getId() + "__gqlv_action_invoke";
    }

    public static String actionParamsTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction objectAction,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + objectAction.getId() + "__gqlv_action_params";
    }

    public static String actionArgsTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction objectAction,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + objectAction.getId() + "__gqlv_action_args";
    }

    public static String actionParamTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectActionParameter objectActionParameter,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + objectActionParameter.getAction().getId() + "__" + objectActionParameter.getId() + "__gqlv_action_parameter";
    }

    public static String propertyTypeNameFor(
            final ObjectSpecification owningType,
            final OneToOneAssociation oneToOneAssociation,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + oneToOneAssociation.getId() + "__gqlv_property";
    }

    public static String propertyBlobTypeNameFor(
            final ObjectSpecification owningType,
            final OneToOneAssociation oneToOneAssociation,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + oneToOneAssociation.getId() + "__gqlv_property_blob";
    }

    public static String collectionTypeNameFor(
            final ObjectSpecification owningType,
            final OneToManyAssociation objectMember,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + objectMember.getId() + "__gqlv_collection";
    }

    private static String sanitized(final String name) {
        return name.replace('.', '_').replace("#", "__").replace("()","");
    }

}
