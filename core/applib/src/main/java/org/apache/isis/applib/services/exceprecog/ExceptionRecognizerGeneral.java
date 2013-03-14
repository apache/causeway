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
package org.apache.isis.applib.services.exceprecog;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import org.apache.isis.applib.annotation.Hidden;

/**
 * A general purpose implementation of {@link ExceptionRecognizer} that looks 
 * exceptions meeting the {@link Predicate} supplied in the constructor
 * and, if found anywhere in the {@link Throwables#getCausalChain(Throwable) causal chain},
 * then returns a non-null message indicating that the exception has been recognized.
 * 
 * <p>
 * If a messaging-parsing {@link Function} is provided through the constructor,
 * then the message can be altered.  Otherwise the exception's {@link Throwable#getMessage() message} is returned as-is.
 */
@Hidden
public class ExceptionRecognizerGeneral extends ExceptionRecognizerAbstract {

    private final Predicate<Throwable> predicate;
    private final Function<String,String> messageParser;

    public ExceptionRecognizerGeneral(Predicate<Throwable> predicate, final Function<String,String> messageParser) {
        this.predicate = predicate;
        this.messageParser = messageParser != null? messageParser: Functions.<String>identity();
    }

    public ExceptionRecognizerGeneral(Predicate<Throwable> predicate) {
        this(predicate, null);
    }

    public String recognize(Throwable ex) {
        List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for (Throwable throwable : causalChain) {
            if(predicate.apply(throwable)) {
                return messageParser.apply(throwable.getMessage());
            }
        }
        return null;
    }
}
