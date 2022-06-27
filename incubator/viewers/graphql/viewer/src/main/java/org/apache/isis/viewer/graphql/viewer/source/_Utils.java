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

    public static final String SINGLE_PARAM_META_DATA_TYPENAME = "Param_Meta_Data";
    final static String GQL_INPUTTYPE_PREFIX = "_gql_input__";
    final static String GQL_MUTATTIONS_FIELDNAME = "_gql_mutations";
    final static String FIELD_META_DATA_TYPENAME = "_gql_Field_Meta_Data";
    final static String FIELD_META_DATA_TYPENAME_SUFFIX = "__Field_metadata";
    final static String MUTATOR_META_DATA_TYPENAME = "Mutator_Meta_Data";
    static final String PARAMS_META_DATA_TYPENAME_SUFFIX = "__Params_Meta_Data";

    static String metaTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_meta";
    }

    static String mutatorsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_mutators";
    }

    static String metaFieldsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_fields";
    }

    static String metaMutationsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_mutations";
    }

    static String parameterizedFieldMetaDataTypeName(final String logicalTypeNameSanitized, final String fieldName){
        return logicalTypeNameSanitized + "_" + fieldName + FIELD_META_DATA_TYPENAME_SUFFIX;
    }

    static String parametersMetaDataTypeName(final String logicalTypeNameSanitized, final String fieldName){
        return logicalTypeNameSanitized + "_" + fieldName + PARAMS_META_DATA_TYPENAME_SUFFIX;
    }

    static String logicalTypeNameSanitized(final String logicalTypeName) {
        return logicalTypeName.replace('.', '_');
    }

}
