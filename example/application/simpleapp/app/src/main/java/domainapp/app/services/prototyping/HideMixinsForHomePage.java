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
package domainapp.app.services.prototyping;

import java.util.List;

import com.google.common.eventbus.Subscribe;

import org.apache.isis.applib.AbstractSubscriber;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.layout.Object_downloadLayoutXml;
import org.apache.isis.applib.services.layout.Object_rebuildMetamodel;

import domainapp.app.services.homepage.HomePageViewModel;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class HideMixinsForHomePage extends AbstractSubscriber{

    @Subscribe
    public void on(Object_rebuildMetamodel.ActionDomainEvent ev) {
        final List<Object> arguments = ev.getArguments();
        if(arguments.get(0) instanceof HomePageViewModel) {
            ev.hide();
        }
    }

    @Subscribe
    public void on(Object_downloadLayoutXml.ActionDomainEvent ev) {
        final List<Object> arguments = ev.getArguments();
        if(arguments.get(0) instanceof HomePageViewModel) {
            ev.hide();
        }
    }
}
