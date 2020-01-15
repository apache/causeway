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

package org.apache.isis.applib.value;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

import javax.activation.MimeType;

import org.apache.isis.core.commons.internal.base._Strings;

import lombok.val;

public final class Blob implements NamedWithMimeType {

    /**
     * Computed for state:
     * <pre>
     *     private final MimeType mimeType;
     *     private final byte[] bytes;
     *     private final String name;
     * </pre>
     */
    private static final long serialVersionUID = 5659679806709601263L;
    
    // -- FACTORIES
    
    /**
     * Returns a new {@link Blob} of given {@code name}, {@code mimeType} and {@code content}.
     * <p>
     * {@code name} may or may not include the desired filename extension, anyway it 
     * is guaranteed, that the resulting Blob has the appropriate extension as constraint by 
     * the given {@code mimeType}.
     * <p>
     * For more fine-grained control use one of the {@link Blob} constructors directly. 
     * @param name - may or may not include the desired filename extension
     * @param mimeType
     * @param content - bytes
     * @return new {@link Blob}
     */
    public static Blob of(String name, CommonMimeType mimeType, byte[] content) {
        val proposedFileExtension = mimeType.getProposedFileExtensions().getFirst().orElse("");
        val fileName = _Strings.asFileNameWithExtension(name, proposedFileExtension);
        return new Blob(fileName, mimeType.getMimeType(), content);
    }
    
     // -- 

    private final MimeType mimeType;
    private final byte[] bytes;
    private final String name;

    public Blob(String name, String primaryType, String subtype, byte[] bytes) {
        this(name, CommonMimeType.newMimeType(primaryType, subtype), bytes);
    }

    public Blob(String name, String mimeTypeBase, byte[] bytes) {
        this(name, CommonMimeType.newMimeType(mimeTypeBase), bytes);
    }

    public Blob(String name, MimeType mimeType, byte[] bytes) {
        if(name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if(mimeType == null) {
            throw new IllegalArgumentException("MimeType cannot be null");
        }
        if(name.contains(":")) {
            throw new IllegalArgumentException("Name cannot contain ':'");
        }
        if(bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null");
        }
        this.name = name;
        this.mimeType = mimeType;
        this.bytes = bytes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Does not close the OutputStream.
     * @param os
     * @throws IOException
     */
    public void writeBytesTo(final OutputStream os) throws IOException {
        if(os==null) {
            return;
        }
        if(bytes!=null) {
            os.write(bytes);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Blob blob = (Blob) o;
        return Objects.equals(mimeType.toString(), blob.mimeType.toString()) &&
                Arrays.equals(bytes, blob.bytes) &&
                Objects.equals(name, blob.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mimeType.toString(), name);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }

    @Override
    public String toString() {
        return getName() + " [" + getMimeType().getBaseType() + "]: " + getBytes().length + " bytes";
    }

}
