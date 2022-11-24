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
package org.apache.causeway.applib;

import java.util.NoSuchElementException;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.registry.ServiceRegistry;

/**
 * Indicates that an object belongs to the UI/application layer and is intended to be used as a view-model.
 * <p>
 * Instances of {@link ViewModel} must include (exactly) one public constructor.
 * <p>
 * Contract:
 * <ul>
 * <li>there is either exactly one public constructor or if there are more than one,
 * then only one of these is annotated with any of {@code @Inject} or {@code @Autowired(required=true)}
 * (meta-annotations are also considered)</li>
 * <li>the constructor may have arbitrary many arguments of arbitrary type</li>
 * <li>first {@link String} argument found is passed in the view-model's memento</li>
 * <li>any other arguments are resolved via the {@link ServiceRegistry} -
 *      if no <i>Bean</i> can be found a {@link NoSuchElementException} is thrown</li>
 * <li>there is no support for <i>Spring</i> programming model specific annotations on constructor arguments (perhaps future work)</li>
 * </ul>
 * Naturally this also allows for the idiom of passing in the {@link ServiceInjector} as an argument
 * and programmatically resolve any field-style injection points via {@link ServiceInjector#injectServicesInto(Object)},
 * that is, if already required during <i>construction</i>.
 * <p>
 * After a view-model got new-ed up by the framework (or programmatically via the {@link FactoryService}),
 * {@link ServiceInjector#injectServicesInto(Object)} is called on the viewmodel instance,
 * regardless of what happened during <i>construction</i>.
 *
 * @since 1.x - revised for 2.0 {@index}
 */
public interface ViewModel {

    /**
     * Obtain a memento of the view-model. (Optional)
     * <p>
     * Captures the state of this view-model as {@link String},
     * which can be passed in to this view-model's constructor for later re-construction.
     */
    @Programmatic
    String viewModelMemento();

}
