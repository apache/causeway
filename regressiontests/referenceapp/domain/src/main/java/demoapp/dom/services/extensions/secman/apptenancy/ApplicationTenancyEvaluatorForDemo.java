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
package demoapp.dom.services.extensions.secman.apptenancy;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import org.apache.causeway.extensions.secman.applib.tenancy.spi.ApplicationTenancyEvaluator;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;

import demoapp.dom.services.extensions.secman.apptenancy.persistence.TenantedEntity;
import lombok.Getter;

//tag::class[]
@Service
public class ApplicationTenancyEvaluatorForDemo
                implements ApplicationTenancyEvaluator {                            // <.>

    @Override
    public boolean handles(final Class<?> cls) {                                          // <.>
        return TenantedEntity.class.isAssignableFrom(cls);
    }

    private Pattern hidePattern;                                                    // <.>
    @Override
    public String hides(final Object domainObject, final ApplicationUser applicationUser) {     // <.>
        if(hidePattern == null) {
            return null;
        }
        var tenantedEntity = (TenantedEntity) domainObject;
        var name = tenantedEntity.getName();

        return hidePattern.matcher(name).matches()
                ? "any non-null value will hide"
                : null;
    }

    private Pattern disablePattern;                                                 // <3>
    @Override
    public String disables(final Object domainObject, final ApplicationUser applicationUser) {  // <.>
        if(disablePattern == null) {
            return null;
        }
        var tenantedEntity = (TenantedEntity) domainObject;
        var name = tenantedEntity.getName();

        return disablePattern.matcher(name).matches()
                ? String.format("disabled, because name matches '%s'", disablePattern)
                : null;
    }
    // ...
//end::class[]

//tag::hideRegex[]
    @Getter
    private String hideRegex;
    public void setHideRegex(final String hideRegex) {
        this.hideRegex = hideRegex;
        this.hidePattern = hideRegex != null
                ? Pattern.compile(hideRegex)
                : null;
    }
//end::hideRegex[]

    @Getter
    private String disableRegex;
    public void setDisableRegex(final String disableRegex) {
        this.disableRegex = disableRegex;
        this.disablePattern = disableRegex != null
                ? Pattern.compile(disableRegex)
                : null;
    }

//tag::class[]
}
//end::class[]
