package org.apache.isis.applib.services;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.metamodel.MetaModelService;

@Action(
        semantics = SemanticsOf.SAFE
        , associateWith = "target"
        , associateWithSequence = "1"
)
@ActionLayout(named = "Open")
public class DomainChangeRecord_openTargetObject {

    private final DomainChangeRecord domainChangeRecord;
    public DomainChangeRecord_openTargetObject(DomainChangeRecord domainChangeRecord) {
        this.domainChangeRecord = domainChangeRecord;
    }

    @Action(semantics = SemanticsOf.SAFE, associateWith = "target", associateWithSequence = "1")
    @ActionLayout(named = "Open")
    public Object openTargetObject() {
        try {
            return bookmarkService != null
                    ? bookmarkService.lookup(domainChangeRecord.getTarget())
                    : null;
        } catch(RuntimeException ex) {
            if(ex.getClass().getName().contains("ObjectNotFoundException")) {
                messageService.warnUser("Object not found - has it since been deleted?");
                return null;
            }
            throw ex;
        }
    }

    public boolean hideOpenTargetObject() {
        return domainChangeRecord.getTarget() == null;
    }

    public String disableOpenTargetObject() {
        final Object targetObject = domainChangeRecord.getTarget();
        if (targetObject == null) {
            return null;
        }
        final BeanSort sortOfObject = metaModelService.sortOf(domainChangeRecord.getTarget(), MetaModelService.Mode.RELAXED);
        return !(sortOfObject.isViewModel() || sortOfObject.isEntity())
                ? "Can only open view models or entities"
                : null;
    }

    @Inject BookmarkService bookmarkService;
    @Inject MessageService messageService;
    @Inject MetaModelService metaModelService;

}
