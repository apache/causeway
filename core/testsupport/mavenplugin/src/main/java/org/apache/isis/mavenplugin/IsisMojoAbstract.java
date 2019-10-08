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

public abstract class IsisMojoAbstract /*extends AbstractMojo*/ {

    protected static final String CURRENT_PLUGIN_KEY = "org.apache.isis.tool:isis-maven-plugin";

    //    @Component
    //    private MavenProject mavenProject;
    //
    //    @Parameter(required = true, readonly = false, property = "appManifest")
    //    private String appManifest;
    //
    //    protected IsisMojoAbstract() {
    //    }
    //
    //    @Override
    //    public void execute() throws MojoExecutionException, MojoFailureException {
    //
    //        new IsisLoggingConfigurer(Level.INFO).configureLogging(".", new String[]{});
    //        IsisSystemEnvironment.setUnitTesting(true);
    //        
    //        final ContextForMojo mjContext = new ContextForMojo(mavenProject, getLog());
    //
    //        final Plugin plugin = MavenProjects.lookupPlugin(mavenProject, CURRENT_PLUGIN_KEY);
    //        _Blackhole.consume(plugin);
    //
    //        final ConfigurableApplicationContext springContext = 
    //                IsisMavenPlugin_SpringContextLauncher.getContext(mavenProject, getLog());
    //        
    //        Objects.requireNonNull(springContext, "Failed to bring up Spring's context.");
    //        
    //        try {
    //            
    //            doExecute(mjContext);
    //            
    //        } catch (IOException e) {
    //            // ignore
    //        } finally {
    //            
    //            if(springContext!=null) {
    //                springContext.close();
    //            }
    //            
    //        }
    //
    //    }
    //
    //    protected abstract void doExecute(
    //            final ContextForMojo mojoContext)
    //                    throws MojoFailureException, IOException;
    //
    //    // -- Context
    //    static class ContextForMojo implements MetaModelProcessor.Context {
    //
    //        private final MavenProject mavenProject;
    //        private final Log log;
    //
    //        public ContextForMojo(final MavenProject mavenProject, final Log log) {
    //            this.mavenProject = mavenProject;
    //            this.log = log;
    //        }
    //
    //        @Override
    //        public MavenProject getMavenProject() {
    //            return mavenProject;
    //        }
    //
    //        @Override
    //        public Log getLog() {
    //            return log;
    //        }
    //
    //        @Override
    //        public void throwFailureException(String errorMessage, Set<String> logMessages) throws MojoFailureException {
    //            logErrors(logMessages);
    //            throw new MojoFailureException(errorMessage);
    //        }
    //
    //        @Override
    //        public void throwFailureException(String errorMessage, String... logMessages) throws MojoFailureException {
    //            logErrors(logMessages);
    //            throw new MojoFailureException(errorMessage);
    //        }
    //
    //        @Override
    //        public void throwExecutionException(String errorMessage, Exception e) throws MojoExecutionException {
    //            logErrors(errorMessage);
    //            throw new MojoExecutionException(errorMessage, e);
    //        }
    //
    //        private void logErrors(Set<String> logMessages) {
    //            logErrors(logMessages.toArray(new String[] {}));
    //        }
    //
    //        @Override
    //        public void logErrors(String... logMessages) {
    //            log.error("");
    //            log.error("");
    //            log.error("");
    //            for (String logMessage : logMessages) {
    //                log.error(logMessage);
    //            }
    //            log.error("");
    //            log.error("");
    //            log.error("");
    //        }
    //    }


}