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

/**
 * Object Reflector API.
 *
 * <p>
 * The job of the reflector is to create the meta-model, typically using annotations and
 * other conventions in its own applib.
 *
 * <p>
 * Concrete implementations are in the <tt>programmingmodel-xxx-impl</tt> modules.  These
 * are expected to be based heavily on <tt>ObjectReflectorAbstract</tt>, defined in
 * <tt>nof-core</tt>.  This implementation defines two further sub-APIs which are based on
 * the {@link org.apache.isis.core.metamodel.facetapi.Facet}s and {@link org.apache.isis.core.metamodel.facetdecorator.FacetDecorator}s:
 * <ul>
 * <li> the {@link ProgrammingModelInstaller} is used to specify the collection of {@link org.apache.isis.core.metamodel.facets.FacetFactory}s
 *      that will be used to actually process and build up the metamodel.
 * <li> the {@link FacetDecoratorInstaller} API specifies how {@link org.apache.isis.core.metamodel.facetapi.Facet}, once created,
 *      can be additionally decorated to modify their behaviour.  A number of other components are implemented as
 *      {@link org.apache.isis.core.metamodel.facetdecorator.FacetDecorator}s, such as {@link org.apache.isis.authorization.AuthorisationFacetDecorator authorisation},
 *      {@link org.apache.isis.help.HelpFacetDecorator help}, and {@link org.apache.isis.transaction.facetdecorator.TransactionFacetDecorator transactions}.  However
 *      it is possible for other {@link org.apache.isis.core.metamodel.facetdecorator.FacetDecorator}s to be defined and installed also (such as <tt>i18n</tt>).
 * </ul>
 *
 */
package org.apache.isis.core.metamodel.specloader;