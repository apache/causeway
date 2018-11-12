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
package org.apache.isis.applib.anyio;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.apache.isis.commons.internal.base._Strings;

/**
 * Universal in-memory sink, that can be read from after having been written to.
 *  
 * @since 2.0.0-M2
 */
public class AnyOutBuffer implements AnyOut {

    private final ByteArrayOutputStream buffer;
    private Try<?> lastTry;

    AnyOutBuffer(int buffersize) {
        buffer = new ByteArrayOutputStream(buffersize);
    }

    @Override
    public <T> Try<T> tryApplyOutputStream(Function<OutputStream, Try<T>> outputConsumer) {
        if(lastTry!=null) {
            throw new IllegalStateException("Buffer was already written to.");
        }
        Try<T> _try = outputConsumer.apply(buffer);
        lastTry = _try;
        return _try;
    }

    public Try<byte[]> tryReadBytes(){
        if(lastTry!=null && lastTry.isFailure()) {
            return Try.failure(lastTry.getFailure());
        }
        return Try.success(buffer.toByteArray());
    }

    public Try<CharSequence> tryReadCharacters(Charset charset){
        return tryReadBytes().map(bytes->_Strings.ofBytes(bytes, charset));
    }

}
