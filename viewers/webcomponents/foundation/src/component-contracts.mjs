/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Stable public custom-element names for the framework-neutral Causeway component vocabulary.
 */
export const CausewayElementName = Object.freeze({
  GRAPHQL_CLIENT: 'cw-graphql-client',
  OBJECT_CONTEXT: 'cw-object-context',
  OBJECT: 'cw-object',
  OBJECT_HEADER: 'cw-object-header',
  BREADCRUMBS: 'cw-breadcrumbs',
  PROPERTY: 'cw-property',
  VALUE: 'cw-value',
  OBJECT_LINK: 'cw-object-link',
  ACTION: 'cw-action',
  PARAMETER: 'cw-parameter',
  INTERACTION_CONTROLLER: 'cw-interaction-controller',
  REFERENCE_EDITOR: 'cw-reference-editor',
  COLLECTION: 'cw-collection',
  STANDALONE_COLLECTION: 'cw-standalone-collection',
  COLLECTION_COLUMN: 'cw-collection-column',
  MENUBARS: 'cw-menubars',
  MENUBAR_PRIMARY: 'cw-menubar-primary',
  MENUBAR_SECONDARY: 'cw-menubar-secondary',
  MENUBAR_TERTIARY: 'cw-menubar-tertiary'
});

/**
 * Stable semantic host classes emitted by public components.
 */
export const CausewayHostClass = Object.freeze({
  OBJECT: 'causeway-object',
  OBJECT_HEADER: 'causeway-object-header',
  BREADCRUMBS: 'causeway-breadcrumbs',
  PROPERTY: 'causeway-property',
  VALUE: 'causeway-value',
  OBJECT_LINK: 'causeway-object-link',
  ACTION: 'causeway-action',
  PARAMETER: 'causeway-parameter',
  INTERACTION_CONTROLLER: 'causeway-interaction-controller',
  ACTION_PROMPT: 'causeway-action-prompt',
  ACTION_RESULT: 'causeway-action-result',
  PROPERTY_EDITOR: 'causeway-property-editor',
  COLLECTION: 'causeway-collection',
  STANDALONE_COLLECTION: 'causeway-standalone-collection',
  COLLECTION_COLUMN: 'causeway-collection-column',
  MENUBARS: 'causeway-menubars',
  MENUBAR: 'causeway-menubar',
  MENU: 'causeway-menu',
  MENU_SECTION: 'causeway-menu-section',
  SERVICE_ACTION: 'causeway-service-action',
  LOADING: 'causeway-loading',
  DISABLED: 'causeway-disabled',
  EMPTY: 'causeway-empty',
  ERROR: 'causeway-error',
  UNSUPPORTED: 'causeway-unsupported'
});

/**
 * Stable attributes shared by semantic member components.
 */
export const CausewayAttribute = Object.freeze({
  MEMBER: 'member',
  LABEL: 'label',
  NAMED: 'named',
  DESCRIBED_AS: 'described-as',
  DESCRIPTION_AS: 'description-as',
  MULTI_LINE: 'multi-line',
  LABEL_POSITION: 'label-position',
  MIN: 'min',
  MAX: 'max',
  PROMPT_STYLE: 'prompt-style',
  ACTIVE: 'active',
  LOGICAL_TYPE: 'logical-type',
  OBJECT_ID: 'object-id',
  TITLE: 'title',
  DISABLED: 'disabled',
  EDITABLE: 'editable',
  BAR: 'bar'
});

/**
 * Structured event payload contracts.
 *
 * Navigation detail: `{target: {logicalTypeName, id, title}, sourceContext}`.
 * Action detail: `{actionId, identity, context}`.
 */
export const CausewaySemanticEvent = Object.freeze({
  NAVIGATION_REQUEST: 'causeway-navigation-request',
  ACTION_REQUEST: 'causeway-action-request',
  ACTION_PARAMETER_CONFIGURATION: 'causeway-action-parameter-configuration-change',
  ACTION_PROMPT_STATE: 'causeway-action-prompt-state-change',
  ACTION_RESULT: 'causeway-action-result',
  PROPERTY_INTERACTION_STATE: 'causeway-property-interaction-state-change',
  PROPERTY_UPDATED: 'causeway-property-updated',
  COLLECTION_STATE: 'causeway-collection-state-change',
  COLLECTION_CONFIGURATION: 'causeway-collection-configuration-change',
  OBJECT_LAYOUT_STATE: 'causeway-object-layout-state-change',
  OBJECT_LAYOUT_DIAGNOSTIC: 'causeway-object-layout-diagnostic',
  MENU_BARS_STATE: 'causeway-menubars-state-change',
  MENU_BARS_DIAGNOSTIC: 'causeway-menubars-diagnostic'
});
