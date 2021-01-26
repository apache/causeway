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
package demoapp.dom.domain.objects.other.customvaluetypes;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.val;

@Repository
@Named("demo.numberConstantRepository")
public class NumberConstantJdoRepository {

    @Inject private RepositoryService repository;
    @Inject private FactoryService factory;

    public List<NumberConstantJdo> listAll(){
        return repository.allInstances(NumberConstantJdo.class);
    }

    public void add(String name, ComplexNumber number) {
        val numConst = factory.detachedEntity(new NumberConstantJdo());
        numConst.setName(name);
        numConst.setNumber(number);
        add(numConst);
    }

    public void add(NumberConstantJdo entry) {
        repository.persist(entry);
    }

}
