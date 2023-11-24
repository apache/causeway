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
module org.apache.isis.persistence.jpa.metamodel {
    exports org.apache.isis.persistence.jpa.metamodel.facets.prop.transients;
    exports org.apache.isis.persistence.jpa.metamodel;
    exports org.apache.isis.persistence.jpa.metamodel.object.table;
    exports org.apache.isis.persistence.jpa.metamodel.facets.prop.column;

    requires java.inject;
    requires java.persistence;
    requires lombok;
    requires org.apache.isis.applib;
    requires org.apache.isis.commons;
    requires org.apache.isis.core.config;
    requires org.apache.isis.core.metamodel;
    requires org.apache.isis.core.runtime;
    requires org.apache.isis.persistence.jpa.applib;
    requires spring.context;
    requires org.apache.isis.persistence.commons;
}
