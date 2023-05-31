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
package demoapp.dom.domain.actions.ActionLayout.redirectPolicy.jdo;

import jakarta.inject.Named;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.value.Blob;

import demoapp.dom.domain.actions.ActionLayout.redirectPolicy.ActionLayoutRedirectPolicyEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Profile("demo-jdo")
@Named("demo.ActionLayoutRedirectPolicyEntity")
@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "demo")
@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@NoArgsConstructor
//tag::class[]
// ...
@DomainObject(nature = Nature.ENTITY)
public class ActionLayoutRedirectPolicyEntityImpl extends ActionLayoutRedirectPolicyEntity {
    // ...
//end::class[]

    public ActionLayoutRedirectPolicyEntityImpl(final String value) {
        setName(value);
    }

    @Getter @Setter
    private String name;

    @Property(optionality = Optionality.OPTIONAL)
    @Getter @Setter
    private Integer count;

    @Persistent(defaultFetchGroup="false", columns = {
            @javax.jdo.annotations.Column(name = "blob_name"),
            @javax.jdo.annotations.Column(name = "blob_mimetype"),
            @Column(name = "blob_bytes")
    })
    @Getter @Setter
    private Blob blob;
//tag::class[]
}
//end::class[]
