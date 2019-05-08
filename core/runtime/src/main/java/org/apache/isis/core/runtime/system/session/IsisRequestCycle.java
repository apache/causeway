package org.apache.isis.core.runtime.system.session;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import lombok.val;

/**
 * TODO [2033] intent is to remove direct dependencies upon Persistence/Transaction for the viewer-modules.   
 * 
 * @since 2.0.0-M3
 */
public class IsisRequestCycle implements AutoCloseable {

	// -- SUPPORTING ISIS TRANSACTION FILTER FOR RESTFUL OBJECTS ...
	
	public static IsisRequestCycle next() {
		return new IsisRequestCycle();
	}
	
	private IsisRequestCycle() {
    	
	}
	
	public void beforeServletFilter() {
		val isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
        // no-op if no session or transaction manager available.
        if(isisTransactionManager==null) {
            return;
        }
        isisTransactionManager.startTransaction();
		
	}

	public void afterServletFilter() {
		// relying on the caller to close this cycle in a finally block
	}
	
	@Override
	public void close() {
		
		val isisSessionFactory = IsisContext.getSessionFactory();
		val isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
		val inTransaction =
				isisSessionFactory !=null && 
				isisTransactionManager!=null &&
				isisSessionFactory.isInTransaction(); 
		
        if(inTransaction) {
            // user/logout will have invalidated the current transaction and also persistence session.
            try {
                isisTransactionManager.endTransaction();
            } catch (Exception ex) {
                // ignore.  Any exceptions will have been mapped into a suitable response already.
            }
        }
		
	}
	
	// -- SUPPORTING WEB REQUEST CYCLE FOR ISIS ...

	public static void onBeginRequest(AuthenticationSession authenticationSession) {
		
		val isisSessionFactory = IsisContext.getSessionFactory();
		isisSessionFactory.openSession(authenticationSession);
		
		IsisContext.getTransactionManager()
				.ifPresent(txMan->txMan.startTransaction());
	}

	public static void onRequestHandlerExecuted() {
		
		val isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
		if (isisTransactionManager==null) {
			return;
		}
		
        try {
            // will commit (or abort) the transaction;
            // an abort will cause the exception to be thrown.
        	isisTransactionManager.endTransaction();
        	
        } catch(Exception ex) {

        	// will redirect to error page after this,
            // so make sure there is a new transaction ready to go.
            if(isisTransactionManager.getCurrentTransaction().getState().isComplete()) {
            	isisTransactionManager.startTransaction();
            }
            
            throw ex;
        }
		
	}

	public static void onEndRequest() {
		
		val isisTransactionManager = IsisContext.getTransactionManager().orElse(null);
		if (isisTransactionManager==null) {
			return;
		}
		
        try {
        	isisTransactionManager.endTransaction();
        } finally {
        	val isisSessionFactory = IsisContext.getSessionFactory();
        	isisSessionFactory.closeSession();
        }
        
	}
	
	// -- SUPPORTING FORM EXECUTOR DEFAULT ...

	public static void onResultAdapterObtained() {
		val isisSession = IsisSession.currentOrElseNull();
		if (isisSession==null) {
			return;
		}
		
		PersistenceSession.current(PersistenceSession.class)
		.stream()
		.forEach(ps->ps.flush());
		
		//isisSession.flush();
	}


	
	// -- 

}
