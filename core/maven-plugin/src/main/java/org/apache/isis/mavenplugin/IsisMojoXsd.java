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
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.runtime.system.context.IsisContext;

@Mojo(
        name = "xsd",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE
        )
public class IsisMojoXsd extends IsisMojoAbstract {

    protected IsisMojoXsd() {
        super();
    }

    /**
     * List of JAXB-annotated classes to generate schema for.
     */
    @Parameter(required = true, readonly = false, property = "jaxbClasses")
    private List<String> jaxbClasses;

    /**
     * Directory to generate XSD schemas; defaults to <code>target/generated-resources/isis-xsd</code>
     */
    @Parameter(required = false, readonly = false, property = "output", defaultValue = "generated-resources/isis-xsd")
    private String output;

    /**
     * Whether a separate directory is created for each JAXB-class, or whether to generate all together (so that they
     * are created as a single bundle).
     */
    @Parameter(required = false, readonly = false, property = "separate", defaultValue = "false")
    private boolean separate;

    /**
     * Whether to also generate the isis common schema(s).
     */
    @Parameter(required = false, readonly = false, property = "commonSchemas", defaultValue = "false")
    private boolean commonSchemas;

    @Override
    protected void doExecute(
            final ContextForMojo context)
                    throws MojoFailureException, IOException {

        final JaxbService jaxbService = 
                IsisContext.getServiceRegistry().lookupServiceElseFail(JaxbService.class);

        final MavenProject mavenProject = context.getMavenProject();
        final File outputDir = determineOutputDir(mavenProject);

        for (String jaxbClass : jaxbClasses) {
            writeSchemas(jaxbService, jaxbClass, outputDir);
        }
    }

    private File determineOutputDir(final MavenProject mavenProject) {
        final String targetDir = mavenProject.getBuild().getDirectory();
        final String outputDirStr = targetDir + File.separator + output;
        return new File(outputDirStr);
    }

    /**
     * to ensure that any schemas with no namespace are given unique file names when written out
     */
    private int unnamed;
    private void writeSchemas(
            final JaxbService jaxbService,
            final String dtoClassName,
            final File outputDir) throws MojoFailureException {

        final Object instance = InstanceUtil.createInstance(dtoClassName);
        final Map<String, String> schemaByNamespace =
                jaxbService.toXsd(
                        instance,
                        commonSchemas ? JaxbService.IsisSchemas.INCLUDE: JaxbService.IsisSchemas.IGNORE);

        final File schemaDir = separate? new File(outputDir, dtoClassName): outputDir;

        unnamed = 0;
        for (Map.Entry<String, String> entry : schemaByNamespace.entrySet()) {
            final String namespaceUri = entry.getKey();
            final String schemaText = entry.getValue();

            final String xsdName = xsdDirNameFor(namespaceUri);
            final File schemaFile = new File(schemaDir, xsdName);
            try {
                Files.createParentDirs(schemaFile);
            } catch (IOException e) {
                throw new MojoFailureException( String.format("Failed to create dir: '%s'", schemaFile.getParent()));
            }
            try {
                Files.write(schemaText, schemaFile, Charsets.UTF_8);
            } catch (IOException e) {
                throw new MojoFailureException("Failed to write out " + schemaFile);
            }
            if(!xsdName.endsWith(".xsd")) {
                final File schemaFileWithXsdSuffix = new File(schemaDir, xsdName + ".xsd");
                try {
                    Files.copy(schemaFile, schemaFileWithXsdSuffix);
                } catch (IOException e) {
                    throw new MojoFailureException("Failed to copy to " + schemaFileWithXsdSuffix);
                }
            }
        }
    }

    String xsdDirNameFor(final String namespaceUri) {
        final String sanitized = sanitize(namespaceUri);
        final String sanitizedElseUnnamed = _Strings.isNullOrEmpty(sanitized) ? ("unnamed-" + (unnamed++)) : sanitized;
        return sanitizedElseUnnamed;
    }

    private static String sanitize(final String namespaceUri) {
        String sanitized = namespaceUri;
        sanitized = removePrefix(sanitized, "http://");
        sanitized = removePrefix(sanitized, "https://");
        sanitized = sanitized.replaceAll(":", "_");
        return  sanitized.replaceAll("[\\\\\\/]", File.separator + File.separator);
    }

    private static String removePrefix(final String str, final String prefix) {
        return str.startsWith(prefix)? str.substring(prefix.length()): str;
    }

}