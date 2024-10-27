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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.function.Function;

import org.apache.causeway.commons.functional.ThrowingConsumer;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

/**
 * General purpose byte data peer that acts as source and sink at the same time.
 *
 * @since 2.0 {@index}
 */
public interface DataPeer extends DataSink, DataSource {

    static DataPeer inMemory() {
        return inMemory(32);
    }

    static DataPeer inMemory(final int initialBufferSize) {

        var byteArrayHolder = new ArrayList<byte[]>(1);

        return new DataPeer() {
            @Override
            public <T> Try<T> tryReadAll(@NonNull final Function<InputStream, Try<T>> consumingMapper) {
                var in = DataSource.ofBytes(bytes());
                return in.tryReadAll(consumingMapper);
            }

            @Override
            public void writeAll(@NonNull final ThrowingConsumer<OutputStream> outputStreamConsumer) {
                if(!byteArrayHolder.isEmpty()) {
                    throw _Exceptions.illegalState("Cannot writeAll to an in-memory DataPeer, that was already written to.");
                }
                var out = DataSink.ofByteArrayConsumer(byteArrayHolder::add, initialBufferSize);
                out.writeAll(outputStreamConsumer);
            }

            @Override
            public byte[] bytes() {
                return byteArrayHolder.isEmpty()
                        ? new byte[0]
                        : byteArrayHolder.get(0);
            }

        };
    }

}
