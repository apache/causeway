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

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.mixins.updates.OnUpdatedByAndAt;

import lombok.Getter;
import lombok.Setter;

@Entity
@DomainObject(
        objectType = "testdomain.jpa.ProductComment")
public class JpaProductComment implements OnUpdatedByAndAt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter @Setter @Column(name = "id")
    private Long id;

    // n:1 relation
    @Property
    @ManyToOne @JoinColumn(nullable = false)
    private @Getter @Setter JpaProduct product;

    @Property
    private @Getter @Setter String comment;

    // -- TIMESTAMPABLE

    @Property
    private @Getter @Setter String updatedBy;

    @Property
    private @Getter @Setter Timestamp updatedAt;


}
