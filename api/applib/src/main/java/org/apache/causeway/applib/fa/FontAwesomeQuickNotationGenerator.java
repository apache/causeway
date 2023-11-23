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

import java.util.stream.Collectors;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.experimental.UtilityClass;

@UtilityClass
class FontAwesomeQuickNotationGenerator {

    String generate(final FontAwesomeLayers model) {
        return _NullSafe.stream(model.getIconEntries())
            .map(iconEntry->processPrefixes(iconEntry.getCssClasses()))
            .collect(Collectors.joining(", "));
    }

    private String processPrefixes(final String cssClasses) {
        return _Strings.splitThenStream(cssClasses, " ")
            .map(String::trim)
            .filter(_Strings::isNotEmpty)
            .map(FontAwesomeQuickNotationGenerator::processPrefix)
            .filter(_Strings::isNotEmpty)
            .collect(Collectors.joining(" "));
    }

    private String processPrefix(final String cssQuickClass) {
        if(cssQuickClass.equals("fa")
                || cssQuickClass.equals("fa-fw")) {
            return null;
        }
        if(cssQuickClass.startsWith("fa-")) {
            return cssQuickClass.substring(3);
        }
        return "." + cssQuickClass;
    }

}
