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
package demoapp.dom.types.isis.blobs.jpa;

import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.persistence.jpa.applib.integration.IsisEntityListener;
import org.apache.isis.persistence.jpa.integration.types.BlobJpaEmbeddable;
import org.springframework.context.annotation.Profile;

import demoapp.dom.types.isis.blobs.persistence.IsisBlobEntity;
import lombok.NoArgsConstructor;

@Profile("demo-jpa")
//tag::class[]
@Entity
@Table(
      schema = "demo",
      name = "IsisBlobJpa"
)
@EntityListeners(IsisEntityListener.class)
@DomainObject(
      logicalTypeName = "demo.IsisBlobEntity"
)
@NoArgsConstructor
public class IsisBlobJpa
        extends IsisBlobEntity {

//end::class[]
    public IsisBlobJpa(Blob initialValue) {
        setReadOnlyProperty(initialValue);
        setReadWriteProperty(initialValue);
    }

//tag::class[]
    @Id
    @GeneratedValue
    private Long id;

    @AttributeOverrides({
        @AttributeOverride(name="name",    column=@Column(name="readOnlyProperty_name")),
        @AttributeOverride(name="mimeType",column=@Column(name="readOnlyProperty_mimeType")),
        @AttributeOverride(name="bytes",   column=@Column(name="readOnlyProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readOnlyProperty;

    @Title(prepend = "Blob JPA entity: ")
    @PropertyLayout(fieldSetId = "read-only-properties", sequence = "1")
    public Blob getReadOnlyProperty() {
        return readOnlyProperty.toBlob();
    }
    public void setReadOnlyProperty(final Blob readOnlyProperty) {
        this.readOnlyProperty = BlobJpaEmbeddable.from(readOnlyProperty);
    }

    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="readWriteProperty_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="readWriteProperty_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="readWriteProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readWriteProperty;

    @Property(editing = Editing.ENABLED)                            // <.>
    @PropertyLayout(fieldSetId = "editable-properties", sequence = "1")
    public Blob getReadWriteProperty() {
        return readWriteProperty.toBlob();
    }

    public void setReadWriteProperty(final Blob readWriteProperty) {
        this.readWriteProperty = BlobJpaEmbeddable.from(readWriteProperty);
    }

    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="readOnlyOptionalProperty_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="readOnlyOptionalProperty_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="readOnlyOptionalProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readOnlyOptionalProperty;

    @Property(optionality = Optionality.OPTIONAL)                   // <.>
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "1")
    public Blob getReadOnlyOptionalProperty() {
        return Optional.ofNullable(readOnlyOptionalProperty).map(BlobJpaEmbeddable::toBlob).orElse(null);
    }

    public void setReadOnlyOptionalProperty(final Blob readOnlyOptionalProperty) {
        this.readOnlyOptionalProperty = BlobJpaEmbeddable.from(readOnlyOptionalProperty);
    }


    @AttributeOverrides({
            @AttributeOverride(name="name",    column=@Column(name="readWriteOptionalProperty_name")),
            @AttributeOverride(name="mimeType",column=@Column(name="readWriteOptionalProperty_mimeType")),
            @AttributeOverride(name="bytes",   column=@Column(name="readWriteOptionalProperty_bytes"))
    })
    @Embedded
    private BlobJpaEmbeddable readWriteOptionalProperty;

    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)
    @PropertyLayout(fieldSetId = "optional-properties", sequence = "2")
    public Blob getReadWriteOptionalProperty() {
        return Optional.ofNullable(readWriteOptionalProperty).map(BlobJpaEmbeddable::toBlob).orElse(null);
    }

    public void setReadWriteOptionalProperty(final Blob readWriteOptionalProperty) {
        this.readWriteOptionalProperty = BlobJpaEmbeddable.from(readWriteOptionalProperty);
    }
}
//end::class[]
