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
package org.apache.causeway.applib.value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Named;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.jaxb.PrimitiveJaxbAdapters;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Represents a character large object.
 *
 * <p>
 * Conceptually you can consider it as a set of characters (an RTF or XML
 * document, for example), though in fact it wraps three pieces of information:
 * </p>
 * <ul>
 *     <li>
 *         the set of characters
 *     </li>
 *     <li>
 *         a name
 *     </li>
 *     <li>
 *         a mime type
 *     </li>
 * </ul>
 *
 * @see Blob
 * @since 1.x {@index}
 */
@Named(CausewayModuleApplib.NAMESPACE + ".value.Clob")
@Value
@XmlJavaTypeAdapter(Clob.JaxbToStringAdapter.class)   // for JAXB view model support
//@Log4j2
public final class Clob implements NamedWithMimeType {

    /**
     * Computed for state:
     *
     * <p>
     * <pre>
     * private final MimeType mimeType;
     * private final CharSequence chars;
     * private final String name;
     * </pre>
     * </p>
     */
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
    public static Clob of(final String name, final CommonMimeType mimeType, final CharSequence content) {
        val proposedFileExtension = mimeType.getProposedFileExtensions().getFirst().orElse("");
        val fileName = _Strings.asFileNameWithExtension(name, proposedFileExtension);
        return new Clob(fileName, mimeType.getMimeType(), content);
    }

    /**
     * Returns a new {@link Clob} of given {@code name}, {@code mimeType} and content from {@code file},
     * wrapped with a {@link Try}.
     * <p>
     * {@code name} may or may not include the desired filename extension, it
     * is guaranteed, that the resulting {@link Clob} has the appropriate extension
     * as constraint by the given {@code mimeType}.
     * <p>
     * For more fine-grained control use one of the {@link Clob} constructors directly.
     * @param name - may or may not include the desired filename extension
     * @param mimeType
     * @param file - the file to be opened for reading
     * @param charset - {@link Charset} to use for reading given file
     * @return new {@link Clob}
     */
    public static Try<Clob> tryRead(final String name, final CommonMimeType mimeType, final File file,
            final @NonNull Charset charset) {
        return Try.call(()->{
            try(val fis = new FileInputStream(file)){
                return Clob.of(name, mimeType, _Strings.read(fis, charset));
            }
        });
    }

    /**
     * Shortcut for {@link #tryRead(String, org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType, File, Charset)}
     * using {@link StandardCharsets#UTF_8}.
     */
    public static Try<Clob> tryReadUtf8(final String name, final CommonMimeType mimeType, final File file) {
        return tryRead(name, mimeType, file, StandardCharsets.UTF_8);
    }

    // --

    public Clob(final String name, final String primaryType, final String subType, final char[] chars) {
        this(name, primaryType, subType, new String(chars));
    }

    public Clob(final String name, final String mimeTypeBase, final char[] chars) {
        this(name, mimeTypeBase, new String(chars));
    }

    public Clob(final String name, final MimeType mimeType, final char[] chars) {
        this(name, mimeType, new String(chars));
    }

    public Clob(final String name, final String primaryType, final String subType, final CharSequence chars) {
        this(name, CommonMimeType.newMimeType(primaryType, subType), chars);
    }

    public Clob(final String name, final String mimeTypeBase, final CharSequence chars) {
        this(name, CommonMimeType.newMimeType(mimeTypeBase), chars);
    }

    public Clob(final String name, final MimeType mimeType, final CharSequence chars) {
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

    // -- UTILITIES

    /**
     * Converts to a {@link Blob}, using given {@link Charset}
     * for the underlying String to byte[] conversion.
     */
    public Blob toBlob(final @NonNull Charset charset) {
        return new Blob(getName(), getMimeType(), _Strings.toBytes(getChars().toString(), charset));
    }

    /**
     * Shortcut for {@link #toBlob(Charset)} using {@link StandardCharsets#UTF_8}.
     */
    public Blob toBlobUtf8() {
        return toBlob(StandardCharsets.UTF_8);
    }

    public void writeCharsTo(final Writer wr) throws IOException {
        if(wr!=null && chars!=null){
            wr.append(chars);
        }
    }

    /**
     * Writes this {@link Clob} to the file represented by
     * the specified <code>File</code> object.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      file the file to be opened for writing; if <code>null</code> this method does nothing
     * @param charset - {@link Charset} to use for writing to given file
     * @see        java.io.FileOutputStream
     * @see        java.io.OutputStreamWriter
     */
    @SneakyThrows
    public void writeTo(final @Nullable File file, final @NonNull Charset charset) {
        if(file==null) {
            return; // just ignore
        }
        try(val os = new FileOutputStream(file)){
            writeCharsTo(new OutputStreamWriter(os, charset));
        }
    }

    /**
     * Shortcut for {@link #writeTo(File, Charset)} using {@link StandardCharsets#UTF_8}.
     */
    public void writeToUtf8(final @Nullable File file) {
        writeTo(file, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public String asString() {
        val sw = new StringWriter();
        writeCharsTo(sw);
        return sw.toString();
    }

    // -- OBJECT CONTRACT

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

    /**
     * (thread-safe)
     * @implNote see also ClobValueSemanticsProvider
     */
    public static final class JaxbToStringAdapter extends XmlAdapter<String, Clob> {

        private final PrimitiveJaxbAdapters.BytesAdapter bytesAdapter = new PrimitiveJaxbAdapters.BytesAdapter(); // thread-safe

        @Override
        public Clob unmarshal(final String data) throws Exception {
            if(data==null) {
                return null;
            }
            final int colonIdx = data.indexOf(':');
            final String name  = data.substring(0, colonIdx);
            final int colon2Idx  = data.indexOf(":", colonIdx+1);
            final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
            final String payload = data.substring(colon2Idx+1);
            final byte[] bytes = bytesAdapter.unmarshal(payload);
            try {
                return new Clob(name, new MimeType(mimeTypeBase), new String(bytes, StandardCharsets.UTF_8));
            } catch (MimeTypeParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String marshal(final Clob clob) throws Exception {
            if(clob==null) {
                return null;
            }
            return new StringBuilder()
            .append(clob.getName())
            .append(':')
            .append(clob.getMimeType().getBaseType())
            .append(':')
            .append(bytesAdapter.marshal(clob.getChars().toString().getBytes(StandardCharsets.UTF_8)))
            .toString();
        }

    }


}
