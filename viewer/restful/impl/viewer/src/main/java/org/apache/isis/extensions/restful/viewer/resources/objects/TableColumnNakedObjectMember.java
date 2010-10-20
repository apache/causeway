package org.apache.isis.extensions.restful.viewer.resources.objects;


import org.apache.isis.extensions.restful.viewer.xom.ResourceContext;
import org.apache.isis.extensions.restful.viewer.xom.TableColumnAbstract;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.authentication.AuthenticationSession;
import org.apache.isis.metamodel.spec.feature.ObjectMember;


public abstract class TableColumnNakedObjectMember<T extends ObjectMember> extends TableColumnAbstract<T> {

    private final ObjectAdapter nakedObject;
    private final AuthenticationSession session;

    protected TableColumnNakedObjectMember(
            final String headerText,
            final AuthenticationSession session,
            final ObjectAdapter nakedObject,
            final ResourceContext resourceContext) {
        super(headerText, resourceContext);
        this.nakedObject = nakedObject;
        this.session = session;
    }

    protected String getOidStr() {
        return getOidStr(getNakedObject());
    }

    public ObjectAdapter getNakedObject() {
        return nakedObject;
    }

    public AuthenticationSession getSession() {
        return session;
    }

}
