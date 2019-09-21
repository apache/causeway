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

package org.apache.isis.wrapper.internal.util;

import static org.apache.isis.metamodel.facets.MethodPrefixConstants.ADD_TO_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.CHOICES_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.CLEAR_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.DEFAULT_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.DISABLE_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.GET_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.HIDE_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.MODIFY_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.REMOVE_FROM_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.SET_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.VALIDATE_ADD_TO_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.VALIDATE_PREFIX;
import static org.apache.isis.metamodel.facets.MethodPrefixConstants.VALIDATE_REMOVE_FROM_PREFIX;

public final class Constants {
    private Constants() {
    }

    public final static String TITLE_METHOD_NAME = "title";
    public final static String TO_STRING_METHOD_NAME = "toString";


    public final static String[] PROPERTY_PREFIXES = { 
            GET_PREFIX, 
            SET_PREFIX, 
            MODIFY_PREFIX, 
            CLEAR_PREFIX, 
            DISABLE_PREFIX, 
            VALIDATE_PREFIX, 
            HIDE_PREFIX, 
            DEFAULT_PREFIX, 
            CHOICES_PREFIX };
    
    public final static String[] COLLECTION_PREFIXES = { 
            GET_PREFIX, 
            SET_PREFIX, 
            ADD_TO_PREFIX, 
            REMOVE_FROM_PREFIX, 
            DISABLE_PREFIX, 
            VALIDATE_ADD_TO_PREFIX, 
            VALIDATE_REMOVE_FROM_PREFIX, 
            HIDE_PREFIX, 
            DEFAULT_PREFIX, 
            CHOICES_PREFIX };
    
    public final static String[] ACTION_PREFIXES = { 
            VALIDATE_PREFIX, 
            DISABLE_PREFIX, 
            HIDE_PREFIX, 
            DEFAULT_PREFIX,
            CHOICES_PREFIX, };

}
