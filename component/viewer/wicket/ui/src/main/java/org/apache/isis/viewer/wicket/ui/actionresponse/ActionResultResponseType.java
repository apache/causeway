package org.apache.isis.viewer.wicket.ui.actionresponse;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.request.IRequestHandler;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.model.models.VoidModel;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.pages.standalonecollection.StandaloneCollectionPage;
import org.apache.isis.viewer.wicket.ui.pages.value.ValuePage;
import org.apache.isis.viewer.wicket.ui.pages.voidreturn.VoidReturnPage;

public enum ActionResultResponseType {
    OBJECT {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final ObjectAdapter actualAdapter = determineActualAdapter(resultAdapter);
            return toEntityPage(model, actualAdapter, null);
        }

        @Override
        public ActionResultResponse interpretResult(final ActionModel model, ObjectAdapter targetAdapter, ConcurrencyException ex) {
            return toEntityPage(model, targetAdapter, ex);
        }

        private ObjectAdapter determineActualAdapter(final ObjectAdapter resultAdapter) {
            if (resultAdapter.getSpecification().isNotCollection()) {
                return resultAdapter;
            } else {
                // will only be a single element
                final List<Object> pojoList = asList(resultAdapter);
                final Object pojo = pojoList.get(0);
                return adapterFor(pojo);
            }
        }
        private ObjectAdapter adapterFor(final Object pojo) {
            return IsisContext.getPersistenceSession().getAdapterManager().adapterFor(pojo);
        }

        private ActionResultResponse toEntityPage(final ActionModel model, final ObjectAdapter actualAdapter, ConcurrencyException exIfAny) {
            return ActionResultResponse.toPage(this, new EntityPage(actualAdapter, exIfAny));
        }

    },
    COLLECTION {
        @Override
        public ActionResultResponse interpretResult(final ActionModel actionModel, final ObjectAdapter resultAdapter) {
            final EntityCollectionModel collectionModel = EntityCollectionModel.createStandalone(resultAdapter);
            collectionModel.setActionHint(actionModel);
            return ActionResultResponse.toPage(this, new StandaloneCollectionPage(collectionModel));
        }
    },
    VALUE {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            ValueModel valueModel = new ValueModel(resultAdapter);
            final ValuePage valuePage = new ValuePage(valueModel);
            return ActionResultResponse.toPage(this, valuePage);
        }
    },
    VALUE_CLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getObject();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(this, handler);
        }
    },
    VALUE_BLOB {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getObject();
            IRequestHandler handler = ActionModel.downloadHandler(value);
            return ActionResultResponse.withHandler(this, handler);
        }

    },
    VALUE_URL {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final Object value = resultAdapter.getObject();
            IRequestHandler handler = ActionModel.redirectHandler(value);
            return ActionResultResponse.withHandler(this, handler);
        }

    },
    VOID {
        @Override
        public ActionResultResponse interpretResult(final ActionModel model, final ObjectAdapter resultAdapter) {
            final VoidModel voidModel = new VoidModel();
            voidModel.setActionHint(model);
            return ActionResultResponse.toPage(this, new VoidReturnPage(voidModel));
        }
    };

    public abstract ActionResultResponse interpretResult(ActionModel model, ObjectAdapter resultAdapter);

    /**
     * Only overridden for {@link ActionResultResponseType#OBJECT object}
     */
    public ActionResultResponse interpretResult(ActionModel model, ObjectAdapter targetAdapter, ConcurrencyException ex) {
        throw new UnsupportedOperationException("Cannot render concurrency exception for any result type other than OBJECT");
    }

    // //////////////////////////////////////

    public static ActionResultResponse determineAndInterpretResult(final ActionModel model, ObjectAdapter resultAdapter) {
        ActionResultResponseType arrt = determineFor(resultAdapter);
        return arrt.interpretResult(model, resultAdapter);
    }

    private static ActionResultResponseType determineFor(final ObjectAdapter resultAdapter) {
        if(resultAdapter == null) {
            return ActionResultResponseType.VOID;
        }
        final ObjectSpecification resultSpec = resultAdapter.getSpecification();
        if (resultSpec.isNotCollection()) {
            if (resultSpec.getFacet(ValueFacet.class) != null) {
                
                final Object value = resultAdapter.getObject();
                if(value instanceof Clob) {
                    return ActionResultResponseType.VALUE_CLOB;
                } 
                if(value instanceof Blob) {
                    return ActionResultResponseType.VALUE_BLOB;
                } 
                if(value instanceof java.net.URL) {
                    return ActionResultResponseType.VALUE_URL;
                } 
                // else
                return ActionResultResponseType.VALUE;
            } else {
                return ActionResultResponseType.OBJECT;
            }
        } else {
            final List<Object> pojoList = asList(resultAdapter);
            switch (pojoList.size()) {
            case 1:
                return ActionResultResponseType.OBJECT;
            default:
                return ActionResultResponseType.COLLECTION;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(final ObjectAdapter resultAdapter) {
        final Collection<Object> coll = (Collection<Object>) resultAdapter.getObject();
        return coll instanceof List
                ? (List<Object>)coll
                : Lists.<Object>newArrayList(coll);
    }


}