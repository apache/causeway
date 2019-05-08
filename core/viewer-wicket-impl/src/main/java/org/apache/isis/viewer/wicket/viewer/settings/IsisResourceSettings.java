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

package org.apache.isis.viewer.wicket.viewer.settings;

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Localizer;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.apache.wicket.resource.loader.ComponentStringResourceLoader;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.resource.loader.InitializerStringResourceLoader;
import org.apache.wicket.resource.loader.PackageStringResourceLoader;
import org.apache.wicket.resource.loader.ValidatorStringResourceLoader;
import org.apache.wicket.settings.ResourceSettings;
import org.apache.wicket.util.lang.Generics;

import lombok.Getter;

public class IsisResourceSettings extends ResourceSettings {

    /** Chain of string resource loaders to use */
    private final List<IStringResourceLoader> stringResourceLoaders = Generics.newArrayList(4);

	@Getter(onMethod=@__({@Override}), lazy=true)
	private final Localizer localizer = new Localizer();

    /**
     * Configures Wicket's default ResourceLoaders.
     *
     * <p>
     * In contrast to the default lookup strategy, Isis' searches for application-specific properties first.
     *
     * </p>
     * For an example in {@code FooApplication} let {@code bar.Foo} extend {@link org.apache.wicket.Component}, this
     * results in the following ordering:
     * <dl>
     * <dt>application specific</dt>
     * <dd>
     * <ul>
     * <li>FooApplication.properties</li>
     * <li>Application.properties</li>
     * </ul>
     * </dd>
     * <dt>component specific</dt>
     * <dd>
     * <ul>
     * <li>bar/Foo.properties</li>
     * <li>org/apache/wicket/Component.properties</li>
     * </ul>
     * </dd>
     * <dt>package specific</dt>
     * <dd>
     * <ul>
     * <li>bar/package.properties</li>
     * <li>package.properties (on Foo's class loader)</li>
     * <li>org/apache/wicket/package.properties</li>
     * <li>org/apache/package.properties</li>
     * <li>org/package.properties</li>
     * <li>package.properties (on Component's class loader)</li>
     * </ul>
     * </dd>
     * <dt>validator specific</dt>
     * <dt>Initializer specific</dt>
     * <dd>
     * <ul>
     * <li>bar.Foo.properties (Foo implementing IInitializer)</li>
     * </ul>
     * </dd>
     * </dl>
     *
     * @param application
     */
    public IsisResourceSettings(Application application) {
        super(application);

        // consult first (the default implementation checks this only third)
        stringResourceLoaders.add(new ClassStringResourceLoader(application.getClass()));

        stringResourceLoaders.add(new ComponentStringResourceLoader());
        stringResourceLoaders.add(new PackageStringResourceLoader());
        // this is where the default implementation registered the search.
        //stringResourceLoaders.add(new ClassStringResourceLoader(application.getClass()));
        stringResourceLoaders.add(new ValidatorStringResourceLoader());
        stringResourceLoaders.add(new InitializerStringResourceLoader(application.getInitializers()));
    }

    /**
     * @see org.apache.wicket.settings.ResourceSettings#getStringResourceLoaders()
     */
    @Override
    public List<IStringResourceLoader> getStringResourceLoaders() {
        return stringResourceLoaders;
    }


}
