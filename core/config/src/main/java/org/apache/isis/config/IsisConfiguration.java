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
package org.apache.isis.config;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 
 * Configuration 'beans' with meta-data (IDE-support).
 * 
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html">spring.io</a>
 * @apiNote should ultimately replace {@link IsisConfigurationLegacy}
 * 
 * @since 2.0
 *
 */
@ConfigurationProperties(ConfigurationConstants.ROOT_PREFIX)
@Data
public class IsisConfiguration {

    private final Reflector reflector = new Reflector();
    @Data
    public static class Reflector {

        private final ExplicitAnnotations explicitAnnotations = new ExplicitAnnotations();
        @Data
        public static class ExplicitAnnotations {

            /**
             * Whether or not a public method needs to be annotated with
             * @{@link org.apache.isis.applib.annotation.Action} in order to be picked up as an action in the metamodel.
             */
            private boolean action = false;
        }

        private final Facet facet = new Facet();
        @Data
        public static class Facet {
            private boolean filterVisibility = true;
        }
        private final Validator validator = new Validator();
        @Data
        public static class Validator {
            private boolean ensureUniqueObjectTypes = true;
        }
    }

    private final Services services = new Services();
    @Data
    public static class Services {

        private final Container container = new Container();

        @Data
        public static class Container {

            /**
             * Normally any queries are automatically preceded by flushing pending executions.
             *
             * <p>
             * This key allows this behaviour to be disabled.
             *
             * <p>
             *     Originally introduced as part of ISIS-1134 (fixing memory leaks in the objectstore)
             *     where it was found that the autoflush behaviour was causing a (now unrepeatable)
             *     data integrity error (see <a href="https://issues.apache.org/jira/browse/ISIS-1134?focusedCommentId=14500638&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14500638">ISIS-1134 comment</a>, in the isis-module-security.
             *     However, that this could be circumvented by removing the call to flush().
             *     We don't want to break existing apps that might rely on this behaviour, on the
             *     other hand we want to fix the memory leak.  Adding this configuration property
             *     seems the most prudent way forward.
             * </p>
             */
            private boolean disableAutoFlush = false;

        }

        private final Translation translation = new Translation();

        @Data
        public static class Translation {

            private final Po po = new Po();

            @Data
            public static class Po {

                TranslationService.Mode mode = TranslationService.Mode.WRITE;
            }

        }
    }

    private final Viewer viewer = new Viewer();
    @Data
    public static class Viewer {

        private final Wicket wicket = new Wicket();
        @Data
        public static class Wicket {

            private final RememberMe rememberMe = new RememberMe();
            @Data
            public static class RememberMe {
                private boolean suppress = false;
            }

            private boolean suppressSignUp = false;
            private boolean suppressPasswordReset = false;
            private boolean clearOriginalDestination = false;

            private PromptStyle promptStyle = PromptStyle.INLINE;

            private boolean showFooter = true;

            private int maxTitleLengthInTables = 12;

            private Integer maxTitleLengthInParentedTables;
            public int getMaxTitleLengthInParentedTables() {
                return maxTitleLengthInParentedTables != null ? maxTitleLengthInParentedTables : getMaxTitleLengthInTables();
            }
            public void setMaxTitleLengthInParentedTables(final int val) {
                maxTitleLengthInParentedTables = val;
            }

            private Integer maxTitleLengthInStandaloneTables;
            public int getMaxTitleLengthInStandaloneTables() {
                return maxTitleLengthInStandaloneTables != null ? maxTitleLengthInStandaloneTables : getMaxTitleLengthInTables();
            }
            public void setMaxTitleLengthInStandaloneTables(final int val) {
                maxTitleLengthInStandaloneTables = val;
            }
        }
    }
    
}
