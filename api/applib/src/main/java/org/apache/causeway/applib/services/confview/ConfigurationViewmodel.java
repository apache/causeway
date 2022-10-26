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
package org.apache.causeway.applib.services.confview;

import java.util.Collections;
import java.util.Set;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;

/**
 * As returned by {@link ConfigurationMenu.configuration#act() ConfigurationMenu}.
 *
 * @since 2.0 {@index}
 */
@Named(ConfigurationViewmodel.LOGICAL_TYPE_NAME)
@DomainObject(
        nature = Nature.VIEW_MODEL)
public class ConfigurationViewmodel {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE_CONF + ".ConfigurationViewmodel";

    @Autowired(required = false)
    private ConfigurationViewService configurationService;

    @ObjectSupport public String title() {
        return "Configuration";
    }

    @Collection
    public Set<ConfigurationProperty> getEnvironment(){
        return configurationService!=null
                ? configurationService.getEnvironmentProperties()
                : Collections.emptySet();
    }

    @Collection
    public Set<ConfigurationProperty> getConfiguration(){
        return configurationService!=null
                ? configurationService.getVisibleConfigurationProperties()
                : Collections.emptySet();
    }

}
