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
package org.apache.isis.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;
import lombok.Getter;


/**
 * Selected configuration for <code>resteasy-spring-boot-starter</code>, to provide IDE-support.
 *
 * @see <a href="https://github.com/resteasy/resteasy-spring-boot/blob/master/mds/USAGE.md">resteasy-spring-boot-starter docs</a>
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html">spring.io</a>
 *
 * @since 2.0
 */
@ConfigurationProperties("resteasy")
@Data
@Validated
public class RestEasyConfiguration {

    @Getter
    private final Jaxrs jaxrs = new Jaxrs();
    @Data
    public static class Jaxrs {

        /**
         * The path at which the RO viewer should be mounted.
         *
         * <p>
         * Note that this is used rather than <code>resteasy.servlet.mapping.prefix</code>
         * because there is <i>NO</i> implementation of {@link javax.ws.rs.core.Application}, so we rely on it being
         * automatically created.
         * </p>
         *
         * @see <a href="https://github.com/resteasy/resteasy-spring-boot/blob/master/mds/USAGE.md">resteasy-spring-boot-starter docs</a>
         */
        @javax.validation.constraints.Pattern(regexp="^[/].*[^/]$")
        private String defaultPath = "/restful";

        private final App app = new App();
        @Data
        public static class App {

            public static enum Registration {
                BEANS,
                PROPERTY,
                SCANNING,
                AUTO,
            }

            /**
             * How the implementation of the JAX-RS application is discovered.
             *
             * <p>
             *     There should be very little reason to change this from its default.
             * </p>
             *
             * @see <a href="https://github.com/resteasy/resteasy-spring-boot/blob/master/mds/USAGE.md">resteasy-spring-boot-starter docs</a>
             */
            private Registration registration = Registration.BEANS;
        }
    }

}
