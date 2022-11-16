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
package org.apache.causeway.core.runtimeservices.menubars.bootstrap;

import java.util.EnumSet;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.Marshaller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBars;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.menu.MenuBarsMarshallerService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".MenuBarsMarshallerBootstrap")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
//@Log4j2
public class MenuBarsMarshallerServiceBootstrap
implements MenuBarsMarshallerService<BSMenuBars> {

    private final JaxbService jaxbService;

    @Getter(onMethod_={@Override}) @Accessors(fluent = true)
    private final EnumSet<CommonMimeType> supportedFormats =
            EnumSet.of(CommonMimeType.XML);

    @Inject
    public MenuBarsMarshallerServiceBootstrap(
            final JaxbService jaxbService) {
        this.jaxbService = jaxbService;
    }

    @Override
    public Class<BSMenuBars> supportedClass() {
        return BSMenuBars.class;
    }

    @Override
    public String marshal(final @NonNull BSMenuBars menuBars, final @NonNull CommonMimeType format) {
        throwIfFormatNotSupported(format);
        return jaxbService.toXml(menuBars, _Maps.unmodifiable(
                Marshaller.JAXB_SCHEMA_LOCATION, menuBars.getTnsAndSchemaLocation()));
    }

    @Override
    public Try<BSMenuBars> unmarshal(
            final @Nullable String layoutFileContent,
            final @NonNull CommonMimeType format) {
        throwIfFormatNotSupported(format);
        if(_Strings.isEmpty(layoutFileContent)) {
            return Try.success(null); // empty
        }
        switch(format) {
        case XML:{
            return Try.call(()->jaxbService.fromXml(BSMenuBars.class, layoutFileContent));
        }
        default:
            throw _Exceptions.unmatchedCase(format); // supported format, but not implemented
        }
    }

    // -- HELPER

    private void throwIfFormatNotSupported(final CommonMimeType format) {
        if(!supportedFormats().contains(format)) {
            throw _Exceptions.unsupportedOperation("menu-bars layout file format %s not supported", format.name());
        }
    }

}

