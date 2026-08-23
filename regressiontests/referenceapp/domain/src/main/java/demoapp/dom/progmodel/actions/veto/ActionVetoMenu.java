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
package demoapp.dom.progmodel.actions.veto;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.factory.FactoryService;

import demoapp.dom.progmodel.actions.TvCharacterPopulator;
import lombok.RequiredArgsConstructor;

@Named("demo.ActionVetoMenu")
@DomainService
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class ActionVetoMenu {

    final FactoryService factoryService;
    final TvCharacterPopulator tvCharacterPopulator;

    @Action
    @ActionLayout(cssClassFa="fa-ban")
    public ActionVetoPage veto(){
        var page = factoryService.viewModel(new ActionVetoPage());
        tvCharacterPopulator.populate(page.getTvCharacters());
        return page;
    }

}
