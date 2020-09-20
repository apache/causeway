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
package org.apache.isis.applib;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.applib.mixins.dto.Dto_downloadXml;
import org.apache.isis.applib.mixins.dto.Dto_downloadXsd;
import org.apache.isis.applib.mixins.layout.Object_downloadLayoutXml;
import org.apache.isis.applib.mixins.layout.Object_openRestApi;
import org.apache.isis.applib.mixins.layout.Object_rebuildMetamodel;
import org.apache.isis.applib.mixins.metamodel.Object_downloadMetamodelXml;
import org.apache.isis.applib.mixins.metamodel.Object_objectIdentifier;
import org.apache.isis.applib.mixins.metamodel.Object_objectType;
import org.apache.isis.applib.services.audit.AuditerServiceLogging;
import org.apache.isis.applib.services.bookmark.BookmarkHolder_lookup;
import org.apache.isis.applib.services.bookmark.BookmarkHolder_object;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.CommandService;
import org.apache.isis.applib.services.command.spi.CommandServiceListener;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.isis.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.isis.applib.services.commanddto.processor.spi.CommandDtoProcessorServiceIdentity;
import org.apache.isis.applib.services.confview.ConfigurationMenu;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.jaxb.JaxbServiceDefault;
import org.apache.isis.applib.services.layout.LayoutServiceMenu;
import org.apache.isis.applib.services.metamodel.MetaModelServiceMenu;
import org.apache.isis.applib.services.publish.PublisherServiceLogging;
import org.apache.isis.applib.services.session.SessionLoggingServiceLogging;
import org.apache.isis.schema.IsisModuleSchema;

@Configuration
@Import({
        // modules
        IsisModuleSchema.class,

        // mixins
        BookmarkHolder_lookup.class,
        BookmarkHolder_object.class,
        Dto_downloadXml.class,
        Dto_downloadXsd.class,
        Object_downloadLayoutXml.class,
        Object_downloadMetamodelXml.class,
        Object_objectIdentifier.class,
        Object_objectType.class,
        Object_openRestApi.class,
        Object_rebuildMetamodel.class,

        // @DomainService's
        ConfigurationMenu.class,
        LayoutServiceMenu.class,
        MetaModelServiceMenu.class,

        // @Service's
        AuditerServiceLogging.class,
        ClockService.class,
        CommandDtoProcessorServiceIdentity.class,
        CommandService.class,
        CommandServiceListener.Null.class,
        ContentMappingServiceForCommandDto.class,
        ContentMappingServiceForCommandsDto.class,
        InteractionContext.class,
        JaxbServiceDefault.class,
        PublisherServiceLogging.class,
        SessionLoggingServiceLogging.class,

})
public class IsisModuleApplib {

    // -- UI EVENT CLASSES

    public abstract static class TitleUiEvent<S>
    extends org.apache.isis.applib.events.ui.TitleUiEvent<S> {}

    public abstract static class IconUiEvent<S>
    extends org.apache.isis.applib.events.ui.IconUiEvent<S> {}

    public abstract static class CssClassUiEvent<S>
    extends org.apache.isis.applib.events.ui.CssClassUiEvent<S> {}
    public abstract static class LayoutUiEvent<S>
    extends org.apache.isis.applib.events.ui.LayoutUiEvent<S> {}

    // -- DOMAIN EVENT CLASSES

    public abstract static class ActionDomainEvent<S>
    extends org.apache.isis.applib.events.domain.ActionDomainEvent<S> {}

    public abstract static class CollectionDomainEvent<S,T>
    extends org.apache.isis.applib.events.domain.CollectionDomainEvent<S,T> {}

    public abstract static class PropertyDomainEvent<S,T>
    extends org.apache.isis.applib.events.domain.PropertyDomainEvent<S,T> {}


}
