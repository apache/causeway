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

package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;

import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * Ideally I'd like to move this to the <tt>org.apache.isis.viewer.wicket.model.isis</tt>
 * package, however to do so would break existing API (gmap3 has a dependency on this, for example).
 */
public interface ImageResourceCache extends Serializable {

    ResourceReference resourceReferenceFor(ManagedObject adapter);

    ResourceReference resourceReferenceForSpec(ObjectSpecification objectSpecification);

}
