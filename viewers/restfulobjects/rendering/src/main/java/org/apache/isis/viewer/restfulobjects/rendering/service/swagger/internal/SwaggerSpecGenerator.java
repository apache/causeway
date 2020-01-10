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

import javax.inject.Inject;
import javax.inject.Named;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

@Component
@Named("isisMetaModel.SwaggerSpecGenerator")
public class SwaggerSpecGenerator {

    private final SpecificationLoader specificationLoader;
    private final Tagger tagger;
    private final ClassExcluder classExcluder;
    private final ValuePropertyFactory valuePropertyFactory;

    @Inject
    public SwaggerSpecGenerator(
            final SpecificationLoader specificationLoader,
            final Tagger tagger,
            final ClassExcluder classExcluder,
            final ValuePropertyFactory valuePropertyFactory) {
        this.specificationLoader = specificationLoader;
        this.tagger = tagger;
        this.classExcluder = classExcluder;
        this.valuePropertyFactory = valuePropertyFactory;
    }

    public String generate(
            final String basePath,
            final SwaggerService.Visibility visibility,
            final SwaggerService.Format format) {

        final Generation generation = newGeneration(basePath, visibility);
        final Swagger swagger = generation.generate();

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

    protected Generation newGeneration(final String basePath, final SwaggerService.Visibility visibility) {
        return new Generation(
                basePath, visibility,
                specificationLoader,
                tagger,
                classExcluder,
                valuePropertyFactory);
    }

}
