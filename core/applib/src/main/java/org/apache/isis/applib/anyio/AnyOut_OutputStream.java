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
import java.io.OutputStream;
import java.util.function.Function;
import java.util.function.Supplier;

class AnyOut_OutputStream implements AnyOut {

    private final Supplier<Try<OutputStream>> outputStreamSupplier;

    public AnyOut_OutputStream(Supplier<Try<OutputStream>> outputStreamSupplier) {
        this.outputStreamSupplier = outputStreamSupplier;
    }

    @Override
    public <T> Try<T> tryApplyOutputStream(Function<OutputStream, Try<T>> outputConsumer) {
        
        Try<OutputStream> try_os = outputStreamSupplier.get();
        if(try_os.isFailure()) {
            return Try.failure(try_os.getFailure());
        }
        
        try(OutputStream os = try_os.getResult()) {
            
            Try<T> _try = outputConsumer.apply(os);
            return _try;
            
        } catch (IOException e) {

            return Try.failure(e);
            
        } 
    }

    
}
