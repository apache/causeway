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

import com.google.inject.AbstractModule;

import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassList;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.viewer.imagecache.ImageCacheClassPath;
import org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistrarDefault;
import org.apache.isis.viewer.wicket.viewer.registries.components.ComponentFactoryRegistryDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassListDefault;
import org.apache.isis.viewer.wicket.viewer.registries.pages.PageClassRegistryDefault;

public class IsisWicketModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ComponentFactoryRegistry.class).to(ComponentFactoryRegistryDefault.class);
        bind(PageClassRegistry.class).to(PageClassRegistryDefault.class);
        bind(PageClassList.class).to(PageClassListDefault.class);
        bind(ComponentFactoryRegistrar.class).to(ComponentFactoryRegistrarDefault.class);
        bind(ImageResourceCache.class).to(ImageCacheClassPath.class);
        bindConstant().annotatedWith(ApplicationCssUrl.class).to("application.css");
    }

}