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
package org.apache.causeway.core.metamodel.services.grid.bootstrap;

import java.util.EnumSet;
import java.util.Objects;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.grid.GridMarshallerService;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.JaxbUtils;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.experimental.Accessors;

/**
 * Default implementation of {@link GridMarshallerService} using DTOs based on
 * <a href="https://getbootstrap.com>Bootstrap</a> design system.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".GridMarshallerServiceBootstrap")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
//@Log4j2
public class GridMarshallerServiceBootstrap
implements GridMarshallerService<BSGrid> {

    private final JaxbService jaxbService;

    @Inject
    public GridMarshallerServiceBootstrap(final JaxbService jaxbService) {
        super();
        this.jaxbService = jaxbService;
        // eagerly create a JAXBContext for this grid type (and cache it)
        JaxbUtils.jaxbContextFor(BSGrid.class, true);
    }

    @Getter(onMethod_={@Override}) @Accessors(fluent = true)
    private final EnumSet<CommonMimeType> supportedFormats =
        EnumSet.of(CommonMimeType.XML);

    @Override
    public Class<BSGrid> supportedClass() {
        return BSGrid.class;
    }

    @Override
    public String marshal(final @NonNull BSGrid grid, final @NonNull CommonMimeType format) {
        throwIfFormatNotSupported(format);
        switch(format) {
        case XML:{
            return jaxbService.toXml(grid,
                    _Maps.unmodifiable(
                            Marshaller.JAXB_SCHEMA_LOCATION,
                            Objects.requireNonNull(grid.getTnsAndSchemaLocation())
                            ));
        }
        default:
            throw _Exceptions.unsupportedOperation("supported format %s is not implemented", format.name());
        }
    }

    @Override
    public Try<BSGrid> unmarshal(final String content, final @NonNull CommonMimeType format) {
        throwIfFormatNotSupported(format);
        switch(format) {
        case XML:{
            return Try.call(()->jaxbService.fromXml(BSGrid.class, content));
        }
        default:
            throw _Exceptions.unsupportedOperation("supported format %s is not implemented", format.name());
        }
    }

    // -- HELPER

    private void throwIfFormatNotSupported(final CommonMimeType format) {
        if(!supportedFormats().contains(format)) {
            throw _Exceptions.unsupportedOperation("object layout file format %s not supported", format.name());
        }
    }

}
