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
package demoapp.dom.viewmodels;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import demoapp.dom._infra.HasAsciiDocDescription;
import demoapp.dom.viewmodels.jaxbrefentity.StatefulViewModelJaxbRefsEntity;
import demoapp.dom.viewmodels.usingjaxb.StatefulViewModelUsingJaxb;

@DomainService(
        objectType = "demoapp.ViewModels"
)
public class ViewModels {

    @Action(semantics = SemanticsOf.SAFE)
    public StatefulViewModelUsingJaxb openStateful(final String message) {
        val viewModel = new StatefulViewModelUsingJaxb();
        viewModel.setMessage(message);
        return viewModel;
    }
    public String default0OpenStateful() {
        return "Some initial state";
    }

    @Action(semantics = SemanticsOf.SAFE)
    public StatefulViewModelJaxbRefsEntity openStatefulRefsEntity(final String message) {
        val viewModel = new StatefulViewModelJaxbRefsEntity();
        viewModel.setMessage(message);
        return viewModel;
    }
    public String default0OpenStatefulRefsEntity() {
        return "Some initial state";
    }

}
