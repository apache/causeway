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
package org.apache.isis.persistence.jpa.applib.types;

import java.util.Arrays;
import java.util.Optional;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Lob;

import org.apache.isis.applib.value.Blob;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * A utility class to enable the persisting of {@link org.apache.isis.applib.value.Blob}s.
 *
 * <p>
 * Although JPA supports custom value types, these can only be for simple values; see
 * <a href="https://github.com/eclipse-ee4j/jpa-api/issues/105">eclipse-ee4j/jpa-api/issues/105</a>.
 * </p>
 *
 * <p>
 * EclipseLink <i>does</i> provide its own extension,
 * <a href="https://www.eclipse.org/eclipselink/documentation/2.5/jpa/extensions/a_transformation.htm>Transformation API</a>,
 * but there's a lot of boilerplate involved even so.
 * </p>
 *
 * <p>
 * This class provides support for an alternative approach, where the Isis {@link Blob} is marshalled in and out of
 * this class.
 * </p>
 *
 * <p>
 *    Example usage:
 *
 *     <pre>
 *     &#064;Embedded
 *     private BlobJpaEmbeddable pdf;
 *
 *     &#064;Property()
 *     &#064;PropertyLayout()
 *     public Blob getPdf() {
 *         return BlobJpaEmbeddable.toBlob(pdf);
 *     }
 *     public void setPdf(final Blob pdf) {
 *         this.pdf = BlobJpaEmbeddable.fromBlob(pdf);
 *     }
 *    </pre>
 * </p>
 *
 * <p>
 *     Lastly; note that {@link javax.persistence.AttributeOverrides} and {@link javax.persistence.AttributeOverride}
 *     provide a standardised way of fine-tuning the column definitions.
 * </p>
 *
 * @since 2.x {@index}
 */
@Embeddable
@Getter @Setter
public final class BlobJpaEmbeddable {

    /**
     * Factory method to marshall a {@link Blob} into a {@link BlobJpaEmbeddable}
     * 
     * @see #toBlob(BlobJpaEmbeddable)
     */
    public static BlobJpaEmbeddable fromBlob(Blob blob) {
        if(blob == null) {
            return null;
        }
        val blobJpaEmbeddable = new BlobJpaEmbeddable();
        blobJpaEmbeddable.bytes = blob.getBytes();
        blobJpaEmbeddable.mimeType = blob.getMimeType().toString();
        blobJpaEmbeddable.name = blob.getName();
        return blobJpaEmbeddable;
    }

    /**
     * Reciprocal method to marshall a {@link BlobJpaEmbeddable} into a {@link Blob}
     * 
     * @see #fromBlob(Blob)
     */
    public static Blob toBlob(final BlobJpaEmbeddable blobJpaEmbeddable) {
        return Optional.ofNullable(blobJpaEmbeddable).map(BlobJpaEmbeddable::asBlob).orElse(null);
    }

    @Column(nullable = false, length = 255)
    @Basic
    private String mimeType;

    @Column(nullable = false)
    @Lob
    @Basic
    private byte[] bytes;

    @Column(nullable = false, length = 255)
    @Basic
    private String name;

    public Blob asBlob() {
        return new Blob(name, mimeType, bytes);
    }

    
    @Override
    public String toString() {
        return asBlob().toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final BlobJpaEmbeddable that = (BlobJpaEmbeddable) o;

        if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null)
            return false;
        if (!Arrays.equals(bytes, that.bytes))
            return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override public int hashCode() {
        int result = mimeType != null ? mimeType.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(bytes);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
