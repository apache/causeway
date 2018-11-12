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

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Supplier;

class AnyIn_InputStream implements AnyIn {

    private final Supplier<Try<InputStream>> inputStreamSupplier;

    public AnyIn_InputStream(Supplier<Try<InputStream>> inputStreamSupplier) {
        this.inputStreamSupplier = inputStreamSupplier;
    }

    @Override
    public <T> Try<T> tryApplyInputStream(Function<InputStream, Try<T>> inputConsumer) {
        
        Try<InputStream> try_is = inputStreamSupplier.get();
        if(try_is.isFailure()) {
            return Try.failure(try_is.getFailure());
        }
        
        try(InputStream is = try_is.getResult()) {

            Try<T> _try = inputConsumer.apply(is);
            return _try;

        } catch (IOException e) {

            return Try.failure(e);

        } 

    }

}

