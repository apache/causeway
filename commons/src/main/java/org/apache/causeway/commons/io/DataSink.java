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
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.causeway.commons.functional.Try;

import lombok.NonNull;

/**
 * General purpose writable byte data sink.
 *
 * @since 2.0 {@index}
 */
@FunctionalInterface
public interface DataSink {

    /**
     * Re-throws any {@link Exception} from the mapped {@link Try},
     * when the Try is a failure case.
     */
    void writeAll(@NonNull Function<OutputStream, Try<Void>> consumingMapper);

    // -- FACTORIES

    /**
     * Acts as a no-op.
     */
    static DataSink none() {
        return consumingMapper -> {};
    }

    static DataSink ofOutputStreamSupplier(final @NonNull Supplier<OutputStream> outputStreamSupplier) {
        return outputConsumer ->
            Try.call(()->{
                try(final OutputStream os = outputStreamSupplier.get()) {
                    return outputConsumer.apply(os);
                }
            })
            .ifFailureFail() // throw if any Exception outside the call to 'outputConsumer.apply(os)'
            // unwrap the inner Try<Void>
            .getValue().orElseThrow()
            .ifFailureFail(); // throw if any Exception within the call to 'outputConsumer.apply(os)'
    }

    static DataSink ofFile(final @NonNull File file) {
        return ofOutputStreamSupplier(()->Try.call(()->new FileOutputStream(file)).ifFailureFail().getValue().orElseThrow());
    }

    static DataSink ofByteArrayConsumer(final @NonNull Consumer<byte[]> byteArrayConsumer) {
        return outputConsumer ->
            Try.call(()->{
                try(final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    var innerTry = outputConsumer.apply(bos);
                    byteArrayConsumer.accept(bos.toByteArray());
                    return innerTry;
                }
            })
            .ifFailureFail() // throw if any Exception outside the call to 'outputConsumer.apply(os)'
            // unwrap the inner Try<Void>
            .getValue().orElseThrow()
            .ifFailureFail(); // throw if any Exception within the call to 'outputConsumer.apply(os)'
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

    static DataSink ofStringUtf8Consumer(final @NonNull StringBuilder stringUtf8Consumer) {
        return ofStringConsumer(stringUtf8Consumer, StandardCharsets.UTF_8);
    }

}
