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
package demoapp.dom.types.causeway.blobs.jpa;

import javax.inject.Named;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.persistence.jpa.applib.integration.CausewayEntityListener;
import org.apache.causeway.persistence.jpa.applib.types.BlobJpaEmbeddable;

import lombok.NoArgsConstructor;

import demoapp.dom.types.causeway.blobs.persistence.CausewayBlobEntity;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "CausewayBlobJpa"
)
@EntityListeners(CausewayEntityListener.class)
@Named("demo.CausewayBlobEntity")
@DomainObject
@NoArgsConstructor
public class CausewayBlobJpa
        extends CausewayBlobEntity {
    // ...
//end::class[]
    public CausewayBlobJpa(final Blob initialValue) {
        setReadOnlyProperty(initialValue);
        setReadWriteProperty(initialValue);
    }

    @Id
    @GeneratedValue
    private Long id;

//tag::field[]
    @AttributeOverrides({
        @AttributeOverride(name="name",    column=@Column(name="readOnlyProperty_name")),
        @AttributeOverride(name="mimeType",column=@Column(name="readOnlyProperty_mimeType")),
        @AttributeOverride(name="bytes",   column=@Column(name="readOnlyProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readOnlyProperty;
//end::field[]

//tag::accessors[]
    @Override
    @Title(prepend = "Blob JPA entity: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    public Blob getReadOnlyProperty() {
        return BlobJpaEmbeddable.toBlob(readOnlyProperty);
    }
    @Override
    public void setReadOnlyProperty(final Blob readOnlyProperty) {
        this.readOnlyProperty = BlobJpaEmbeddable.fromBlob(readOnlyProperty);
    }
//end::accessors[]

    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="readWriteProperty_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="readWriteProperty_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="readWriteProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readWriteProperty;

    @Override
    @Property(editing = Editing.ENABLED)
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1")
    public Blob getReadWriteProperty() {
        return BlobJpaEmbeddable.toBlob(readWriteProperty);
    }

    @Override
    public void setReadWriteProperty(final Blob readWriteProperty) {
        this.readWriteProperty = BlobJpaEmbeddable.fromBlob(readWriteProperty);
    }

    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="readOnlyOptionalProperty_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="readOnlyOptionalProperty_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="readOnlyOptionalProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readOnlyOptionalProperty;

    @Override
    @Property(optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    public Blob getReadOnlyOptionalProperty() {
        return BlobJpaEmbeddable.toBlob(readOnlyOptionalProperty);
    }

    @Override
    public void setReadOnlyOptionalProperty(final Blob readOnlyOptionalProperty) {
        this.readOnlyOptionalProperty = BlobJpaEmbeddable.fromBlob(readOnlyOptionalProperty);
    }


    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="readWriteOptionalProperty_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="readWriteOptionalProperty_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="readWriteOptionalProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readWriteOptionalProperty;

    @Override
    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "2")
    public Blob getReadWriteOptionalProperty() {
        return BlobJpaEmbeddable.toBlob(readWriteOptionalProperty);
    }

    @Override
    public void setReadWriteOptionalProperty(final Blob readWriteOptionalProperty) {
        this.readWriteOptionalProperty = BlobJpaEmbeddable.fromBlob(readWriteOptionalProperty);
    }
//tag::class[]
}
//end::class[]
