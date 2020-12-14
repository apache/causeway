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
package org.apache.isis.commons.internal.resources;

import org.apache.isis.commons.collections.Can;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilities for locating resources.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
@Value
public final class _ResourceCoordinates implements Comparable<_ResourceCoordinates> {
    
    private final @NonNull Can<String> components;
    
    @Override
    public int compareTo(final @NonNull _ResourceCoordinates o) {
        
        val compa = this.getComponents();
        val compb = o.getComponents();
        
        val a = compa.iterator();
        val b = compb.iterator();
        
        while(a.hasNext() || b.hasNext()) {
            
            val left = a.hasNext() ? a.next() : "";
            val right = b.hasNext() ? b.next() : "";
            
            int c = left.compareTo(right);
            if(c!=0) {
                return c;
            } 
            
        }
        return 0;
    }
    

}
