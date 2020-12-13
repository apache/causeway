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

import java.util.Map;

import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;

import lombok.val;

@Service
@Named("demo.MarkupVariableResolverService")
public class MarkupVariableResolverService {

    private final Map<String, String> constants = _Maps.unmodifiable(
        "SOURCES_ISIS", "https://github.com/apache/isis/blob/master/core/applib/src/main/java",
        "SOURCES_DEMO", "https://github.com/apache/isis/tree/master/examples/demo/domain/src/main/java",
        "ISSUES_DEMO", "https://issues.apache.org/jira/",
        "ISIS_VERSION", IsisSystemEnvironment.VERSION
    );

    /**
     * For the given {@code input} replaces '${var-name}' with the variable's value.
     * @param input
     * @return
     */
    public String resolveVariables(String input) {
        val stringRef = _Refs.objectRef(input);
        constants.forEach((k, v)->{
            stringRef.update(string->string.replace(var(k), v));
        });
        return stringRef.getValueElseDefault(input);
    }

    private String var(String name) {
        return String.format("${%s}", name);
    }

}
