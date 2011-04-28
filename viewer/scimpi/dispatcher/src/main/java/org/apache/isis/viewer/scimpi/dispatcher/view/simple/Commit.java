package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class Commit extends AbstractElementProcessor {

    public String getName() {
        return "commit";
    }

    public void process(Request request) {
        // Note - the session will have changed since the earlier call if a user has logged in or out in the action processing above.
        IsisTransactionManager transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        if (transactionManager.getTransaction().getState().canCommit()) {
            transactionManager.endTransaction();
            transactionManager.startTransaction();
        }
    }

}

