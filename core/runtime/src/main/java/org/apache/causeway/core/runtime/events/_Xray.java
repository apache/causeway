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
package org.apache.causeway.core.runtime.events;

import org.apache.causeway.applib.services.confview.ConfigurationViewService;
import org.apache.causeway.applib.services.confview.ConfigurationViewService.Scope;
import org.apache.causeway.commons.internal.debug.xray.XrayDataModel;
import org.apache.causeway.commons.internal.debug.xray.XrayModel.Stickiness;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;

final class _Xray {

    static void addConfiguration(final ConfigurationViewService configurationService) {

        XrayUi.updateModel(model->{

            var root = model.getRootNode();

            var env = model.addDataNode(
                    root, new XrayDataModel.KeyValue("causeway-env", "Environment", Stickiness.CANNOT_DELETE_NODE));
            configurationService.getConfigurationProperties(Scope.ENV).forEach(item->{
                env.getData().put(item.getKey(), item.getValue());
            });

            var config = model.addDataNode(
                    root, new XrayDataModel.KeyValue("causeway-conf", "Config", Stickiness.CANNOT_DELETE_NODE));
            configurationService.getConfigurationProperties(Scope.PRIMARY).forEach(item->{
                config.getData().put(item.getKey(), item.getValue());
            });

        });

    }

}
