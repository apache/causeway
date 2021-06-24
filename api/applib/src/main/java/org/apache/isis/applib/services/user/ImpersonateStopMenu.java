package org.apache.isis.applib.services.user;

import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.Redirect;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;

/**
 * Provides the UI to allow a current user to be impersonated.
 *
 * <p>
 *     All of the actions provided here are restricted to PROTOTYPE mode only;
 *     this feature is <i>not</i> intended for production use as it would imply
 *     a large security hole !
 * </p>
 *
 * @see UserService
 * @see ImpersonateMenuAdvisor
 * @see ImpersonatedUserHolder
 *
 * @since 2.0 {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = ImpersonateStopMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImpersonateStopMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE + ".ImpersonateStopMenu";   // deliberately IS part of isis.applib

    final UserService userService;
    final MessageService messageService;


    public static abstract class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<ImpersonateStopMenu> {}


    public static class StopImpersonatingDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = ImpersonateStopMenu.StopImpersonatingDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            executionPublishing = Publishing.DISABLED,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(sequence = "100.3", redirectPolicy = Redirect.EVEN_IF_SAME)
    public void stopImpersonating() {
        this.userService.stopImpersonating();
        this.messageService.informUser("No longer impersonating another user");
    }
    public boolean hideStopImpersonating() {
        return ! isImpersonating();
    }

    private boolean isImpersonating() {
        return this.userService.supportsImpersonation() && this.userService.isImpersonating();
    }

}
