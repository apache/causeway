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
package org.apache.causeway.viewer.wicket.viewer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.viewer.wicket.ui.CausewayModuleViewerWicketUi;
import org.apache.causeway.viewer.wicket.viewer.registries.components.ComponentFactoryRegistrarDefault;
import org.apache.causeway.viewer.wicket.viewer.registries.components.ComponentFactoryRegistryDefault;
import org.apache.causeway.viewer.wicket.viewer.registries.pages.PageClassListDefault;
import org.apache.causeway.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault;
import org.apache.causeway.viewer.wicket.viewer.registries.pages.PageNavigationServiceDefault;
import org.apache.causeway.viewer.wicket.viewer.services.BookmarkUiServiceWicket;
import org.apache.causeway.viewer.wicket.viewer.services.DeepLinkServiceWicket;
import org.apache.causeway.viewer.wicket.viewer.services.HintStoreUsingWicketSession;
import org.apache.causeway.viewer.wicket.viewer.services.ImageResourceCacheClassPath;
import org.apache.causeway.viewer.wicket.viewer.services.TranslationsResolverWicket;
import org.apache.causeway.viewer.wicket.viewer.webmodule.WebModuleWicket;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.config.BootstrapInitWkt;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.config.DebugInitWkt;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.config.JQueryInitWkt;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.config.Select2InitWkt;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.config.WebjarsInitWkt;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.config.WicketViewerCssBundleInit;

/**
 * @since 1.x {@index}
 */
@Configuration
@Import({
        // Modules
        CausewayModuleViewerWicketUi.class,

        // @Configuration's
        BootstrapInitWkt.class,
        JQueryInitWkt.class,
        Select2InitWkt.class,
        WebjarsInitWkt.class,
        WicketViewerCssBundleInit.class,
        DebugInitWkt.class,

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

})
public class CausewayModuleViewerWicketViewer {

    public static final String NAMESPACE = "causeway.viewer.wicket";
}
