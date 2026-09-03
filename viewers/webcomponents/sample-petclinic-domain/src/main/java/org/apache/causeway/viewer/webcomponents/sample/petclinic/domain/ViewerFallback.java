/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.petclinic.domain;

import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.ObjectSupport;

@Entity
@Table(name = "petclinic_viewer_fallback")
@Named(PetClinicDomainModule.NAMESPACE + ".ViewerFallback")
@DomainObject
@DomainObjectLayout
public class ViewerFallback {

    public static final String ID = "viewer-fallback";

    @Id
    @Column(nullable = false, length = 40)
    private String id;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false, length = 80)
    private String message;

    protected ViewerFallback() {
    }

    public ViewerFallback(final String id, final String message) {
        this.id = id;
        this.message = message;
    }

    @ObjectSupport
    public String title() {
        return "Generic viewer fallback";
    }

    public String getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }
}
