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
package org.apache.isis.viewer.restfulobjects.jaxrsresteasy4;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.conneg.RestfulObjectsJaxbWriterForXml;
import org.apache.isis.viewer.restfulobjects.jaxrsresteasy4.webmodule.WebModuleJaxrsResteasy4;
import org.apache.isis.viewer.restfulobjects.viewer.IsisModuleViewerRestfulObjectsViewer;

/**
 * @since 1.x {@index}
 */
@Configuration
@Import({
        // modules
        IsisModuleViewerRestfulObjectsViewer.class,

        // @Service's
        WebModuleJaxrsResteasy4.class,

        // @Component's
        RestfulObjectsJaxbWriterForXml.class,


})
public class IsisModuleViewerRestfulObjectsJaxrsResteasy4 {

}
