/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.nof.reflect.remote.spec;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.adapter.ObjectAdapterReference;
import org.apache.isis.noa.facets.Facet;
import org.apache.isis.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.metamodel.facets.actions.executed.ExecutedFacet;
import org.apache.isis.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.noa.reflect.ObjectAction;
import org.apache.isis.noa.reflect.ObjectActionParameter;
import org.apache.isis.noa.reflect.ObjectActionParameter.Filter;
import org.apache.isis.noa.spec.ObjectSpecification;
import org.apache.isis.nof.core.context.IsisContext;
import org.apache.isis.nof.core.reflect.Allow;
import org.apache.isis.nof.core.util.NameConvertor;
import org.apache.isis.nof.core.util.NotImplementedException;
import org.apache.isis.nof.reflect.peer.ActionPeer;
import org.apache.isis.nof.reflect.peer.MemberIdentifier;
import org.apache.isis.nof.reflect.spec.DefaultOneToOneActionParameter;
import org.apache.isis.nof.reflect.spec.DefaultValueActionParameter;
import org.apache.isis.testing.TestSession;


public class DummyAction implements ObjectAction {
    public final ActionPeer peer;

	public DummyAction(final ActionPeer peer) {
		this.peer = peer;
	}

	public boolean[] canParametersWrap() {
        return null;
    }

	public String debugData() {
		return "";
	}

	public ObjectAdapter execute(final ObjectAdapterReference object, final ObjectAdapter[] parameters) {
		return peer.execute(object, parameters);
	}

	public ObjectAction[] getActions() {
		return new ObjectAction[0];
	}

	public ObjectAdapter[] getDefaultParameterValues(ObjectAdapterReference target) {
		throw new NotImplementedException();
	}

	public String getDescription() {
		return "";
	}

	public Facet getFacet(final Class cls) {
		return null;
	}

	public Class[] getFacetTypes() {
		return new Class[0];
	}
	
	public Facet[] getFacets(Facet.Filter filter) {
		return new Facet[]{};
	}

	public void addFacet(Facet facet) {
	}

	public void removeFacet(Facet facet) {
	}

	public String getHelp() {
		return "";
	}

	public String getId() {
		return NameConvertor.simpleName(peer.getIdentifier().getName());
	}

	public MemberIdentifier getIdentifier() {
		throw new NotImplementedException();
	}

	public String getName() {
		return "";
	}
    
    public ObjectSpecification getOnType() {
        return peer.getOnType();
    }

	public ObjectAdapter[][] getOptions(ObjectAdapterReference target) {
		return null;
	}

    public int getParameterCount() {
		return peer.getParameterCount();
	}
    
	public String[] getParameterDescriptions() {
        return null;
    }

	public int[] getParameterMaxLengths() {
        return null;
    }

	public String[] getParameterNames() {
		return new String[]{};
	}

	public int[] getParameterNoLines() {
        return null;
    }

    /**
     * Build lazily by {@link #getParameters()}.
     */
    private ObjectActionParameter[] parameters;
    public ObjectActionParameter[] getParameters() {
        if (parameters == null) {
            parameters = new ObjectActionParameter[getParameterCount()];
            ObjectSpecification[] parameterTypes = getParameterTypes();
            String[] parameterNames = getParameterNames();
            String[] parameterDescriptions = getParameterDescriptions();
            boolean[] optionalParameters = getOptionalParameters();
            
            int[] parameterNoLines = getParameterNoLines();
            boolean[] canParametersWrap = canParametersWrap();
            int[] parameterMaxLengths = getParameterMaxLengths();
            int[] parameterTypicalLengths = getParameterTypicalLengths();
            
            for(int i=0; i<getParameterCount(); i++) {
                if (parameterTypes[i].getType() == ObjectSpecification.VALUE) {
                    parameters[i] = new DefaultValueActionParameter(
                            i, 
                            this,
                            null,
                            parameterTypes[i], 
                            parameterNames[i], parameterDescriptions[i], 
                            optionalParameters[i], 
                            parameterTypicalLengths[i], parameterMaxLengths[i],
                            parameterNoLines[i], canParametersWrap[i]);
                } else if (parameterTypes[i].getType() == ObjectSpecification.OBJECT) {
                    parameters[i] = new DefaultOneToOneActionParameter(
                            i, 
                            this,
                            null,
                            parameterTypes[i], 
                            parameterNames[i], parameterDescriptions[i], 
                            optionalParameters[i]);
                } else if (parameterTypes[i].getType() == ObjectSpecification.COLLECTION) {
                    // TODO: not supported; should we throw an exception of some sort here?
                    parameters[i] = null;
                }
                
            }
        }
        return parameters;
    }
    
    
    public ObjectActionParameter[] getParameters(
            Filter filter) {
        ObjectActionParameter[] allParameters = getParameters();
        
        ObjectActionParameter[] selectedParameters = new ObjectActionParameter[allParameters.length];
        int v = 0;
        for (int i = 0; i < allParameters.length; i++) {
            if (filter.accept(allParameters[i])) {
                selectedParameters[v++] = allParameters[i];
            }
        }

        ObjectActionParameter[] parameters = new ObjectActionParameter[v];
        System.arraycopy(selectedParameters, 0, parameters, 0, v);
        return parameters;
    }


//	public ObjectSpecification[] getParameterTypes() {
//		return peer.getParameterTypes();
//	}

