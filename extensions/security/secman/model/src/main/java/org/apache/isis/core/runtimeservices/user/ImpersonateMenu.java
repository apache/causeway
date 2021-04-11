package org.apache.isis.core.runtimeservices.user;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;
import lombok.val;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isis.ext.secman.ImpersonateMenu"
)
@DomainServiceLayout(
        named="Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImpersonateMenu {

    final UserService userService;
    final MessageService messageService;
    final ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    final ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;


    // -- domain event classes
    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ImpersonateMenu, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ImpersonateMenu, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ImpersonateMenu> {}


    public static class ImpersonateDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = ImpersonateMenu.ImpersonateDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(sequence = "1")
    public void impersonate(
            final ApplicationUser applicationUser,
            @ParameterLayout(describedAs = "If set, then the roles specified below are used.  Otherwise uses roles of the specified user.")
            final boolean useExplicitRolesBelow,
            @ParameterLayout(describedAs = "Only used if 'useExplicitRolesBelow' is set, otherwise is ignored.")
            final Set<? extends ApplicationRole> applicationRoleList) {
        Set<? extends ApplicationRole> applicationRoles = useExplicitRolesBelow ? applicationRoleList : applicationUser.getRoles();
        val roleNames = applicationRoles.stream().map(ApplicationRole::getName).collect(Collectors.toList());

        this.userService.impersonateUser(applicationUser.getName(), roleNames);
        this.messageService.informUser("Now impersonating " + applicationUser.getName());
    }

    public boolean hideImpersonate() {
        return ! this.userService.supportsImpersonation();
    }

    public List<? extends ApplicationUser> choices0Impersonate() {
        return this.applicationUserRepository.allUsers()
                    .stream()
                    .filter(x -> x.getStatus() == ApplicationUserStatus.ENABLED)
                    .collect(Collectors.toList());
    }

    public boolean default1Impersonate() {
        return false;
    }

    public Collection<? extends ApplicationRole> default2Impersonate() {
        return this.applicationRoleRepository.allRoles();
    }



    public static class StopImpersonatingDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = ImpersonateMenu.StopImpersonatingDomainEvent.class,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(sequence = "2")
    public void stopImpersonating() {
        this.userService.stopImpersonating();
        this.messageService.informUser("No longer impersonating another user");
    }

    public boolean hideStopImpersonating() {
        return this.userService.supportsImpersonation() && this.userService.isImpersonating();
    }



}
