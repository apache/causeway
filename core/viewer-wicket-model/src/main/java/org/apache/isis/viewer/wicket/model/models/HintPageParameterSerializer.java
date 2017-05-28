package org.apache.isis.viewer.wicket.model.models;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.hint.HintStore;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;

class HintPageParameterSerializer implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PREFIX = "hint-";

    private HintPageParameterSerializer() {}

//    private final EntityModel entityModel;
//
//    public HintPageParameterSerializer(final EntityModel entityModel) {
//        this.entityModel = entityModel;
//    }

//    public void hintStoreToPageParameters(
//            final PageParameters pageParameters) {
//        EntityModel entityModel = this.entityModel;
//        hintStoreToPageParameters(pageParameters, entityModel);
//    }

    public static void hintStoreToPageParameters(final PageParameters pageParameters, final EntityModel entityModel) {
        ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
        hintStoreToPageParameters(pageParameters, objectAdapterMemento);
    }

    public static void hintStoreToPageParameters(
            final PageParameters pageParameters,
            final ObjectAdapterMemento objectAdapterMemento) {
        if(objectAdapterMemento == null) {
            return;
        }
        final HintStore hintStore = getHintStore();
        final Bookmark bookmark = objectAdapterMemento.asHintingBookmark();
        Set<String> hintKeys = hintStore.findHintKeys(bookmark);
        for (String hintKey : hintKeys) {
            ComponentHintKey.create(hintKey).hintTo(bookmark, pageParameters, PREFIX);
        }
    }

//    public void updateHintStore(final PageParameters pageParameters) {
//        EntityModel entityModel = this.entityModel;
//        updateHintStore(pageParameters, entityModel);
//    }

//    public static void updateHintStore(final PageParameters pageParameters, final EntityModel entityModel) {
//        ObjectAdapterMemento objectAdapterMemento = entityModel.getObjectAdapterMemento();
//        updateHintStore(pageParameters, objectAdapterMemento);
//    }

    public static void updateHintStore(
            final PageParameters pageParameters,
            final ObjectAdapterMemento objectAdapterMemento) {
        if(objectAdapterMemento == null) {
            return;
        }
        Set<String> namedKeys = pageParameters.getNamedKeys();
        if (namedKeys.contains("no-hints")) {
            getHintStore().removeAll(objectAdapterMemento.asHintingBookmark());
            return;
        }
        List<ComponentHintKey> newComponentHintKeys = Lists.newArrayList();
        for (String namedKey : namedKeys) {
            if (namedKey.startsWith(PREFIX)) {
                String value = pageParameters.get(namedKey).toString(null);
                String key = namedKey.substring(5);
                final ComponentHintKey componentHintKey = ComponentHintKey.create(key);
                newComponentHintKeys.add(componentHintKey);
                final Bookmark bookmark = objectAdapterMemento.asHintingBookmark();
                componentHintKey.set(bookmark, value);
            }
        }
    }

    private static HintStore getHintStore() {
        return getIsisSessionFactory().getServicesInjector().lookupService(HintStore.class);
    }

    private static IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

}
