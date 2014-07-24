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

package org.apache.isis.core.wrapper.internal.util;

public final class Constants {
    private Constants() {
    }

    public static final String PREFIX_CHOICES = "choices";
    public static final String PREFIX_DEFAULT = "default";
    public static final String PREFIX_HIDE = "hide";
    public static final String PREFIX_DISABLE = "disable";
    public static final String PREFIX_VALIDATE_REMOVE_FROM = "validateRemoveFrom";
    public static final String PREFIX_VALIDATE_ADD_TO = "validateAddTo";
    public static final String PREFIX_VALIDATE = "validate";
    public static final String PREFIX_REMOVE_FROM = "removeFrom";
    public static final String PREFIX_ADD_TO = "addTo";
    public static final String PREFIX_MODIFY = "modify";
    public static final String PREFIX_CLEAR = "clear";
    public static final String PREFIX_SET = "set";
    public static final String PREFIX_GET = "get";

    public final static String TITLE_METHOD_NAME = "title";
    public final static String TO_STRING_METHOD_NAME = "toString";

    /**
     * Cannot invoke methods with these prefixes.
     */
    public final static String[] INVALID_PREFIXES = { PREFIX_MODIFY, PREFIX_CLEAR, PREFIX_DISABLE, PREFIX_VALIDATE, PREFIX_VALIDATE_ADD_TO, PREFIX_VALIDATE_REMOVE_FROM, PREFIX_HIDE, };

    public final static String[] PROPERTY_PREFIXES = { PREFIX_GET, PREFIX_SET, PREFIX_MODIFY, PREFIX_CLEAR, PREFIX_DISABLE, PREFIX_VALIDATE, PREFIX_HIDE, PREFIX_DEFAULT, PREFIX_CHOICES };
    public final static String[] COLLECTION_PREFIXES = { PREFIX_GET, PREFIX_SET, PREFIX_ADD_TO, PREFIX_REMOVE_FROM, PREFIX_DISABLE, PREFIX_VALIDATE_ADD_TO, PREFIX_VALIDATE_REMOVE_FROM, PREFIX_HIDE, PREFIX_DEFAULT, PREFIX_CHOICES };
    public final static String[] ACTION_PREFIXES = { PREFIX_VALIDATE, PREFIX_DISABLE, PREFIX_HIDE, PREFIX_DEFAULT, PREFIX_CHOICES, };

}
