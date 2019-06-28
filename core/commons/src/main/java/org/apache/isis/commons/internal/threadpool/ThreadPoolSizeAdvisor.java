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

package org.apache.isis.commons.internal.threadpool;

final class ThreadPoolSizeAdvisor {

    /*
     * For the thread-pool let there be at least 4 concurrent threads, 
     * limited by the number of available (logical) processor cores, 
     * but with a maximum of 16.
     *
     * Note: Future improvements might make these values configurable,
     * but for now lets try to be reasonable.
     *
     */
    private final int minThreadCount = 4; // allows for a maximum concurrent task nesting depth of 3
    private final int maxThreadCount = 16; // reasonable upper limit

    // if we are running in a container, eg docker, its up to the JVM to report correct values
    private final int logicalCoreCount = 
            Runtime.getRuntime().availableProcessors(); // reasonable upper limit    
    
    int corePoolSize() {
        return minThreadCount;
    }
    
    int maximumPoolSize() {
        final int upperLimit = Math.min(maxThreadCount, logicalCoreCount);
        return Math.max(minThreadCount, upperLimit);
    }
    
    public static ThreadPoolSizeAdvisor get() {
        return new ThreadPoolSizeAdvisor();
    }
    
    
}
