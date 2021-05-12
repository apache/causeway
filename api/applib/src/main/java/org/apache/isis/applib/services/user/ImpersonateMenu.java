package org.apache.isis.applib.services.user;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Accessors;

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
        objectType = "isis.applib.ImpersonateMenu"
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImpersonateMenu {

    final UserService userService;
    final MessageService messageService;
    final List<ImpersonateMenuAdvisor> impersonateMenuAdvisors;



    public static abstract class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<ImpersonateMenu> {}



    public static class ImpersonateDomainEvent extends ActionDomainEvent { }

    /**
     * Simple implementation that is surfaced if there is no advisor.
     *
     * @param userName
     * @return
     */
    @Action(
            domainEvent = ImpersonateDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            executionPublishing = Publishing.DISABLED,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(sequence = "100.1", cssClassFa = "fa-mask")
    public void impersonate(
            final String userName) {

        this.userService.impersonateUser(userName, Collections.emptyList());
        this.messageService.informUser("Now impersonating " + userName);
    }
    public boolean hideImpersonate() {
        return ! this.userService.supportsImpersonation();
    }
    public String disableImpersonate() {
        return this.userService.isImpersonating() ? "currently impersonating" : null;
    }





    public static class ImpersonateWithRolesDomainEvent extends ActionDomainEvent { }

    /**
     * Impersonate a selected user, either using their current roles or
     * with a specific set of roles.
     *
     * <p>
     * This more sophisticated implementation is only available if there is
     * an {@link ImpersonateMenuAdvisor} implementation to provide the
     * choices.
     * </p>
     *
     * @param userName
     * @param roleNames
     */
    @Action(
            domainEvent = ImpersonateWithRolesDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            executionPublishing = Publishing.DISABLED,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(sequence = "100.2", cssClassFa = "fa-mask")
    public void impersonateWithRoles(
            final String userName,
            @ParameterLayout(named = "Use user's roles?", labelPosition = LabelPosition.RIGHT)
            final boolean useUsersRoles,
            final List<String> roleNames) {

        val rolesToUse = useUsersRoles
                ? impersonateMenuAdvisor().roleNamesFor(userName)
                : roleNames;

        this.userService.impersonateUser(userName, rolesToUse);
        this.messageService.informUser("Now impersonating " + userName);
    }
    @MemberSupport public boolean hideImpersonateWithRoles() {
        return ! this.userService.supportsImpersonation() || choices0ImpersonateWithRoles().isEmpty();
    }
    @MemberSupport public String disableImpersonateWithRoles() {
        return this.userService.isImpersonating() ? "currently impersonating" : null;
    }
    @MemberSupport public List<String> choices0ImpersonateWithRoles() {
        return impersonateMenuAdvisor().allUserNames();
    }
    @MemberSupport public boolean default1ImpersonateWithRoles() {
        return true;
    }
    @MemberSupport public boolean hide2ImpersonateWithRoles(final String userName, boolean useUsersRoles) {
        return useUsersRoles;
    }
    @MemberSupport public List<String> choices2ImpersonateWithRoles(final String userName, boolean useUsersRoles) {
        return impersonateMenuAdvisor().allRoleNames();
    }
    @MemberSupport public List<String> default2ImpersonateWithRoles(final String userName, boolean useUsersRoles) {
        // TODO: this is never called, unfortunately; ISIS-2666
        // TODO: and attempting to use Parameters fails; ISIS-2667
        return impersonateMenuAdvisor().roleNamesFor(userName);
    }

    private ImpersonateMenuAdvisor impersonateMenuAdvisor() {
        // this is safe because there will always be at least one implementation.
        return impersonateMenuAdvisors.get(0);
    }





    public static class StopImpersonatingDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = ImpersonateMenu.StopImpersonatingDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            commandPublishing = Publishing.DISABLED,
            executionPublishing = Publishing.DISABLED,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(sequence = "100.3")
    public void stopImpersonating() {
        this.userService.stopImpersonating();
        this.messageService.informUser("No longer impersonating another user");
    }
    public boolean hideStopImpersonating() {
        return ! this.userService.supportsImpersonation();
    }
    public String disableStopImpersonating() {
        return ! this.userService.isImpersonating() ? "no user is currently being impersonated": null;
    }




}
