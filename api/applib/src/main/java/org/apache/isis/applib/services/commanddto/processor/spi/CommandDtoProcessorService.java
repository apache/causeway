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
package org.apache.isis.applib.services.commanddto.processor.spi;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.schema.cmd.v2.CommandDto;

/**
 * Service used to convert a domain object into a {@link CommandDto}, called by
 * {@link ContentMappingServiceForCommandDto}.
 *
 * <p>
 *     The service is used as a fallback if an {@link CommandDtoProcessor},
 *     hasn't been explicitly specified using
 *     {@link Action#commandDtoProcessor()} or
 *     {@link Property#commandDtoProcessor()}.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface CommandDtoProcessorService {

    /**
     * Converts the domain object (acting as the source) into a {@link CommandDto}.
     *
     * <p>
     *     The {@link CommandDto} that is also passed into the method will be
     *     from a default implementation provided by the framework.  Most
     *     implementations will typically refine this provided DTO and return,
     *     for example adding additional user metadata to
     *     {@link CommandDto#getUserData()}.
     * </p>
     *
     * @param domainObject - is the target that acts as the source of the
     *                       {@link CommandDto}.
     * @param commandDto - is either <code>null</code>, or is passed from a
     *                     previous implementation for further refinement.
     */
    CommandDto process(final Object domainObject, final @Nullable CommandDto commandDto);


}

