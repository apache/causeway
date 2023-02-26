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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.ThrowingConsumer;
import org.apache.causeway.commons.functional.ThrowingFunction;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;

/**
 * General purpose readable byte data source.
 *
 * @since 2.0 {@index}
 */
@FunctionalInterface
public interface DataSource {

    <T> Try<T> readAll(@NonNull Function<InputStream, Try<T>> consumingMapper);

    /**
     * Passes an {@link InputStream} to given {@link Function} for application.
     * @return either a successful or failed {@link Try} (non-null),
     *      where in the success case, the returned Try is holding the returned value from the given {@link Function inputStreamMapper};
     *      if the InputStream is absent or not readable, the returned Try will hold the underlying {@link Exception}
     */
    default <T> Try<T> tryReadAndApply(final @NonNull ThrowingFunction<InputStream, T> inputStreamMapper) {
        return readAll(inputStream->
            Try.call(()->inputStreamMapper.apply(inputStream)));
    }

    /**
     * Passes an {@link InputStream} to given {@link Consumer} for consumption.
     * @return either a successful or failed {@link Try} (non-null);
     *     if the InputStream is absent or not readable, the returned Try will hold the underlying {@link Exception}
     */
    default Try<Void> tryReadAndAccept(final @NonNull ThrowingConsumer<InputStream> inputStreamConsumer) {
        return readAll(inputStream->
            Try.run(()->inputStreamConsumer.accept(inputStream)));
    }

    // -- FACTORIES

    /**
     * Acts as a no-op.
     */
    static DataSource empty() {
        return new DataSource() {
            @Override public <T> Try<T> readAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return Try.empty();
            }
        };
    }

    /**
     * Creates a {@link DataSource} for given InputStream Supplier.
     * @param inputStreamSupplier - required non-null
     * @throws NullPointerException - if the single argument is null
     */
    static DataSource ofInputStreamSupplier(final @NonNull Supplier<InputStream> inputStreamSupplier) {
        return new DataSource() {
            @Override public <T> Try<T> readAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return Try.call(()->{
                    try(final InputStream is = inputStreamSupplier.get()) {
                        return consumingMapper.apply(is);
                    }
                })
                // unwrap the inner try
                .mapEmptyToFailure()
                .mapSuccessAsNullable(wrappedTry->wrappedTry.valueAsNonNullElseFail());
            }
        };
    }

    /**
     * Creates a {@link DataSource} for given resource path relative to {@link Class}.
     * @param cls - required non-null
     * @param resourcePath - required non-null
     * @throws NullPointerException - if the any argument is null
     */
    static DataSource ofResource(final @NonNull Class<?> cls, final @NonNull String resourcePath) {
        return cls==null
                ? empty()
                : ofInputStreamSupplier(()->cls.getResourceAsStream(resourcePath));
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
                        .ifFailureFail()
                        .getValue().orElseThrow());
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
