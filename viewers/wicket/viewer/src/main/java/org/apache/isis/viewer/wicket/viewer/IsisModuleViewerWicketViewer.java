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
package org.apache.isis.viewer.wicket.viewer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.viewer.wicket.ui.IsisModuleViewerWicketUi;
import org.apache.isis.viewer.wicket.viewer.mixins.Object_clearHints;
import org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistrarDefault;
import org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistryDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageNavigationServiceDefault;
import org.apache.isis.viewer.wicket.viewer.services.BookmarkUiServiceWicket;
import org.apache.isis.viewer.wicket.viewer.services.DeepLinkServiceWicket;
import org.apache.isis.viewer.wicket.viewer.services.HintStoreUsingWicketSession;
import org.apache.isis.viewer.wicket.viewer.services.ImageResourceCacheClassPath;
import org.apache.isis.viewer.wicket.viewer.services.TranslationsResolverWicket;
import org.apache.isis.viewer.wicket.viewer.services.WicketViewerSettingsDefault;
import org.apache.isis.viewer.wicket.viewer.webmodule.WebModuleWicket;

/**
 * @since 1.x {@index}
 */
@Configuration
@Import({
        // modules
        IsisModuleViewerWicketUi.class,

        // @Service's
        BookmarkUiServiceWicket.class,
        ComponentFactoryRegistrarDefault.class,
        ComponentFactoryRegistryDefault.class,
        DeepLinkServiceWicket.class,
        ImageResourceCacheClassPath.class,
        HintStoreUsingWicketSession.class,
        PageClassListDefault.class,
        PageClassRegistryDefault.class,
        PageNavigationServiceDefault.class,
        TranslationsResolverWicket.class,
        WebModuleWicket.class,
        WicketViewerSettingsDefault.class,

        // @Mixin's
        Object_clearHints.class,

})
public class IsisModuleViewerWicketViewer {

    public static final String NAMESPACE = "isis.viewer.wicket";
}
