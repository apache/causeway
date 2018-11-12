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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Universal data sink.
 * 
 * @since 2.0.0-M2
 */
public interface AnyOut {

    // -- INTERFACE
    
    <T> Try<T> tryApplyOutputStream(Function<OutputStream, Try<T>> outputConsumer);

    // -- FACTORIES
    
    static AnyOut ofTryOutputStream(final Supplier<Try<OutputStream>> outputStreamSupplier) {
        return new AnyOut_OutputStream(outputStreamSupplier);
    }
    
    static AnyOut ofFile(final File file) {
        return ofTryOutputStream(()->{
            try {
                OutputStream fos = new FileOutputStream(file);
                return Try.success(fos);
            } catch (FileNotFoundException e) {
                return Try.failure(e);
            }
        });
    }
    
    static AnyOut ofOutputStream(final Supplier<OutputStream> outputStreamSupplier) {
        return ofTryOutputStream(()->Try.success(outputStreamSupplier.get()));
    }
    
    static AnyOutBuffer buffer(final int buffersize) {
        return new AnyOutBuffer(buffersize);
    }
    
    static AnyOutBuffer buffer16k() {
        return buffer(1024*16); 
    }
    
}
