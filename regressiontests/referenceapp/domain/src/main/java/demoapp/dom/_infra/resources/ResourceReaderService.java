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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.stereotype.Service;

@Service
@Named("demo.ResourceReaderService")
public class ResourceReaderService {

    public String readResource(final Class<?> aClass, final String resourceName) {
        return readResource(aClass, resourceName, Collections.emptyMap());
    }
    public String readResource(final Class<?> aClass, final String resourceName, final Map<String, Object> attributes) {
        InputStream resourceStream = aClass.getResourceAsStream(resourceName);
        if(resourceStream==null) {
            // horrendous hack...
            resourceStream = aClass.getResourceAsStream("../" + resourceName);
        }
        if(resourceStream==null) {
            return String.format("Resource '%s' not found.", resourceName);
        }
        try {
            return read(resourceStream, attributes);
        } catch (IOException e) {
            return String.format("Failed to read from resource '%s': '%s': ", resourceName, e.getMessage());
        }
    }

//    /**
//     * Read the given {@code input} into a String, while also pre-processing placeholders.
//     *
//     * @param input
//     * @return
//     * @throws IOException
//     */
//    private String read(InputStream input) throws IOException {
//        return read(input, Collections.emptyMap());
//    }

    /**
     * Read the given {@code input} into a String, while also pre-processing placeholders.
     *
     * @param input
     * @return
     * @throws IOException
     */
    private String read(final InputStream input, final Map<String, Object> attributes) throws IOException {
        var in = new InputStreamReader(input);
        var tagHandler = new TagHandler(attributes);
        try (var bufferReader = new BufferedReader(in)) {
            return bufferReader.lines()
                    .map(tagHandler::handle)
                    .filter(Objects::nonNull)
                    .map(markupVariableResolverService::resolveVariables)
                    .collect(Collectors.joining("\n"));
        }
    }

    @Inject
    MarkupVariableResolverService markupVariableResolverService;

}
