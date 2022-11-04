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
package org.apache.causeway.applib.services.layout;

import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

/**
 * Provides a UI to allow layouts (obtained from {@link LayoutService}) to be downloaded.
 *
 * @since 1.x {@index}
 */
@Named(LayoutServiceMenu.LOGICAL_TYPE_NAME)
@DomainService()
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class LayoutServiceMenu {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".LayoutServiceMenu";

    public static abstract class ActionDomainEvent<T> extends CausewayModuleApplib.ActionDomainEvent<T> {}

    private final LayoutService layoutService;
    private final MimeType mimeTypeApplicationZip;

    public LayoutServiceMenu(final LayoutService layoutService) {
        this.layoutService = layoutService;
        try {
            mimeTypeApplicationZip = new MimeType("application", "zip");
        } catch (final MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }


    @Action(
            domainEvent = downloadLayouts.ActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Object Layouts (ZIP)",
            sequence="500.400.1")
    public class downloadLayouts{

        public class ActionDomainEvent extends LayoutServiceMenu.ActionDomainEvent<downloadLayouts> {}

        @MemberSupport public Blob act(final LayoutExportStyle style, final CommonMimeType format) {
            final String fileName = "layouts." + style.name().toLowerCase() + ".zip";
            final byte[] zipBytes = layoutService.toZip(style, format);
            return new Blob(fileName, mimeTypeApplicationZip, zipBytes);
        }

        @MemberSupport public LayoutExportStyle default0Act() {
            return LayoutExportStyle.defaults(); }
        @MemberSupport public CommonMimeType default1Act() {
            return layoutService.supportedObjectLayoutFormats().iterator().next(); }
        @MemberSupport public Set<CommonMimeType> choices1Act() {
            return layoutService.supportedObjectLayoutFormats(); }
    }


    @Action(
            domainEvent = downloadMenuBarsLayout.ActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Menu Bars Layout",
            sequence="500.400.2")
    public class downloadMenuBarsLayout{

        public class ActionDomainEvent extends LayoutServiceMenu.ActionDomainEvent<downloadMenuBarsLayout> {}

        @MemberSupport public Clob act(
                @ParameterLayout(named = "File name (no need to add the file extension)")
                final String fileName,
                final MenuBarsService.Type type,
                final CommonMimeType format) {

            final String serializedLayout = layoutService.menuBarsLayout(type, format);
            return Clob.of(fileName, format, serializedLayout);
        }

        @MemberSupport public String default0Act() { return "menubars.layout"; }
        @MemberSupport public MenuBarsService.Type default1Act() { return MenuBarsService.Type.DEFAULT; }
        @MemberSupport public CommonMimeType default2Act() {
            return layoutService.supportedMenuBarsLayoutFormats().iterator().next(); }
        @MemberSupport public Set<CommonMimeType> choices2Act() {
            return layoutService.supportedMenuBarsLayoutFormats(); }
    }

}
