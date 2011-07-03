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
package org.apache.isis.viewer.xhtml.viewer;

import org.apache.isis.viewer.xhtml.applib.providers.StringApplicationXhtmlXmlProvider;
import org.apache.isis.viewer.xhtml.viewer.resources.HomePageResourceImpl;
import org.apache.isis.viewer.xhtml.viewer.resources.objects.ObjectResourceImpl;
import org.apache.isis.viewer.xhtml.viewer.resources.services.ServicesResourceImpl;
import org.apache.isis.viewer.xhtml.viewer.resources.specs.SpecsResourceImpl;
import org.apache.isis.viewer.xhtml.viewer.resources.user.UserResourceImpl;

public class XhtmlApplication extends AbstractJaxRsApplication {

    public XhtmlApplication() {
        addSingleton(new HomePageResourceImpl());
        addSingleton(new ObjectResourceImpl());
        addSingleton(new ServicesResourceImpl());
        addSingleton(new SpecsResourceImpl());
        addSingleton(new UserResourceImpl());

        addClass(StringApplicationXhtmlXmlProvider.class);
    }

}
