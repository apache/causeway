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
package org.apache.causeway.core.metamodel.facets.members.cssclassfa;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;

/**
 * @since 2.0
 */
public interface CssClassFaFactory {

    /**
     * Position of <a href="http://fortawesome.github.io/Font-Awesome/">Font Awesome</a> icon.
     */
    CssClassFaPosition getPosition();

    Stream<String> streamCssClasses();

    /**
     * Space separated (distinct) CSS-class strings.
     */
    default String asSpaceSeparated() {
        return streamCssClasses()
                .collect(Collectors.joining(" "));
    }

    /**
     * Space separated (distinct) CSS-class strings.
     * @param additionalClasses - trimmed and filtered by non-empty, then added to the resulting string
     */
    default String asSpaceSeparatedWithAdditional(final String ... additionalClasses) {

        if(_NullSafe.size(additionalClasses)==0) {
            return asSpaceSeparated();
        }

        return Stream.concat(
                streamCssClasses(),
                _NullSafe.stream(additionalClasses)
                    .map(String::trim)
                    .filter(_Strings::isNotEmpty))
        .distinct()
        .collect(Collectors.joining(" "));

    }

    /**
     * @implNote because {@link CssClassFaStaticFacetAbstract} has all the fa-icon logic,
     * we simply reuse it here by creating an anonymous instance
     */
    public static CssClassFaFactory ofIconAndPosition(final String faIcon, final CssClassFaPosition position) {
        return new CssClassFaStaticFacetAbstract(
                faIcon, position, null) {};
    }

}
