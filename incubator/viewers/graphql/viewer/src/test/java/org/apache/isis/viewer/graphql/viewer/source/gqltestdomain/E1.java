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
package org.apache.isis.viewer.graphql.viewer.source.gqltestdomain;

import lombok.Getter;
import lombok.Setter;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;

//@Profile("demo-jpa")
@Entity
@Table(
        schema = "public",
        name = "E1"
)
@DomainObject(nature = Nature.ENTITY, logicalTypeName = "gqltestdomain.E1")
public class E1 implements TestEntity, Comparable {

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    @Property
    @OneToOne(optional = true)
    @JoinColumn(name = "e2_id")
    private E2 e2;

    @Override
    public int compareTo(Object o) {
        E1 e1 = (E1) o;
        return this.getName().compareTo(e1.getName());
    }
}
