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
package demoapp.dom.types.tuple;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;

import demoapp.utils.DemoStub;

@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.TupleDemo")
public class TupleDemo extends DemoStub {

    @Inject private NumberConstantRepository numberConstantRepo;
    
    @Override
    public String title() {
        return "Tuple Demo";
    }
    
    @Override
    public void initDefaults() {
        if(numberConstantRepo.listAll().size() == 0) {
            numberConstantRepo.add("Pi", ComplexNumber.of(Math.PI, 0.));    
            numberConstantRepo.add("Euler's Constant", ComplexNumber.of(Math.E, 0.));
            numberConstantRepo.add("Imaginary Unit", ComplexNumber.of(0, 1.));
        }
    }

    @Collection
    public List<NumberConstant> getAllConstants(){
        return numberConstantRepo.listAll();
    }

}
