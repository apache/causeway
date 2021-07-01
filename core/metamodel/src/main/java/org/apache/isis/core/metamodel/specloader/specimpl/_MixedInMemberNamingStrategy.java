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
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.Objects;

import org.apache.isis.core.metamodel.commons.StringExtensions;

import lombok.experimental.UtilityClass;

@UtilityClass
class _MixedInMemberNamingStrategy {

    /**
     * @param mixinActionAsRegular - first pass MM introspection produces regular ObjectAction instances
     *              for mixin main methods
     */
    String determineNameFrom(final ObjectActionDefault mixinActionAsRegular) {
        return StringExtensions.asCapitalizedName(suffix(mixinActionAsRegular));
    }

    /**
     * @param mixinActionAsRegular - first pass MM introspection produces regular ObjectAction instances
     *              for mixin main methods
     */
    String determineIdFrom(final ObjectActionDefault mixinActionAsRegular) {
        return StringExtensions.asCamelLowerFirst(compress(suffix(mixinActionAsRegular)));
    }

    // -- HELPER

    private static String compress(final String suffix) {
        return suffix.replaceAll(" ","");
    }

    private static String suffix(final ObjectActionDefault mixinActionAsRegular) {
        return deriveMemberNameFrom(mixinActionAsRegular.getOnType().getFeatureIdentifier().getClassNaturalName());
    }

    // subject of JUnit testing
    String deriveMemberNameFrom(final String mixinClassName) {
        final String deriveFromUnderscore = derive(mixinClassName, "_");
        if(!Objects.equals(mixinClassName, deriveFromUnderscore)) {
            return deriveFromUnderscore;
        }
        final String deriveFromDollar = derive(mixinClassName, "$");
        if(!Objects.equals(mixinClassName, deriveFromDollar)) {
            return deriveFromDollar;
        }
        return mixinClassName;
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
