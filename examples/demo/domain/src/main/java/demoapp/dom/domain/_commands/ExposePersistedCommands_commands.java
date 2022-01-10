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
package demoapp.dom.domain._commands;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.extensions.commandlog.jdo.entities.CommandJdoRepository;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection
@CollectionLayout(defaultView = "table")
@RequiredArgsConstructor
public class ExposePersistedCommands_commands {
    // ...
//end::class[]
    private final ExposePersistedCommands exposePersistedCommands;

    //tag::class[]
    public List<? extends CommandModel> coll() {
        return commandJdoRepository.findCompleted();
    }

    @Inject CommandJdoRepository commandJdoRepository;
}
//end::class[]
