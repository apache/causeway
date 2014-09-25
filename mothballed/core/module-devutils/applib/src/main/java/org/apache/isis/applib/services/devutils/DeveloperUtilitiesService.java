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
package org.apache.isis.applib.services.devutils;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

@Named("Developer Utilities")
public interface DeveloperUtilitiesService {

    @MemberOrder(sequence="1")
    @ActionSemantics(Of.SAFE)
    @Prototype
    public Clob downloadMetaModel();


    /**
     * Downloads a zip of the layout of all domain classes.
     */
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="3")
    @Prototype
    public Blob downloadLayouts();

    /**
     * Rebuilds the metamodel of all registered domain services.
     */
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="3")
    @Prototype
    public void refreshServices();

    /**
     * Download the JSON layout of the domain object's type.
     */
    @NotInServiceMenu
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="2")
    @Prototype
    public Clob downloadLayout(Object domainObject);

    /**
     * @deprecated - in prototype mode the Wicket viewer (at least) will automatically invalidate 
     *               the Isis metamodel whenever the object is re-rendered.
     */
    @Deprecated
    @Hidden
    @NotInServiceMenu
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="99")
    @Prototype
    public Object refreshLayout(Object domainObject);

}
