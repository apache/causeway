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

import {CAUSEWAY_MENU_BARS_NAMESPACE} from '../../src/menu-layout.mjs';

export const SAMPLE_SERVICE_LOGICAL_TYPE = 'causeway.webcomponents.sample.SampleMenu';
export const SAMPLE_SERVICE_FIELD = 'causeway_webcomponents_sample_SampleMenu';
export const SAMPLE_SERVICE_TYPE = 'rich__causeway_webcomponents_sample_SampleMenu';
const MEMBER_METADATA_TYPE = 'RichMemberMetadata';
const GREET_PARAMETER_TYPE = `${SAMPLE_SERVICE_TYPE}__greet__name__gqlv_action_parameter`;
const GREET_AUTOCOMPLETE_WINDOW_TYPE = `${GREET_PARAMETER_TYPE}_autocomplete_window`;

export function createMenuGraphQLTypes({menuBarsAvailable = true} = {}) {
  const applicationFields = [
    ...(menuBarsAvailable ? [field('menuBars', named('rich__gqlv_application_menu_bars'))] : []),
    field('home', named('rich__gqlv_application_home')),
    field('issues', list(named('rich__gqlv_application_issue')))
  ];
  return new Map([
    ['SimpleAndRich', objectType('SimpleAndRich', [field('rich', named('RICHSchema'))])],
    ['RICHSchema', objectType('RICHSchema', [
      field('application', nonNull(named('rich__gqlv_application_entry'))),
      field(SAMPLE_SERVICE_FIELD, named(SAMPLE_SERVICE_TYPE))
    ])],
    ['rich__gqlv_application_entry', objectType('rich__gqlv_application_entry', applicationFields)],
    ['rich__gqlv_application_menu_bars', objectType('rich__gqlv_application_menu_bars', [
      field('href', nonNull(scalar('String'))),
      field('mediaType', nonNull(scalar('String'))),
      field('formatVersion', nonNull(scalar('String'))),
      field('generation', nonNull(scalar('String'))),
      field('cacheControl', nonNull(scalar('String')))
    ])],
    ['rich__gqlv_application_issue', objectType('rich__gqlv_application_issue', [
      field('code', nonNull(scalar('String'))),
      field('message', nonNull(scalar('String')))
    ])],
    ['rich__gqlv_application_home', objectType('rich__gqlv_application_home', [
      field('kind', nonNull(named('rich__gqlv_application_home_kind'))),
      field('logicalTypeName', nonNull(scalar('String'))),
      field('object', nonNull(named('rich__gqlv_application_home_object')))
    ])],
    ['rich__gqlv_application_home_object', unionType('rich__gqlv_application_home_object', ['rich__sample_Home'])],
    [MEMBER_METADATA_TYPE, objectType(MEMBER_METADATA_TYPE, [
      field('areYouSure', scalar('Boolean')),
      field('promptStyle', scalar('String')),
      field('resultElementLogicalTypeName', scalar('String'))
    ])],
    [SAMPLE_SERVICE_TYPE, objectType(SAMPLE_SERVICE_TYPE, [
      field('welcomeMessage', named(`${SAMPLE_SERVICE_TYPE}__welcomeMessage__gqlv_action`)),
      field('greet', named(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action`)),
      field('disabledAction', named(`${SAMPLE_SERVICE_TYPE}__disabledAction__gqlv_action`)),
      field('hiddenAction', named(`${SAMPLE_SERVICE_TYPE}__hiddenAction__gqlv_action`)),
      field('clearNotes', named(`${SAMPLE_SERVICE_TYPE}__clearNotes__gqlv_action`)),
      field('openView', named(`${SAMPLE_SERVICE_TYPE}__openView__gqlv_action`))
    ])],
    [`${SAMPLE_SERVICE_TYPE}__welcomeMessage__gqlv_action`, actionType(
      `${SAMPLE_SERVICE_TYPE}__welcomeMessage__gqlv_action`,
      field('invoke', named(`${SAMPLE_SERVICE_TYPE}__welcomeMessage__gqlv_action_invoke`))
    )],
    [`${SAMPLE_SERVICE_TYPE}__welcomeMessage__gqlv_action_invoke`, invokeType(
      `${SAMPLE_SERVICE_TYPE}__welcomeMessage__gqlv_action_invoke`, scalar('String')
    )],
    [`${SAMPLE_SERVICE_TYPE}__disabledAction__gqlv_action`, actionType(
      `${SAMPLE_SERVICE_TYPE}__disabledAction__gqlv_action`,
      field('invoke', named(`${SAMPLE_SERVICE_TYPE}__disabledAction__gqlv_action_invoke`))
    )],
    [`${SAMPLE_SERVICE_TYPE}__disabledAction__gqlv_action_invoke`, invokeType(
      `${SAMPLE_SERVICE_TYPE}__disabledAction__gqlv_action_invoke`, scalar('String')
    )],
    [`${SAMPLE_SERVICE_TYPE}__hiddenAction__gqlv_action`, actionType(
      `${SAMPLE_SERVICE_TYPE}__hiddenAction__gqlv_action`,
      field('invoke', named(`${SAMPLE_SERVICE_TYPE}__hiddenAction__gqlv_action_invoke`))
    )],
    [`${SAMPLE_SERVICE_TYPE}__hiddenAction__gqlv_action_invoke`, invokeType(
      `${SAMPLE_SERVICE_TYPE}__hiddenAction__gqlv_action_invoke`, scalar('String')
    )],
    [`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action`, objectType(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action`, [
      field('hidden', scalar('Boolean')),
      field('disabled', scalar('String')),
      field('metadata', named(MEMBER_METADATA_TYPE)),
      field('params', named(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_params`)),
      field('validate', scalar('String'), [argument('name', scalar('String'))]),
      field('invoke', named(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_invoke`), [argument('name', nonNull(scalar('String')))])
    ])],
    [`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_params`, objectType(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_params`, [
      field('name', named(`${SAMPLE_SERVICE_TYPE}__greet__name__gqlv_action_parameter`))
    ])],
    [GREET_PARAMETER_TYPE, objectType(GREET_PARAMETER_TYPE, [
      field('hidden', scalar('Boolean'), [argument('name', scalar('String'))]),
      field('disabled', scalar('String'), [argument('name', scalar('String'))]),
      field('default', scalar('String'), [argument('name', scalar('String'))]),
      field('choices', list(scalar('String')), [argument('name', scalar('String'))]),
      field('autoComplete', list(scalar('String')), [argument('search', scalar('String'))]),
      field('autoCompleteWindow', named(GREET_AUTOCOMPLETE_WINDOW_TYPE), [
        argument('search', scalar('String')),
        {...argument('offset', scalar('Int')), defaultValue: '0'},
        {...argument('size', scalar('Int')), defaultValue: '2'}
      ]),
      field('validity', scalar('String'), [argument('name', scalar('String'))]),
      field('datatype', scalar('String'))
    ])],
    [GREET_AUTOCOMPLETE_WINDOW_TYPE, objectType(GREET_AUTOCOMPLETE_WINDOW_TYPE, [
      field('items', list(scalar('String'))), field('offset', scalar('Int')),
      field('requestedSize', scalar('Int')), field('returnedCount', scalar('Int')),
      field('totalCount', scalar('Int')), field('maximumSize', scalar('Int')),
      field('hasPrevious', scalar('Boolean')), field('hasNext', scalar('Boolean')),
      field('ordering', scalar('String'))
    ])],
    [`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_invoke`, invokeType(
      `${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_invoke`, scalar('String')
    )],
    [`${SAMPLE_SERVICE_TYPE}__clearNotes__gqlv_action`, actionType(
      `${SAMPLE_SERVICE_TYPE}__clearNotes__gqlv_action`,
      field('invokeNonIdempotent', named(`${SAMPLE_SERVICE_TYPE}__clearNotes__gqlv_action_invoke`))
    )],
    [`${SAMPLE_SERVICE_TYPE}__clearNotes__gqlv_action_invoke`, invokeType(
      `${SAMPLE_SERVICE_TYPE}__clearNotes__gqlv_action_invoke`, scalar('String')
    )],
    [`${SAMPLE_SERVICE_TYPE}__openView__gqlv_action`, objectType(
      `${SAMPLE_SERVICE_TYPE}__openView__gqlv_action`, [
        field('hidden', scalar('Boolean')),
        field('disabled', scalar('String')),
        field('metadata', named(MEMBER_METADATA_TYPE)),
        field('validate', scalar('String'))
      ]
    )],
    ['rich__sample_VersionlessViewModel', objectType('rich__sample_VersionlessViewModel', [
      field('_meta', named('rich__sample_VersionlessViewModel__gqlv_meta'))
    ])],
    ['rich__sample_VersionlessViewModel__gqlv_meta', objectType('rich__sample_VersionlessViewModel__gqlv_meta', [
      field('id', scalar('ID')),
      field('logicalTypeName', scalar('String')),
      field('title', scalar('String'))
    ])],
    ['Mutation', objectType('Mutation', [
      field(`${SAMPLE_SERVICE_FIELD}__clearNotes`, scalar('String')),
      field(`${SAMPLE_SERVICE_FIELD}__openView`, named('rich__sample_VersionlessViewModel'))
    ])]
  ]);
}

export function createMenuGraphQLExecutor({
  types = createMenuGraphQLTypes(),
  applicationEntries = [applicationEntryResponse()],
  actionState = null,
  delayApplication = null
} = {}) {
  const calls = [];
  const applicationCalls = [];
  const serviceCalls = [];
  const mutationCalls = [];
  let applicationIndex = 0;
  const executor = async request => {
    if (request.signal?.aborted) {
      throw abortError();
    }
    calls.push(request);
    if (request.operationName === 'CausewayDescribeTypes') {
      const data = {};
      for (const [variable, typeName] of Object.entries(request.variables)) {
        data[`describedType${Number(variable.slice('type'.length))}`] = types.get(typeName) ?? null;
      }
      return {data};
    }
    if (request.operationName === 'CausewayDescribeOperationRoots') {
      return {data: {__schema: {queryType: {name: 'SimpleAndRich'}, mutationType: {name: 'Mutation'}}}};
    }
    if (request.operationName === 'CausewayReadApplicationEntry') {
      applicationCalls.push(request);
      const currentIndex = applicationIndex;
      applicationIndex += 1;
      if (delayApplication) {
        await delayApplication(request, currentIndex);
      }
      const response = applicationEntries[Math.min(currentIndex, applicationEntries.length - 1)];
      return typeof response === 'function' ? response(request) : response;
    }
    if (request.operationName === 'CausewayReadServiceActionStates') {
      serviceCalls.push(request);
      return {data: {rich: {[SAMPLE_SERVICE_FIELD]: actionState ?? {
        welcomeMessage: {hidden: false, disabled: null, metadata: {areYouSure: false, promptStyle: 'DIALOG_MODAL'}},
        greet: {hidden: false, disabled: null, metadata: {areYouSure: false, promptStyle: 'DIALOG_SIDEBAR'}},
        disabledAction: {hidden: false, disabled: 'Available to administrators only.', metadata: {areYouSure: false, promptStyle: 'DIALOG_MODAL'}},
        hiddenAction: {hidden: true, disabled: null, metadata: {areYouSure: false, promptStyle: 'DIALOG_MODAL'}},
        clearNotes: {hidden: false, disabled: null, metadata: {areYouSure: true, promptStyle: 'DIALOG_MODAL'}}
      }}}};
    }
    if (request.operationName === 'CausewayPrepareServiceAction') {
      serviceCalls.push(request);
      return {data: {rich: {[SAMPLE_SERVICE_FIELD]: {greet: {params: {name: {
        hidden: false,
        disabled: null,
        default: 'Ada',
        choices: ['Ada', 'Grace'],
        validity: null,
        datatype: 'String'
      }}}}}}};
    }
    if (request.operationName === 'CausewayServiceActionParameterAutoCompleteWindow') {
      serviceCalls.push(request);
      return {data: {rich: {[SAMPLE_SERVICE_FIELD]: {greet: {params: {name: {
        autoCompleteWindow: {
          items: ['Grace'], offset: 2, requestedSize: 2, returnedCount: 1,
          totalCount: 3, maximumSize: 2, hasPrevious: true, hasNext: false,
          ordering: 'APPLICATION'
        }
      }}}}}}};
    }
    if (request.operationName === 'CausewayValidateServiceAction') {
      serviceCalls.push(request);
      const value = Object.values(request.variables)[0];
      return {data: {rich: {[SAMPLE_SERVICE_FIELD]: {greet: {validate: value ? null : 'A name is required.'}}}}};
    }
    if (request.operationName === 'CausewayInvokeServiceAction') {
      if (request.document.startsWith('mutation')) {
        mutationCalls.push(request);
        if (request.document.includes(`${SAMPLE_SERVICE_FIELD}__openView`)) {
          return {data: {[`${SAMPLE_SERVICE_FIELD}__openView`]: {_meta: {
            id: 'view-1', logicalTypeName: 'sample.VersionlessViewModel', title: 'View one'
          }}}};
        }
        return {data: {[`${SAMPLE_SERVICE_FIELD}__clearNotes`]: 'Cleared'}};
      }
      serviceCalls.push(request);
      const value = Object.values(request.variables)[0];
      const actionId = request.document.includes('greet') ? 'greet' : 'welcomeMessage';
      const result = actionId === 'greet' ? `Hello, ${value}!` : 'Welcome!';
      return {data: {rich: {[SAMPLE_SERVICE_FIELD]: {[actionId]: {invoke: {results: result}}}}}};
    }
    throw new Error(`Unexpected menu fixture operation '${request.operationName}'.`);
  };
  Object.assign(executor, {calls, applicationCalls, serviceCalls, mutationCalls, types});
  return executor;
}

export function applicationEntryResponse({href = '/graphql/application/menu-bars', issues = [], home = {
  kind: 'OBJECT',
  logicalTypeName: 'sample.Home',
  object: {__typename: 'rich__sample_Home', _meta: {id: 'home-1', logicalTypeName: 'sample.Home', title: 'Home'}}
}} = {}) {
  return {data: {rich: {application: {
    menuBars: {
      href,
      mediaType: 'application/xml',
      formatVersion: CAUSEWAY_MENU_BARS_NAMESPACE,
      generation: 'fixture-generation',
      cacheControl: 'private, no-store'
    },
    home,
    issues
  }}}};
}

function actionType(name, invocationField) {
  return objectType(name, [
    field('hidden', scalar('Boolean')),
    field('disabled', scalar('String')),
    field('metadata', named(MEMBER_METADATA_TYPE)),
    invocationField
  ]);
}

function invokeType(name, resultType) {
  return objectType(name, [field('results', resultType), field('target', scalar('String'))]);
}

function objectType(name, fields) {
  return {kind: 'OBJECT', name, description: null, fields};
}

function unionType(name, possibleTypeNames) {
  return {
    kind: 'UNION',
    name,
    description: null,
    fields: [],
    possibleTypes: possibleTypeNames.map(possibleName => ({kind: 'OBJECT', name: possibleName}))
  };
}

function field(name, type, args = []) {
  return {name, description: null, args, type};
}

function argument(name, type) {
  return {name, description: null, defaultValue: null, type};
}

function scalar(name) {
  return {kind: 'SCALAR', name, ofType: null};
}

function named(name) {
  return {kind: 'OBJECT', name, ofType: null};
}

function list(ofType) {
  return {kind: 'LIST', name: null, ofType};
}

function nonNull(ofType) {
  return {kind: 'NON_NULL', name: null, ofType};
}

function abortError() {
  const error = new Error('Aborted');
  error.name = 'AbortError';
  return error;
}
