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

package org.apache.isis.applib.annotation;

import java.lang.annotation.*;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.command.CommandDtoProcessor;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.services.conmap.command.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.conmap.command.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Intended to be annotated on the root marker class of a (Maven) module which contains a single coherent set of
 * functionality, possibly including domain services.
 *
 * <p>
 * A {@link Module @Module} (because it's a meta-annotation) is also a Spring {@link Configuration @Configuration},
 * which means that the functionality can be depended upon transitively using Spring's {@link Import @Import}
 * annotation. Normally the import graph mirrors the dependencies in Maven.
 * </p>
 *
 * <p>
 * Also, a {@link Module @Module} also declares the Spring {@link ComponentScan @ComponentScan} (with no parameters),
 * which means that any domain services in the same package or subpackages are automatically found and registered.
 * </p>
 *
 * @implNote - there are possible performance implications from using this annotation, because it may result in
 *             scanning more classes than are needed.  It is therefore not used within the framework code, but is
 *             provided as a convenience by domain applications.
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Configuration
@Documented
@ComponentScan
public @interface Module {

}