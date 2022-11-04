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
package org.apache.causeway.applib;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.domain.DomainObjectList;
import org.apache.causeway.applib.mixins.dto.Dto_downloadXml;
import org.apache.causeway.applib.mixins.dto.Dto_downloadXsd;
import org.apache.causeway.applib.mixins.layout.Object_downloadLayout;
import org.apache.causeway.applib.mixins.metamodel.Object_downloadMetamodelXml;
import org.apache.causeway.applib.mixins.metamodel.Object_rebuildMetamodel;
import org.apache.causeway.applib.mixins.rest.Object_openRestApi;
import org.apache.causeway.applib.services.appfeatui.ApplicationFeatureMenu;
import org.apache.causeway.applib.services.appfeatui.ApplicationNamespace;
import org.apache.causeway.applib.services.appfeatui.ApplicationType;
import org.apache.causeway.applib.services.appfeatui.ApplicationTypeAction;
import org.apache.causeway.applib.services.appfeatui.ApplicationTypeCollection;
import org.apache.causeway.applib.services.appfeatui.ApplicationTypeMember;
import org.apache.causeway.applib.services.appfeatui.ApplicationTypeProperty;
import org.apache.causeway.applib.services.bookmark.BookmarkHolder_lookup;
import org.apache.causeway.applib.services.bookmark.BookmarkHolder_object;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.commanddto.conmap.ContentMappingServiceForCommandDto;
import org.apache.causeway.applib.services.commanddto.conmap.ContentMappingServiceForCommandsDto;
import org.apache.causeway.applib.services.commanddto.processor.spi.CommandDtoProcessorServiceIdentity;
import org.apache.causeway.applib.services.confview.ConfigurationMenu;
import org.apache.causeway.applib.services.confview.ConfigurationProperty;
import org.apache.causeway.applib.services.layout.LayoutServiceMenu;
import org.apache.causeway.applib.services.metamodel.MetaModelServiceMenu;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.session.SessionLogger;
import org.apache.causeway.applib.services.sitemap.SitemapServiceMenu;
import org.apache.causeway.applib.services.sudo.SudoService;
import org.apache.causeway.applib.services.user.ImpersonateMenu;
import org.apache.causeway.applib.services.user.ImpersonateStopMenu;
import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.services.userui.UserMenu;
import org.apache.causeway.schema.CausewayModuleSchema;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // Modules
    CausewayModuleSchema.class,

    // -- ViewModels
    ApplicationNamespace.class,
    ApplicationType.class,
    ApplicationTypeAction.class,
    ApplicationTypeCollection.class,
    ApplicationTypeMember.class,
    ApplicationTypeProperty.class,

    // @DomainObject(s)
    ConfigurationProperty.class,
    DomainObjectList.class,
    RoleMemento.class,
    UserMemento.class,

    // @DomainService(s)
    ClockService.class,
    ConfigurationMenu.class,
    LayoutServiceMenu.class,
    SitemapServiceMenu.class,
    ImpersonateMenu.class,
    ImpersonateStopMenu.class,
    MetaModelServiceMenu.class,
    QueryResultsCache.class,
    ApplicationFeatureMenu.class,
    UserMenu.class,

    // @Service(s)
    CommandDtoProcessorServiceIdentity.class,
    ContentMappingServiceForCommandDto.class,
    ContentMappingServiceForCommandsDto.class,
    SessionLogger.class,
    SudoService.class,
    UserService.class,
    UserMemento.UiSubscriber.class,
    RoleMemento.UiSubscriber.class,

    // Mixins (essential)
    BookmarkHolder_lookup.class,
    BookmarkHolder_object.class,

    // Mixins (prototyping)
    Dto_downloadXml.class,
    Dto_downloadXsd.class,
    Object_downloadLayout.class,
    Object_downloadMetamodelXml.class,
    Object_openRestApi.class,
    Object_rebuildMetamodel.class,

})
public class CausewayModuleApplib {

    public static final String NAMESPACE = "causeway.applib";
    public static final String NAMESPACE_CONF = "causeway.conf";    // for configuration; to minimize the risk of granting perms accidentally
    public static final String NAMESPACE_SUDO = "causeway.sudo";    // for impersonation
    public static final String NAMESPACE_FEAT = "causeway.feat";    // for app features

    // -- UI EVENT CLASSES

    public abstract static class TitleUiEvent<S>
    extends org.apache.causeway.applib.events.ui.TitleUiEvent<S> {}

    public abstract static class IconUiEvent<S>
    extends org.apache.causeway.applib.events.ui.IconUiEvent<S> {}

    public abstract static class CssClassUiEvent<S>
    extends org.apache.causeway.applib.events.ui.CssClassUiEvent<S> {}
    public abstract static class LayoutUiEvent<S>
    extends org.apache.causeway.applib.events.ui.LayoutUiEvent<S> {}

    // -- DOMAIN EVENT CLASSES

    public abstract static class ActionDomainEvent<S>
    extends org.apache.causeway.applib.events.domain.ActionDomainEvent<S> {}

    public abstract static class CollectionDomainEvent<S,T>
    extends org.apache.causeway.applib.events.domain.CollectionDomainEvent<S,T> {}

    public abstract static class PropertyDomainEvent<S,T>
    extends org.apache.causeway.applib.events.domain.PropertyDomainEvent<S,T> {}


}
