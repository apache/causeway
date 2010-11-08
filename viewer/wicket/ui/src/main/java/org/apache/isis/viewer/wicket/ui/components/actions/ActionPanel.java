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


package org.apache.isis.viewer.wicket.ui.components.actions;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.viewer.wicket.model.common.SelectionHandler;
import org.apache.isis.viewer.wicket.model.models.ActionExecutor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.model.nof.PersistenceSessionAccessor;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing an action invocation, backed by an
 * {@link ActionModel}.
 * 
 * <p>
 * Based on the {@link ActionModel.Mode mode}, will render either parameter
 * dialog or the results.
 * 
 * <p>
 * TODO: on results panel, have a button to resubmit?
 */
public class ActionPanel extends PanelAbstract<ActionModel> implements
		ActionExecutor {

	private static final long serialVersionUID = 1L;

	/**
	 * The various component types, one of which will be rendered.
	 * 
	 * @see #hideAllBut(ComponentType)
	 */
	private static final List<ComponentType> COMPONENT_TYPES = Arrays.asList(
			ComponentType.PARAMETERS, ComponentType.ENTITY_LINK,
			ComponentType.ENTITY, ComponentType.VALUE,
			ComponentType.EMPTY_COLLECTION, ComponentType.VOID_RETURN,
			ComponentType.COLLECTION_CONTENTS);

	public ActionPanel(String id, ActionModel actionModel) {
		super(id, actionModel);
		actionModel.setExecutor(this);
		buildGui(actionModel);
	}

	private void buildGui(ActionModel actionModel) {
		if (actionModel.getActionMode() == ActionModel.Mode.PARAMETERS) {
			buildGuiForParameters();
		} else {
			executeActionAndProcessResults();
		}
	}

	private ActionModel getActionModel() {
		return (ActionModel) super.getModel();
	}

	private void buildGuiForParameters() {
		hideAllBut(ComponentType.PARAMETERS);
		getComponentFactoryRegistry().addOrReplaceComponent(this,
				ComponentType.PARAMETERS, getActionModel());
	}

	public void executeActionAndProcessResults() {

		final ObjectAdapter targetAdapter = getModel().getTargetAdapter();

		ActionModel actionModel = getActionModel();
		String invalidReasonIfAny = actionModel.getReasonInvalidIfAny();
		if (invalidReasonIfAny != null) {
			error(invalidReasonIfAny);
			return;
		}

		// executes the action
		ObjectAdapter resultAdapter = actionModel.getObject();
		if (resultAdapter == null) {
			// TODO: a void; should indicate somehow
			resultAdapter = targetAdapter;
		}

		ResultType resultType = ResultType.determineFor(resultAdapter);

		resultType.addResults(this, resultAdapter);
	}

	enum ResultType {
		OBJECT {
			@Override
			public void addResults(ActionPanel panel, ObjectAdapter resultAdapter) {

				ObjectAdapter actualAdapter = determineActualAdapter(
						resultAdapter, panel);

				addResultsAccordingToSingleResultsMode(panel, actualAdapter);
			}

			private ObjectAdapter determineActualAdapter(
					ObjectAdapter resultAdapter, PersistenceSessionAccessor psa) {
				ObjectAdapter actualAdapter;
				if (resultAdapter.getSpecification().isNotCollection()) {
					actualAdapter = resultAdapter;
				} else {
					// will only be a single element
					List<Object> pojoList = asList(resultAdapter);
					Object pojo = pojoList.get(0);
					actualAdapter = adapterFor(pojo, psa);
				}
				return actualAdapter;
			}

			private void addResultsAccordingToSingleResultsMode(
					ActionPanel panel, ObjectAdapter actualAdapter) {
				final ActionModel actionModel = panel.getActionModel();
				ActionModel.SingleResultsMode singleResultsMode = actionModel
						.getSingleResultsMode();

				if (singleResultsMode == ActionModel.SingleResultsMode.REDIRECT) {
					panel.setResponsePage(new EntityPage(actualAdapter));
				} else if (singleResultsMode == ActionModel.SingleResultsMode.SELECT) {
					actionModel.getSelectionHandler().onSelected(panel, actualAdapter);
				} else if (singleResultsMode == ActionModel.SingleResultsMode.INLINE) {
					ComponentType componentType = ComponentType.ENTITY;
					panel.hideAllBut(componentType);
					panel.addOrReplace(componentType, new EntityModel(
							actualAdapter));
				} else {
					ComponentType componentType = ComponentType.ENTITY_LINK;
					panel.hideAllBut(componentType);
					panel.addOrReplace(componentType, new EntityModel(
							actualAdapter));
				}
			}

			private ObjectAdapter adapterFor(Object pojo,
					PersistenceSessionAccessor psa) {
				return psa.getPersistenceSession().getAdapterManager()
						.adapterFor(pojo);
			}
		},
		COLLECTION {
			@Override
			public void addResults(ActionPanel panel, ObjectAdapter resultAdapter) {
				panel.hideAllBut(ComponentType.COLLECTION_CONTENTS);
				addOrReplaceCollectionResultsPanel(panel, resultAdapter);
			}

			private void addOrReplaceCollectionResultsPanel(ActionPanel panel,
					ObjectAdapter resultAdapter) {
				EntityCollectionModel collectionModel = EntityCollectionModel
						.createStandalone(resultAdapter);
				SelectionHandler selectionHandler = panel.getModel()
						.getSelectionHandler();
				if (selectionHandler != null) {
					collectionModel.setSelectionHandler(selectionHandler);
				}
				panel.getComponentFactoryRegistry().addOrReplaceComponent(
						panel, ComponentType.COLLECTION_CONTENTS,
						collectionModel);
			}
		},
		EMPTY {
			@Override
			public void addResults(final ActionPanel panel, ObjectAdapter resultAdapter) {
				panel.hideAllBut(ComponentType.EMPTY_COLLECTION);
				final ActionModel actionModel = panel.getActionModel();
				panel.getComponentFactoryRegistry().addOrReplaceComponent(
						panel, ComponentType.EMPTY_COLLECTION,
						actionModel);
			}
		},
		VALUE {
			@Override
			public void addResults(ActionPanel panel, ObjectAdapter resultAdapter) {
				panel.hideAllBut(ComponentType.VALUE);
				panel.getComponentFactoryRegistry().addOrReplaceComponent(
						panel, ComponentType.VALUE,
						new ValueModel(resultAdapter));
			}
		},
		VOID {
			@Override
			public void addResults(ActionPanel panel, ObjectAdapter resultAdapter) {
				panel.hideAllBut(ComponentType.VOID_RETURN);

				// TODO: implement panel for void
				panel.permanentlyHide(ComponentType.VOID_RETURN);
			}
		};

		public abstract void addResults(ActionPanel panel,
				ObjectAdapter resultAdapter);

		static ResultType determineFor(ObjectAdapter resultAdapter) {
			ObjectSpecification resultSpec = resultAdapter.getSpecification();
			if (resultSpec.isNotCollection()) {
				if (resultSpec.getFacet(ValueFacet.class) != null) {
					return ResultType.VALUE;
				} else {
					return ResultType.OBJECT;
				}
			} else {
				List<Object> pojoList = asList(resultAdapter);
				switch (pojoList.size()) {
				case 0:
					return ResultType.EMPTY;
				case 1:
					return ResultType.OBJECT;
				default:
					return ResultType.COLLECTION;
				}
			}
		}

		@SuppressWarnings("unchecked")
		private static List<Object> asList(ObjectAdapter resultAdapter) {
			return (List<Object>) resultAdapter.getObject();
		}
	}

	private void hideAllBut(ComponentType visibleComponentType) {
		for (ComponentType componentType : COMPONENT_TYPES) {
			if (componentType != visibleComponentType) {
				permanentlyHide(componentType);
			}
		}
	}

}
