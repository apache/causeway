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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;

@Component
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".TaggerDefault")
public class TaggerDefault implements Tagger {

    static Pattern tagPatternForFqcn = Pattern.compile("^.*\\.([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForTwoParts = Pattern.compile("^([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForJaxbDto = Pattern.compile("^.*\\.([^\\.]+)\\.(v[0-9][^\\.]*)\\.([^\\.]+)$");

    @Override
    @Programmatic
    public String tagForLogicalTypeName(final String logicalTypeName, final String fallback) {

        if (logicalTypeName.startsWith("org.apache.causeway.")) {
            return ". apache causeway internals";
        }
        if (logicalTypeName.startsWith("causeway.applib.")) {
            return ". apache causeway applib";
        }
        if (logicalTypeName.startsWith("causeway.conf.")) {
            return ". apache causeway conf";
        }
        if (logicalTypeName.startsWith("causeway.sudo.")) {
            return ". apache causeway sudo";
        }
        if (logicalTypeName.startsWith("causeway.persistence.")) {
            return ". apache causeway persistence - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("causeway.security.")) {
            return ". apache causeway security";
        }
        if (logicalTypeName.startsWith("causeway.ext.")) {
            return ". apache causeway extensions - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("causeway.sub.")) {
            return ". apache causeway subdomains - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("org.springframework.")) {
            return "> spring framework " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }

        Matcher matcher;
        matcher = tagPatternForJaxbDto.matcher(logicalTypeName);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        matcher = tagPatternForFqcn.matcher(logicalTypeName);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        matcher = tagPatternForTwoParts.matcher(logicalTypeName);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return fallback != null? fallback: logicalTypeName;
    }


    private static Stream<String> partsOf(final String logicalTypeName) {
        return _Strings.splitThenStream(logicalTypeName, ".");
    }

}
