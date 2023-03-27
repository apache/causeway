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
package org.apache.causeway.incubator.viewer.javafx.headless;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import org.apache.causeway.commons.internal._Constants;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFx {@link Application},
 * that also initializes a Spring {@link GenericApplicationContext} in the background.
 * <p>
 * Example usage:
 * <pre>
 * // Bootstrap the application.
 * &#64;SpringBootApplication
 * &#64;Import({
 *     StageInitializer.class
 * })
 * public class JavaFxSampleApp {
 *   public static void main(final String[] args) {
 *     SpringBootJavaFxApplication.launchSpringBootApplication(JavaFxSampleApp.class, args);
 *   }
 * }
 *
 * public class StageInitializer implements PrimaryStageInitializer {
 *   public void onPrimaryStageReady(final Stage primaryStage) {
 *     primaryStage.setScene(new Scene(new Label("Hello World!"), 250, 150));
 *   }
 * }
 * </pre>
 *
 * @since 3.0
 */
public class SpringBootJavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    // helps during initialization
    private static Class<?>[] sources;

    @Override
    public void init() {

        final ApplicationContextInitializer<GenericApplicationContext> initializer =
                ac->{
                    ac.registerBean(Application.class, ()->SpringBootJavaFxApplication.this);
                    ac.registerBean(Parameters.class, this::getParameters);
                    ac.registerBean(HostServices.class, this::getHostServices);
                };

                this.springContext = new SpringApplicationBuilder()
                        .sources(sources)
                        .initializers(initializer)
                        .run(getParameters().getRaw().toArray(_Constants.emptyStringArray));
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.springContext.publishEvent(new PrimaryStageReadyEvent(primaryStage));
    }

    @Override
    public void stop() throws Exception {
        this.springContext.close();
        Platform.exit();
    }

    /**
     *
     * @param appClass the class that is annotated with {@literal @SpringBootApplication}
     * @param args program arguments
     */
    public static final void launchSpringBootApplication(final Class<?> appClass, final String... args) {
        SpringBootJavaFxApplication.sources = new Class<?>[]{appClass};
        Application.launch(SpringBootJavaFxApplication.class, args);
    }

}
