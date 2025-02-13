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
package org.apache.causeway.viewer.wicket.ui.util;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.causeway.viewer.commons.prism.PrismLanguage;
import org.apache.causeway.viewer.commons.prism.PrismTheme;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.webjars.request.resource.WebjarsCssResourceReference;
import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

@UtilityClass
public final class PrismResourcesWkt{

    /**
     * Returns the main Prism CSS resource for selected theme
     */
    public List<CssResourceReference> cssResources(final PrismTheme theme) {
        return theme.cssFiles().stream()
                .map(WebjarsCssResourceReference::new)
                .collect(Collectors.toList());
    }

    /**
     * Returns the main Prism JS resource
     */
    public JavaScriptResourceReference jsResourceMain() {
        return new WebjarsJavaScriptResourceReference("prism/prism.js");
    }
    
    /**
     * Returns the Prism JS resources for selected language
     */
    public JavaScriptResourceReference jsResource(String languageId) {
        return new WebjarsJavaScriptResourceReference(new PrismLanguage(languageId).jsFile());
    }
    
    @SneakyThrows
    public Optional<String> read(JavaScriptResourceReference jsRef) {
        var resourceStream = jsRef.getResource().getResourceStream();
        return resourceStream!=null 
            ? Optional.of(new String(resourceStream.getInputStream().readAllBytes()))
            : Optional.empty();
    }

}
