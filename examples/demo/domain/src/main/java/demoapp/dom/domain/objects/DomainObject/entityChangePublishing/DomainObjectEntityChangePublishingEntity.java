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
package demoapp.dom.domain.objects.DomainObject.entityChangePublishing;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import demoapp.dom._infra.values.ValueHolder;

@Named("demo.DomainObjectEntityChangePublishingEntity")
@DomainObject
//tag::class[]
public abstract class DomainObjectEntityChangePublishingEntity
implements
    HasAsciiDocDescription,
    ValueHolder<String> {

    @Override
    public String value() {
        return getProperty();
    }

    public abstract String getProperty();
    public abstract void setProperty(String value);

    public abstract String getPropertyUpdatedByAction();
    public abstract void setPropertyUpdatedByAction(String value);

}
//end::class[]
