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
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;

import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.function.ThrowingConsumer;
import org.springframework.util.function.ThrowingFunction;
import org.springframework.util.function.ThrowingSupplier;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.HashUtils.HashAlgorithm;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

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
        val self = this;
        return new DataSource() {
            @Override public <T> Try<T> tryReadAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return self.tryReadAll(is->consumingMapper.apply(inputStreamMapper.apply(is)));
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
                val buffer = new byte[bufferSize]; int n;
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
                : ofInputStreamSupplier(()->cls.getResourceAsStream(resourcePath));
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
                : ofInputStreamSupplier(springResource::getInputStream);
    }

    /**
     * Creates a {@link DataSource} for given {@link File}.
     * If <code>null</code>, an 'empty' DataSource is returned.
     */
    static DataSource ofFile(final @Nullable File file) {
        return file==null
                ? empty()
                : ofInputStreamSupplier(
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
                : ofInputStreamSupplier(()->new ByteArrayInputStream(string.getBytes(charset)));
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
                : ofInputStreamSupplier(()->new ByteArrayInputStream(bytes));
    }

}
