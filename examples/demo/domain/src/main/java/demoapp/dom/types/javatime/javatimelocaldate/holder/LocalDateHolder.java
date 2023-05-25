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

import javax.inject.Named;

import java.time.LocalDate;

@Named("demo.LocalDateHolder")
//tag::class[]
public interface LocalDateHolder {

    LocalDate getReadOnlyProperty();
    void setReadOnlyProperty(LocalDate c);

    LocalDate getReadWriteProperty();
    void setReadWriteProperty(LocalDate c);

    LocalDate getReadOnlyOptionalProperty();
    void setReadOnlyOptionalProperty(LocalDate c);

    LocalDate getReadWriteOptionalProperty();
    void setReadWriteOptionalProperty(LocalDate c);

}
//end::class[]