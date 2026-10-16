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
module org.apache.causeway.valuetypes.markdown.applib {
    exports org.apache.causeway.valuetypes.markdown.applib;
    exports org.apache.causeway.valuetypes.markdown.applib.value;
    exports org.apache.causeway.valuetypes.markdown.applib.jaxb;

    requires static lombok;

    requires flexmark;
    requires flexmark.ext.gfm.strikethrough; //unstable automatic module name
    requires flexmark.ext.tables; //unstable automatic module name
    requires flexmark.util.ast; //unstable automatic module name
    requires flexmark.util.builder; //unstable automatic module name
    requires flexmark.util.data; //unstable automatic module name
    requires flexmark.util.misc; //unstable automatic module name
    requires flexmark.util.sequence; //unstable automatic module name
    requires jakarta.inject;
    requires jakarta.xml.bind;

    requires org.apache.causeway.applib;
    requires org.apache.causeway.commons;
    requires spring.context;
}