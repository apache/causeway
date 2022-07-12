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
package org.apache.isis.viewer.graphql.viewer.source;

final class _Utils {

    final static String GQL_GENERIC_FIELDNAME = "_gql_generic";
    final static String GQL_GENERIC_PROPERTIES_FIELDNAME = "properties";
    final static String GQL_GENERIC_COLLECTIONS_FIELDNAME = "collections";
    final static String GQL_GENERIC_ACTIONS_FIELDNAME = "actions";
    final static String GQL_INPUTTYPE_PREFIX = "_gql_input__";
    final static String GQL_MUTATTIONS_FIELDNAME = "_gql_mutations";
    final static String GQL_GENERIC_STRUCTURE_TYPENAME = "_gql__DomainObject_generic_structure";
    final static String GQL_GENERIC_PROPERTY_TYPENAME = "_gql__DomainObject_generic_property";
    final static String GQL_GENERIC_COLLECTION_TYPENAME = "_gql__DomainObject_generic_collection";
    final static String GQL_SEMANTICS_TYPENAME = "_gql__Semantics";

    final public static String SINGLE_PARAM_META_DATA_TYPENAME = "Param_Meta_Data"; //TODO: should go
    final static String MUTATOR_META_DATA_TYPENAME = "Mutator_Meta_Data"; //TODO: should go
    static final String PARAMS_META_DATA_TYPENAME_SUFFIX = "_Params_Meta_Data"; //TODO: should go

    static String genericTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_generic";
    }

    static String mutationsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_mutations";
    }

    static String genericPropertiesTypeName(final String logicalTypeNameSanitized){
        return genericTypeName(logicalTypeNameSanitized) + "_properties";
    }

    static String genericCollectionsTypeName(final String logicalTypeNameSanitized){
        return genericTypeName(logicalTypeNameSanitized) + "_collections";
    }

    static String genericActionsTypeName(final String logicalTypeNameSanitized){
        return genericTypeName(logicalTypeNameSanitized) + "_actions";
    }

    static String objectActionGenericTypeName(final String logicalTypeNameSanitized, final String fieldName){
        return logicalTypeNameSanitized + "__" + fieldName + "__ObjectAction_generic";
    }

    static String objectActionGenericParamsTypeName(final String logicalTypeNameSanitized, final String fieldName){
        return objectActionGenericTypeName(logicalTypeNameSanitized, fieldName) + "_" + "params";
    }

    static String objectActionParameterGenericTypeName(final String logicalTypeNameSanitized, final String fieldName, final String paramName){
        return logicalTypeNameSanitized + "__" + fieldName + "__" + paramName + "__ObjectActionParam_generic";
    }

    static String logicalTypeNameSanitized(final String logicalTypeName) {
        return logicalTypeName.replace('.', '_');
    }

}
