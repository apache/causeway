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
package org.apache.isis.testdomain.jdo;

import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@PersistenceCapable(identityType=IdentityType.DATASTORE, schema = "testdomain")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="Product")
@DatastoreIdentity(
        strategy=javax.jdo.annotations.IdGeneratorStrategy.INCREMENT,
        column="id")
@DomainObject
@AllArgsConstructor(access = AccessLevel.PROTECTED) @ToString
public class Product {

    public String title() {
        return toString();
    }

    @Property(editing = Editing.DISABLED) // used for an async rule check test
    @Getter @Setter @Column(allowsNull = "true")
    private String name;
//    public void setName(String name) {
//        System.err.println("!!! setting name " + name);
//        this.name = name;
//    }

    @Property
    @Getter @Setter @Column(allowsNull = "true")
    private String description;

    @Property
    @Getter @Setter @Column(allowsNull = "false")
    private double price;
    
    @Collection 
    @Persistent(mappedBy="product") @Column(allowsNull = "true") 
    @Getter @Setter 
    private List<ProductComment> comments;

}
