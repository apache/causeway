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
package org.apache.causeway.viewer.graphql.viewer.test.source.gqltestdomain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.Getter;
import lombok.Setter;

//@Profile("demo-jpa")
@Entity
@Table(
        schema = "public",
        name = "E2"
)
@Named("gqltestdomain.E2")
@DomainObject(nature = Nature.ENTITY)
public class E2 implements TestEntity{

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    @Column(unique=true)
    private String name;

    @Getter @Setter
    @Property
    @OneToOne(optional = true)
    @JoinColumn(name = "e1_id")
    private E1 e1;

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public E2 changeName(final String newName){
        setName(newName);
        return this;
    }

    public String default0ChangeName(){
        return getName();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public E2 changeE1(final E1 e1){
        setE1(e1);
        return this;
    }

    public List<E1> choices0ChangeE1(){
        return testEntityRepository.findAllE1().stream().filter(e->e!=getE1()).collect(Collectors.toList());
    }

    public String validateChangeE1(final E1 e1){
        if (getE1() == e1) return "Already there";
        return null;
    }

    @OneToMany
    @Getter @Setter
    private List<E2> otherE2List = new ArrayList<>();

    @Getter @Setter
    @Collection
    private List<String> stringList = new ArrayList<>();

    @Getter @Setter
    @Collection
    private List<Integer> zintList = new ArrayList<>();

    @Action(semantics = SemanticsOf.SAFE)
    public List<TestEntity> otherEntities(){
        List<TestEntity> result = new ArrayList<>();
        result.addAll(testEntityRepository.findAllE1());
        result.addAll(testEntityRepository.findAllE2().stream().filter(e2->e2!=this).collect(Collectors.toList()));
        return result;
    }

    @Inject
    @Transient
    TestEntityRepository testEntityRepository;

}
