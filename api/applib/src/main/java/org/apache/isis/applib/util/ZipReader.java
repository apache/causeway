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
package org.apache.isis.applib.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * Zip utility for processing compressed input.
 * 
 * @since 2.0 {@index}
 * @see ZipWriter
 */
public class ZipReader {
    
    /**
     * BiPredicate stating whether to continue visiting after consuming {@link ZipEntry}.
     * <p>
     * The passed in {@link ZipInputStream} corresponds to given {@link ZipEntry} and must not be closed.
     */
    public static interface ZipVisistor extends BiPredicate<ZipEntry, ZipInputStream> {
    }
    
    /**
     * BiFunction that stops visiting after the result is non-null for given {@link ZipEntry}.
     * <p>
     * The passed in {@link ZipInputStream} corresponds to given {@link ZipEntry} and must not be closed.
     */
    public static interface ZipDigester<R> extends BiFunction<ZipEntry, ZipInputStream, R> {
    }

    @SneakyThrows
    public static void read(
            final @Nullable InputStream inputStream, 
            final @NonNull Charset charset, 
            final @NonNull ZipVisistor zipVisistor) {
        
        if(inputStream==null) {
            return; // no-op
        }
        
        try(ZipInputStream in = new ZipInputStream(new BufferedInputStream(inputStream, 64*1024), charset)){
            ZipEntry entry;
            while((entry=in.getNextEntry())!=null) {
                if(!zipVisistor.test(entry, in)) {
                    return; // break request from visitor
                }
            }
        }
    }

    @SneakyThrows
    public static <R> Optional<R> digest(
            final @Nullable InputStream inputStream, 
            final @NonNull Charset charset, 
            final @NonNull ZipDigester<R> zipDigester) {
        
        if(inputStream==null) {
            return Optional.empty();
        }
        
        try(ZipInputStream in = new ZipInputStream(new BufferedInputStream(inputStream, 64*1024), charset)){
            ZipEntry entry;
            while((entry=in.getNextEntry())!=null) {
                val digest = zipDigester.apply(entry, in);
                if(digest!=null) {
                    return Optional.of(digest); 
                }
            }
        }
        
        return Optional.empty();
    }
    
    
}
