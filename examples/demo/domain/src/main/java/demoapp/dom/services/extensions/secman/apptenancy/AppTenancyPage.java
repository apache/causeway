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
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PromptStyle;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.services.extensions.secman.apptenancy.persistence.TenantedEntity;

import lombok.NoArgsConstructor;

//tag::class[]
@Named("demo.AppTenancyPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-home")
@XmlRootElement(name = "root")
@XmlType()
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class AppTenancyPage implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "Tenancy demo";
    }

    public List<? extends TenantedEntity> getTenantedEntities() {
        return tenantedEntities.all();
    }

//tag::hideRegex[]
    public String getHideRegex() {
        return applicationTenancyEvaluatorForDemo.getHideRegex();
    }
    @Action()
    @ActionLayout(
            associateWith = "hideRegex",
            promptStyle = PromptStyle.INLINE_AS_IF_EDIT)
    public AppTenancyPage updateHideRegex(
            @Parameter(optionality = Optionality.OPTIONAL) final
            String regex) {
        applicationTenancyEvaluatorForDemo.setHideRegex(regex);
        return this;
    }
    @MemberSupport public String default0UpdateHideRegex() {
        return getHideRegex();
    }
//end::hideRegex[]

//tag::disableRegex[]
    public String getDisableRegex() {
        return applicationTenancyEvaluatorForDemo.getDisableRegex();
    }
    @Action()
    @ActionLayout(
            associateWith = "disableRegex",
            promptStyle = PromptStyle.INLINE_AS_IF_EDIT)
    public AppTenancyPage updateDisableRegex(
            @Parameter(optionality = Optionality.OPTIONAL) final
            String regex) {
        applicationTenancyEvaluatorForDemo.setDisableRegex(regex);
        return this;
    }
    @MemberSupport public String default0UpdateDisableRegex() {
        return getDisableRegex();
    }
//end::disableRegex[]


    @Inject @XmlTransient
    ValueHolderRepository<String, ? extends TenantedEntity> tenantedEntities;

    @Inject @XmlTransient
    ApplicationTenancyEvaluatorForDemo applicationTenancyEvaluatorForDemo;

}
//end::class[]
