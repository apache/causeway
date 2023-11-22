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
package org.apache.causeway.applib.fa;

import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.experimental.UtilityClass;

@UtilityClass
class FontAwesomeQuickNotationParser {

    FontAwesomeLayers parse(final String quickNotation) {
        var iconEntryCssClasses = _Strings.splitThenStream(quickNotation, ",")
            .map(String::trim)
            .map(_Strings::emptyToNull)
            .map(FontAwesomeQuickNotationParser::processPrefixes)
            .collect(Can.toCan());
        if(iconEntryCssClasses.isEmpty()) {
            return FontAwesomeLayers.empty();
        }
        if(iconEntryCssClasses.isCardinalityOne()) {
            return FontAwesomeLayers.singleIcon(iconEntryCssClasses.getFirstElseFail());
        }
        var stackBuilder = FontAwesomeLayers.stackBuilder()
            .containerCssClasses("fa-lg")
            .containerCssStyle("width:1.25em");
        iconEntryCssClasses.forEach(stackBuilder::addIconEntry);
        return stackBuilder.build();
    }

    private String processPrefixes(final String cssQuickClasses) {
        var elements = _Strings.splitThenStream(cssQuickClasses, " ")
            .map(String::trim)
            .filter(_Strings::isNotEmpty)
            .collect(Collectors.toCollection(TreeSet::new));
        elements.add("fa-stack-1x"); // mandatory
        return elements.stream()
                .map(FontAwesomeQuickNotationParser::processPrefix)
                .collect(Collectors.joining(" "));
    }

    private String processPrefix(final String cssQuickClass) {
        if(cssQuickClass.equals("fa")
                || cssQuickClass.startsWith("fa-")) {
            return cssQuickClass;
        }
        if(cssQuickClass.startsWith(".")) {
            return cssQuickClass.substring(1);
        }
        return "fa-" + cssQuickClass;
    }

}
