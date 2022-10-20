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
module org.apache.causeway.viewer.commons.applib {
    exports org.apache.causeway.viewer.commons.applib.services.userprof;
    exports org.apache.causeway.viewer.commons.applib.services.header;
    exports org.apache.causeway.viewer.commons.applib.services.branding;
    exports org.apache.causeway.viewer.commons.applib.services.menu;
    exports org.apache.causeway.viewer.commons.applib;
    exports org.apache.causeway.viewer.commons.applib.mixins;

    requires java.inject;
    requires lombok;
    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
    requires spring.context;
    requires spring.core;
}