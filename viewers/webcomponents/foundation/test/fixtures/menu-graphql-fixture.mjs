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

export function createMenuGraphQLTypes({menuBarsAvailable = true} = {}) {
  const applicationFields = [
    ...(menuBarsAvailable ? [field('menuBars', named('rich__gqlv_application_menu_bars'))] : []),
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
    [SAMPLE_SERVICE_TYPE, objectType(SAMPLE_SERVICE_TYPE, [
      field('welcomeMessage', named(`${SAMPLE_SERVICE_TYPE}__welcomeMessage__gqlv_action`)),
      field('greet', named(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action`)),
      field('disabledAction', named(`${SAMPLE_SERVICE_TYPE}__disabledAction__gqlv_action`)),
      field('hiddenAction', named(`${SAMPLE_SERVICE_TYPE}__hiddenAction__gqlv_action`)),
      field('clearNotes', named(`${SAMPLE_SERVICE_TYPE}__clearNotes__gqlv_action`))
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
      field('params', named(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_params`)),
      field('validate', scalar('String'), [argument('name', scalar('String'))]),
      field('invoke', named(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_invoke`), [argument('name', nonNull(scalar('String')))])
    ])],
    [`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_params`, objectType(`${SAMPLE_SERVICE_TYPE}__greet__gqlv_action_params`, [
      field('name', named(`${SAMPLE_SERVICE_TYPE}__greet__name__gqlv_action_parameter`))
    ])],
    [`${SAMPLE_SERVICE_TYPE}__greet__name__gqlv_action_parameter`, objectType(`${SAMPLE_SERVICE_TYPE}__greet__name__gqlv_action_parameter`, [
      field('hidden', scalar('Boolean'), [argument('name', scalar('String'))]),
      field('disabled', scalar('String'), [argument('name', scalar('String'))]),
      field('default', scalar('String'), [argument('name', scalar('String'))]),
      field('choices', list(scalar('String')), [argument('name', scalar('String'))]),
      field('validity', scalar('String'), [argument('name', scalar('String'))]),
      field('datatype', scalar('String'))
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
    ['Mutation', objectType('Mutation', [
      field(`${SAMPLE_SERVICE_FIELD}__clearNotes`, scalar('String'))
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
        welcomeMessage: {hidden: false, disabled: null},
        greet: {hidden: false, disabled: null},
        disabledAction: {hidden: false, disabled: 'Available to administrators only.'},
        hiddenAction: {hidden: true, disabled: null},
        clearNotes: {hidden: false, disabled: null}
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
    if (request.operationName === 'CausewayValidateServiceAction') {
      serviceCalls.push(request);
      const value = Object.values(request.variables)[0];
      return {data: {rich: {[SAMPLE_SERVICE_FIELD]: {greet: {validate: value ? null : 'A name is required.'}}}}};
    }
    if (request.operationName === 'CausewayInvokeServiceAction') {
      if (request.document.startsWith('mutation')) {
        mutationCalls.push(request);
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

export function applicationEntryResponse({href = '/graphql/application/menu-bars', issues = []} = {}) {
  return {data: {rich: {application: {
    menuBars: {
      href,
      mediaType: 'application/xml',
      formatVersion: CAUSEWAY_MENU_BARS_NAMESPACE,
      generation: 'fixture-generation',
      cacheControl: 'private, no-store'
    },
    issues
  }}}};
}

function actionType(name, invocationField) {
  return objectType(name, [
    field('hidden', scalar('Boolean')),
    field('disabled', scalar('String')),
    invocationField
  ]);
}

function invokeType(name, resultType) {
  return objectType(name, [field('results', resultType), field('target', scalar('String'))]);
}

function objectType(name, fields) {
  return {kind: 'OBJECT', name, description: null, fields};
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
