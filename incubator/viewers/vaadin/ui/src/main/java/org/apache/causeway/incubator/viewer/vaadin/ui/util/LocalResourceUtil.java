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
package org.apache.causeway.incubator.viewer.vaadin.ui.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import com.vaadin.flow.component.UI;

import org.apache.causeway.commons.internal.base._Strings;

import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class LocalResourceUtil {

    @Value(staticConstructor = "of")
    public static class ResourceDescriptor {
        private final String url;

        public static ResourceDescriptor webjars(String resourcePath) {
            return of("context://webjars/" + resourcePath);
        }

        public static ResourceDescriptor staticRoot(String resourcePath) {
            return of("context://" + resourcePath);
        }

    }

    public static void addStyleSheet(ResourceDescriptor resourceDescriptor) {
        UI.getCurrent().getPage().addStyleSheet(resourceDescriptor.getUrl());
    }

    public static void addJavaScript(ResourceDescriptor resourceDescriptor) {
        UI.getCurrent().getPage().addJavaScript(resourceDescriptor.getUrl());
    }

    public static void executeJavaScript(Supplier<InputStream> scriptResourceProvider) {
        UI.getCurrent().getPage().executeJs(_Strings.read(scriptResourceProvider.get(), StandardCharsets.UTF_8));
    }

}
