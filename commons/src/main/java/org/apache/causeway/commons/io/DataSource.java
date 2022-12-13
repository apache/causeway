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
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Try;
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

    // -- FACTORIES

    /**
     * Acts as a no-op.
     */
    static DataSource none() {
        return new DataSource() {
            @Override public <T> Try<T> readAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return Try.empty();
            }
        };
    }

    static DataSource fromInputStreamSupplier(final @NonNull Supplier<InputStream> inputStreamSupplier) {
        return new DataSource() {
            @Override public <T> Try<T> readAll(final @NonNull Function<InputStream, Try<T>> consumingMapper) {
                return Try.call(()->{
                    try(final InputStream is = inputStreamSupplier.get()) {
                        return consumingMapper.apply(is);
                    }
                })
                // unwrap the inner try
                .mapSuccess(wrappedTry->wrappedTry.getValue().get());
            }
        };
    }

    static DataSource ofResource(final @NonNull Class<?> cls, final @NonNull String resourcePath) {
        return fromInputStreamSupplier(()->cls.getResourceAsStream(resourcePath));
    }

    static DataSource ofFile(final @NonNull File file) {
        return fromInputStreamSupplier(()->Try.call(()->new FileInputStream(file)).ifFailureFail().getValue().orElseThrow());
    }

    static DataSource ofString(final @Nullable String string, final Charset charset) {
        return _Strings.isNullOrEmpty(string)
                ? none()
                : fromInputStreamSupplier(()->new ByteArrayInputStream(string.getBytes(charset)));
    }

    static DataSource ofStringUtf8(final @Nullable String string) {
        return ofString(string, StandardCharsets.UTF_8);
    }

}
