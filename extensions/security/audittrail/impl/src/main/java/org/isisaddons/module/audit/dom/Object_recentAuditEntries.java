package org.isisaddons.module.audit.dom;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.applib.services.metamodel.MetaModelService2;
import org.apache.isis.applib.services.metamodel.MetaModelService3;

@Mixin(method="act")
public class Object_recentAuditEntries {

    private final Object domainObject;

    public Object_recentAuditEntries(final Object domainObject) {
        this.domainObject = domainObject;
    }

    public static class ActionDomainEvent
            extends org.apache.isis.applib.services.eventbus.ActionDomainEvent<Object_recentAuditEntries> {
    }

    @Action(
            domainEvent = ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            contributed = Contributed.AS_ACTION
    )
    @MemberOrder(name = "Metadata", sequence = "10")
    public List<AuditEntry> act(
            @ParameterLayout(named = "Object property")
            final String propertyName) {
        final Bookmark target = bookmarkService.bookmarkFor(domainObject);
        return auditingServiceRepository.findRecentByTargetAndPropertyId(target, propertyName);
    }
    public List<String> choices0Act() {
        final Class<?> domainClass = domainObject.getClass();
        final String packageName = domainClass.getPackage().getName();
        final String className = domainClass.getSimpleName();
        return Lists.newArrayList(
                FluentIterable.from(
                        applicationFeatureRepository
                                .memberNamesOf(packageName, className, ApplicationMemberType.PROPERTY)
                ).filter(Predicates.not(excludedProperties)));
    }
    public String default0Act() {
        final List<String> choices = choices0Act();
        return choices.size() == 1 ? choices.get(0): null;
    }

    public boolean hideAct() {
        MetaModelService2.Sort sort = metaModelService3.sortOf(domainObject.getClass(), MetaModelService3.Mode.RELAXED);
        return !sort.isJdoEntity() || domainObject instanceof HasTransactionId;
    }

    static final Predicate<String> excludedProperties = new Predicate<String>() {
        private List<String> excluded = Lists.newArrayList(
                "datanucleusIdLong", "datanucleusVersionLong", "datanucleusVersionTimestamp"
        );
        @Override
        public boolean apply(@Nullable final String propertyName) {
            return excluded.contains(propertyName);
        }
    };

    @javax.inject.Inject
    private MetaModelService3 metaModelService3;

    @Inject
    ApplicationFeatureRepository applicationFeatureRepository;

    @Inject
    AuditingServiceRepository auditingServiceRepository;

    @Inject
    BookmarkService2 bookmarkService;
}
