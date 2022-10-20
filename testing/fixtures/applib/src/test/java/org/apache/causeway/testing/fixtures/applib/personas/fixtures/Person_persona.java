/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.testing.fixtures.applib.personas.fixtures;

import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.testing.fixtures.applib.personas.Persona;
import org.apache.causeway.testing.fixtures.applib.personas.dom.Person;
import org.apache.causeway.testing.fixtures.applib.personas.dom.PersonRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Person_persona
        implements Persona<Person, PersonBuilderScript> {

    SteveSingle(1, "Steve", "Single", 21),
    MeghanMarriedMum(2, "Meghan", "Married-Mum", 35);

    private final int id;
    private final String firstName;
    private final String lastName;
    private final int age;

    @Override
    public PersonBuilderScript builder() {
        return new PersonBuilderScript(this);
    }

    @Override
    public Person findUsing(ServiceRegistry serviceRegistry2) {
        return serviceRegistry2.lookupServiceElseFail(PersonRepository.class).findById(id).orElseThrow(RuntimeException::new);
    }
}
