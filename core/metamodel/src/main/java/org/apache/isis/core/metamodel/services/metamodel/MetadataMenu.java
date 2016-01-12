/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.metamodel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.layout.v1_0.ObjectLayoutMetadata;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.ObjectLayoutMetadataService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY
)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "500.400"
)
public class MetadataMenu implements SpecificationLoaderSpiAware {

    public static abstract class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<MetadataMenu> {
    }

    // //////////////////////////////////////


    private final MimeType mimeTypeApplicationZip;

    public MetadataMenu() {
        try {
            mimeTypeApplicationZip = new MimeType("application", "zip");
        } catch (final MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }


    // //////////////////////////////////////

    public static class DownloadLayoutsDomainEvent extends ActionDomainEvent {
    }

    @Action(
            domainEvent = DownloadLayoutsDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-download"
    )
    @MemberOrder(sequence="500.400.1")
    public Blob downloadLayouts() {
        final Collection<ObjectSpecification> allSpecs = specificationLoader.allSpecifications();
        final Collection<ObjectSpecification> domainObjectSpecs = Collections2
                .filter(allSpecs, new Predicate<ObjectSpecification>(){
                    @Override
                    public boolean apply(final ObjectSpecification input) {
                        return  !input.isAbstract() &&
                                !input.isService() &&
                                !input.isValue() &&
                                !input.isParentedOrFreeCollection();
                    }});
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            final OutputStreamWriter writer = new OutputStreamWriter(zos);
            for (final ObjectSpecification objectSpec : domainObjectSpecs) {
                final Class<?> domainClass = objectSpec.getCorrespondingClass();
                final ObjectLayoutMetadata metadata = objectLayoutMetadataService.toMetadata(domainClass);
                if(metadata != null) {
                    zos.putNextEntry(new ZipEntry(zipEntryNameFor(objectSpec)));
                    String xml = jaxbService.toXml(metadata);
                    writer.write(xml);
                    writer.flush();
                    zos.closeEntry();
                }
            }
            writer.close();
            return new Blob("layouts.zip", mimeTypeApplicationZip, baos.toByteArray());
        } catch (final IOException ex) {
            throw new FatalException("Unable to create zip of layouts", ex);
        }
    }

    private static String zipEntryNameFor(final ObjectSpecification objectSpec) {
        final String fqn = objectSpec.getFullIdentifier();
        return fqn.replace(".", File.separator)+".layout.xml";
    }


    // //////////////////////////////////////

    public static class SwitchOnDynamicReloadingOfLayoutsDomainEvent extends ActionDomainEvent {
    }

    @Action(
            domainEvent = SwitchOnDynamicReloadingOfLayoutsDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-check"
    )
    @MemberOrder(sequence="500.400.2")
    public void switchOnDynamicReloadingOfLayouts() {
        objectLayoutMetadataService.toggleDynamicReloading();
    }
    public boolean hideSwitchOnDynamicReloadingOfLayouts() {
        return objectLayoutMetadataService.isDynamicReloading();
    }

    // //////////////////////////////////////

    public static class SwitchOffDynamicReloadingOfLayoutsDomainEvent extends ActionDomainEvent {
    }

    @Action(
            domainEvent = SwitchOffDynamicReloadingOfLayoutsDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-times"
    )
    @MemberOrder(sequence="500.400.3")
    public void switchOffDynamicReloadingOfLayouts() {
        objectLayoutMetadataService.toggleDynamicReloading();
    }
    public boolean hideSwitchOffDynamicReloadingOfLayouts() {
        return !objectLayoutMetadataService.isDynamicReloading();
    }



    // //////////////////////////////////////

    @Inject
    ObjectLayoutMetadataService objectLayoutMetadataService;

    @Inject
    JaxbService jaxbService;

    private SpecificationLoaderSpi specificationLoader;

    @Programmatic
    @Override
    public void setSpecificationLoaderSpi(final SpecificationLoaderSpi specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

}