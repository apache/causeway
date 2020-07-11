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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.val;

import demoapp.dom.viewmodels.jaxbrefentity.StatefulVmJaxbRefsEntity;
import demoapp.dom.viewmodels.usingjaxb.StatefulVmUsingJaxb;

@DomainService(
        objectType = "demo.ViewModelMenu"
)
@DomainServiceLayout(named = "View Models")
public class ViewModelMenu {

    @Action(semantics = SemanticsOf.SAFE)
    public StatefulVmUsingJaxb stateful(final String message) {
        val viewModel = new StatefulVmUsingJaxb();
        viewModel.setMessage(message);
        return viewModel;
    }
    public String default0Stateful() {
        return "Some initial state";
    }

    @Action(semantics = SemanticsOf.SAFE)
    public StatefulVmJaxbRefsEntity statefulRefsEntity(final String message) {
        val viewModel = new StatefulVmJaxbRefsEntity();
        viewModel.setMessage(message);
        return viewModel;
    }
    public String default0StatefulRefsEntity() {
        return "Some initial state";
    }

}
