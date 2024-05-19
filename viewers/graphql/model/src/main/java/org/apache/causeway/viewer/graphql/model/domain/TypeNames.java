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

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class TypeNames {

    public String objectTypeFieldNameFor(
            final ObjectSpecification objectSpecification) {
        return sanitized(objectSpecification.getLogicalTypeName());
    }

    public String objectTypeNameFor(
            final ObjectSpecification objectSpecification,
            final SchemaType schemaType) {
        return schemaType.name().toLowerCase() + "__" + sanitized(objectSpecification.getLogicalTypeName());
    }

    public String metaTypeNameFor(
            final ObjectSpecification objectSpecification,
            final SchemaType schemaType) {
        return objectTypeNameFor(objectSpecification, schemaType) + "__gqlv_meta";
    }

    public String inputTypeNameFor(
            final ObjectSpecification objectSpecification,
            final SchemaType schemaType) {
        return objectTypeNameFor(objectSpecification, schemaType) + "__gqlv_input";
    }

    public String enumTypeNameFor(
            final ObjectSpecification objectSpec,
            final SchemaType schemaType) {
        return objectTypeNameFor(objectSpec, schemaType) + "__gqlv_enum";
    }

    public String actionTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction oa,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + oa.asciiId() + "__gqlv_action";
    }

    public String actionInvokeTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction oa,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + oa.asciiId() + "__gqlv_action_invoke";
    }

    public String actionParamsTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction oa,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + oa.asciiId() + "__gqlv_action_params";
    }

    public String actionArgsTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectAction oa,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + oa.asciiId() + "__gqlv_action_args";
    }

    public String actionParamTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectActionParameter oap,
            final SchemaType schemaType) {
        final ObjectFeature objectFeature = oap.getAction();
        return objectTypeNameFor(owningType, schemaType) + "__" + objectFeature.asciiId() + "__" + oap.asciiId() + "__gqlv_action_parameter";
    }

    public String propertyTypeNameFor(
            final ObjectSpecification owningType,
            final OneToOneAssociation otoa,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + otoa.asciiId() + "__gqlv_property";
    }

    public String propertyLobTypeNameFor(
            final ObjectSpecification owningType,
            final OneToOneAssociation otoa,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + otoa.asciiId() + "__gqlv_property_lob";
    }

    public String collectionTypeNameFor(
            final ObjectSpecification owningType,
            final OneToManyAssociation otma,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + otma.asciiId() + "__gqlv_collection";
    }

    public String memberTypeNameFor(
            final ObjectSpecification owningType,
            final ObjectMember objectMember,
            final SchemaType schemaType) {
        return objectTypeNameFor(owningType, schemaType) + "__" + objectMember.asciiId() + "__gqlv_member";
    }

    // -- HELPER
    
    String sanitized(final String name) {
        var result = name.replace('.', '_').replace("#", "__").replace("()", "");
        result = hyphenedToCamelCase(result);
        return result;    
    }

    /**
     * Converts e.g. {@code a-b} to {@code aB}.
     * Which allows namespaces that contain a hyphen like 
     * {@code university.calc.calculator-hyphenated} to be referenced from QraphQL via
     * {@code university.calc.calculatorHyphenated} say. 
     */
    private String hyphenedToCamelCase(String string) {
      final int hyphenStart = string.indexOf("-");
      return hyphenStart > 0
        ? string.substring(0,hyphenStart) + Arrays.stream(string.substring(hyphenStart + 1).split("-"))
              .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
              .collect(Collectors.joining())
        : string;
    }

}
