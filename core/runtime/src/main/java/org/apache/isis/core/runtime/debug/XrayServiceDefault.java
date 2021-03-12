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
package org.apache.isis.core.runtime.debug;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.confview.ConfigurationProperty;
import org.apache.isis.applib.services.confview.ConfigurationViewService;
import org.apache.isis.commons.internal.debug.xray.XrayDataModel;
import org.apache.isis.commons.internal.debug.xray.XrayService;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.events.MetamodelEvent;

import lombok.val;

@Service
@Named("isis.runtime.XrayServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class XrayServiceDefault implements XrayService {

    @Inject private ConfigurationViewService configurationService;
 
    @EventListener(MetamodelEvent.class)
    public void onAppLifecycleEvent(final MetamodelEvent event) {
        if(event.isPostMetamodel()) {
            XrayUi.updateModel(model->{
                
                val root = model.getRootNode();
                
                val env = model.addDataNode(root, new XrayDataModel.KeyValue("Environment"));
                getEnvironment().forEach(item->{
                    env.getData().put(item.getKey(), item.getValue());    
                });
                
                val config = model.addDataNode(root, new XrayDataModel.KeyValue("Config"));
                getConfiguration().forEach(item->{
                    config.getData().put(item.getKey(), item.getValue());    
                });
                
            });
        }
    }
    
    private Set<ConfigurationProperty> getEnvironment(){
        return configurationService.getEnvironmentProperties();
    }
    
    private Set<ConfigurationProperty> getConfiguration(){
        return configurationService.getVisibleConfigurationProperties();
    }
    
}
