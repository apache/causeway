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
module org.apache.causeway.security.api {
    exports org.apache.causeway.core.security;
    exports org.apache.causeway.core.security._testing;
    exports org.apache.causeway.core.security.authentication.fixtures;
    exports org.apache.causeway.core.security.authentication.login;
    exports org.apache.causeway.core.security.authentication.logout;
    exports org.apache.causeway.core.security.authentication.manager;
    exports org.apache.causeway.core.security.authentication.singleuser;
    exports org.apache.causeway.core.security.authentication.standard;
    exports org.apache.causeway.core.security.authentication;
    exports org.apache.causeway.core.security.authorization.manager;
    exports org.apache.causeway.core.security.authorization.standard;
    exports org.apache.causeway.core.security.authorization;
    exports org.apache.causeway.core.security.util;

    requires java.annotation;
    requires java.desktop;
    requires java.inject;
    requires lombok;
    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.schema;
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;
}