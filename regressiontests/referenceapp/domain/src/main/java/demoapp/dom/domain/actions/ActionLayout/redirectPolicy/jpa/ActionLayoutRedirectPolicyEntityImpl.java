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
package demoapp.dom.domain.actions.ActionLayout.redirectPolicy.jpa;

import jakarta.inject.Named;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;
import org.apache.causeway.persistence.jpa.applib.types.BlobJpaEmbeddable;

import demoapp.dom.domain.actions.ActionLayout.redirectPolicy.ActionLayoutRedirectPolicyEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Profile("demo-jpa")
@Entity
@Table(
    schema = "demo",
    name = "ActionLayoutRedirectPolicyEntity"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.ActionLayoutRedirectPolicyEntity")
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

    @Id
    @GeneratedValue
    private Long id;

    @Getter @Setter
    private String name;

    @Property(optionality = Optionality.OPTIONAL)
    @Getter @Setter
    private Integer count;

    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="blob_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="blob_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="blob_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable blobJpaEmbeddable;

    @Override
    public Blob getBlob() {
        return BlobJpaEmbeddable.toBlob(blobJpaEmbeddable);
    }
    @Override
    public void setBlob(final Blob blob) {
        this.blobJpaEmbeddable = BlobJpaEmbeddable.fromBlob(blob);
    }

//tag::class[]
}
//end::class[]
