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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.apache.causeway.commons.functional.ThrowingConsumer;
import org.apache.causeway.commons.functional.ThrowingSupplier;
import org.apache.causeway.commons.functional.Try;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * General purpose writable byte data sink.
 *
 * @since 2.0 {@index}
 */
@FunctionalInterface
public interface DataSink {

    /**
     * Offers this {@link DataSink}'s {@link OutputStream} to the caller,
     * so it can write data to it.
     */
    void writeAll(@NonNull final ThrowingConsumer<OutputStream> outputStreamConsumer);

    // -- FACTORIES

    /**
     * Acts as a no-op.
     */
    static DataSink none() {
        return consumingMapper -> {};
    }

    static DataSink ofOutputStreamSupplier(final @NonNull ThrowingSupplier<OutputStream> outputStreamSupplier) {
        return new DataSink() {
            @Override @SneakyThrows
            public void writeAll(final @NonNull ThrowingConsumer<OutputStream> outputStreamConsumer) {
                try(final OutputStream os = outputStreamSupplier.get()) {
                    outputStreamConsumer.accept(os);
                }
            }
        };
    }

    static DataSink ofByteArrayConsumer(final @NonNull ThrowingConsumer<byte[]> byteArrayConsumer, final int initalBufferSize) {
        return new DataSink() {
            @Override @SneakyThrows
            public void writeAll(final @NonNull ThrowingConsumer<OutputStream> outputStreamConsumer) {
                try(final ByteArrayOutputStream bos = new ByteArrayOutputStream(initalBufferSize)) {
                    outputStreamConsumer.accept(bos);
                    byteArrayConsumer.accept(bos.toByteArray());
                }
            }
        };
    }

    static DataSink ofByteArrayConsumer(final @NonNull ThrowingConsumer<byte[]> byteArrayConsumer) {
        // using the default initalBufferSize from constructor ByteArrayOutputStream()
        return ofByteArrayConsumer(byteArrayConsumer, 32);
    }

    static DataSink ofFile(final @NonNull File file) {
        return ofOutputStreamSupplier(()->Try.call(()->new FileOutputStream(file)).valueAsNonNullElseFail());
    }

    static DataSink ofStringConsumer(final @NonNull Consumer<String> stringConsumer, final @NonNull Charset charset) {
        return ofByteArrayConsumer(bytes->stringConsumer.accept(new String(bytes, charset)));
    }

    static DataSink ofStringUtf8Consumer(final @NonNull Consumer<String> stringUtf8Consumer) {
        return ofStringConsumer(stringUtf8Consumer, StandardCharsets.UTF_8);
    }

    static DataSink ofStringConsumer(final @NonNull StringBuilder stringConsumer, final @NonNull Charset charset) {
        return ofByteArrayConsumer(bytes->stringConsumer.append(new String(bytes, charset)));
    }

    /**
     * Example:
     * <pre>
     * var sb = new StringBuffer();
     * var dataSink = DataSink.ofStringUtf8Consumer(sb);
     * //... write to dataSink
     * String result = sb.toString(); // read the buffer
     * </pre>
     */
    static DataSink ofStringUtf8Consumer(final @NonNull StringBuilder stringUtf8Consumer) {
        return ofStringConsumer(stringUtf8Consumer, StandardCharsets.UTF_8);
    }

}
