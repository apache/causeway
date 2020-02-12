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
package org.apache.isis.testing.integtestsupport.applib.swagger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.swagger.SwaggerService;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0
 *
 */
@Log4j2
public class SwaggerExporter {

    private final SwaggerService swaggerService;

    @Inject
    public SwaggerExporter(final ServiceRegistry registry) {
        this(registry.lookupServiceElseFail(SwaggerService.class));
    }

    public SwaggerExporter(final SwaggerService swaggerService) {
        this.swaggerService = swaggerService;
    }

    public void export(SwaggerService.Visibility visibility, SwaggerService.Format format) throws IOException {
        export(visibility, format, defaultOutputDir());
    }

    public void export(SwaggerService.Visibility visibility, SwaggerService.Format format, File outputDir) throws IOException {
        export(visibility, format, outputDir, defaultFileNamePrefix());
    }

    public void export(SwaggerService.Visibility visibility, SwaggerService.Format format, File outputDir, String fileNamePrefix) throws IOException {
        final File swaggerSpecFile = buildSwaggerSpecFile(outputDir, fileNamePrefix, visibility, format);
        writeSwaggerSpec(visibility, format, swaggerSpecFile);
        swaggerService.generateSwaggerSpec(visibility, format);
    }


    private File buildSwaggerSpecFile(
            final File outputDir, final String fileNamePrefix,
            final SwaggerService.Visibility visibility,
            final SwaggerService.Format format) {
        final String swaggerSpecName = fileNamePrefix + "-" + visibility + "." + format.name().toLowerCase();
        return new File(outputDir, swaggerSpecName);
    }

    private void writeSwaggerSpec(
            final SwaggerService.Visibility visibility,
            final SwaggerService.Format format,
            final File swaggerSpecFile) throws IOException {
        final String swaggerSpec = swaggerService.generateSwaggerSpec(visibility, format);

        createParentDirs(swaggerSpecFile);
        Files.write(swaggerSpecFile.toPath(), Collections.singletonList(swaggerSpec));
    }

    private static void createParentDirs(File file) throws IOException {
        File parent = file.getCanonicalFile().getParentFile();
        if (parent != null) {
            parent.mkdirs();
            if (!parent.isDirectory()) {
                throw new IOException("Unable to create parent directories of " + file);
            }
        }
    }


    private File defaultOutputDir() {
        return new File("target/generated-resources/swagger-export");
    }

    private String defaultFileNamePrefix() {
        return "swagger";
    }

}