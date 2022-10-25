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
package org.apache.causeway.viewer.wicket.viewer.wicketapp.config;

import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;
import org.apache.causeway.viewer.wicket.ui.util.XrayWkt;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

/**
 * Activates visual debugging mode for the Wicket Viewer.
 * Use for troubleshooting and bug hunting.
 * <p>
 * Not imported by {@link CausewayModuleViewerWicketViewer}.
 */
@Configuration
public class WicketViewerXrayEnable
implements WicketApplicationInitializer {

    @Override
    public void init(final WebApplication webApplication) {
        XrayWkt.setEnabled(true);
        webApplication.getDebugSettings()
            .setOutputMarkupContainerClassName(true);

        //debug
        //replace the resource locator
//        val defaultResourceStreamLocator = webApplication.getResourceSettings()
//            .getResourceStreamLocator();
//        webApplication.getResourceSettings()
//            .setResourceStreamLocator(new XrayResourceStreamLocator(defaultResourceStreamLocator));
    }

//debug
//    @RequiredArgsConstructor
//    private static class XrayResourceStreamLocator
//    implements IResourceStreamLocator {
//        private final IResourceStreamLocator delegate;
//
//        @Override
//        public IResourceStream locate(final Class<?> clazz, final String path) {
//            return delegate.locate(clazz, path);
//        }
//
//        @Override
//        public IResourceStream locate(final Class<?> clazz, final String path,
//                final String style, final String variation, final Locale locale,
//                final String extension, final boolean strict) {
//            if(variation!=null) {
//                val result = delegate.locate(clazz, path, style, variation, locale, extension, strict);
//                System.err.printf("variation %s:%s->%b%n", clazz.getName(), variation, result!=null);
//            }
//
//            return delegate.locate(clazz, path, style, variation, locale, extension, strict);
//        }
//
//        @Override
//        public IResourceNameIterator newResourceNameIterator(final String path,
//                final Locale locale, final String style, final String variation,
//                final String extension, final boolean strict) {
//            return delegate.newResourceNameIterator(path, locale, style, variation, extension, strict);
//        }
//    }

}
