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

package org.apache.isis.security.sql.authentication;

import java.lang.reflect.InvocationTargetException;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.authentication.standard.Authenticator;
import org.apache.isis.runtimes.dflt.runtime.authentication.AuthenticationManagerStandardInstallerAbstractForDfltRuntime;

public class SqlAuthenticationManagerInstaller extends AuthenticationManagerStandardInstallerAbstractForDfltRuntime {

    public static final String NAME = "sql";

    public SqlAuthenticationManagerInstaller() {
        super(NAME);
    }

    @Override
    protected Authenticator createAuthenticator(final IsisConfiguration configuration) {
        String className = configuration.getString("isis.authentication.authenticator");
        if (className == null) {
            return new SqlAuthenticator(configuration);
        } else {
            try {
                Class<?> authenticatorClass = Class.forName(className);
                return (Authenticator) authenticatorClass.getConstructor(IsisConfiguration.class).newInstance(
                    configuration);
            } catch (ClassNotFoundException e) {
                throw new ApplicationException("Unable to find authenticator class", e);
            } catch (IllegalArgumentException e) {
                throw new ApplicationException("IllegalArgumentException creating authenticator class", e);
            } catch (SecurityException e) {
                throw new ApplicationException("SecurityException creating authenticator class", e);
            } catch (InstantiationException e) {
                throw new ApplicationException("InstantiationException creating authenticator class", e);
            } catch (IllegalAccessException e) {
                throw new ApplicationException("IllegalAccessException creating authenticator class", e);
            } catch (InvocationTargetException e) {
                throw new ApplicationException("InvocationTargetException creating authenticator class", e);
            } catch (NoSuchMethodException e) {
                throw new ApplicationException("NoSuchMethodException creating authenticator class", e);
            }
        }
    }

}
