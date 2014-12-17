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
package org.apache.isis.tool.mavenplugin;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderDefault;
import org.apache.isis.core.metamodel.app.IsisMetaModel;
import org.apache.isis.core.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;
import org.apache.isis.core.runtime.services.ServicesInstaller;
import org.apache.isis.core.runtime.services.ServicesInstallerFromAnnotation;
import org.apache.isis.core.runtime.services.ServicesInstallerFromConfigurationAndAnnotation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;
import org.apache.isis.tool.mavenplugin.util.IsisMetaModels;
import org.apache.isis.tool.mavenplugin.util.MavenProjects;
import org.apache.isis.tool.mavenplugin.util.Xpp3Doms;

@Mojo(
        name = "validate",
        defaultPhase = LifecyclePhase.TEST,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE
)
public class ValidateMojo extends AbstractMojo {

    private static final String CURRENT_PLUGIN_KEY = "org.apache.isis.tool:isis-maven-plugin";

    @Component
    protected MavenProject mavenProject;

    public void execute() throws MojoExecutionException, MojoFailureException {

        final Plugin plugin = MavenProjects.lookupPlugin(mavenProject, CURRENT_PLUGIN_KEY);
        final List<Object> serviceList = plugin != null ? serviceListFor(plugin) : null;
        if(serviceList == null || serviceList.size() == 0) {
            return;
        }
        getLog().info("Found " + serviceList.size() + " services");

        final ValidationFailures validationFailures = bootIsisThenShutdown(serviceList);
        if (validationFailures.occurred()) {
            throwFailureException(validationFailures.getNumberOfMessages() + " problems found.", validationFailures.getMessages());
        }
    }

    private ValidationFailures bootIsisThenShutdown(List<Object> serviceList) throws MojoExecutionException, MojoFailureException {
        IsisMetaModel isisMetaModel = null;
        try {
            isisMetaModel = bootstrapIsis(serviceList);
            final Collection<ObjectSpecification> objectSpecifications = isisMetaModel.getSpecificationLoader().allSpecifications();
            for (ObjectSpecification objectSpecification : objectSpecifications) {
                getLog().debug("loaded: " + objectSpecification.getFullIdentifier());
            }
            return isisMetaModel.getValidationFailures();
        } finally {
            IsisMetaModels.disposeSafely(isisMetaModel);
        }
    }

    private List<Object> serviceListFor(Plugin plugin) throws MojoFailureException {
        IsisConfiguration isisConfiguration = isisConfigurationFor(plugin);

        final ServicesInstaller servicesInstaller;
        if(isisConfiguration == null) {
            servicesInstaller = new ServicesInstallerFromAnnotation();
        } else {
            final ServicesInstallerFromConfigurationAndAnnotation servicesInstallerFromConfigurationAndAnnotation = new ServicesInstallerFromConfigurationAndAnnotation();
            servicesInstallerFromConfigurationAndAnnotation.setConfiguration(isisConfiguration);
            servicesInstaller = servicesInstallerFromConfigurationAndAnnotation;
        }

        servicesInstaller.setIgnoreFailures(true);
        servicesInstaller.init();

        return servicesInstaller.getServices(DeploymentType.SERVER_PROTOTYPE);
    }

    private IsisConfiguration isisConfigurationFor(final Plugin plugin) throws MojoFailureException {
        final Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        if (configuration == null) {
            throwFailureException("Configuration error", "No <configuration> element found");
        }

        final Xpp3Dom servicesEl = configuration.getChild("isisConfigDir");
        if (servicesEl == null) {
            throwFailureException("Configuration error", "No <configuration>/<isisConfigDir> element found");
        }
        final String isisConfigDir = Xpp3Doms.GET_VALUE.apply(servicesEl);

        final File basedir = mavenProject.getBasedir();
        final File file = new File(basedir, isisConfigDir);
        final String absoluteConfigDir = file.getAbsolutePath();
        if(!file.exists() || !file.isDirectory()) {
            throwFailureException("Configuration error",
                    String.format("isisConfigDir (%s) does not exist or is not a directory", absoluteConfigDir));
        }
        final IsisConfigurationBuilderDefault configBuilder = new IsisConfigurationBuilderDefault(absoluteConfigDir);
        configBuilder.addDefaultConfigurationResources();
        return configBuilder.getConfiguration();
    }

    private static IsisMetaModel bootstrapIsis(List<Object> serviceList) {

        IsisMetaModel isisMetaModel = new IsisMetaModel(
                                            new RuntimeContextNoRuntime(),
                                            new ProgrammingModelFacetsJava5(),
                                            serviceList);
        isisMetaModel.init();
        return isisMetaModel;
    }

    private void throwFailureException(String errorMessage, Set<String> logMessages) throws MojoFailureException {
        logErrors(logMessages);
        throw new MojoFailureException(errorMessage);
    }

    private void throwFailureException(String errorMessage, String... logMessages) throws MojoFailureException {
        logErrors(logMessages);
        throw new MojoFailureException(errorMessage);
    }

    private void throwExecutionException(String errorMessage, Exception e) throws MojoExecutionException {
        logErrors(errorMessage);
        throw new MojoExecutionException(errorMessage, e);
    }

    private void logErrors(String... logMessages) {
        getLog().error("");
        for (String logMessage : logMessages) {
            getLog().error(logMessage);
        }
        getLog().error("");
    }

    private void logErrors(Set<String> logMessages) {
        logErrors(logMessages.toArray(new String[] {}));
    }

}