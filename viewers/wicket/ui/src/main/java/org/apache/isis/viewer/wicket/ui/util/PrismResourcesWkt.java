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
package org.apache.isis.viewer.wicket.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.request.resource.ResourceReference;

import org.apache.isis.viewer.commons.prism.Prism;
import org.apache.isis.viewer.commons.prism.PrismLanguage;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class PrismResourcesWkt{

    @Getter(lazy = true) private static final ResourceReference cssResourceReferenceWkt =
            new WebjarsCssResourceReference(Prism.COY.cssFile());

    @Getter(lazy = true) private static final List<ResourceReference> jsResourceReferencesWkt =
            assembleJsResources();

    // -- HELPER

    /**
     * Returns the main Prism JS resource with theme 'coy' + most common languages
     */
    private List<ResourceReference> assembleJsResources() {
        final List<ResourceReference> resources = PrismLanguage.mostCommon().stream()
                .map(PrismLanguage::jsFile)
                .map(WebjarsJavaScriptResourceReference::new)
                .collect(Collectors.toCollection(ArrayList::new));

        resources.add(0, new WebjarsJavaScriptResourceReference(Prism.COY.jsFile()));
        return resources;
    }

}
