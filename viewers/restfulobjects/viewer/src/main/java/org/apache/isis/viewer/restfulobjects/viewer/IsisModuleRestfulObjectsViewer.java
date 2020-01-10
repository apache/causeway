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

package org.apache.isis.viewer.restfulobjects.viewer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.viewer.restfulobjects.rendering.IsisModuleRestfulObjectsRendering;
import org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader.AcceptHeaderServiceForRest;
import org.apache.isis.viewer.restfulobjects.viewer.mappers.ExceptionMapperForObjectNotFound;
import org.apache.isis.viewer.restfulobjects.viewer.mappers.ExceptionMapperForRestfulObjectsApplication;
import org.apache.isis.viewer.restfulobjects.viewer.mappers.ExceptionMapperForRuntimeException;
import org.apache.isis.viewer.restfulobjects.viewer.resources.DomainObjectResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.DomainServiceResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.DomainTypeResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.HomePageResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.ImageResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.MenuBarsResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.SwaggerSpecResource;
import org.apache.isis.viewer.restfulobjects.viewer.resources.UserResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.VersionResourceServerside;
import org.apache.isis.core.webapp.IsisModuleWebapp;

/**
 * 
 * @since 2.0
 */
@Configuration
@Import({
        // modules
        IsisModuleWebapp.class,
        IsisModuleRestfulObjectsRendering.class,
        
        // @Component's
        HomePageResourceServerside.class,
        DomainTypeResourceServerside.class,
        UserResourceServerside.class,
        MenuBarsResourceServerside.class,
        ImageResourceServerside.class,
        DomainObjectResourceServerside.class,
        DomainServiceResourceServerside.class,
        VersionResourceServerside.class,
        SwaggerSpecResource.class,

        ExceptionMapperForRestfulObjectsApplication.class,
        ExceptionMapperForRuntimeException.class,
        ExceptionMapperForObjectNotFound.class,
        AcceptHeaderServiceForRest.RequestFilter.class,
        AcceptHeaderServiceForRest.ResponseFilter.class,

})
public class IsisModuleRestfulObjectsViewer {

}
