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
package org.apache.isis.webapp.util;

import javax.servlet.ServletContext;

import org.springframework.web.context.support.WebApplicationContextUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;

/**
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IsisWebAppUtils {

    public static <T> T getManagedBean(Class<T> requiredType, ServletContext servletContext) {
        val webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        return webApplicationContext.getBean(requiredType);
    }
    
}
