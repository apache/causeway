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

import java.io.PrintStream;
import java.util.function.Consumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.Try;

import lombok.NonNull;
import lombok.val;

public interface DtoMapper<T> {

    public T read(@NonNull DataSource dataSource);
    public void write(@Nullable T dto, @NonNull DataSink dataSink);

    @Nullable
    default T read(final @Nullable String source) {
        if(source==null) return null;
        return read(DataSource.ofStringUtf8(source));
    }

    @Nullable
    default String toString(final @Nullable T dto) {
        if(dto==null) return null;
        class StringHolder implements Consumer<String> {
            String s;
            @Override public void accept(final String s) { this.s = s; }
        }
        val sh = new StringHolder();
        write(dto, DataSink.ofStringUtf8Consumer(sh));
        return sh.s;
    }

    // -- CLONE

    default Try<T> tryClone(final @Nullable T dto) {
        return Try.call(()->clone(dto));
    }

    default T clone(final @Nullable T dto) {
        if(dto==null) return dto;
        class BytesHolder implements Consumer<byte[]> {
            byte[] b;
            @Override public void accept(final byte[] b) { this.b = b; }
        }
        val bh = new BytesHolder();
        write(dto, DataSink.ofByteArrayConsumer(bh));
        return read(DataSource.ofBytes(bh.b));
    }

    // -- DEBUG

    default void dump(final @Nullable T dto, final PrintStream out) {
        out.println(toString(dto));
    }

}
