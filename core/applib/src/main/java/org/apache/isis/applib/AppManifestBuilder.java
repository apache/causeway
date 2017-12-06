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

import java.util.List;
import java.util.Map;
import java.util.Set;

interface AppManifestBuilder<T> {

    T withAdditionalModules(final Class<?>... modules);

    T withAdditionalModules(final List<Class<?>> modules);

    T withAdditionalServices(final Class<?>... additionalServices);

    T withAdditionalServices(final List<Class<?>> additionalServices);

    T withConfigurationProperties(final Map<String,String> configurationProperties);

    T withConfigurationPropertiesFile(final String propertiesFile);

    T withConfigurationPropertiesFile(
            final Class<?> propertiesFileContext, final String propertiesFile, final String... furtherPropertiesFiles);

    T withConfigurationProperty(final String key, final String value);


    List<Class<?>> getAllModulesAsClass();

    Set<Class<?>> getAllAdditionalServices();

    List<AppManifestAbstract.PropertyResource> getAllPropertyResources();

    List<AppManifestAbstract.ConfigurationProperty> getAllIndividualConfigProps();
}