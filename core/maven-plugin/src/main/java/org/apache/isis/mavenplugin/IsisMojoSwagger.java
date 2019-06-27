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
package org.apache.isis.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import org.apache.isis.applib.services.swagger.SwaggerService;
import org.apache.isis.core.runtime.system.context.IsisContext;

@Mojo(
        name = "swagger",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE
        )
public class IsisMojoSwagger extends IsisMojoAbstract {

    protected IsisMojoSwagger() {
        super();
    }

    /**
     * Filename prefix; is appended by the visibility parameter.
     */
    @Parameter(required = false, readonly = false, property = "fileNamePrefix", defaultValue = "swagger")
    private String fileNamePrefix;

    /**
     * List of visibilities required.  Defaults to both `PUBLIC` and `PRIVATE`
     */
    @Parameter(required = false, readonly = false, property = "visibilities")
    private List<SwaggerService.Visibility> visibilities;

    /**
     * Preferred format.  Defaults to <code>JSON</code>.
     */
    @Parameter(required = false, readonly = false, property = "format", defaultValue = "JSON")
    private SwaggerService.Format format;

    /**
     * Directory to generate Swagger spec file; defaults to <code>target/generated-resources/isis-swagger</code>
     */
    @Parameter(required = false, readonly = false, property = "output", defaultValue = "generated-resources/isis-swagger")
    private String output;

    @Override
    protected void doExecute(
            final ContextForMojo context)
                    throws MojoFailureException, IOException {

        final SwaggerService swaggerService = 
                IsisContext.getServiceRegistry().lookupServiceElseFail(SwaggerService.class);

        final MavenProject mavenProject = context.getMavenProject();
        final File outputDir = determineOutputDir(mavenProject, output);

        final List<SwaggerService.Visibility> visibilityList = determineVisibility(visibilities);
        for (SwaggerService.Visibility visibility : visibilityList) {
            final File swaggerSpecFile = buildSwaggerSpecFile(visibility, format, outputDir, fileNamePrefix);
            writeSwaggerSpec(swaggerService, visibility, format, swaggerSpecFile);
        }
    }

    private static File determineOutputDir(final MavenProject mavenProject, final String output) {
        final String targetDir = mavenProject.getBuild().getDirectory();
        final String outputDirStr = targetDir + File.separator + output;
        return new File(outputDirStr);
    }

    private static List<SwaggerService.Visibility> determineVisibility(final List<SwaggerService.Visibility> visibilities) {
        if (visibilities == null || visibilities.isEmpty()) {
            return Arrays.asList(SwaggerService.Visibility.PUBLIC, SwaggerService.Visibility.PRIVATE);
        }
        return visibilities;
    }

    private File buildSwaggerSpecFile(
            final SwaggerService.Visibility visibility,
            final SwaggerService.Format format,
            final File outputDir,
            final String fileNamePrefix) {
        final String swaggerSpecName = fileNamePrefix + "-" + visibility + "." + format.name().toLowerCase();
        return new File(outputDir, swaggerSpecName);
    }

    private void writeSwaggerSpec(
            final SwaggerService swaggerService,
            final SwaggerService.Visibility visibility,
            final SwaggerService.Format format,
            final File swaggerSpecFile) throws MojoFailureException {
        final String swaggerSpec = swaggerService.generateSwaggerSpec(visibility, format);

        try {
            Files.createParentDirs(swaggerSpecFile);
        } catch (IOException e) {
            throw new MojoFailureException( String.format("Failed to create dir: '%s'", swaggerSpecFile.getParent()));
        }
        try {
            Files.write(swaggerSpec, swaggerSpecFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MojoFailureException("Failed to write out " + swaggerSpecFile);
        }
    }

}