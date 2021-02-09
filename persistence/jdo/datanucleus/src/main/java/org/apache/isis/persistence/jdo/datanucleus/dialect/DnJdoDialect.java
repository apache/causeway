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
package org.apache.isis.persistence.jdo.datanucleus.dialect;

import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.exceptions.NucleusException;
import org.springframework.dao.DataAccessException;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.persistence.jdo.spring.integration.DefaultJdoDialect;
import org.apache.isis.persistence.jdo.spring.integration.JdoDialect;

import lombok.val;

/**
 * Vendor (<i>Datanucleus</i>) specific implementation of <i>Spring's</i> {@link JdoDialect}
 * interface.
 * 
 * @since 2.0 {@index}
 * @see JdoDialect
 */
public class DnJdoDialect extends DefaultJdoDialect {

    public DnJdoDialect(Object connectionFactory) {
        super(connectionFactory);
    }

//TODO[2502] remove entire class if no longer need for ISIS-2502
    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        
        // translate from Datanucleus to JDO standard
        val translatedException = _Exceptions.streamCausalChain(ex)
        .<RuntimeException>map(e->{
            if(e instanceof NucleusException) {
                return NucleusJDOHelper
                        .getJDOExceptionForNucleusException((NucleusException)e);
            }
            return null;
        })
        .filter(_NullSafe::isPresent)
        .findFirst()
        .orElse(ex);
        
        // translate from JDO standard to Spring's DataAccessException
        return super.translateExceptionIfPossible(translatedException);
    }
    
}
