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
package org.apache.isis.applib.services.layout;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.base._Strings;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isisApplib.LayoutServiceMenu"
        )
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class LayoutServiceMenu {

    public static abstract class ActionDomainEvent
    extends IsisModuleApplib.ActionDomainEvent<LayoutServiceMenu> {}

    private final MimeType mimeTypeApplicationZip;

    public LayoutServiceMenu() {
        try {
            mimeTypeApplicationZip = new MimeType("application", "zip");
        } catch (final MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class DownloadLayoutsDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DownloadLayoutsDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Object Layouts (ZIP)"
            )
    @MemberOrder(sequence="500.400.1")
    public Blob downloadLayouts(final LayoutService.Style style) {

        final String fileName = "layouts." + style.name().toLowerCase() + ".zip";

        final byte[] zipBytes = layoutService.toZip(style);
        return new Blob(fileName, mimeTypeApplicationZip, zipBytes);
    }

    public LayoutService.Style default0DownloadLayouts() {
        return LayoutService.Style.NORMALIZED;
    }

    public static class DownloadMenuBarsLayoutDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DownloadMenuBarsLayoutDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Menu Bars Layout (XML)"
            )
    @MemberOrder(sequence="500.400.2")
    public Clob downloadMenuBarsLayout(
            @ParameterLayout(named = "File name") final String fileName,
            final MenuBarsService.Type type) {

        final String xml = layoutService.toMenuBarsXml(type);

        return new Clob(_Strings.asFileNameWithExtension(fileName,  ".xml"), "text/xml", xml);
    }

    public String default0DownloadMenuBarsLayout() {
        return "menubars.layout.xml";
    }

    public MenuBarsService.Type default1DownloadMenuBarsLayout() {
        return MenuBarsService.Type.DEFAULT;
    }


    // //////////////////////////////////////


    @Inject LayoutService layoutService;


}