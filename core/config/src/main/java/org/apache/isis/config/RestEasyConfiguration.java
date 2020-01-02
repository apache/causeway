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

import lombok.Data;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.validation.annotation.Validated;


/**
 * Configuration 'beans' with meta-data (IDE-support).
 *
 * @see <a href="https://github.com/resteasy/resteasy-spring-boot/blob/master/mds/USAGE.md">resteasy-spring-boot docs</a>
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html">spring.io</a>
 * 
 * @since 2.0
 */
@ConfigurationProperties("resteasy")
@Data
@Validated
public class RestEasyConfiguration {

    @Autowired private ConfigurableEnvironment environment;

    @Inject @Named("resteasy-settings") private Map<String, String> resteasySettings;
    public Map<String, String> getAsMap() { return Collections.unmodifiableMap(resteasySettings); }

    private final Servlet servlet = new Servlet();
    @Data
    public static class Servlet {
        private final Mapping mapping = new Mapping();
        @Data
        public static class Mapping {
            @javax.validation.constraints.Pattern(regexp="^[/].*[^/]$")
            private String prefix = "/restful";
        }
    }

    private final Jaxrs jaxrs = new Jaxrs();
    @Data
    public static class Jaxrs {

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
            private Registration registration = Registration.BEANS;
        }
    }

}
