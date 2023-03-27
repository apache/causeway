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
package org.apache.causeway.incubator.viewer.javafx.viewer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.incubator.viewer.javafx.headless.SpringBootJavaFxApplication;
import org.apache.causeway.incubator.viewer.javafx.ui.CausewayModuleIncViewerJavaFxUi;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;

/**
 *
 * @since 3.0
 */
@Configuration
@Import({
        // Modules
        CausewayModuleIncViewerJavaFxUi.class,
        CausewayModuleViewerCommonsServices.class,
})
public class CausewayModuleIncViewerJavaFxViewer {

    /**
     *
     * @param appClass the class that is annotated with {@literal @SpringBootApplication}
     * @param args program arguments
     */
    public static final void launch(final Class<?> appClass, final String... args) {
        SpringBootJavaFxApplication.launchSpringBootApplication(appClass, args);
    }

}
