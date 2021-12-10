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
package demoapp.dom.types.javatime.javatimelocaldate.holder;

import org.apache.isis.applib.annotation.LogicalTypeName;

@LogicalTypeName("demo.JavaTimeLocalDateHolder")
//tag::class[]
public interface JavaTimeLocalDateHolder {

    java.time.LocalDate getReadOnlyProperty();
    void setReadOnlyProperty(java.time.LocalDate c);

    java.time.LocalDate getReadWriteProperty();
    void setReadWriteProperty(java.time.LocalDate c);

    java.time.LocalDate getReadOnlyOptionalProperty();
    void setReadOnlyOptionalProperty(java.time.LocalDate c);

    java.time.LocalDate getReadWriteOptionalProperty();
    void setReadWriteOptionalProperty(java.time.LocalDate c);

}
//end::class[]
