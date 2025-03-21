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
package org.apache.causeway.testing.archtestsupport.applib.classrules;

import java.util.Optional;

import jakarta.inject.Named;

import com.tngtech.archunit.core.domain.JavaClass;

import org.apache.causeway.commons.internal.base._Strings;

import lombok.experimental.UtilityClass;

@UtilityClass
class _LogicalNaming {

    public String logicalNameFor(final JavaClass javaClass) {
        return explicitLogicalNameFor(javaClass)
                .orElseGet(javaClass::getName);
    }

    public Optional<String> explicitLogicalNameFor(final JavaClass javaClass) {

        final String nullableLogicalTypeName = javaClass
                .tryGetAnnotationOfType(Named.class)
                .map(Named::value)
                .orElse(null);

        return _Strings.nonEmpty(nullableLogicalTypeName);

    }

    public boolean hasExplicitLogicalName(final JavaClass javaClass) {
        return explicitLogicalNameFor(javaClass).isPresent();
    }

}
