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
package org.apache.isis.extensions.secman.jpa.dom.constants;

public final class NamedQueryNames {

    public static final String TENANCY_BY_NAME = "ApplicationTenancy.findByName";
    public static final String TENANCY_BY_PATH = "ApplicationTenancy.findByPath";
    public static final String TENANCY_BY_NAME_OR_PATH_MATCHING = "ApplicationTenancy.findByNameOrPathMatching";

    public static final String USER_BY_USERNAME = "ApplicationUser.findByUsername";
    public static final String USER_BY_EMAIL = "ApplicationUser.findByEmailAddress";
    public static final String USER_FIND = "ApplicationUser.find";
    public static final String USER_BY_ATPATH = "ApplicationUser.findByAtPath";

}
