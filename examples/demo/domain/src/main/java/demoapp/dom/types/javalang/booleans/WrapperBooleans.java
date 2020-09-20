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
package demoapp.dom.types.javalang.booleans;

import java.util.List;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.extern.log4j.Log4j2;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom.types.javalang.booleans.jdo.WrapperBooleanJdo;
import demoapp.dom.types.javalang.booleans.jdo.WrapperBooleanJdoEntities;
import demoapp.dom.types.javalang.booleans.vm.WrapperBooleanVm;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.WrapperBooleans", editing=Editing.ENABLED)
@Log4j2
public class WrapperBooleans implements HasAsciiDocDescription {

    public String title() {
        return "Boolean (wrapper) data type";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public WrapperBooleanVm openViewModel(Boolean initialValue) {
        return new WrapperBooleanVm(initialValue);
    }
    public Boolean default0OpenViewModel() {
        return true;
    }

    @Collection
    public List<WrapperBooleanJdo> getEntities() {
        return entities.all();
    }

    @Inject
    @XmlTransient
    WrapperBooleanJdoEntities entities;


    //FIXME[ISIS-2387]
    @Action
    @ActionLayout(
            promptStyle = PromptStyle.DIALOG_MODAL
            , describedAs = "FIXME[ISIS-2387] even though primitive1 gets initialized with true, the model thinks its null"
    )
    public WrapperBooleans booleanParams(
            boolean primitive0,
            boolean primitive1) {
        return this;
    }
    @Model
    public boolean default1BooleanParams() {
        return true;
    }

    //FIXME[ISIS-2387]
    @Action
    @ActionLayout(
            promptStyle = PromptStyle.DIALOG_MODAL
            , describedAs =
            "FIXME[ISIS-2387] contrary to the above, second parameter works; " +
                    "however, first parameter is however, first parameter is initialized to null but not " +
                    "rendered as 3-state.  Either we fix rendering or we" +
                    " initialize with FALSE when null"
    )
    public WrapperBooleans booleanBoxedParams(
            Boolean boxed0,
            Boolean boxed1) {
        return this;
    }
    @Model
    public Boolean default1BooleanBoxedParams() {
        return true;
    }


}
