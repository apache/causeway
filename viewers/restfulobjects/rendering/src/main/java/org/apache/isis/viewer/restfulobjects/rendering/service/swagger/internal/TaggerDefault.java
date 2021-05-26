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
package org.apache.isis.viewer.restfulobjects.rendering.service.swagger.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.base._Strings;

@Component
@Named("isis.metamodel.TaggerDefault")
public class TaggerDefault implements Tagger {

    static Pattern tagPatternForFqcn = Pattern.compile("^.*\\.([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForTwoParts = Pattern.compile("^([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForJaxbDto = Pattern.compile("^.*\\.([^\\.]+)\\.(v[0-9][^\\.]*)\\.([^\\.]+)$");

    @Override
    @Programmatic
    public String tagForLogicalTypeName(final String logicalTypeName, final String fallback) {

        if (logicalTypeName.startsWith("org.apache.isis.")) {
            return ". apache isis internals";
        }
        if (logicalTypeName.startsWith("isis.applib.")) {
            return ". apache isis applib";
        }
        if (logicalTypeName.startsWith("isis.conf.")) {
            return ". apache isis conf";
        }
        if (logicalTypeName.startsWith("isis.sudo.")) {
            return ". apache isis sudo";
        }
        if (logicalTypeName.startsWith("isis.persistence.")) {
            return ". apache isis persistence - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("isis.security.")) {
            return ". apache isis security";
        }
        if (logicalTypeName.startsWith("isis.ext.")) {
            return ". apache isis extensions - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("isis.sub.")) {
            return ". apache isis subdomains - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
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
