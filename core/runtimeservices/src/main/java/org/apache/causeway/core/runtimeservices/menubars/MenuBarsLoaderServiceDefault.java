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
package org.apache.causeway.core.runtimeservices.menubars;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.menubars.MenuBars;
import org.apache.causeway.applib.services.menu.MenuBarsLoaderService;
import org.apache.causeway.applib.services.menu.MenuBarsMarshallerService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".MenuBarsLoaderServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class MenuBarsLoaderServiceDefault
implements MenuBarsLoaderService {

    private final boolean supportsReloading;
    private final AtomicReference<AbstractResource> menubarsLayoutResourceRef;
    private final CommonMimeType menubarsLayoutMimeType;

    @Inject
    public MenuBarsLoaderServiceDefault(final MetaModelContext mmc) {
        this.supportsReloading = mmc.getSystemEnvironment().isPrototyping();

        val menubarsLayoutFile = mmc.getConfiguration().getViewer().getCommon().getApplication()
                .getMenubarsLayoutFile();
        val menubarsLayoutResource = new ClassPathResource(menubarsLayoutFile);
        if(!menubarsLayoutResource.exists()) {
            log.warn("menubarsLayoutFile {} (as configured for Apache Causeway) not found",
                    menubarsLayoutFile);
        }
        this.menubarsLayoutResourceRef = new AtomicReference<>(menubarsLayoutResource);
        this.menubarsLayoutMimeType = CommonMimeType.valueOfFileName(menubarsLayoutFile)
                .orElse(CommonMimeType.XML); // fallback default
    }

    // JUnit support
    public MenuBarsLoaderServiceDefault(
            final AtomicReference<AbstractResource> menubarsLayoutResourceRef,
            final CommonMimeType formatUnderTest) {
        this.supportsReloading = true;
        menubarsLayoutResourceRef.getAndUpdate(r->r!=null
                ? r
                : new AbstractResource() {
                    @Override public String getDescription() { return "Empty Resource"; }
                    @Override public InputStream getInputStream() throws IOException { return null; }}
                );
        this.menubarsLayoutResourceRef = menubarsLayoutResourceRef;
        this.menubarsLayoutMimeType = formatUnderTest;
    }

    @Override
    public boolean supportsReloading() {
        return supportsReloading;
    }

    @Override
    public <T extends MenuBars> Optional<T> menuBars(@NonNull final MenuBarsMarshallerService<T> marshaller) {
        return marshaller.unmarshal(loadMenubarsLayoutResource(), menubarsLayoutMimeType)
            .ifFailure(failure->severeCannotLoad(menubarsLayoutResourceRef.get(), failure))
            .getValue();
    }

    // -- HELPER

    private String loadMenubarsLayoutResource() {

        val menubarsLayoutResource = menubarsLayoutResourceRef.get();
        try {

            if(!menubarsLayoutResource.exists()) {
                return null;
            }

            val source = menubarsLayoutResource.getInputStream(); // throws if not found
            final String layoutFileContent =
                    _Strings.read(source, StandardCharsets.UTF_8);

            if(layoutFileContent == null) {
                warnNotFound(menubarsLayoutResource);
            }
            return layoutFileContent;
        } catch (Exception e) {
            severeCannotLoad(menubarsLayoutResource, e);
            return null;
        }

    }

    private boolean warnedOnce = false;

    private void warnNotFound(final AbstractResource menubarsLayoutResource) {
        if(warnedOnce) {
            return;
        }
        log.warn(
                "{}: could not find readable resource {} for the Menubars-Layout.",
                        WebAppContextPath.class.getName(),
                        menubarsLayoutResource);
        warnedOnce = true;
    }

    private void severeCannotLoad(final AbstractResource menubarsLayoutResource, final Throwable cause) {
        log.error("{}: could not find readable resource {} for the Menubars-Layout.",
                        WebAppContextPath.class.getName(),
                        menubarsLayoutResource,
                cause);
    }



}

