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
package demoapp.dom.domain.properties.Property.hidden.jpa;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.properties.Property.hidden.PropertyHidden;
import demoapp.dom.domain.properties.Property.hidden.PropertyHiddenRepository;

@Profile("demo-jpa")
@Service
public class PropertyHiddenJpaEntities
extends ValueHolderRepository<String, PropertyHiddenJpa> implements PropertyHiddenRepository {

    protected PropertyHiddenJpaEntities() {
        super(PropertyHiddenJpa.class);
    }

    @Override
    protected PropertyHiddenJpa newDetachedEntity(String value) {
        return new PropertyHiddenJpa(value);
    }

    @Override
    public List<? extends PropertyHidden> allInstances() {
        return all();
    }

    public List<? extends PropertyHidden> allMatches(final String s) {
        return all();
    }
    public List<? extends PropertyHidden> allMatches() {
        return all();
    }
}
