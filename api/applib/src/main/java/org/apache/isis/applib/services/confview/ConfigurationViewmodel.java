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
package org.apache.isis.applib.services.confview;

import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.ObjectSupport;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        logicalTypeName = ConfigurationViewmodel.LOGICAL_TYPE_NAME)
public class ConfigurationViewmodel {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE_CONF + ".ConfigurationViewmodel";

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
