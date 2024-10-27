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
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.functions._Predicates;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
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
                try(var bis = new ByteArrayInputStream(bytes)){
                    return consumingMapper.apply(bis);
                }
            } catch (Throwable e) {
                return Try.failure(e);
            }
        }

        public static ZipEntryDataSource of(
                final @NonNull ZipEntry zipEntry,
                final @Nullable byte[] bytes) {
            return new ZipEntryDataSource(zipEntry, _NullSafe.toNonNull(bytes));
        }

        public static ZipEntryDataSource of(
                final @NonNull ZipEntry zipEntry,
                final @NonNull DataSource dataSource) {
            return of(zipEntry, dataSource.bytes());
        }

        @SneakyThrows
        void writeTo(
                final ZipOutputStream zipOutputStream) {
            zipOutputStream.putNextEntry(zipEntry());
            if(!_NullSafe.isEmpty(bytes)) {
                zipOutputStream.write(bytes);
            }
            zipOutputStream.closeEntry();
        }
    }

    // -- READING

    /**
     * Returns a {@link Stream} of {@link ZipEntryDataSource}, buffered in memory,
     * which allows consumption even after the underlying zipped {@link DataSource} was closed.
     * @implNote Only partly optimized for heap usage, as it just reads all pre-filtered data into memory,
     *      doing so, regardless of what is actually consumed later from the returned {@link Stream}.
     */
    @SneakyThrows
    public Stream<ZipEntryDataSource> streamZipEntries(
            final @NonNull DataSource zippedSource,
            final @NonNull ZipOptions zipOptions) {

        var zipEntryDataSources = _Lists.<ZipEntryDataSource>newArrayList();
        
        zippedSource.consumeAsFile(zipFile->{
            try (FileSystem fs = FileSystems.newFileSystem(zipFile.toPath(), null)) {
                try (Stream<Path> entries = Files.walk(fs.getPath("/"))) {
                    final List<Path> filesInZip = entries.filter(Files::isRegularFile).collect(Collectors.toList());
                    for(Path path : filesInZip) {
                        var zipEntry = new ZipEntry(path.toString());
                        if(!zipOptions.zipEntryFilter().test(zipEntry)) continue;
                        var bytes = Files.readAllBytes(path);
                        zipEntryDataSources.add(new ZipEntryDataSource(zipEntry, bytes));
                    }
                }
            }
        });
        
        return zipEntryDataSources.stream();
    }

    /*
     * Former implementation: did not require a temp file,
     * but may result in Exception 'Only DEFLATED entries can have EXT descriptor'
     * @see https://bugs.openjdk.org/browse/JDK-8327690
     */
    /*
    public Stream<ZipEntryDataSource> streamZipEntries(
            final @NonNull DataSource zippedSource,
            final @NonNull ZipOptions zipOptions) {

        var zipEntryDataSources = _Lists.<ZipEntryDataSource>newArrayList();

        zippedSource.tryReadAndAccept(is->{
            try(final ZipInputStream in = new ZipInputStream(
                    new BufferedInputStream(is, zipOptions.bufferSize()),
                    zipOptions.zipEntryCharset())) {

                ZipEntry zipEntry;
                while((zipEntry = in.getNextEntry())!=null) {
                    if(zipEntry.isDirectory()) continue;
                    if(zipOptions.zipEntryFilter().test(zipEntry)) {
                        zipEntryDataSources.add(
                                new ZipEntryDataSource(zipEntry, _Bytes.ofKeepOpen(in)));
                    }
                }
            }
        })
        .ifFailureFail();

        return zipEntryDataSources.stream();
    }*/

    /**
     * Shortcut for {@code streamZipEntries(zippedSource, ZipOptions.builder().build())}
     * @see #streamZipEntries(DataSource, ZipOptions)
     */
    public Stream<ZipEntryDataSource> streamZipEntries(
            final @NonNull DataSource zippedSource) {
        return streamZipEntries(zippedSource, ZipOptions.builder().build());
    }

    /**
     * Optionally the first zip-entry as {@link ZipEntryDataSource}, based on whether an entry exists.
     */
    public Optional<ZipEntryDataSource> firstZipEntry(
            final @NonNull DataSource zippedSource,
            final @NonNull ZipOptions zipOptions) {

        var zipEntryDataSources = _Lists.<ZipEntryDataSource>newArrayList(1);

        zippedSource.tryReadAndAccept(is->{
            try(final ZipInputStream in = new ZipInputStream(
                    new BufferedInputStream(is, zipOptions.bufferSize()),
                    zipOptions.zipEntryCharset())) {

                ZipEntry zipEntry;
                while((zipEntry = in.getNextEntry())!=null) {
                    if(zipEntry.isDirectory()) continue;
                    if(zipOptions.zipEntryFilter().test(zipEntry)) {
                        zipEntryDataSources.add(
                                new ZipEntryDataSource(zipEntry, _Bytes.ofKeepOpen(in)));
                        return; // stop further processing
                    }
                }
            }
        })
        .ifFailureFail();

        return _Lists.firstElement(zipEntryDataSources);
    }

    /**
     * Shortcut for {@code firstZipEntry(zippedSource, ZipOptions.builder().build())}
     * @see #firstZipEntry(DataSource, ZipOptions)
     */
    public Optional<ZipEntryDataSource> firstZipEntry(
            final @NonNull DataSource zippedSource) {
        return firstZipEntry(zippedSource, ZipOptions.builder().build());
    }

    // -- WRITING

    public static byte[] zipToBytes(final @NonNull Stream<ZipEntryDataSource> entryStream) {
        var buffer = DataPeer.inMemory(16*1024); // 16k default
        writeTo(entryStream, buffer);
        return buffer.bytes();
    }

    @SneakyThrows
    public static void writeTo(final @NonNull Stream<ZipEntryDataSource> entryStream, final @NonNull DataSink dataSink) {
        dataSink.writeAll(os->{
            try(var zos = new ZipOutputStream(os)) {
                entryStream.forEach(entry->entry.writeTo(zos));
            }
        });
    }

    // -- ENTRY BUILDER

    public static class EntryBuilder {

        private final List<ZipEntryDataSource> entries = new ArrayList<>();

        public EntryBuilder add(final @NonNull ZipEntryDataSource zipEntryDataSource){
            entries.add(zipEntryDataSource);
            return this;
        }

        // -- SHORTCUTS

        public EntryBuilder add(final @NonNull String entryName, final @Nullable byte[] bytes){
            return add(ZipEntryDataSource.of(new ZipEntry(entryName), bytes));
        }

        public EntryBuilder add(final @NonNull String entryName, final @NonNull DataSource dataSource){
            return add(entryName, dataSource.bytes());
        }

        public EntryBuilder add(final @NonNull String entryName, final @Nullable String string, final @NonNull Charset charset){
            return add(entryName, _Strings.toBytes(string, charset));
        }

        public EntryBuilder addAsUtf8(final @NonNull String entryName, final @Nullable String string){
            return add(entryName, _Strings.toBytes(string, StandardCharsets.UTF_8));
        }

        // -- TERMINALS

        public Stream<ZipEntryDataSource> stream(){
            return entries.stream();
        }

        public void writeTo(final DataSink dataSink) {
            ZipUtils.writeTo(stream(), dataSink);
        }

        public byte[] toBytes() {
            return ZipUtils.zipToBytes(stream());
        }

    }

    /**
     * typical example:
     * <pre>{@code
     * var builder = ZipUtils.zipEntryBuilder();
     * for (Map.Entry<String, String> entry : schemaMap.entrySet()) {
     *     var namespaceUri = entry.getKey();
     *     var schemaText = entry.getValue();
     *     builder.addAsUtf8(zipEntryNameFor(namespaceUri), schemaText);
     * }
     * return Blob.of(fileName, CommonMimeType.ZIP, builder.toBytes());
     * }
     * <pre>
     */
    public EntryBuilder zipEntryBuilder() {
        return new EntryBuilder();
    }

}
