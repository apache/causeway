/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.progmodel.facets;

import org.apache.isis.core.commons.lang.StringUtils;

public class MethodPrefixConstants {

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";
    public static final String CLEAR_PREFIX = "clear";
    public static final String MODIFY_PREFIX = "modify";
    public static final String ADD_TO_PREFIX = "addTo";
    public static final String REMOVE_FROM_PREFIX = "removeFrom";
    
    public static final String NAME_PREFIX = "name";
    public static final String DESCRIPTION_PREFIX = "description";
    public static final String OPTIONAL_PREFIX = "optional";
    
    public static final String DEFAULT_PREFIX = "default";
    public static final String CHOICES_PREFIX = "choices";
    
    public static final String HIDE_PREFIX = "hide";
    public static final String ALWAYS_HIDE_PREFIX = "alwaysHide";

    public static final String DISABLE_PREFIX = "disable";
    public static final String PROTECT_PREFIX = "protect";

    public static final String VALIDATE_PREFIX = "validate";
    public static final String VALIDATE_ADD_TO_PREFIX = VALIDATE_PREFIX + ADD_TO_PREFIX;
    public static final String VALIDATE_ADD_TO_PREFIX_2 = VALIDATE_PREFIX + StringUtils.capitalize(ADD_TO_PREFIX);
    public static final String VALIDATE_REMOVE_FROM_PREFIX = VALIDATE_PREFIX + REMOVE_FROM_PREFIX;
    public static final String VALIDATE_REMOVE_FROM_PREFIX_2 = VALIDATE_PREFIX + StringUtils.capitalize(REMOVE_FROM_PREFIX);

}
