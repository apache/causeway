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

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.plugins.environment.IsisSystemEnvironment;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.runtime.systemusinginstallers.IsisComponentProvider;
import org.apache.isis.tool.mavenplugin.util.MavenProjects;

public abstract class IsisMojoAbstract extends AbstractMojo {

    protected static final String CURRENT_PLUGIN_KEY = "org.apache.isis.tool:isis-maven-plugin";

    @Component
    private MavenProject mavenProject;

    @Parameter(required = true, readonly = false, property = "appManifest")
    private String appManifest;

    protected IsisMojoAbstract() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        new IsisLoggingConfigurer(Level.INFO).configureLogging(".", new String[]{});
        _Config.clear();
        IsisSystemEnvironment.setUnitTesting(true);
        
        final ContextForMojo context = new ContextForMojo(mavenProject, getLog());

        final Plugin plugin = MavenProjects.lookupPlugin(mavenProject, CURRENT_PLUGIN_KEY);

        final AppManifest appManifest = InstanceUtil.createInstance(this.appManifest, AppManifest.class);
        final IsisComponentProvider isisComponentProvider = IsisComponentProvider.builder()
            .appManifest(appManifest)
            .build();
        final IsisSessionFactoryBuilder isisSessionFactoryBuilder = 
                new IsisSessionFactoryBuilder(isisComponentProvider);
        
        IsisSessionFactory isisSessionFactory = null;
        try {
            isisSessionFactory = isisSessionFactoryBuilder.buildSessionFactory();
            if(!isisSessionFactoryBuilder.isMetaModelValid()) {
                MetaModelInvalidException metaModelInvalidException = IsisContext
                        .getMetaModelInvalidExceptionIfAny();
                Set<String> validationErrors = metaModelInvalidException.getValidationErrors();
                context.throwFailureException(validationErrors.size() + " meta-model problems found.", validationErrors);
                return;
            }
            final IsisSessionFactory isisSessionFactoryFinal = isisSessionFactory;
            isisSessionFactory.doInSession(new Runnable() {
                @Override
                public void run() {
                    try {
                        doExecute(context, isisSessionFactoryFinal);
                    } catch (IOException e) {
                        ;
                        // ignore
                    } catch (MojoFailureException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

        } catch(RuntimeException e) {
            if(e.getCause() instanceof MojoFailureException) {
                throw (MojoFailureException)e.getCause();
            }
            throw e;
        } finally {
            if(isisSessionFactory != null) {
                isisSessionFactory.destroyServicesAndShutdown();
            }
        }

    }

    protected abstract void doExecute(
            final ContextForMojo context,
            final IsisSessionFactory isisSessionFactory)
                    throws MojoFailureException, IOException;

    // -- Context
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


}