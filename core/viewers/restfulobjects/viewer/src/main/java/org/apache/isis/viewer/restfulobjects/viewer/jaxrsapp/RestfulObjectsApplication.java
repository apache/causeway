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
package org.apache.isis.viewer.restfulobjects.viewer.jaxrsapp;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader.AcceptHeaderServiceForRest;
import org.apache.isis.viewer.restfulobjects.viewer.IsisJaxrsServerPlugin;
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

public class RestfulObjectsApplication extends AbstractJaxRsApplication {

    public static final String SPEC_VERSION = "1.0.0";

    public RestfulObjectsApplication() {
        addClass(HomePageResourceServerside.class);
        addClass(DomainTypeResourceServerside.class);
        addClass(UserResourceServerside.class);
        addClass(MenuBarsResourceServerside.class);
        addClass(ImageResourceServerside.class);
        addClass(DomainObjectResourceServerside.class);
        addClass(DomainServiceResourceServerside.class);
        addClass(VersionResourceServerside.class);

        addClass(SwaggerSpecResource.class);

        //override=false, allows for IsisWicketApplication to take precedence
        _Context.setDefaultClassLoader(this.getClass().getClassLoader(), /*override*/false);
        addSingleton(IsisJaxrsServerPlugin.get().newRestfulObjectsJaxbWriterForXml());


        addSingleton(new ExceptionMapperForRestfulObjectsApplication());
        addSingleton(new ExceptionMapperForRuntimeException());
        addSingleton(new ExceptionMapperForObjectNotFound());

        addSingleton(new AcceptHeaderServiceForRest.RequestFilter());
        addSingleton(new AcceptHeaderServiceForRest.ResponseFilter());

        // TODO: doesn't get injected
        // addSingleton(new TypedReprBuilderFactoryRegistry());

        // TODO: idea being to remove the init()
        // addSingleton(new PreProcessInterceptorForIsisSession());
    }

}
