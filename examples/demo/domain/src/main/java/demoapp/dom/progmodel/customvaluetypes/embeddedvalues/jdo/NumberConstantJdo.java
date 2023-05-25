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
package demoapp.dom.progmodel.customvaluetypes.embeddedvalues.jdo;

import jakarta.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;

import lombok.Getter;
import lombok.Setter;

import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.ComplexNumber;
import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.NumberConstantEntity;

@Profile("demo-jdo")
//tag::class[]
@Named("demo.NumberConstantEntityJdo")
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo" )
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@DomainObject
public class NumberConstantJdo
//end::class[]
        extends NumberConstantEntity
//tag::class[]
{

    // ...

//end::class[]
    @ObjectSupport public String title() {
        return getName();
    }

    @Override
    public ComplexNumber value() {
        return getNumber();
    }

//tag::class[]
    @javax.jdo.annotations.Column(allowsNull = "false")
    @Property
    @Getter @Setter
    private String name;

    @javax.jdo.annotations.Embedded(members={                           // <.>
            @Persistent(name="re", columns=@Column(name="number_re")),  // <1>
            @Persistent(name="im", columns=@Column(name="number_im"))   // <1>
    })
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private ComplexNumberJdo number;
}
//end::class[]
