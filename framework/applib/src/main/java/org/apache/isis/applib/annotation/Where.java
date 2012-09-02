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

package org.apache.isis.applib.annotation;

import org.apache.isis.applib.util.Enums;

public enum Where {
    /**
     * The member should be disabled/hidden everywhere.
     */
    EVERYWHERE,
    /**
     * The member should be disabled/hidden when displayed within an object form.
     * 
     * <p>
     * For most viewers, this applies to property and collection members, not actions.
     */
    OBJECT_FORM, 
    /**
     * The member should be disabled/hidden when displayed as a column of a table within
     * an object's collection.
     * 
     * <p>
     * For most (all?) viewers, this will have meaning only if applied to a property member.
     */
    COLLECTION_TABLE,
    /**
     * The member should be disabled/hidden when displayed as a column of a table showing a standalone list
     * of objects, for example as returned by a repository query.
     * 
     * <p>
     * For most (all?) viewers, this will have meaning only if applied to a property member.
     */
    STANDALONE_TABLE,
    /**
     * The member should be disabled/hidden when displayed as a column of a table, either an object's
     * collection or a standalone list.
     * 
     * <p>
     * This combines {@link #COLLECTION_TABLE} and {@link #STANDALONE_TABLE}.
     */
    ALL_TABLES;
    
    public String getFriendlyName() {
        return Enums.getFriendlyNameOf(this);
    }
}
