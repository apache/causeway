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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the (service) action should be not be displayed in the menu.
 * 
 * <p>
 * It may still be contributed (unless it has been annotated as
 * {@link NotContributed}). If {@link Hidden}, then also implies that the menu
 * should not appear in the service menu.
 * 
 * <p>
 * Has no meaning if annotated on an action of a regular entity.
 * 
 * @deprecated - instead move such actions into a separate domain service and specify nature of {@link org.apache.isis.applib.annotation.NatureOfService#VIEW_CONTRIBUTIONS_ONLY contributions} or {@link org.apache.isis.applib.annotation.NatureOfService#DOMAIN domain} using {@link DomainService#nature()}.
 */
@Deprecated
@Inherited
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotInServiceMenu {
}
