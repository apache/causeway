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
package org.apache.isis.persistence.jpa.applib.integration;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/**
 * Use {@link IsisEntityListener} instead.
 */
@Deprecated
//@Log4j2
public class JpaEntityInjectionPointResolver extends IsisEntityListener {

    @Override
    @PrePersist
    void onPrePersist(final Object entityPojo) {
        super.onPrePersist(entityPojo);
    }

    @Override
    @PreUpdate
    void onPreUpdate(final Object entityPojo) {
        super.onPreUpdate(entityPojo);
    }

    @Override
    @PreRemove
    void onPreRemove(final Object entityPojo) {
        super.onPreRemove(entityPojo);
    }

    @Override
    @PostPersist
    void onPostPersist(final Object entityPojo) {
        super.onPostPersist(entityPojo);
    }

    @Override
    @PostUpdate
    void onPostUpdate(final Object entityPojo) {
        super.onPostUpdate(entityPojo);
    }

    @Override
    @PostRemove
    void onPostRemove(final Object entityPojo) {
        super.onPostRemove(entityPojo);
    }

    @Override
    @PostLoad
    void onPostLoad(final Object entityPojo) {
        super.onPostLoad(entityPojo);
    }


}
