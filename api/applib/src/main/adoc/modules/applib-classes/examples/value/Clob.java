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
import java.io.Writer;
import java.util.Objects;

import javax.activation.MimeType;

import org.apache.isis.commons.internal.base._Strings;

import lombok.val;

// tag::refguide[]
// end::refguide[]
public final class Clob implements NamedWithMimeType {

    private static final long serialVersionUID = 8694189924062378527L;

    private final String name;
    private final MimeType mimeType;
    private final CharSequence chars;
    
    
    // -- FACTORIES
    
    /**
     * Returns a new {@link Clob} of given {@code name}, {@code mimeType} and {@code content}.
     * <p>
     * {@code name} may or may not include the desired filename extension, it 
     * is guaranteed, that the resulting {@link Clob} has the appropriate extension 
     * as constraint by the given {@code mimeType}.
     * <p>
     * For more fine-grained control use one of the {@link Clob} constructors directly. 
     * @param name - may or may not include the desired filename extension
     * @param mimeType
     * @param content - chars
     * @return new {@link Clob}
     */
    public static Clob of(String name, CommonMimeType mimeType, CharSequence content) {
        val proposedFileExtension = mimeType.getProposedFileExtensions().getFirst().orElse("");
        val fileName = _Strings.asFileNameWithExtension(name, proposedFileExtension);
        return new Clob(fileName, mimeType.getMimeType(), content);
    }
    
    // --

    public Clob(String name, String primaryType, String subType, char[] chars) {
        this(name, primaryType, subType, new String(chars));
    }

    public Clob(String name, String mimeTypeBase, char[] chars) {
        this(name, mimeTypeBase, new String(chars));
    }

    public Clob(String name, MimeType mimeType, char[] chars) {
        this(name, mimeType, new String(chars));
    }

    public Clob(String name, String primaryType, String subType, CharSequence chars) {
        this(name, CommonMimeType.newMimeType(primaryType, subType), chars);
    }

    public Clob(String name, String mimeTypeBase, CharSequence chars) {
        this(name, CommonMimeType.newMimeType(mimeTypeBase), chars);
    }

    public Clob(String name, MimeType mimeType, CharSequence chars) {
        if(name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if(mimeType == null) {
            throw new IllegalArgumentException("MimeType cannot be null");
        }
        if(name.contains(":")) {
            throw new IllegalArgumentException("Name cannot contain ':'");
        }
        if(chars == null) {
            throw new IllegalArgumentException("Chars cannot be null");
        }
        this.name = name;
        this.mimeType = mimeType;
        this.chars = chars;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MimeType getMimeType() {
        return mimeType;
    }

    public CharSequence getChars() {
        return chars;
    }

    public void writeCharsTo(final Writer wr) throws IOException {
        if(wr!=null && chars!=null){
            wr.append(chars);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final Clob clob = (Clob) o;
        return Objects.equals(name, clob.name) &&
                Objects.equals(mimeType.toString(), clob.mimeType.toString()) &&
                Objects.equals(chars, clob.chars);
    }

    @Override public int hashCode() {
        return Objects.hash(name, mimeType.toString(), chars);
    }

    @Override
    public String toString() {
        return getName() + " [" + getMimeType().getBaseType() + "]: " + getChars().length() + " chars";
    }

}
