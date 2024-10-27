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

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayModuleCoreConfig;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.runtime.CausewayModuleCoreRuntime;
import org.apache.causeway.schema.CausewayModuleSchema;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.rendering.CausewayModuleRestfulObjectsRendering;

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
            return "… asf causeway internals";
        }
        if (logicalTypeName.startsWith(CausewayModuleApplib.NAMESPACE)) {
            return "… asf causeway applib";
        }
        if (logicalTypeName.startsWith(CausewayModuleApplib.NAMESPACE_CONF) ||
            logicalTypeName.startsWith(CausewayModuleCoreConfig.NAMESPACE)) {
            return "… asf causeway config";
        }
        if (logicalTypeName.startsWith(CausewayModuleApplib.NAMESPACE_FEAT)) {
            return "… asf causeway feat";
        }
        if (logicalTypeName.startsWith(CausewayModuleSchema.NAMESPACE)) {
            return "… asf causeway schema";
        }
        if (logicalTypeName.startsWith(CausewayModuleApplib.NAMESPACE_SUDO)) {
            return "… asf causeway sudo";
        }
        if (logicalTypeName.startsWith(CausewayModuleCoreMetamodel.NAMESPACE)) {
            return "… asf causeway metamodel";
        }
        if (logicalTypeName.startsWith(CausewayModuleCoreRuntime.NAMESPACE) ||
            logicalTypeName.startsWith("causeway.runtimeservices") ||
            logicalTypeName.startsWith("causeway.interaction") ||
            logicalTypeName.startsWith("causeway.transaction") ||
            logicalTypeName.startsWith("causeway.webapp")
        ) {
            return "… asf causeway runtime";
        }
        if (logicalTypeName.startsWith(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE) ||
            logicalTypeName.startsWith(CausewayModuleRestfulObjectsRendering.NAMESPACE)) {
            return "… asf causeway viewer (restful)";
        }
        if (logicalTypeName.startsWith("causeway.viewer.wicket")) {
            return "… asf causeway viewer (wicket)";
        }
        if (logicalTypeName.startsWith("causeway.viewer.")) {
            return "… asf causeway viewer";
        }
        if (logicalTypeName.startsWith("causeway.persistence.")) {
            return "… asf causeway persistence - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
        }
        if (logicalTypeName.startsWith("causeway.security.")) {
            return "… asf causeway security";
        }
        if (logicalTypeName.startsWith("causeway.testing")) {
            return "… asf causeway testing";
        }
        if (logicalTypeName.startsWith("causeway.ext.")) {
            return "… asf causeway extensions - " + partsOf(logicalTypeName).skip(2).limit(1).collect(Collectors.joining("."));
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
