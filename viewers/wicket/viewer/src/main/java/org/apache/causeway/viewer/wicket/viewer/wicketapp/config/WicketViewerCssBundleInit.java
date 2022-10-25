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

import java.util.Set;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.resource.CssResourceReference;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.viewer.wicket.model.causeway.WicketApplicationInitializer;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.string.MultiLineStringPanel;
import org.apache.causeway.viewer.wicket.ui.panels.PanelUtil;
import org.apache.causeway.viewer.wicket.viewer.wicketapp.CausewayWicketApplication;

@Configuration
public class WicketViewerCssBundleInit implements WicketApplicationInitializer {

    @Override
    public void init(final WebApplication webApplication) {

        // get the css for all components built by component factories
        final Set<CssResourceReference> cssReferences = cssResourceReferencesForAllComponents();

        // some additional special cases.
        addSpecialCasesToCssBundle(cssReferences);

        // create the bundle
        webApplication.getResourceBundles()
        .addCssBundle(
                CausewayWicketApplication.class,
                "causeway-wicket-viewer-bundle.css",
                cssReferences.toArray(new CssResourceReference[]{}));

    }

    // -- HELPER

    private Set<CssResourceReference> cssResourceReferencesForAllComponents() {
        // TODO mgrigorov: CAUSEWAY-537 temporary disabled to not mess up with Bootstrap styles
        //        Collection<ComponentFactory> componentFactories = getComponentFactoryRegistry().listComponentFactories();
        return _Sets.newLinkedHashSet(
                //                Iterables.concat(
                //                        Iterables.transform(
                //                                componentFactories,
                //                                getCssResourceReferences))
                );
    }

    /**
     * Additional special cases to be included in the main CSS bundle.
     * <p>
     * These are typically either super-classes or components
     * that don't have their own ComponentFactory, or
     * for {@link ComponentFactory}s (such as <tt>StringPanelFactory</tt>)
     * that don't quite follow the usual pattern
     * (because they create different types of panels).
     * <p>
     * Note that it doesn't really matter if we miss one or two;
     * their CSS will simply be served up individually.
     */
    private void addSpecialCasesToCssBundle(final Set<CssResourceReference> references) {

        // abstract classes

        // ... though it turns out we cannot add this particular one to the bundle, because
        // it has CSS image links intended to be resolved relative to LinksSelectorPanelAbstract.class.
        // Adding them into the bundle would mean these CSS links are resolved relative to CausewayWicketApplication.class
        // instead.
        // references.add(PanelUtil.cssResourceReferenceFor(LinksSelectorPanelAbstract.class));

        // components without factories
        references.add(PanelUtil.cssResourceReferenceFor(AdditionalLinksPanel.class));

        // non-conforming component factories
        references.add(PanelUtil.cssResourceReferenceFor(MultiLineStringPanel.class));
    }

}
