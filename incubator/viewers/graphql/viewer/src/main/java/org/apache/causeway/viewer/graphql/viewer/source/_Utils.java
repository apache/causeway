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
package org.apache.causeway.viewer.graphql.viewer.source;

final class _Utils {

    final static String GQL_INPUTTYPE_PREFIX = "_gql_input__";
    final static String GQL_MUTATTIONS_FIELDNAME = "_gql_mutations";

    static String metaTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_meta";
    }

    static String mutatorsTypeName(final String logicalTypeNameSanitized){
        return logicalTypeNameSanitized + "__DomainObject_mutators";
    }

    static String logicalTypeNameSanitized(final String logicalTypeName) {
        return logicalTypeName.replace('.', '_');
    }

}
