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

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.core.config.IsisModuleCoreConfig;
import org.apache.isis.core.metamodel.IsisModuleCoreMetamodel;
import org.apache.isis.core.runtime.IsisModuleCoreRuntime;
import org.apache.isis.schema.IsisModuleSchema;
import org.apache.isis.viewer.restfulobjects.rendering.IsisModuleRestfulObjectsRendering;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.viewer.restfulobjects.applib.IsisModuleViewerRestfulObjectsApplib;

@Component
@Named(IsisModuleViewerRestfulObjectsApplib.NAMESPACE + ".TaggerDefault")
public class TaggerDefault implements Tagger {

    static Pattern tagPatternForFqcn = Pattern.compile("^.*\\.([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForTwoParts = Pattern.compile("^([^\\.]+)\\.([^\\.]+)$");
    static Pattern tagPatternForJaxbDto = Pattern.compile("^.*\\.([^\\.]+)\\.(v[0-9][^\\.]*)\\.([^\\.]+)$");

    @Override
    @Programmatic
    public String tagForLogicalTypeName(final String logicalTypeName, final String fallback) {

        if (logicalTypeName.startsWith("org.apache.isis.")) {
            return "… asf isis internals";
        }
        if (logicalTypeName.startsWith(IsisModuleApplib.NAMESPACE)) {
            return "… asf isis applib";
        }
        if (logicalTypeName.startsWith(IsisModuleApplib.NAMESPACE_CONF) ||
            logicalTypeName.startsWith(IsisModuleCoreConfig.NAMESPACE)) {
            return "… asf isis config";
        }
        if (logicalTypeName.startsWith(IsisModuleApplib.NAMESPACE_FEAT)) {
            return "… asf isis feat";
        }
        if (logicalTypeName.startsWith(IsisModuleSchema.NAMESPACE)) {
            return "… asf isis schema";
        }
        if (logicalTypeName.startsWith(IsisModuleApplib.NAMESPACE_SUDO)) {
            return "… asf isis sudo";
        }
        if (logicalTypeName.startsWith(IsisModuleCoreMetamodel.NAMESPACE)) {
            return "… asf isis metamodel";
        }
        if (logicalTypeName.startsWith(IsisModuleCoreRuntime.NAMESPACE) ||
            logicalTypeName.startsWith("isis.runtimeservices") ||
            logicalTypeName.startsWith("isis.interaction") ||
            logicalTypeName.startsWith("isis.transaction") ||
            logicalTypeName.startsWith("isis.webapp")
        ) {
            return "… asf isis runtime";
        }
        if (logicalTypeName.startsWith(IsisModuleViewerRestfulObjectsApplib.NAMESPACE) ||
            logicalTypeName.startsWith(IsisModuleRestfulObjectsRendering.NAMESPACE)) {
            return "… asf isis viewer (restful)";
        }
        if (logicalTypeName.startsWith("isis.viewer.wicket")) {
            return "… asf isis viewer (wicket)";
        }
        if (logicalTypeName.startsWith("isis.viewer.")) {
            return "… asf isis viewer";
        }
        if (logicalTypeName.startsWith("isis.persistence.")) {
            return "… asf isis persistence - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("isis.security.")) {
            return "… asf isis security";
        }
        if (logicalTypeName.startsWith("isis.testing")) {
            return "… asf isis testing";
        }
        if (logicalTypeName.startsWith("isis.ext.")) {
            return "… asf isis extensions - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("org.springframework.")) {
            return "… spring framework " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
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
