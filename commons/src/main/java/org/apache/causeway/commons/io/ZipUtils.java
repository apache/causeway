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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.functions._Predicates;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

/**
 * Utilities to zip and unzip data.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class ZipUtils {

    //XXX record candidate
    @Builder
    @Value @Accessors(fluent=true)
    public static class ZipOptions {
        @Builder.Default
        private final int bufferSize = 64*1024; // 64k
        /**
         * The {@link java.nio.charset.Charset charset} to be
         *        used to decode the ZIP entry name (ignored if the
         *        <a href="package-summary.html#lang_encoding"> language
         *        encoding bit</a> of the ZIP entry's general purpose bit
         *        flag is set).
         */
        @Builder.Default @NonNull
        private final Charset zipEntryCharset = StandardCharsets.UTF_8;

        @Builder.Default @NonNull
        private final Predicate<ZipEntry> zipEntryFilter = _Predicates.alwaysTrue();
    }

    //XXX record candidate
    @RequiredArgsConstructor
    public static class ZipEntryDataSource implements DataSource {
        @Getter @Accessors(fluent=true)
        private final ZipEntry zipEntry;
        private final byte[] bytes;

        @Override
        public <T> Try<T> tryReadAll(@NonNull final Function<InputStream, Try<T>> consumingMapper) {
            try {
                try(val bis = new ByteArrayInputStream(bytes)){
                    return consumingMapper.apply(bis);
                }
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }
    }

    /**
     * Returns a {@link Stream} of {@link ZipEntryDataSource}, buffered in memory,
     * which allows consumption even after the underlying zipped {@link DataSource} was closed.
     * @implNote Only partly optimized for heap usage, as it just reads all pre-filtered data into memory,
     *      but doing so, regardless of what is actually consumed later from the returned {@link Stream}.
     */
    public Stream<ZipEntryDataSource> streamZipEntries(
            final DataSource zippedSource,
            final ZipOptions zipOptions) {

        val zipEntryDataSources = _Lists.<ZipEntryDataSource>newArrayList();

        zippedSource.tryReadAndApply(is->{
            try(final ZipInputStream in = new ZipInputStream(
                    new BufferedInputStream(is, zipOptions.bufferSize()),
                    zipOptions.zipEntryCharset())) {

                ZipEntry zipEntry;
                while((zipEntry = in.getNextEntry())!=null) {
                    if(zipEntry.isDirectory()) continue;
                    if(zipOptions.zipEntryFilter().test(zipEntry)) {
                        zipEntryDataSources.add(
                                new ZipEntryDataSource(zipEntry, DataSource.ofInputStreamSupplier(()->in).bytes()));
                    }
                }
            }
            return null;
        })
        .mapFailure(IOException::new)
        .ifFailureFail();

        return zipEntryDataSources.stream();
    }

    /**
     * Shortcut for {@code streamZipEntries(zippedSource, ZipOptions.builder().build())}
     * @see #streamZipEntries(DataSource, ZipOptions)
     */
    public Stream<ZipEntryDataSource> streamZipEntries(
            final DataSource zippedSource) {
        return streamZipEntries(zippedSource, ZipOptions.builder().build());
    }

}
