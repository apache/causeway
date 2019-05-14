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
package org.apache.isis.commons.internal.cdi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.functions._Functions.CheckedRunnable;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
final class _CDI_Lifecycle implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(_CDI_Lifecycle.class);
    
    private final CheckedRunnable onClose;
    
    @Override
    public void close() {
        try {
            onClose.run();
        } catch (Exception e) {
            val note = "This implementation expects the IocPlugin to provide a CDIProvider "
                    + "that creates CDI instances that implement AutoClosable";
            
            LOG.warn("Failed to properly close the CDI container. Note: {}", note, e);
        }
    }
    
}
