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
package demoapp.dom.types.javautil.javautildate.holder;

import org.apache.isis.applib.annotation.LogicalTypeName;

@LogicalTypeName("demo.JavaUtilDateHolder")
//tag::class[]
public interface JavaUtilDateHolder {

    java.util.Date getReadOnlyProperty();
    void setReadOnlyProperty(java.util.Date c);

    java.util.Date getReadWriteProperty();
    void setReadWriteProperty(java.util.Date c);

    java.util.Date getReadOnlyOptionalProperty();
    void setReadOnlyOptionalProperty(java.util.Date c);

    java.util.Date getReadWriteOptionalProperty();
    void setReadWriteOptionalProperty(java.util.Date c);

}
//end::class[]
