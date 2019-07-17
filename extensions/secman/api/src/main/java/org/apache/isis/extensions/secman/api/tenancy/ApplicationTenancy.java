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
package org.apache.isis.extensions.secman.api.tenancy;

import org.apache.isis.extensions.secman.api.SecurityModule;

public interface ApplicationTenancy {
	
    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationTenancy, T> {
        private static final long serialVersionUID = 1L;}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationTenancy, T> {
        private static final long serialVersionUID = 1L;}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationTenancy> {
        private static final long serialVersionUID = 1L;}

    

    public static final int MAX_LENGTH_PATH = 255;
    public static final int MAX_LENGTH_NAME = 40;
    public static final int TYPICAL_LENGTH_NAME = 20;

}
