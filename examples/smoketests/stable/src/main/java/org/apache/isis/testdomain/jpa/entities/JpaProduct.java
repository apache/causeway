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

import java.util.List;

import javax.jdo.annotations.Persistent;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
//@PersistenceCapable(identityType=IdentityType.DATASTORE, schema = "testdomain")
//@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
//@Discriminator(strategy=DiscriminatorStrategy.VALUE_MAP, value="Product")
@DomainObject(
        objectType = "testdomain.jpa.Product"
        )
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED) 
@ToString
public class JpaProduct {

    public String title() {
        return toString();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter @Setter @Column(name = "id")
    private Long id;

    @Property(editing = Editing.DISABLED) // used for an async rule check test
    @Getter @Setter @Column(nullable = true)
    private String name;
//    public void setName(String name) {
//        System.err.println("!!! setting name " + name);
//        this.name = name;
//    }

    @Property
    @Getter @Setter @Column(nullable = true)
    private String description;

    @Property
    @Getter @Setter @Column(nullable = false)
    private double price;
    
    @Collection 
    @Persistent(mappedBy="product") @Column(nullable = true) 
    @Getter @Setter 
    private List<JpaProductComment> comments;

}
