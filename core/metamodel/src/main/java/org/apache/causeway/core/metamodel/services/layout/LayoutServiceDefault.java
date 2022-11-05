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
package org.apache.causeway.core.metamodel.services.layout;

import java.io.File;
import java.util.EnumSet;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.layout.LayoutExportStyle;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.util.ZipWriter;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".LayoutServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class LayoutServiceDefault implements LayoutService {

    private final SpecificationLoader specificationLoader;
    private final GridService gridService;
    private final MenuBarsService menuBarsService;

    // -- MENUBARS LAYOUT

    @Override
    public EnumSet<CommonMimeType> supportedMenuBarsLayoutFormats() {
        return menuBarsService.marshaller().supportedFormats();
    }

    @Override
    public String menuBarsLayout(
            final MenuBarsService.Type type,
            final CommonMimeType format) {
        val menuBars = menuBarsService.menuBars(type);
        return menuBarsService.marshaller().marshal(_Casts.uncheckedCast(menuBars), format);
    }

    // -- OBJECT LAYOUT

    @Override
    public EnumSet<CommonMimeType> supportedObjectLayoutFormats() {
        return gridService.marshaller().supportedFormats();
    }

    @Override
    public String objectLayout(final Class<?> domainClass, final LayoutExportStyle style, final CommonMimeType format) {
        return tryGridToFormatted(domainClass, style, format)
                .ifFailureFail()
                .getValue()
                .orElse(null);
    }

    @Override
    public byte[] toZip(final LayoutExportStyle style, final CommonMimeType format) {
        val domainObjectSpecs = specificationLoader.snapshotSpecifications()
                .filter(spec ->
                        !spec.isAbstract()
                        && (spec.isEntity() || spec.isViewModel()));

        val zipWriter = ZipWriter.ofFailureMessage("Unable to create zip of layouts");

        for (val objectSpec : domainObjectSpecs) {
            val domainClass = objectSpec.getCorrespondingClass();

            tryGridToFormatted(domainClass, style, format)
            .accept(
                    failure->
                        log.warn("failed to generate layout file for {}", domainClass),
                    contentIfAny->{
                        contentIfAny.ifPresent(contentString->{
                            zipWriter.nextEntry(zipEntryNameFor(objectSpec, format), writer->
                                writer.writeCharactersUtf8(contentString)
                            );
                        });
                    });
        }

        return zipWriter.toBytes();
    }

    // -- HELPER

    private Try<String> tryGridToFormatted(
            final Class<?> domainClass,
            final LayoutExportStyle style,
            final CommonMimeType format) {
        return Try.call(()->
            gridToFormatted(gridService.toGridForExport(domainClass, style), format));
    }

    private String gridToFormatted(final @Nullable Grid grid, final CommonMimeType format) {
        if(grid==null) {
            return null;
        }
        return gridService.marshaller().marshal(_Casts.uncheckedCast(grid), format);
    }

    private static String zipEntryNameFor(
            final ObjectSpecification objectSpec,
            final CommonMimeType format) {
        final String fqn = objectSpec.getFullIdentifier();
        return fqn.replace(".", File.separator)
                + ".layout."
                + format.getProposedFileExtensions().getFirstOrFail();
    }

}
