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
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.types.javalang.booleans.persistence.WrapperBooleanEntity;
import demoapp.dom.types.javalang.booleans.vm.WrapperBooleanVm;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, logicalTypeName = "demo.WrapperBooleans", editing=Editing.ENABLED)
//@Log4j2
public class WrapperBooleans implements HasAsciiDocDescription {

    @ObjectSupport public String title() {
        return "Boolean (wrapper) data type";
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(promptStyle = PromptStyle.DIALOG_MODAL)
    public WrapperBooleanVm openViewModel(final Boolean initialValue) {
        return new WrapperBooleanVm(initialValue);
    }
    @MemberSupport public Boolean default0OpenViewModel() {
        return true;
    }

    @Collection
    public List<? extends WrapperBooleanEntity> getEntities() {
        return entities.all();
    }

    @Inject
    @XmlTransient
    ValueHolderRepository<Boolean, ? extends WrapperBooleanEntity> entities;


    //FIXME[ISIS-2387]
    @Action
    @ActionLayout(
            promptStyle = PromptStyle.DIALOG_MODAL
            , describedAs = "FIXME[ISIS-2387] even though primitive1 gets initialized with true, the model thinks its null"
    )
    public WrapperBooleans booleanParams(
            final boolean primitive0,
            final boolean primitive1) {
        return this;
    }
    @MemberSupport public boolean default1BooleanParams() {
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
            final Boolean boxed0,
            final Boolean boxed1) {
        return this;
    }
    @MemberSupport public Boolean default1BooleanBoxedParams() {
        return true;
    }


}
