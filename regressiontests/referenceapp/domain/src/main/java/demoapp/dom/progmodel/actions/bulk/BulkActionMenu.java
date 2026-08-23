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
package demoapp.dom.progmodel.actions.bulk;

import java.util.Arrays;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;

@Named("demo.BulkActionMenu")
@DomainService
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class BulkActionMenu {

    private static final List<String> FRIENDS_NAMES =
            Arrays.asList("Joey", "Monica", "Rachel", "Phoebe", "Chandler", "Ross");

    final FactoryService factoryService;
    final BulkActionItemEntityRepository repository;

    @Action
    @ActionLayout(
            cssClassFa="fa-bolt",
            describedAs = "Bulk actions"
    )
    public BulkActionPage bulkActions() {
        var page = factoryService.viewModel(new BulkActionPage());
        repository.all()
                .stream()
                .filter(x -> FRIENDS_NAMES.contains(x.getName()))
                .forEach(x -> page.getAmericanCharacters().add(x));
        repository.all()
                .stream()
                .filter(x -> ! FRIENDS_NAMES.contains(x.getName()))
                .forEach(x -> page.getBritishCharacters().add(x));
        return page;
    }

}
