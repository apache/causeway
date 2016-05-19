package org.apache.isis.core.metamodel.services.persistsession;

import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService2;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

@DomainService(nature = NatureOfService.DOMAIN)
public class PersistenceSessionServiceInternalNoop extends PersistenceSessionServiceInternalAbstract {

    @Override
    public void injectInto(final Object candidate) {
        if (AdapterManagerAware.class.isAssignableFrom(candidate.getClass())) {
            final AdapterManagerAware cast = AdapterManagerAware.class.cast(candidate);
            cast.setAdapterManager(this);
        }
    }

    @Override
    public ObjectAdapter getAdapterFor(final Object pojo) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectAdapter adapterFor(
            final Object pojo,
            final ObjectAdapter ownerAdapter,
            final OneToManyAssociation collection) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectAdapter mapRecreatedPojo(Oid oid, Object recreatedPojo) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void removeAdapter(ObjectAdapter adapter) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectAdapter adapterFor(final Object domainObject) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectAdapter getAdapterFor(Oid oid) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectAdapter createTransientInstance(final ObjectSpecification spec) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public Object lookup(
            final Bookmark bookmark,
            final BookmarkService2.FieldResetPolicy fieldResetPolicy) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public Bookmark bookmarkFor(Object domainObject) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public Bookmark bookmarkFor(Class<?> cls, String identifier) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void resolve(final Object parent, final Object field) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void resolve(final Object parent) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void beginTran() {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public boolean flush() {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void remove(final ObjectAdapter adapter) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public void makePersistent(final ObjectAdapter adapter) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public <T> ObjectAdapter firstMatchingQuery(final Query<T> query) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public <T> List<ObjectAdapter> allMatchingQuery(final Query<T> query) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

}
