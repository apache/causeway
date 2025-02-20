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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.inject.Named;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.applib.jaxb.PrimitiveJaxbAdapters;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.image._Images;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.commons.io.HashUtils;
import org.apache.causeway.commons.io.HashUtils.HashAlgorithm;
import org.apache.causeway.commons.io.ZipUtils;
import org.apache.causeway.commons.io.ZipUtils.ZipOptions;

import org.jspecify.annotations.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

/**
 * Represents a binary large object.
 * <p>
 * Conceptually you can consider it as a set of bytes (a picture, a video etc),
 * though in fact it wraps three pieces of information:
 * <ul>
 *     <li>
 *         the set of bytes
 *     </li>
 *     <li>
 *         a name
 *     </li>
 *     <li>
 *         a mime type
 *     </li>
 * </ul>
 *
 * @see Clob
 * @since 1.x {@index}
 */
@Named(CausewayModuleApplib.NAMESPACE + ".value.Blob")
@Value
@XmlJavaTypeAdapter(Blob.JaxbToStringAdapter.class)   // for JAXB view model support
@Log4j2
public record Blob(
    String name,
    MimeType mimeType,
    byte[] bytes
    ) implements NamedWithMimeType {

    // -- FACTORIES

    /**
     * Returns a new {@link Blob} of given {@code name}, {@code mimeType} and {@code content}.
     * <p>
     * {@code name} may or may not include the desired filename extension, as it
     * is guaranteed, that the resulting {@link Blob} has the appropriate extension
     * as constraint by the given {@code mimeType}.
     * <p>
     * For more fine-grained control use one of the {@link Blob} constructors directly.
     * @param name - may or may not include the desired filename extension
     * @param mimeType
     * @param content - bytes
     * @return new {@link Blob}
     */
    public static Blob of(final String name, final CommonMimeType mimeType, final byte[] content) {
        var fileName = _Strings.asFileNameWithExtension(name, mimeType.getProposedFileExtensions());
        return new Blob(fileName, mimeType.getMimeType(), content);
    }

    /**
     * Returns a new {@link Blob} of given {@code name}, {@code mimeType} and content from {@code dataSource},
     * wrapped with a {@link Try}.
     * <p>
     * {@code name} may or may not include the desired filename extension, as it
     * is guaranteed, that the resulting {@link Blob} has the appropriate extension
     * as constraint by the given {@code mimeType}.
     * <p>
     * For more fine-grained control use one of the {@link Blob} factories directly.
     * @param name - may or may not include the desired filename extension
     * @param mimeType
     * @param dataSource - the {@link DataSource} to be opened for reading
     * @return new {@link Blob}
     */
    public static Try<Blob> tryRead(final String name, final CommonMimeType mimeType, final DataSource dataSource) {
        return dataSource.tryReadAsBytes()
                .mapSuccess(bytes->Blob.of(name, mimeType, bytes.orElse(null)));
    }

    /**
     * Shortcut for {@code tryRead(name, mimeType, DataSource.ofFile(file))}
     * @see #tryRead(String, org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType, DataSource)
     */
    public static Try<Blob> tryRead(final String name, final CommonMimeType mimeType, final File file) {
        return tryRead(name, mimeType, DataSource.ofFile(file));
    }

     // --

    public Blob(final String name, final String primaryType, final String subtype, final byte[] bytes) {
        this(name, CommonMimeType.newMimeType(primaryType, subtype), bytes);
    }

    public Blob(final String name, final String mimeTypeBase, final byte[] bytes) {
        this(name, CommonMimeType.newMimeType(mimeTypeBase), bytes);
    }

    // canonical constructor
    public Blob(final String name, final MimeType mimeType, final byte[] bytes) {
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

    /**
     * @deprecated use {@link #bytes()} instead
     */
    public byte[] getBytes() { return bytes(); }

    // -- UTILITIES

    /**
     * Converts to a {@link Clob}, using given {@link Charset}
     * for the underlying byte[] to String conversion.
     */
    public Clob toClob(final @NonNull Charset charset) {
        return new Clob(name(), mimeType(), _Strings.ofBytes(bytes(), charset));
    }

    /**
     * Does not close the OutputStream.
     */
    @SneakyThrows
    public void writeBytesTo(final @Nullable OutputStream os) {
        if(os==null) return;
        
        if(bytes!=null) {
            os.write(bytes);
        }
    }

    /**
     * Writes this {@link Blob} to the file represented by
     * the specified <code>File</code> object.
     * <p>
     * If the file exists but is a directory rather than a regular file, does
     * not exist but cannot be created, or cannot be opened for any other
     * reason then a <code>FileNotFoundException</code> is thrown.
     *
     * @param      file the file to be opened for writing; if <code>null</code> this method does nothing
     * @see        java.io.FileOutputStream
     */
    @SneakyThrows
    public void writeTo(final @Nullable File file) {
        if(file==null) return; // just ignore
        
        try(var os = new FileOutputStream(file)){
            writeBytesTo(os);
        }
    }

    /**
     * Returns a new {@link DataSource} for underlying byte array.
     * @see DataSource
     */
    public DataSource asDataSource() {
        return DataSource.ofBytes(_NullSafe.toNonNull(getBytes()));
    }

    /**
     * Returns a new {@link Blob} that has this Blob's underlying byte array
     * zipped into a zip-entry using this Blob's name.
     */
    public Blob zip() {
        return zip(name());
    }

    /**
     * Returns a new {@link Blob} that has this Blob's underlying byte array
     * zipped into a zip-entry with given zip-entry name.
     * @param zipEntryNameIfAny - if null or empty this Blob's name is used
     */
    public Blob zip(final @Nullable String zipEntryNameIfAny) {
        var zipEntryName = _Strings.nonEmpty(zipEntryNameIfAny)
            .orElseGet(this::name);
        var zipBuilder = ZipUtils.zipEntryBuilder();
        zipBuilder.add(zipEntryName, getBytes());
        return Blob.of(name()+".zip", CommonMimeType.ZIP, zipBuilder.toBytes());
    }

    public Blob unZip(final @NonNull CommonMimeType resultingMimeType) {
        return unZip(resultingMimeType, ZipOptions.builder().build());
    }

    public Blob unZip(final @NonNull CommonMimeType resultingMimeType, final @NonNull ZipOptions zipOptions) {
        return ZipUtils.firstZipEntry(asDataSource(), zipOptions) // assuming first entry is the one we want
                .map(zipEntryDataSource->Blob.of(
                        zipEntryDataSource.zipEntry().getName(),
                        resultingMimeType,
                        zipEntryDataSource.bytes()))
                .orElseThrow(()->_Exceptions
                      .unrecoverable("failed to unzip blob, no entry found %s", name()));
    }

    // -- HASHING

    public Try<HashUtils.Hash> tryHash(final @NonNull HashAlgorithm hashAlgorithm) {
        return HashUtils.tryDigest(hashAlgorithm, bytes, 4*1024); // 4k default
    }

    public String md5Hex() {
        return tryHash(HashAlgorithm.MD5)
                .valueAsNonNullElseFail()
                .asHexString();
    }

    public String sha256Hex() {
        return tryHash(HashAlgorithm.SHA256)
                .valueAsNonNullElseFail()
                .asHexString();
    }

    // -- OBJECT CONTRACT

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
        return name() + " [" + mimeType().getBaseType() + "]: " + bytes().length + " bytes";
    }

    /**
     * (thread-safe)
     * @implNote see also BlobValueSemanticsProvider
     */
    public static final class JaxbToStringAdapter extends XmlAdapter<String, Blob> {

        private final PrimitiveJaxbAdapters.BytesAdapter bytesAdapter = new PrimitiveJaxbAdapters.BytesAdapter(); // thread-safe

        @Override
        public Blob unmarshal(final String data) throws Exception {
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
                return new Blob(name, new MimeType(mimeTypeBase), bytes);
            } catch (MimeTypeParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String marshal(final Blob blob) throws Exception {
            if(blob==null) {
                return null;
            }
            String s = blob.name() +
                    ':' +
                    blob.mimeType().getBaseType() +
                    ':' +
                    bytesAdapter.marshal(blob.bytes());
            return s;
        }

    }

    /**
     * @return optionally the payload as a {@link BufferedImage} based on whether
     * this Blob's MIME type identifies as image and whether the payload is not empty
     */
    public Optional<BufferedImage> asImage() {

        var bytes = getBytes();
        if(bytes == null) {
            return Optional.empty();
        }

        var mimeType = mimeType();
        if(mimeType == null || !mimeType.getPrimaryType().equals("image")) {
            return Optional.empty();
        }

        try {
            var img = _Images.fromBytes(bytes());
            return Optional.ofNullable(img);
        } catch (Exception e) {
            log.error("failed to read image data", e);
            return Optional.empty();
        }

    }

    // -- SERIALIZATION PROXY

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        /**
         * Generated, based on String, String, bytes[]
         */
        private static final long serialVersionUID = -950845631214162726L;
        private final String name;
        private final String mimeTypeBase;
        private final byte[] bytes;

        private SerializationProxy(final Blob blob) {
            this.name = blob.name();
            this.mimeTypeBase = blob.mimeType().getBaseType();
            this.bytes = blob.bytes();
        }

        private Object readResolve() {
            return new Blob(name, mimeTypeBase, bytes);
        }

    }

}
