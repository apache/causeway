package org.apache.isis.core.runtimeservices.user;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.user.ImpersonateMenuAdvisor;

/**
 * This default implementation simply returns empty lists.
 *
 * <p>
 *  This has the effect that the
 *  {@link org.apache.isis.applib.services.user.ImpersonateMenu}'s
 *  {@link org.apache.isis.applib.services.user.ImpersonateMenu#impersonateWithRoles(String, List) impersonateWithRoles}
 *  action will be hidden.
 * </p>
 */
@Service
@Named("isis.runtimeservices.ImpersonateMenuAdvisorDefault")
@javax.annotation.Priority(OrderPrecedence.LAST)
@Qualifier("Default")
public class ImpersonateMenuAdvisorDefault implements ImpersonateMenuAdvisor {

    @Override
    public List<String> allUserNames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> allRoleNames() {
        return Collections.emptyList();
    }

    @Override
    public List<String> roleNamesFor(
            final String username) {
        return Collections.emptyList();
    }

}
