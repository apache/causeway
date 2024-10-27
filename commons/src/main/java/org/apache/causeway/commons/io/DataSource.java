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
package org.apache.causeway.commons.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.ThrowingConsumer;
import org.apache.causeway.commons.functional.ThrowingFunction;
import org.apache.causeway.commons.functional.ThrowingSupplier;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.HashUtils.HashAlgorithm;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * General purpose readable byte data source.
 *
 * @since 2.0 {@index}
 */
@FunctionalInterface
public interface DataSource {

    <T> Try<T> tryReadAll(@NonNull Function<InputStream, Try<T>> consumingMapper);

    /**
     * Passes an {@link InputStream} to given {@link Function} for application.
     * @return either a successful or failed {@link Try} (non-null),
     *      where in the success case, the returned Try is holding the returned value from the given {@link Function inputStreamMapper};
     *      if the InputStream is absent or not readable, the returned Try will hold the underlying {@link Exception}
     */
    default <T> Try<T> tryReadAndApply(final @NonNull ThrowingFunction<InputStream, T> inputStreamMapper) {
        return tryReadAll(inputStream->
            Try.call(()->inputStreamMapper.apply(inputStream)));
    }

    /**
     * Passes an {@link InputStream} to given {@link Consumer} for consumption.
     * @return either a successful or failed {@link Try} (non-null);
     *     if the InputStream is absent or not readable, the returned Try will hold the underlying {@link Exception}
     */
    default Try<Void> tryReadAndAccept(final @NonNull ThrowingConsumer<InputStream> inputStreamConsumer) {
        return tryReadAll(inputStream->
            Try.run(()->inputStreamConsumer.accept(inputStream)));
    }

    // -- READ AS BYTES

    /**
     * Reads from this DataSource into a String using given charset encoding.
     * <p>
     * If the underlying {@link InputStream} is null a success {@link Try} is returned, containing a null value.
     */
    default Try<byte[]> tryReadAsBytes() {
        return tryReadAndApply(inputStream->_Bytes.of(inputStream));
    }

    /**
     * Shortcut for {@code tryReadAsBytes().valueAsNonNullElseFail()}.
     */
    default byte[] bytes() {
        return tryReadAsBytes()
                .valueAsNonNullElseFail();
    }

    // -- READ AS STRING

    /**
     * Reads from this DataSource into a String using given charset encoding.
     * <p>
     * If the underlying {@link InputStream} is null a success {@link Try} is returned, containing a null value.
     */
    default Try<String> tryReadAsString(final @NonNull Charset charset) {
        return tryReadAndApply(inputStream->_Strings.ofBytes(_Bytes.of(inputStream), charset));
    }

    /**
     * Reads from this DataSource into a String using UTF-8 encoding.
     * <p>
     * If the underlying {@link InputStream} is null a success {@link Try} is returned, containing a null value.
     */
    default Try<String> tryReadAsStringUtf8() {
        return tryReadAsString(StandardCharsets.UTF_8);
    }

    // -- READ LINES

    /**
     * Reads from this DataSource all lines using given charset encoding.
     * <p>
     * If the underlying {@link InputStream} is null a success {@link Try} is returned, containing a null value.
     */
    default Try<Can<String>> tryReadAsLines(final @NonNull Charset charset) {
        return tryReadAndApply(inputStream->TextUtils.readLinesFromInputStream(inputStream, charset));
    }

    /**
     * Reads from this DataSource all lines using UTF-8 encoding.
     * <p>
     * If the underlying {@link InputStream} is null a success {@link Try} is returned, containing a null value.
     */
    default Try<Can<String>> tryReadAsLinesUtf8() {
        return tryReadAsLines(StandardCharsets.UTF_8);
    }

    // -- IMAGE DATA

    default Try<BufferedImage> tryReadAsImage() {
        return tryReadAndApply(ImageIO::read);
    }

    // -- HASHING

    default Try<HashUtils.Hash> tryHash(final @NonNull HashAlgorithm hashAlgorithm) {
        return HashUtils.tryDigest(hashAlgorithm, this, 4*1024); // 4k default
    }

    default Try<HashUtils.Hash> tryMd5() {
        return tryHash(HashAlgorithm.MD5);
    }

    default String md5Hex() {
        return tryMd5()
                .valueAsNonNullElseFail()
                .asHexString();
    }

    // -- MAP

