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
package org.apache.causeway.core.metamodel.spec.impl;

import org.apache.causeway.core.metamodel.spec.IntrospectionTrigger;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

interface ObjectSpecificationMutable extends ObjectSpecification {

    enum IntrospectionRequest {
        /**
         * No introspection, just register the type, that is, create an initial yet empty {@link ObjectSpecification}.
         */
        REGISTER,
        /**
         * Partial introspection, that only includes type-hierarchy but not members.
         */
        TYPE_ONLY,
        /**
         * Full introspection, that includes type-hierarchy and members.
         */
        FULL
    }

    void introspect(IntrospectionRequest request, IntrospectionTrigger introspectionTrigger);

}
