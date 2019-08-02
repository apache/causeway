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
package org.apache.isis.runtime.services.confmenu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.confview.ConfigurationProperty;
import org.apache.isis.applib.services.confview.ConfigurationViewService;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;

/**
 * @since 2.0
 */
@DomainService(
        nature = NatureOfService.DOMAIN
        )
public class ConfigurationViewServiceDefault implements ConfigurationViewService {

    @Override
    public Set<ConfigurationProperty> allProperties() {
        return new TreeSet<>(config.get().values());
    }

    // -- HELPER

    private _Lazy<Map<String, ConfigurationProperty>> config = _Lazy.of(this::loadConfiguration);

    private Map<String, ConfigurationProperty> loadConfiguration() {

        final Map<String, ConfigurationProperty> map = new HashMap<>();

        _Config.getConfiguration().copyToMap().forEach((k, v)->add(k, v, map));

        // for convenience add some additional info to the top ...
        add("[ Isis Version ]", IsisConfiguration.getVersion(), map);
        add("[ Deployment Type ]", _Context.getEnvironment().getDeploymentType().name(), map);
        add("[ Unit Testing ]", ""+_Context.getEnvironment().isUnitTesting(), map);

        return map;
    }

    private static void add(String key, String value, Map<String, ConfigurationProperty> map) {

        value = ConfigurationConstants.maskIfProtected(key, value);

        map.put(key, new ConfigurationProperty(key, value));
    }

}
