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

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.swagger.Format;
import org.apache.causeway.applib.services.swagger.Visibility;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

@Component
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".OpenApiSpecGenerator")
public class OpenApiSpecGenerator {

    private final SpecificationLoader specificationLoader;
    private final Tagger tagger;
    private final ClassExcluder classExcluder;
    private final ValueSchemaFactory valuePropertyFactory;

    @Inject
    public OpenApiSpecGenerator(
            final SpecificationLoader specificationLoader,
            final Tagger tagger,
            final ClassExcluder classExcluder,
            final ValueSchemaFactory valuePropertyFactory) {
        this.specificationLoader = specificationLoader;
        this.tagger = tagger;
        this.classExcluder = classExcluder;
        this.valuePropertyFactory = valuePropertyFactory;
    }

    public String generate(
            final String basePath,
            final Visibility visibility,
            final Format format) {

        final _OpenApiModelFactory generation = newGeneration(basePath, visibility);
        final OpenAPI swagger = generation.generate();

        switch (format) {
        case JSON:
            return Json.pretty(swagger);
        case YAML:
            try {
                return Yaml.pretty().writeValueAsString(swagger);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        default:
            throw new IllegalArgumentException("Unrecognized format: " + format);
        }
    }

    protected _OpenApiModelFactory newGeneration(final String basePath, final Visibility visibility) {
        return new _OpenApiModelFactory(
                basePath, visibility,
                specificationLoader,
                tagger,
                classExcluder,
                valuePropertyFactory);
    }

}
