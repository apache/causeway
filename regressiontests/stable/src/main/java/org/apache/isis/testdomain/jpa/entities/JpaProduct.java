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

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.commons.internal.base._Strings;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Product")
@DiscriminatorColumn(
        name="product_type", 
        discriminatorType = DiscriminatorType.STRING)
@DomainObject(
        objectType = "testdomain.jpa.Product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED) 
@ToString
public class JpaProduct implements Comparable<JpaProduct> {

    public String title() {
        return toString();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private @Getter @Setter Long id;

    @Property(editing = Editing.DISABLED) // used for an async rule check test
    @Column(nullable = true)
    private @Getter @Setter String name;
//    public void setName(String name) {
//        System.err.println("!!! setting name " + name);
//        this.name = name;
//    }

    @Property
    @Column(nullable = true)
    private @Getter @Setter String description;

    @Property
    @Column(nullable = false)
    private @Getter @Setter double price;
    
    // 1:n relation
    @Collection 
    @OneToMany(mappedBy = "product") @JoinColumn(nullable = true)  
    private @Getter @Setter List<JpaProductComment> comments;

    @Override
    public int compareTo(JpaProduct other) {
        return _Strings.compareNullsFirst(this.getName(), other==null ? null : other.getName());
    }

}
