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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.isis.commons.internal.base._Strings;

/**
 * Universal data source.
 * 
 * @since 2.0.0-M2
 */
public interface AnyIn {

    // -- INTERFACE
    
    <T> Try<T> tryApplyInputStream(Function<InputStream, Try<T>> inputConsumer);

    // -- FACTORIES

    static AnyIn ofTryInputStream(final Supplier<Try<InputStream>> inputStreamSupplier) {
        return new AnyIn_InputStream(inputStreamSupplier);
    }
    
    static AnyIn ofFile(final File file) {
        return ofTryInputStream(()->{
            try {
                InputStream fis = new FileInputStream(file);
                return Try.success(fis);
            } catch (FileNotFoundException e) {
                return Try.failure(e);
            }
        });
    }
    
    static AnyIn ofInputStream(final Supplier<InputStream> inputStreamSupplier) {
        return ofTryInputStream(()->Try.success(inputStreamSupplier.get()));
    }
    
    static AnyIn ofBytes(final byte[] bytes) {
        return ofInputStream(()->new ByteArrayInputStream(bytes));
    }
    
    static AnyIn ofString(final String string, Charset charset) {
        return ofBytes(_Strings.toBytes(string, charset));
    }
    
}

