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
package demoapp.dom.types.primitive.booleans.holder;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;

//tag::class[]
public interface PrimitiveBooleanHolder {

//end::class[]
    @Property
    @PropertyLayout(group = "read-only-properties", sequence = "1") // TODO: doesn't seem to get picked up
//tag::class[]
    boolean isReadOnlyProperty();
    void setReadOnlyProperty(boolean c);

//end::class[]
    @Property
    @PropertyLayout(group = "editable-properties", sequence = "1") // TODO: doesn't seem to get picked up
//tag::class[]
    boolean isReadWriteProperty();
    void setReadWriteProperty(boolean c);

}
//end::class[]
