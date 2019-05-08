package org.apache.isis.core.runtime.system.context.session;

import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Tuples;
import org.apache.isis.commons.internal.base._Tuples.Tuple2;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

/**
 * TODO [2033], there's already a service doing this
 * 
 * @since 2.0.0-M3
 */
@RequiredArgsConstructor
final class RuntimeContextBase_findHomepage {

	private final RuntimeContextBase holder;

	public Tuple2<ObjectAdapter, ObjectAction> findHomePageAction() {
		
		var metaModelContext = MetaModelContext.current();
		
		final Stream<ObjectAdapter> serviceAdapters = metaModelContext.streamServiceAdapters();
		return serviceAdapters.map(serviceAdapter->{
			final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
			final Stream<ObjectAction> objectActions = serviceSpec.streamObjectActions(Contributed.EXCLUDED);

			val homePageAction = objectActions
					.map(objectAction->objectAndActionIfHomePageAndUsable(serviceAdapter, objectAction))
					.filter(_NullSafe::isPresent)
					.findAny()
					.orElse(null);
			return homePageAction;
		})
		.filter(_NullSafe::isPresent)
		.findAny()
		.orElse(null)
		;
	}

	private _Tuples.Tuple2<ObjectAdapter, ObjectAction> objectAndActionIfHomePageAndUsable(
			ObjectAdapter serviceAdapter, 
			ObjectAction objectAction) {

		if (!objectAction.containsDoOpFacet(HomePageFacet.class)) {
			return null;
		}

		final Consent visibility =
				objectAction.isVisible(
						serviceAdapter,
						InteractionInitiatedBy.USER,
						Where.ANYWHERE);
		if (visibility.isVetoed()) {
			return null;
		}

		final Consent usability =
				objectAction.isUsable(
						serviceAdapter,
						InteractionInitiatedBy.USER,
						Where.ANYWHERE
						);
		if (usability.isVetoed()) {
			return  null;
		}

		return _Tuples.pair(serviceAdapter, objectAction);
	}

}
