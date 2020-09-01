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
package org.apache.isis.testdomain.jpa.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
//@PersistenceCapable(identityType=IdentityType.DATASTORE, schema = "testdomain")
//@DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY, column="id")
//@Version(strategy= VersionStrategy.DATE_TIME, column="version")
@DomainObject(
        objectType = "testdomain.jdo.Inventory",
        nature = Nature.JPA_ENTITY, //TODO[ISIS-2332] should not be required, when using JPA quick classify SPI
        auditing = Auditing.ENABLED)
@DomainObjectLayout()  // causes UI events to be triggered
@NoArgsConstructor(access = AccessLevel.PROTECTED) 
@AllArgsConstructor(staticName = "of") 
@ToString
public class JpaInventory {

    public String title() {
        return toString();
    }

    @Property
    @Getter @Setter @Column(nullable = true)
    private String name;

    @Property
    @Getter @Setter @Column(nullable = true)
    private Set<JpaProduct> products;
}
