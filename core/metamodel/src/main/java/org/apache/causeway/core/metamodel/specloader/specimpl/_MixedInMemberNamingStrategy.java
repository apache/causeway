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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.Objects;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.commons.StringExtensions;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
class _MixedInMemberNamingStrategy {

    /**
     * @param mixinActionAsRegular - first pass MM introspection produces regular ObjectAction instances
     *              for mixin main methods
     */
    String mixinFriendlyName(final @NonNull ObjectActionDefault mixinActionAsRegular) {
        return mixinFriendlyName(mixinActionAsRegular.getFeatureIdentifier().getClassNaturalName());
    }

    String mixinFriendlyName(final @NonNull String mixinClassSimpleName) {
        return _Strings.capitalize(suffix(mixinClassSimpleName));
    }

    /**
     * @param mixinActionAsRegular - first pass MM introspection produces regular ObjectAction instances
     *              for mixin main methods
     */
    String mixinMemberId(final @NonNull ObjectActionDefault mixinActionAsRegular) {
        return mixinMemberId(mixinActionAsRegular.getFeatureIdentifier().getClassNaturalName());
    }

    String mixinMemberId(final @NonNull String mixinClassSimpleName) {
        return StringExtensions.asCamelLowerFirst(compress(suffix(mixinClassSimpleName)));
    }

    // -- HELPER

    private static String compress(final String suffix) {
        return suffix.replaceAll(" ", "");
    }

    private static String suffix(final String mixinClassSimpleName) {
        final String deriveFromUnderscore = derive(mixinClassSimpleName, "_");
        if(!Objects.equals(mixinClassSimpleName, deriveFromUnderscore)) {
            return deriveFromUnderscore;
        }
        final String deriveFromDollar = derive(mixinClassSimpleName, "$");
        if(!Objects.equals(mixinClassSimpleName, deriveFromDollar)) {
            return deriveFromDollar;
        }
        return mixinClassSimpleName;
    }

    private String derive(final String singularName, final String separator) {
        final int indexOfSeparator = singularName.lastIndexOf(separator);
        return occursNotAtEnd(singularName, indexOfSeparator)
                ? singularName.substring(indexOfSeparator + 1)
                : singularName;
    }

    private boolean occursNotAtEnd(final String singularName, final int indexOfUnderscore) {
        return indexOfUnderscore != -1
                && indexOfUnderscore != singularName.length() - 1;
    }

}