    /**
     * Returns a new {@link DataSource} that maps the {@link InputStream} of this {@link DataSource} to another
     * through means of applying given unary operator {@code inputStreamMapper}.
     * (eg the decode or encode the originating input stream)
     */
    default DataSource map(final @NonNull ThrowingFunction<InputStream, InputStream> inputStreamMapper) {
        var self = this;
        return new DataSource() {
            @Override public <T> Try<T> tryReadAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return self.tryReadAll(is->consumingMapper.apply(inputStreamMapper.apply(is)));
            }
            @Override public String getDescription() {
                return descriptionForMapped(self);
            }
        };
    }

    // -- PIPE

    /**
     * Acts as a pipe, reading from this {@link DataSource} and writing to given {@link DataSink},
     * using given bufferSize for the underlying byte data junks.
     * @return a success or failed {@link Try}, based on whether the write was successful or not
     */
    default Try<Void> tryReadAndWrite(final @NonNull DataSink dataSink, final int bufferSize) {
        return tryReadAndAccept(inputStream->{
            dataSink.writeAll(os->{
                var buffer = new byte[bufferSize]; int n;
                while((n = inputStream.read(buffer)) > -1) {
                    os.write(buffer, 0, n);
                }
            });
        });
    }

    /**
     * Acts as a pipe, reading from this {@link DataSource} and writing to given {@link DataSink},
     * using given bufferSize for the underlying byte data junks.
     * <p>
     * Throws if the write failed.
     */
    default void pipe(final @NonNull DataSink dataSink, final int bufferSize) {
        tryReadAndWrite(dataSink, bufferSize).ifFailureFail();
    }
    /**
     * Acts as a pipe, reading from this {@link DataSource} and writing to given {@link DataSink},
     * using default bufferSize of 16k for the underlying byte data junks.
     * <p>
     * Throws if the write failed.
     */
    default void pipe(final @NonNull DataSink dataSink) {
        pipe(dataSink, 16*1024);
    }

    // -- FACTORIES

    /**
     * Acts as a no-op.
     */
    static DataSource empty() {
        return new DataSource() {
            @Override public <T> Try<T> tryReadAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return Try.empty();
            }
            @Override public String getDescription() {
                return descriptionForEmpty();
            }
            @Override
            public String toString() {
                return getDescription();
            }
        };
    }

    /**
     * Creates a {@link DataSource} for given InputStream Supplier.
     * @param inputStreamSupplier - required non-null
     * @throws NullPointerException - if the single argument is null
     */
    static DataSource ofInputStreamSupplier(final @NonNull ThrowingSupplier<InputStream> inputStreamSupplier) {
        return new DataSource() {
            @Override public <T> Try<T> tryReadAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return Try.call(()->{
                    try(final InputStream is = inputStreamSupplier.get()) {
                        return consumingMapper.apply(is);
                    }
                })
                // unwrap the inner try
                .mapSuccessAsNullable(wrappedTry->wrappedTry.valueAsNullableElseFail());
            }
        };
    }

    /**
     * Creates a {@link DataSource} for given {@link InputStream} eagerly.
     * That is, it reads the InputStream into a byte array,
     * which can be later read from repeatedly.
     * <p>
     * If reading from given {@link InputStream} throws any exception, it is propagated without catching.
     */
    @SneakyThrows
    static DataSource ofInputStreamEagerly(final @Nullable InputStream inputStream) {
        return ofBytes(_Bytes.of(inputStream));
    }

    /**
     * Creates a {@link DataSource} for given resource path relative to {@link Class}.
     * <p>
     * If any of the args is null (or empty), returns an 'empty' {@link DataSource}.
     * @apiNote may silently fail if this module cannot read resources from the calling module;
     *      a workaround is to use {@code DataSource.ofInputStreamEagerly(cls.getResourceAsStream(resourcePath))}
     *      at the call site
     */
    static DataSource ofResource(final @Nullable Class<?> cls, final @Nullable String resourcePath) {
        return cls==null
                || _Strings.isNullOrEmpty(resourcePath)
                ? empty()
                : ofInputStreamSupplierInternal(
                        descriptionForResource(cls, resourcePath),
                        ()->cls.getResourceAsStream(resourcePath));
    }

    /**
     * Creates a {@link DataSource} for given Spring {@link Resource}.
     * <p>
     * If the single argument is null, returns an 'empty' {@link DataSource}.
     * @apiNote may silently fail if this module cannot read resources from the calling module;
     *      a workaround is to use {@code DataSource.ofInputStreamEagerly(springResource.getInputStream())}
     *      at the call site
     */
    static DataSource ofSpringResource(final @Nullable Resource springResource) {
        return springResource==null
                ? empty()
                : ofInputStreamSupplierInternal(
                        descriptionForResource(springResource),
                        fileForResource(springResource),
                        springResource::getInputStream);
    }

    /**
     * Creates a {@link DataSource} for given {@link File}.
     * If <code>null</code>, an 'empty' DataSource is returned.
     */
    static DataSource ofFile(final @Nullable File file) {
        return file==null
                ? empty()
                : ofInputStreamSupplierInternal(
                        descriptionForFile(file),
                        Optional.of(file),
                    ()->Try.call(()->new FileInputStream(FileUtils.existingFileElseFail(file)))
                        .valueAsNonNullElseFail());
    }

    /**
     * Creates a {@link DataSource} for given {@link String}.
     * If <code>null</code> or empty, an 'empty' DataSource is returned.
     */
    static DataSource ofString(final @Nullable String string, final Charset charset) {
        return _Strings.isNullOrEmpty(string)
                ? empty()
                : ofInputStreamSupplierInternal(
                        descriptionForString(string),
                        ()->new ByteArrayInputStream(string.getBytes(charset)));
    }

    /**
     * Creates a {@link DataSource} for given {@link String}.
     * If <code>null</code> or empty, an 'empty' DataSource is returned.
     */
    static DataSource ofStringUtf8(final @Nullable String string) {
        return ofString(string, StandardCharsets.UTF_8);
    }

    /**
     * Creates a {@link DataSource} for given byte array.
     * If <code>null</code> or empty, an 'empty' DataSource is returned.
     */
    static DataSource ofBytes(final @Nullable byte[] bytes) {
        return _NullSafe.isEmpty(bytes)
                ? empty()
                : ofInputStreamSupplierInternal(
                        descriptionForBytes(bytes),
                        ()->new ByteArrayInputStream(bytes));
    }
    
    /**
     * Optionally returns the underlying {@link File},
     * based on whether this resource originates from a file.
     */
    default Optional<File> getFile() {
        return Optional.empty();
    }
    
    /**
     * The given file-consumer is either passed the underlying {@link File}
     * (if this resource originates from a file), 
     * or a temporary file.
     * <p>In the temporary file case, the temporary file is deleted after consumption. 
     */
    @SneakyThrows
    default void consumeAsFile(ThrowingConsumer<File> fileConsumer) {
        var file = getFile().orElse(null);
        if(file!=null) {
            fileConsumer.accept(file);
            return;
        }
        var tempFile = File.createTempFile("causeway", "ds");
        try {
            tryReadAndWrite(DataSink.ofFile(tempFile), 4096);
            fileConsumer.accept(tempFile);
        } finally {
            Files.deleteIfExists(tempFile.toPath()); // cleanup
        }
    }

    /**
     * Return a description for this DataSource,
     * to be used for error output when working with the resource.
     */
    default String getDescription() {
        return "";
    }

    // -- HELPER
    
    // internal factory
    private static DataSource ofInputStreamSupplierInternal(
            final @NonNull String description,
            final @NonNull ThrowingSupplier<InputStream> inputStreamSupplier) {
        return ofInputStreamSupplierInternal(description, Optional.empty(), inputStreamSupplier);
    }
    
    // internal factory
    private static DataSource ofInputStreamSupplierInternal(
            final @NonNull String description,
            final Optional<File> file,
            final @NonNull ThrowingSupplier<InputStream> inputStreamSupplier) {
        return new DataSource() {
            @Override public <T> Try<T> tryReadAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return Try.call(()->{
                    try(final InputStream is = inputStreamSupplier.get()) {
                        return consumingMapper.apply(is);
                    }
                })
                // unwrap the inner try
                .mapSuccessAsNullable(wrappedTry->wrappedTry.valueAsNullableElseFail());
            }
            @Override public Optional<File> getFile() {
                return file;
            }
            @Override public String getDescription() {
                return description;
            }
            @Override
            public String toString() {
                return description;
            }
        };
    }
    
    private static String descriptionForEmpty() {
        return "Empty-Resource";
    }

    private static String descriptionForBytes(final byte[] bytes) {
        if(bytes.length>16) {
            byte[] sample = new byte[16];
            System.arraycopy(bytes, 0, sample, 0, sample.length);
            return String.format("Byte-Resource[%s ...]", _Bytes.hexDump(sample));    
        }
        return String.format("Byte-Resource[%s]", _Bytes.hexDump(bytes));
    }

    private static String descriptionForString(final String string) {
        return String.format("String-Resource[%s]", _Strings.ellipsifyAtEnd(string, 25, "..."));
    }

    private static String descriptionForResource(final Resource springResource) {
        return springResource.getDescription();
    }

    private static String descriptionForResource(final Class<?> cls, final String resourcePath) {
        return String.format("Class-Resource[%s, %s]", cls.getName(), resourcePath);
    }
    
    private static String descriptionForMapped(DataSource ds) {
        return ds.getDescription() + " mapped";
    }
    
    private static String descriptionForFile(File file) {
        return String.format("File-Resource[%s]", file.getPath());
    }
    
    private static Optional<File> fileForResource(final Resource springResource) {
        return springResource.isFile()
                ? Try.call(springResource::getFile).getValue()
                : Optional.empty();
    }

}
