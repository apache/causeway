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
package demoapp.dom._infra.resources;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.val;

@Service
@Named("demo.MarkupVariableResolverService")
public class MarkupVariableResolverService {

    private final Map<String, String> constants;


    @Inject
    public MarkupVariableResolverService(final CausewayConfiguration configuration, final Environment environment) {
        constants = computeConstants(configuration, environment);
    }

    private static Map<String, String> computeConstants(CausewayConfiguration configuration, Environment environment) {

        val map = _Maps.<String,String>newLinkedHashMap();
        val orm = determineOrmProfileFrom(environment);

        map.put("SOURCES_CAUSEWAY", "https://github.com/apache/causeway/blob/master/core/applib/src/main/java");
        map.put("SOURCES_DEMO", "https://github.com/apache/causeway/tree/master/examples/demo/domain/src/main/java");
        map.put("ISSUES_DEMO", "https://issues.apache.org/jira/");
        map.put("CAUSEWAY_VERSION", determineCausewayVersion(configuration));
        map.put("ORM_TITLECASE", titleCase(orm));
        map.put("ORM_LOWERCASE", orm.toLowerCase());

        return Collections.unmodifiableMap(map);
    }

    private static String determineCausewayVersion(CausewayConfiguration configuration) {
        return Optional.ofNullable(configuration.getViewer().getCommon().getApplication().getVersion())
                .orElse("unknown-version");
    }

    private static String determineOrmProfileFrom(Environment environment) {
        val activeProfiles = Arrays.asList(environment.getActiveProfiles());
        if(activeProfiles.contains("demo-jpa")) {
            return "jpa";
        }
        if(activeProfiles.contains("demo-jdo")) {
            return "jdo";
        }
        throw new IllegalStateException("Could not determine ORM");
    }

    private static String titleCase(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase(Locale.ROOT);
    }




    /**
     * For the given {@code input} replaces '${var-name}' with the variable's value.
     * @param input
     */
    public String resolveVariables(final String input) {
        val stringRef = _Refs.objectRef(input);
        constants.forEach((k, v)->{
            stringRef.update(string->string.replace(var(k), v));
        });
        return stringRef.getValueElseDefault(input);
    }

    private String var(final String name) {
        return String.format("${%s}", name);
    }

}
