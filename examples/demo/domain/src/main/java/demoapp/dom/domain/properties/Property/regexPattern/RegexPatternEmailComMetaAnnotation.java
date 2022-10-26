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
package demoapp.dom.domain.properties.Property.regexPattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

//tag::class[]
@Property(
    regexPattern = "^\\w+@\\w+[.]com$"                          // <.>
    , regexPatternReplacement = "Must be .com email address"    // <.>
    , regexPatternFlags = Pattern.CASE_INSENSITIVE              // <.>
)
@Parameter(
    regexPattern = "^\\w+@\\w+[.]com$"                          // <.>
    , regexPatternReplacement = "Must be .com email address"    // <.>
    , regexPatternFlags = Pattern.CASE_INSENSITIVE              // <.>
)
@PropertyLayout(
    describedAs =
        "@Parameter(regexPattern = \"^\\w+@\\w+[.]com$\")"
)
@ParameterLayout(
    describedAs =
        "@Parameter(regexPattern = \"^\\w+@\\w+[.]com$\")"
)
@Inherited
@Target({
    ElementType.METHOD, ElementType.FIELD,                      // <.>
    ElementType.PARAMETER,                                      // <.>
})
@Retention(RetentionPolicy.RUNTIME)
public @interface RegexPatternEmailComMetaAnnotation {

}
//end::class[]
