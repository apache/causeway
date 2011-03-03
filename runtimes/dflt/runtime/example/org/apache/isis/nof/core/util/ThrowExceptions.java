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


package org.apache.isis.nof.core.util;

import org.apache.isis.noa.ObjectAdapterRuntimeException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


public class ThrowExceptions {
    private static final Logger LOG = Logger.getLogger(ThrowExceptions.class);

    public static void main(final String[] args) {
        method1();
    }

    private static void method1() {
        method2();
    }

    private static void method2() {
        method3();
    }

    private static void method3() {
        BasicConfigurator.configure();

        ObjectAdapterRuntimeException exception = new ObjectAdapterRuntimeException("exception message");

        LOG.info("Testing logging", exception);
        LOG.info("");
        LOG.info("");
        System.out.println();

        try {
            method4();
        } catch (Exception e) {
            ObjectAdapterRuntimeException exception2 = new ObjectAdapterRuntimeException("cascading exception message", e);

            LOG.info("Testing logging 2", exception2);
            LOG.info("");
            LOG.info("");
            System.out.println();

            throw exception2;
        }
    }

    private static void method4() {
        method5();
    }

    private static void method5() {
        throw new NullPointerException("system exception message");
    }
}
