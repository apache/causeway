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
package org.apache.isis.extensions.secman.jpa.dom.role;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepositoryAbstract;
import org.apache.isis.extensions.secman.jpa.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.permission.dom.mixins.ApplicationPermission_delete;

import lombok.val;

@Repository
@Named("isis.ext.secman.ApplicationRoleRepository")
public class ApplicationRoleRepository
extends ApplicationRoleRepositoryAbstract {

    public ApplicationRoleRepository(Class applicationRoleClass) {
        super(ApplicationRole.class);
    }

}
