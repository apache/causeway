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
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.DomainServiceLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.RestrictTo;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.base._Strings;

/**
 * Simply provides a UI to allow layouts (obtained from {@link LayoutService}
 * to be downloaded within the UI.
 *
 * @since 1.x {@index}
 */
@Named(LayoutServiceMenu.LOGICAL_TYPE_NAME)
@DomainService(logicalTypeName = LayoutServiceMenu.LOGICAL_TYPE_NAME)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class LayoutServiceMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".LayoutServiceMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleApplib.ActionDomainEvent<T> {}

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
            domainEvent = downloadLayouts.ActionEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Object Layouts (ZIP)",
            sequence="500.400.1")
    public class downloadLayouts{

        public class ActionEvent extends ActionDomainEvent<downloadLayouts> {}

        @MemberSupport public Blob act(final Style style) {

            final String fileName = "layouts." + style.name().toLowerCase() + ".zip";

            final byte[] zipBytes = layoutService.toZip(style);
            return new Blob(fileName, mimeTypeApplicationZip, zipBytes);
        }

        @MemberSupport public Style default0Act() { return Style.NORMALIZED; }
    }



    @Action(
            domainEvent = downloadMenuBarsLayout.ActionEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download Menu Bars Layout (XML)",
            sequence="500.400.2")
    public class downloadMenuBarsLayout{

        public class ActionEvent extends ActionDomainEvent<downloadMenuBarsLayout> {}

        @MemberSupport public Clob act(
                @ParameterLayout(named = "File name") final String fileName,
                final MenuBarsService.Type type) {

            final String xml = layoutService.toMenuBarsXml(type);

            return new Clob(_Strings.asFileNameWithExtension(fileName,  ".xml"), "text/xml", xml);
        }

        @MemberSupport public String default0Act() { return "menubars.layout.xml"; }
        @MemberSupport public MenuBarsService.Type default1Act() { return MenuBarsService.Type.DEFAULT; }
    }

}
