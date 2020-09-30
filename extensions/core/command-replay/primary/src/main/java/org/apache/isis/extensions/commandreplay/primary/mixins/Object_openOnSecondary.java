package org.apache.isis.extensions.commandreplay.primary.mixins;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.extensions.commandreplay.primary.IsisModuleExtCommandReplayPrimary;
import org.apache.isis.extensions.commandreplay.primary.config.PrimaryConfig;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
    semantics = SemanticsOf.SAFE,
    domainEvent = Object_openOnSecondary.ActionDomainEvent.class,
    restrictTo = RestrictTo.PROTOTYPING
)
@RequiredArgsConstructor
public class Object_openOnSecondary {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandReplayPrimary.ActionDomainEvent<Object_openOnSecondary> { }

    final Object object;

    public URL act() {
        val baseUrlPrefix = lookupBaseUrlPrefix();
        val urlSuffix = bookmarkService.bookmarkFor(object).toString();

        try {
            return new URL(baseUrlPrefix + urlSuffix);
        } catch (MalformedURLException e) {
            throw new ApplicationException(e);
        }
    }
    public boolean hideAct() {
        return !primaryConfig.isConfigured();
    }

    private String lookupBaseUrlPrefix() {
        return primaryConfig.getSecondaryBaseUrlWicket() + "entity/";
    }

    @Inject PrimaryConfig primaryConfig;
    @Inject BookmarkService bookmarkService;

}
