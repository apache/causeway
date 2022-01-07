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
package demoapp.dom.domain.properties.PropertyLayout.describedAs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.PropertyLayout;

//tag::class[]
@PropertyLayout(describedAs = "Described from meta-annotation")     // <.>
@ParameterLayout(describedAs = "Described from meta-annotation")    // <.>
@Inherited
@Target({
        ElementType.METHOD, ElementType.FIELD,                      // <.>
        ElementType.PARAMETER,                                      // <.>
        ElementType.TYPE                                            // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface DescribedAsMetaAnnotation {

}
//end::class[]
