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

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.stereotype.Component;

/**
 * An object that is conceptually part of the application layer, and which surfaces behaviour and/or state that
 * is aggregate of one or more domain entity.
 *
 * <p>
 *     The identity of a view model is determined solely by the state of object's properties (that have
 *     not been set to be ignored using {@link org.apache.isis.applib.annotation.Property#notPersisted()}).
 *     Using this nature should be considered exactly equivalent to annotating with {@link DomainObject#nature()} with
 *     a nature of {@link Nature#VIEW_MODEL}.
 * </p>
 *
 * <p>
 *     Note that collections are ignored; if their state is required to fully identify the view model, define the view
 *     model using the JAXB {@link XmlRootElement} annotation instead (where the object's state is serialized
 *     to an arbitrarily deep graph of data, with references to persistent entities transparently resolved to
 *     <code>&lt;oid-dto&gt;</code> elements).
 * </p>
 *
 * @see ViewModel
 * 
 * @apiNote Meta annotation {@link Component} allows for the Spring framework to pick up (discover) the 
 * annotated type. 
 * For more details see {@link org.apache.isis.config.beans.IsisBeanFactoryPostProcessorForSpring}.
 */
@Inherited
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ViewModel {


}
