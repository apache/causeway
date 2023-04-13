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
package demoapp.dom.domain.actions.Action.choicesFrom.jpa;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.actions.Action.choicesFrom.ActionChoicesFrom;
import demoapp.dom.domain.actions.Action.choicesFrom.ActionChoicesFromRepository;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("demo-jpa")
@Service
public class ActionChoicesFromJpaEntities
extends ValueHolderRepository<String, ActionChoicesFromJpa> implements ActionChoicesFromRepository {

    protected ActionChoicesFromJpaEntities() {
        super(ActionChoicesFromJpa.class);
    }

    @Override
    protected ActionChoicesFromJpa newDetachedEntity(String value) {
        return new ActionChoicesFromJpa(value);
    }

    @Override
    public List<? extends ActionChoicesFrom> allInstances() {
        return all();
    }

    public List<? extends ActionChoicesFrom> allMatches(final String s) {
        return all();
    }
    public List<? extends ActionChoicesFrom> allMatches() {
        return all();
    }
}
