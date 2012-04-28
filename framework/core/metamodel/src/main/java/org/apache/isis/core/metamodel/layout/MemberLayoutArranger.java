/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layout;

import java.util.List;

import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Orders both associations (properties and collections) and also actions.
 * 
 * <p>
 * TODO: for now, 'arranging' means merely ordering. In the future, this might
 * evolve into a more general specification of a layout, eg columns and other
 * layouts.
 */
public interface MemberLayoutArranger {

    OrderSet createAssociationOrderSetFor(ObjectSpecification objectSpecification, List<FacetedMethod> associationFacetedMethods);

    OrderSet createActionOrderSetFor(ObjectSpecification spec, List<FacetedMethod> actionFacetedMethods);

}
