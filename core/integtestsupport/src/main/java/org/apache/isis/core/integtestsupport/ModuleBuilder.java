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
package org.apache.isis.core.integtestsupport;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.Module;
import org.apache.isis.core.integtestsupport.components.HeadlessTransactionSupportDefault;

/**
 * Provides the ability to wrap or extend a {@link Module} with additional services/configuration to allow the
 * Module be used for bootstrapping integration tests.
 */
public class ModuleBuilder {

    public static ModuleBuilder create(Module module) {
        return new ModuleBuilder(module);
    }

    final Module module;
    ModuleBuilder(Module module) {
        this.module = module;
    }

    public Module build() {
        return module;
    }

    /**
     * Registers HeadlessTransactionSupportDefault as an additional service.
     */
    public ModuleBuilder withHeadlessTransactionSupport() {
        module.getAdditionalServices().add(HeadlessTransactionSupportDefault.class);
        return this;
    }

    /**
     * Adds default fallback configuration values for integration tests,
     * without overriding any existing key value pairs.
     */
    public ModuleBuilder withIntegrationTestConfigFallback() {
        return withIntegrationTestConfigFallback(AppManifest.Util.MemDb.HSQLDB);
    }
    public ModuleBuilder withIntegrationTestConfigFallback(AppManifest.Util.MemDb memDb) {
        final Map<String, String> map = new HashMap<>();
        memDb.withProperties(map);
        AppManifest.Util.withDataNucleusProperties(map);
        AppManifest.Util.withIsisIntegTestProperties(map);

        map.forEach((k, v)-> module.getFallbackConfigProps().putIfAbsent(k, v));
        return this;
    }
}
