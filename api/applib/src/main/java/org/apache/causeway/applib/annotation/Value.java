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

package org.apache.causeway.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Named;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Indicates that the class has value semantics.
 *
 * @since 1.x {@index}
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component @Scope("prototype")
public @interface Value {

    /**
     * The logical name of this value's type, that uniquely and fully qualifies it.
     * The logical name is analogous to - but independent of - the actual fully qualified class name.
     * eg. {@code sales.Customer} for a class 'org.mycompany.dom.Customer'
     * <p>
     * If not specified, the fully qualified class name is used instead.
     * </p>
      * @deprecated use {@link Named} instead
     * @see Named
     */
    @Deprecated(forRemoval = true, since = "2.0.0-M8")
    String logicalTypeName()
            default "";

}
