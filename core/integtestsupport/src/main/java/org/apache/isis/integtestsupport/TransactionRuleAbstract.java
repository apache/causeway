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

package org.apache.isis.integtestsupport;

import java.util.List;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.headless.IsisSystem;

public abstract class TransactionRuleAbstract {

    void handleTransactionContextException(Exception e) throws Exception {
        // determine if underlying cause is an applib-defined exception,
        final RecoverableException recoverableException =
                determineIfRecoverableException(e);
        final NonRecoverableException nonRecoverableException =
                determineIfNonRecoverableException(e);

        if(recoverableException != null) {
            try {
                final IsisSystem isft = IsisSystem.get();
                isft.getService(TransactionService.class).flushTransaction(); // don't care if npe
                isft.getService(IsisJdoSupport.class).getJdoPersistenceManager().flush();
            } catch (Exception ignore) {
                // ignore
            }
        }
        // attempt to close this
        try {
            final IsisSystem isft = IsisSystem.getElseNull();
            isft.closeSession(); // don't care if npe
        } catch(Exception ignore) {
            // ignore
        }

        // attempt to start another
        try {
            final IsisSystem isft = IsisSystem.getElseNull();
            isft.openSession(); // don't care if npe
        } catch(Exception ignore) {
            // ignore
        }


        // if underlying cause is an applib-defined, then
        // throw that rather than Isis' wrapper exception
        if(recoverableException != null) {
            throw recoverableException;
        }
        if(nonRecoverableException != null) {
            throw nonRecoverableException;
        }

        // report on the error that caused
        // a problem for *this* test
        throw e;
    }

    // -- HELPER

    private static NonRecoverableException determineIfNonRecoverableException(final Exception e) {
        NonRecoverableException nonRecoverableException = null;
        final List<Throwable> causalChain2 = _Exceptions.getCausalChain(e);
        for (final Throwable cause : causalChain2) {
            if(cause instanceof NonRecoverableException) {
                nonRecoverableException = (NonRecoverableException) cause;
                break;
            }
        }
        return nonRecoverableException;
    }

    private static RecoverableException determineIfRecoverableException(final Exception e) {
        RecoverableException recoverableException = null;
        final List<Throwable> causalChain = _Exceptions.getCausalChain(e);
        for (final Throwable cause : causalChain) {
            if(cause instanceof RecoverableException) {
                recoverableException = (RecoverableException) cause;
                break;
            }
        }
        return recoverableException;
    }


}
