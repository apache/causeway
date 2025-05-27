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
module org.apache.causeway.valuetypes.vega.ui.wkt {
    exports org.apache.causeway.valuetypes.vega.ui.wkt;
    exports org.apache.causeway.valuetypes.vega.ui.wkt.components;

    requires static lombok;
    requires org.slf4j;

    requires org.apache.causeway.applib;
    requires org.apache.causeway.core.config;
    requires org.apache.causeway.core.metamodel;
    requires org.apache.causeway.valuetypes.vega.applib;
    requires org.apache.causeway.viewer.commons.model;
    requires org.apache.causeway.viewer.wicket.model;
    requires org.apache.causeway.viewer.wicket.ui;
    requires org.apache.wicket.core;
    requires org.apache.wicket.util;
    requires spring.context;
}