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
package demoapp.web.security;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.mixins.layout.Object_downloadLayoutXml;
import org.apache.isis.applib.mixins.layout.Object_openRestApi;
import org.apache.isis.applib.mixins.layout.Object_rebuildMetamodel;
import org.apache.isis.applib.mixins.metamodel.Object_downloadMetamodelXml;
import org.apache.isis.core.metamodel.inspect.Object_inspectMetamodel;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "demoapp.PrototypeActionsVisibilityAdvisor"
)
@DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.TERTIARY)
public class PrototypeActionsVisibilityAdvisor {

    @EventListener(Object_downloadMetamodelXml.ActionDomainEvent.class)
    public void on(Object_downloadMetamodelXml.ActionDomainEvent ev) {
        if(doNotShow) ev.hide();
    }

    @EventListener(Object_downloadLayoutXml.ActionDomainEvent.class)
    public void on(Object_downloadLayoutXml.ActionDomainEvent ev) {
        if(doNotShow) ev.hide();
    }

    @EventListener(Object_openRestApi.ActionDomainEvent.class)
    public void on(Object_openRestApi.ActionDomainEvent ev) {
        if(doNotShow) ev.hide();
    }

    @EventListener(Object_inspectMetamodel.ActionDomainEvent.class)
    public void on(Object_inspectMetamodel.ActionDomainEvent ev) {
        if(doNotShow) ev.hide();
    }

    @EventListener(Object_rebuildMetamodel.ActionDomainEvent.class)
    public void on(Object_rebuildMetamodel.ActionDomainEvent ev) {
        if(doNotShow) ev.hide();
    }


    private boolean doNotShow = false;

    @Action(restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(cssClassFa = "eye-slash")
    public void doNotShowPrototypeActions() { doNotShow = true; }
    public boolean hideDoNotShowPrototypeActions() { return doNotShow; }


    @Action(restrictTo = RestrictTo.PROTOTYPING)
    @ActionLayout(cssClassFa = "eye")
    public void showPrototypeActions() { doNotShow = false; }
    public boolean hideShowPrototypeActions() { return !doNotShow; }

}
