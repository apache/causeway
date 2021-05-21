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
package org.apache.isis.core.metamodel.commons;

/**
 * Provides a mechanism to avoid infinite loops
 * whereby method A -&gt; method B -&gt; method C -&gt; method A and so on.
 */
public final class Wormhole {

    private Wormhole(){}

    private ThreadLocal<Boolean> inWormhole = ThreadLocal.<Boolean>withInitial(()->Boolean.FALSE);

    public void run(final Runnable runnable) {
        try {
            if(inWormhole.get()) {
                return;
            }
            inWormhole.set(true);
            runnable.run();
        } finally {
            inWormhole.remove();
        }
    }

    public static void invoke(final Runnable runnable) {
        new Wormhole().run(runnable);
    }
}
