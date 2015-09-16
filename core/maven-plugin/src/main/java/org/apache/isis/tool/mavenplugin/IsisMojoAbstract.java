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

import java.util.Set;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorDefault;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProviderDefault2;
import org.apache.isis.tool.mavenplugin.util.MavenProjects;

public abstract class IsisMojoAbstract extends AbstractMojo {

    protected static final String CURRENT_PLUGIN_KEY = "org.apache.isis.tool:isis-maven-plugin";

    @Component
    private MavenProject mavenProject;

    @Parameter(required = true, readonly = false, property = "appManifest")
    private String appManifest;

    private final MetaModelProcessor metaModelProcessor;
    private final ContextForMojo context;

    protected IsisMojoAbstract(final MetaModelProcessor metaModelProcessor) {
        this.metaModelProcessor = metaModelProcessor;
        this.context = new ContextForMojo(mavenProject, getLog());
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        final Plugin plugin = MavenProjects.lookupPlugin(mavenProject, CURRENT_PLUGIN_KEY);

        final AppManifest manifest = InstanceUtil.createInstance(this.appManifest, AppManifest.class);
        final IsisComponentProviderDefault2 componentProvider = new IsisComponentProviderDefault2(
                DeploymentType.UNIT_TESTING, manifest, null, null, null, null, null);

        final IsisSystem isisSystem = new IsisSystem(componentProvider);
        try {
            isisSystem.init();

        } catch(RuntimeException ex) {
            ;
            // ignore
        } finally {
            isisSystem.shutdown();
        }

        final ObjectReflectorDefault specificationLoader =
                (ObjectReflectorDefault) isisSystem.getSessionFactory().getSpecificationLoader();
        metaModelProcessor.process(specificationLoader, context);


    }

    //region > Context
    static class ContextForMojo implements MetaModelProcessor.Context {

        private final MavenProject mavenProject;
        private final Log log;

        public ContextForMojo(final MavenProject mavenProject, final Log log) {
            this.mavenProject = mavenProject;
            this.log = log;
        }

        @Override
        public MavenProject getMavenProject() {
            return mavenProject;
        }

        @Override
        public Log getLog() {
            return log;
        }

        @Override
        public void throwFailureException(String errorMessage, Set<String> logMessages) throws MojoFailureException {
            logErrors(logMessages);
            throw new MojoFailureException(errorMessage);
        }

        @Override
        public void throwFailureException(String errorMessage, String... logMessages) throws MojoFailureException {
            logErrors(logMessages);
            throw new MojoFailureException(errorMessage);
        }

        @Override
        public void throwExecutionException(String errorMessage, Exception e) throws MojoExecutionException {
            logErrors(errorMessage);
            throw new MojoExecutionException(errorMessage, e);
        }

        private void logErrors(Set<String> logMessages) {
            logErrors(logMessages.toArray(new String[] {}));
        }

        @Override
        public void logErrors(String... logMessages) {
            log.error("");
            log.error("");
            log.error("");
            for (String logMessage : logMessages) {
                log.error(logMessage);
            }
            log.error("");
            log.error("");
            log.error("");
        }
    }
    //endregion

}