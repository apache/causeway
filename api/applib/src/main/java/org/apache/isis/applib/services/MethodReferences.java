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
package org.apache.isis.applib.services;

import lombok.experimental.UtilityClass;

/**
 * 
 * @since 2.0 {@index}
 */
@UtilityClass
public class MethodReferences {

    // -- METHOD REFERENCE MATCHERS (WITHOUT RETURN VALUE)
    
    @FunctionalInterface
    public static interface Run0 {
        void run();
    }
    
    @FunctionalInterface
    public static interface Run1<A0> {
        void run(A0 arg0);
    }
    
    @FunctionalInterface
    public static interface Run2<A0, A1> {
        void run(A0 arg0, A1 arg1);
    }
    
    @FunctionalInterface
    public static interface Run3<A0, A1, A2> {
        void run(A0 arg0, A1 arg1, A2 arg2);
    }
    
    @FunctionalInterface
    public static interface Run4<A0, A1, A2, A3> {
        void run(A0 arg0, A1 arg1, A2 arg2, A3 arg3);
    }
    
    @FunctionalInterface
    public static interface Run5<A0, A1, A2, A3, A4> {
        void run(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    }
    
    // -- METHOD REFERENCE MATCHERS (WITH RETURN VALUE)
    
    @FunctionalInterface
    public static interface Call0<R> {
        R call();
    }
    
    @FunctionalInterface
    public static interface Call1<R, A0> {
        R call(A0 arg0);
    }
    
    @FunctionalInterface
    public static interface Call2<R, A0, A1> {
        R call(A0 arg0, A1 arg1);
    }
    
    @FunctionalInterface
    public static interface Call3<R, A0, A1, A2> {
        R call(A0 arg0, A1 arg1, A2 arg2);
    }
    
    @FunctionalInterface
    public static interface Call4<R, A0, A1, A2, A3> {
        R call(A0 arg0, A1 arg1, A2 arg2, A3 arg3);
    }
    
    @FunctionalInterface
    public static interface Call5<R, A0, A1, A2, A3, A4> {
        R call(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4);
    }
    
}
