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
package org.apache.isis.core.config.util;

import java.util.stream.Stream;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Can be removed once objectType got removed.
 */
@UtilityClass
public class LogicalTypeNameUtil {

    public static String logicalTypeName(final @NonNull DomainService aDomainService) {
        return Stream.of(
                aDomainService.logicalTypeName(),
                aDomainService.objectType())
            .filter(_Strings::isNotEmpty)
            .findFirst()
            .orElse(null);
    }

    public static String logicalTypeName(final @NonNull DomainObject aDomainObject) {
        return Stream.of(
                aDomainObject.logicalTypeName(),
                aDomainObject.objectType())
            .filter(_Strings::isNotEmpty)
            .findFirst()
            .orElse(null);
    }

}
