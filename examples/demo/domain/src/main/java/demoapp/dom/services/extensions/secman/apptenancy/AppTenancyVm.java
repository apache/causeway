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
package demoapp.dom.services.extensions.secman.apptenancy;

import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.PromptStyle;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.services.extensions.secman.apptenancy.entities.TenantedJdo;
import demoapp.dom.services.extensions.secman.apptenancy.entities.TenantedJdoEntities;

//tag::class[]
@XmlRootElement(name = "root")
@XmlType()
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(
        nature=Nature.VIEW_MODEL
        , objectType = "demo.SecManVm"
)
public class AppTenancyVm implements HasAsciiDocDescription {

    public String title() {
        return "Tenancy demo";
    }

    public List<TenantedJdo> getTenantedEntities() {
        return tenantedJdoEntities.all();
    }

    //tag::hideRegex[]
    public String getHideRegex() {
        return applicationTenancyEvaluatorForDemo.getHideRegex();
    }
    @Action(associateWith = "hideRegex")
    @ActionLayout(promptStyle = PromptStyle.INLINE_AS_IF_EDIT)
    public AppTenancyVm updateHideRegex(
            @Parameter(optionality = Optionality.OPTIONAL)
            String regex) {
        applicationTenancyEvaluatorForDemo.setHideRegex(regex);
        return this;
    }
    public String default0UpdateHideRegex() {
        return getHideRegex();
    }
    //end::hideRegex[]

    //tag::disableRegex[]
    public String getDisableRegex() {
        return applicationTenancyEvaluatorForDemo.getDisableRegex();
    }
    @Action(associateWith = "disableRegex")
    @ActionLayout(promptStyle = PromptStyle.INLINE_AS_IF_EDIT)
    public AppTenancyVm updateDisableRegex(
            @Parameter(optionality = Optionality.OPTIONAL)
            String regex) {
        applicationTenancyEvaluatorForDemo.setDisableRegex(regex);
        return this;
    }
    public String default0UpdateDisableRegex() {
        return getDisableRegex();
    }
    //end::disableRegex[]


    @Inject @XmlTransient
    TenantedJdoEntities tenantedJdoEntities;

    @Inject @XmlTransient
    ApplicationTenancyEvaluatorForDemo applicationTenancyEvaluatorForDemo;

}
//end::class[]
