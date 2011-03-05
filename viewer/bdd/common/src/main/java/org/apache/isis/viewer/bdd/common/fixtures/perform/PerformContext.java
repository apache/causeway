package org.apache.isis.viewer.bdd.common.fixtures.perform;

import java.util.List;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.UsingIsisViewerPeer;
import org.apache.isis.viewer.bdd.common.parsers.DateParser;

/**
 * Represents the context for a single {@link Perform} command; in effect, a row
 * of a table.
 */
public class PerformContext {

	private final UsingIsisViewerPeer peer;

	private final ObjectAdapter onAdapter;
	private final ObjectMember objectMember;
	private final List<ScenarioCell> argumentCells;

	public PerformContext(final UsingIsisViewerPeer peer,
			final ObjectAdapter onAdapter,
			final ObjectMember objectMember,
			final List<ScenarioCell> argumentCells) {
		this.onAdapter = onAdapter;
		this.objectMember = objectMember;
		this.peer = peer;
		this.argumentCells = argumentCells;
	}

	public UsingIsisViewerPeer getPeer() {
		return peer;
	}

	public ObjectAdapter getOnAdapter() {
		return onAdapter;
	}

	public ObjectMember getObjectMember() {
		return objectMember;
	}

	public List<ScenarioCell> getArgumentCells() {
		return argumentCells;
	}

	public Consent visibleMemberConsent() {
		return getObjectMember().isVisible(getAuthenticationSession(),
				getOnAdapter());
	}

	public Consent usableMemberConsent() {
		return getObjectMember().isUsable(getAuthenticationSession(),
				getOnAdapter());
	}

	public Consent validObjectConsent() {
		final ObjectAdapter onAdapter = getOnAdapter();
		return onAdapter.getSpecification().isValid(onAdapter);
	}

	public void ensureVisible(CellBinding onMemberBinding, final ScenarioCell onMemberCell)
	throws ScenarioBoundValueException {
		final Consent visible = objectMember.isVisible(getAuthenticationSession(),
				getOnAdapter());
		if (visible.isVetoed()) {
			throw ScenarioBoundValueException.current(onMemberBinding, "(not visible)");
		}
	}
	
	public void ensureUsable(CellBinding onMemberBinding, final ScenarioCell onMemberCell)
			throws ScenarioBoundValueException {
		final Consent usable = objectMember.isUsable(getAuthenticationSession(),
				getOnAdapter());
		if (usable.isVetoed()) {
			throw ScenarioBoundValueException.current(onMemberBinding, "(not usable)");
		}
	}
	
    public void ensureAvailableForDeploymentType(CellBinding onMemberBinding, ScenarioCell onMemberCell) throws ScenarioBoundValueException {
        DeploymentType deploymentType = this.peer.getDeploymentType();
        
        boolean isExploration = objectMember.getFacet(ExplorationFacet.class) != null;
        if(isExploration && !deploymentType.isExploring() ) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(not running in exploration mode)");
        }
        
        boolean isPrototype = objectMember.getFacet(PrototypeFacet.class) != null;
        if(isPrototype && !deploymentType.isPrototyping()) {
            throw ScenarioBoundValueException.current(onMemberBinding, "(not running in prototype mode)");
        }
    }


	protected AuthenticationSession getAuthenticationSession() {
		return getPeer().getAuthenticationSession();
	}

    public DateParser getDateParser() {
        return peer.getDateParser();
    }

}
