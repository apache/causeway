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

package org.apache.isis.runtime.profiler;

import org.apache.isis.runtime.profiler.ProfilerSystem;

public class ProfilerTestSystem extends ProfilerSystem {
    long[] memory = new long[] { 10000, 20300, 30000 };
    int memoryIndex = 0;
    long[] time = new long[] { 1000, 1100, 1300, 1600, 2000 };
    int timeIndex = 0;

    @Override
    protected long memory() {
        return memory[memoryIndex++];
    }

    @Override
    protected long time() {
        return time[timeIndex++];
    }
}
