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
package demoapp.dom.types.javasql.javasqldate.holder;

import javax.inject.Named;

import java.sql.Date;

@Named("demo.JavaSqlDateHolder")
//tag::class[]
public interface JavaSqlDateHolder {

    Date getReadOnlyProperty();
    void setReadOnlyProperty(Date c);

    Date getReadWriteProperty();
    void setReadWriteProperty(Date c);

    Date getReadOnlyOptionalProperty();
    void setReadOnlyOptionalProperty(Date c);

    Date getReadWriteOptionalProperty();
    void setReadWriteOptionalProperty(Date c);

}
//end::class[]
