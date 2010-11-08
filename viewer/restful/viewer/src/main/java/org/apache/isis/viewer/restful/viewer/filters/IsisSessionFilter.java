package org.apache.isis.viewer.restful.viewer.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.authentication.AuthenticationRequest;
import org.apache.isis.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.runtime.authentication.standard.exploration.ExplorationSession;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.log4j.Logger;


public class IsisSessionFilter implements Filter {

    private static final String NOF_SESSION_REQUEST_KEY = "session";
    public static final String AUTHENTICATION_MANAGER_WEBAPP_CONTEXT_KEY = "authenticationManager";
    
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
	private final Logger LOG = Logger.getLogger(IsisSessionFilter.class);

    /**
     * does nothing.
     */
    public void init(final FilterConfig config) throws ServletException {

    }

    /**
     * does nothing.
     */
    public void destroy() {}

    /**
     * If the {@link DeploymentType} of effective {@link NakedObjectsContext} is
     * {@link DeploymentType#isExploration() exploration}, then automatically create a session.
     */
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        final AuthenticationSession nofSession = getNofSession(request);

        if (nofSession != null) {
            IsisContext.openSession(nofSession);
        }
        chain.doFilter(request, response);

        IsisContext.closeSession();
    }

    /**
     * Locates the {@link Session Naked Objects session} from the {@link HttpSession}, if available, and
     * ensures is {@link AuthenticationManager#isSessionValid(Session) still valid}.
     * 
     * <p>
     * Any bound {@link Session Naked Objects session} is bound onto the {@link HttpSession} for further
     * requests.
     * 
     * <p>
     * If no valid {@link Session Naked Objects session} exists, then attempts to create one using
     * <tt>user</tt> and <tt>password</tt> parameters.
     * 
     * <p>
     * Note: if running in {@link DeploymentType#isExploration() exploration mode}, then always sets up an
     * {@link ExplorationSession exploration session}.
     * 
     * @param request
     * @return
     */
    private AuthenticationSession getNofSession(final ServletRequest request) {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;

        final HttpSession httpSession = httpRequest.getSession(true);

        final AuthenticationManager authenticationManager = (AuthenticationManager) httpSession.getServletContext().getAttribute(
                AUTHENTICATION_MANAGER_WEBAPP_CONTEXT_KEY);

        if (authenticationManager == null) {
            throw new IllegalStateException("No authentication manager configured.");
        }

        AuthenticationSession nofSession = null;

        final DeploymentType deploymentType = IsisContext.getDeploymentType();

        if (deploymentType.isExploring()) {
            nofSession = new ExplorationSession();
        } else {
            nofSession = (AuthenticationSession) httpSession.getAttribute(NOF_SESSION_REQUEST_KEY);
            if (nofSession != null) {
                if (!authenticationManager.isSessionValid(nofSession)) {
                    nofSession = null;
                }
            }

            if (nofSession == null) {
                final String user = httpRequest.getParameter("user");
                final String password = httpRequest.getParameter("password");
                final AuthenticationRequest passwordRequest = new AuthenticationRequestPassword(user, password);
                nofSession = authenticationManager.authenticate(passwordRequest);
            }
        }
        httpSession.setAttribute(NOF_SESSION_REQUEST_KEY, nofSession);
        return nofSession;
    }

}
