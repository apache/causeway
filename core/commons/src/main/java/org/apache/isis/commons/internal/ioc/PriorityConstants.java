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
package org.apache.isis.commons.internal.ioc;

import javax.annotation.Priority;

/**
 * To use with the {@link Priority} annotation.
 */
public class PriorityConstants {

    /**
     * To use with the {@link Priority} annotation.
     */
    public static final int PRIORITY_HIGHEST = Integer.MIN_VALUE;

    /**
     * To use with the {@link Priority} annotation.
     */
    public static final int PRIORITY_ABOVE_DEFAULT = -100;
    
    /**
     * To use with the {@link Priority} annotation.
     */
    public static final int PRIORITY_DEFAULT = 0;
    
    /**
     * To use with the {@link Priority} annotation.
     */
    public static final int PRIORITY_BELOW_DEFAULT = 100;
    
    /**
     * To use with the {@link Priority} annotation.
     */
    public static final int PRIORITY_LOWEST = Integer.MAX_VALUE;
    
}
