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

package org.apache.causeway.testdomain.wrapperfactory;

import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "Counter"
        //schema = WrapperTestFixtures.SCHEMA
)
@Named(WrapperTestFixtures.NAMESPACE + ".Counter")
@DomainObject(nature = Nature.ENTITY)
@NoArgsConstructor
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Counter implements Comparable<Counter> {

    @Id
    @GeneratedValue
    @Getter @Setter
    private Long id;

    @Property(editing = Editing.ENABLED, commandPublishing = Publishing.ENABLED)
    @Column(nullable = true)
    @Getter @Setter
    private Long num;

    @Column(nullable = false)
    @Getter @Setter
    private String name;

    @Column(nullable = true)
    @Getter @Setter
    private Long num2;

    @Action(commandPublishing = Publishing.ENABLED)
    public Counter increment() {
        if (getNum() == null) {
            setNum(1L);
        } else {
            setNum(getNum() + 1);
        }
        return this;
    }

    @Override
    public int compareTo(final Counter o) {
        return this.getName().compareTo(o.getName());
    }
}