	public int[] getParameterTypicalLengths() {
        return null;
    }

//	public boolean[] getOptionalParameters() {
//		return peer.getOptionalParameters();
//	}

	public ObjectSpecification getReturnType() {
		return peer.getReturnType();
	}

	public Target getTarget() {
		ExecutedFacet executedFacet = (ExecutedFacet) peer.getFacet(ExecutedFacet.class);
		return executedFacet == null?ObjectAction.DEFAULT:executedFacet.getTarget();
	}

	public Type getType() {
		DebugFacet debugFacet = (DebugFacet) peer.getFacet(DebugFacet.class);
		if (debugFacet != null) {
			return ObjectAction.DEBUG;
		}
		ExplorationFacet explorationFacet = (ExplorationFacet) peer.getFacet(ExplorationFacet.class);
		if (explorationFacet != null) {
			return ObjectAction.EXPLORATION;
		}
		return ObjectAction.USER;
	}

	public boolean hasReturn() {
		return false;
	}

	public boolean isOnInstance() {
		return peer.isOnInstance();
	}

    /**
     * Delegates to {@link #isParameterSetValidDeclaratively(ObjectAdapterReference, ObjectAdapter[])} and
     * then {@link #isParameterSetValidImperatively(ObjectAdapterReference, ObjectAdapter[])}, as per the
     * contract in the {@link ObjectAction implemented interface}.
     */
	public Consent isParameterSetValid(final ObjectAdapterReference object,
			final ObjectAdapter[] parameters) {
	    Consent consentDeclaratively = isParameterSetValidDeclaratively(object, parameters);
        if (consentDeclaratively.isVetoed()) {
            return consentDeclaratively;
        }
		return isParameterSetValidImperatively(object, parameters);
	}

    /**
     * Always returns an {@link Allow}.
     */
    public Consent isParameterSetValidDeclaratively(
        ObjectAdapterReference object,
        ObjectAdapter[] parameters) {
        return Allow.DEFAULT;
    }

    public Consent isParameterSetValidImperatively(
        ObjectAdapterReference object,
        ObjectAdapter[] parameters) {
        return peer.isParameterSetValidImperatively(object, parameters);
    }

    public Consent isUsable(final ObjectAdapterReference target) {
		return peer.isUsable(target);
	}

    public Consent isUsable() {
        Consent usableDeclaratively = isUsableDeclaratively();
        if (usableDeclaratively.isVetoed()) {
            return usableDeclaratively;
        }
        return isUsableForSession();
    }

    public Consent isUsableDeclaratively() {
        return peer.isUsableDeclaratively();
    }

    public Consent isUsableForSession() {
        return peer.isUsableForSession(IsisContext.getSession());
    }

    public boolean isContributedMethodWithSuitableParameter() {
        return false;
    }
    
    public boolean isVisible() {
        return isVisibleDeclaratively() && isVisibleForSession();
    }

    public boolean isVisibleDeclaratively() {
		return peer.isVisibleDeclaratively();
	}

    public boolean isVisibleForSession() {
        return peer.isVisibleForSession(new TestSession());
    }

    public boolean isVisible(final ObjectAdapterReference target) {
		return peer.isVisible(target);
	}

    public ObjectAdapter[] parameterStubs() {
		throw new NotImplementedException();
	}
    
    public ObjectAdapterReference realTarget(ObjectAdapterReference target) {
        return target;
    }

    public ObjectSpecification getSpecification() {
        return null;
    }




}
