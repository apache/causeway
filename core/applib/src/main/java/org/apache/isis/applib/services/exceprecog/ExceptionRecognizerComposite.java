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

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.annotation.Hidden;

import com.google.common.collect.Lists;

/**
 * Convenience implementation of {@link ExceptionRecognizer} that loops through a list of
 * {@link #add(ExceptionRecognizer) registered} services.
 * 
 * <p>
 * Note that the framework <i>does</i> allow multiple {@link ExceptionRecognizer service}s
 * to be registered in <tt>isis.properties</tt>, and each will be consulted in turn.  Therefore
 * it is not necessary to use this class to register more than one such service.  However,
 * it may be useful to use to treat a group of similar exceptions as a single unit.  For example,
 * the <i>JDO object store</i> (in its applib) provides a set of {@link ExceptionRecognizer} to
 * recognize various types of constraint violations.  These are grouped together as a single
 * set through the use of this class.
 */
@Hidden
public class ExceptionRecognizerComposite implements ExceptionRecognizer {

    private final List<ExceptionRecognizer> services = Lists.newArrayList();

    public ExceptionRecognizerComposite(ExceptionRecognizer... exceptionRecognizers) {
        this(Arrays.asList(exceptionRecognizers));
    }
    
    public ExceptionRecognizerComposite(List<ExceptionRecognizer> exceptionRecognizers) {
        for (ExceptionRecognizer er : exceptionRecognizers) {
            add(er);
        }
    }

    /**
     * Register an {@link ExceptionRecognizer} to be consulted when
     * {@link #recognize(Exception)} is called.
     * 
     * <p>
     * The most specific {@link ExceptionRecognizer recognizer}s should be registered
     * before the more general ones.  See the <i>JDO object store</i> applib for
     * an example.
     */
    public void add(ExceptionRecognizer ers) {
        services.add(ers);
    }
    
    /**
     * Returns the non-<tt>null</tt> message of the first {@link #add(ExceptionRecognizer) add}ed 
     * {@link ExceptionRecognizer service} that recognizes the exception. 
     */
    public String recognize(Throwable ex) {
        for (ExceptionRecognizer ers : services) {
            String message = ers.recognize(ex);
            if(message != null) {
                return message;
            }
        }
        return null;
    }
}
