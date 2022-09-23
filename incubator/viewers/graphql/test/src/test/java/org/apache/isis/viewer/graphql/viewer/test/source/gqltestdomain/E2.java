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
package org.apache.isis.viewer.graphql.viewer.test.source.gqltestdomain;

import lombok.Getter;
import lombok.Setter;
import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.viewer.graphql.viewer.test.source.gqltestdomain.TestEntity;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@Profile("demo-jpa")
@Entity
@Table(
        schema = "public",
        name = "E2"
)
@Named("gqltestdomain.E2")
@DomainObject(nature = Nature.ENTITY)
public class E2 implements TestEntity {

    @Id
    @Setter
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

    public String validateChangeName(final String newName){
        if (getName().equals("e2")) return "The name of an object called e2 cannot be changed";
        return null;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public E2 changeE1(final E1 e1){
        setE1(e1);
        return this;
    }

    //TODO: when testing, testEntityRepository is not injected resulting in a NPE
//    public E1 default0ChangeE1(){
//        return choices0ChangeE1().isEmpty() ? null : choices0ChangeE1().get(0);
//    }

    public List<E1> choices0ChangeE1(){
        return testEntityRepository.findAllE1().stream().filter(e->e!=getE1()).collect(Collectors.toList());
    }

    public String validateChangeE1(final E1 e1){
        if (getE1() == e1) return "Already there";
        return null;
    }

    @OneToMany
    @Getter @Setter
    @Collection(hidden = Where.EVERYWHERE)
    private List<E2> otherE2List = new ArrayList<>();

    @Getter @Setter
    @Collection
    private List<String> stringList = new ArrayList<>();

    @Getter @Setter
    @Collection
    private List<Integer> zintList = new ArrayList<>();

    @Action(semantics = SemanticsOf.SAFE)
    public List<TestEntity> otherEntities(@Nullable final String name, @Nullable final Integer numberOfResults){
        List<TestEntity> result = new ArrayList<>();
        result.addAll(testEntityRepository.findAllE1());
        result.addAll(testEntityRepository.findAllE2().stream().filter(e2->e2!=this).collect(Collectors.toList()));
        // TODO: maybe make number of results functional; for we leave it
        return name == null ? result : result.stream().filter(e->e.getName().contains(name)).collect(Collectors.toList());
    }

    public String default0OtherEntities(){
        return "e";
    }

    public Integer default1OtherEntities(){
        return Integer.valueOf(10);
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    public void remove(){
        repositoryService.removeAndFlush(this);
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE)
    public void removeWithReason(final String reason){
        repositoryService.removeAndFlush(this);
    }

    @Inject
    @Transient
    TestEntityRepository testEntityRepository;

    @Inject
    @Transient
    RepositoryService repositoryService;

}
