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

const OBJECT_TYPE = 'rich__university_dept_Department';
const META_TYPE = `${OBJECT_TYPE}__gqlv_meta`;
const NAME_TYPE = `${OBJECT_TYPE}__name__gqlv_property`;
const CODE_TYPE = `${OBJECT_TYPE}__code__gqlv_property`;
const COLLECTION_TYPE = `${OBJECT_TYPE}__staffMembers__gqlv_collection`;
const ACTION_TYPE = `${OBJECT_TYPE}__changeName__gqlv_action`;
const ACTION_PARAMS_TYPE = `${OBJECT_TYPE}__changeName__gqlv_action_params`;
const ACTION_PARAM_TYPE = `${OBJECT_TYPE}__changeName__newName__gqlv_action_parameter`;

export const DEPARTMENT_LOGICAL_TYPE = 'university.dept.Department';
export const DEPARTMENT_OBJECT_FIELD = 'university_dept_Department';

export function createRichSchemaTypes() {
  return new Map([
    [OBJECT_TYPE, objectType(OBJECT_TYPE, 'A university department.', [
      field('_meta', 'Object metadata', named(META_TYPE)),
      field('name', 'Department name', named(NAME_TYPE)),
      field('code', 'Department code', named(CODE_TYPE)),
      field('staffMembers', 'Staff members', named(COLLECTION_TYPE)),
      field('changeName', 'Change the department name', named(ACTION_TYPE))
    ])],
    [META_TYPE, objectType(META_TYPE, 'Object metadata', [
      field('id', null, scalar('ID')),
      field('logicalTypeName', null, scalar('String')),
      field('version', null, scalar('String')),
      field('title', null, scalar('String'))
    ])],
    [NAME_TYPE, propertyType(NAME_TYPE)],
    [CODE_TYPE, propertyType(CODE_TYPE)],
    [COLLECTION_TYPE, objectType(COLLECTION_TYPE, null, [
      field('hidden', null, scalar('Boolean')),
      field('disabled', null, scalar('String')),
      field('get', null, list(named('rich__university_dept_StaffMember'))),
      field('datatype', null, scalar('String'))
    ])],
    [ACTION_TYPE, objectType(ACTION_TYPE, null, [
      field('hidden', null, scalar('Boolean')),
      field('disabled', null, scalar('String')),
      field('params', null, named(ACTION_PARAMS_TYPE))
    ])],
    [ACTION_PARAMS_TYPE, objectType(ACTION_PARAMS_TYPE, null, [
      field('newName', null, named(ACTION_PARAM_TYPE))
    ])],
    [ACTION_PARAM_TYPE, objectType(ACTION_PARAM_TYPE, null, [
      field('hidden', null, scalar('Boolean')),
      field('disabled', null, scalar('String')),
      field('default', null, scalar('String')),
      field('validate', null, scalar('String'), [argument('newName', scalar('String'))]),
      field('datatype', null, scalar('String'))
    ])]
  ]);
}

export function departmentObjectData({
  name = 'Classics',
  code = 'CLA',
  codeHidden = false,
  nameDisabled = null,
  version = '7'
} = {}) {
  return {
    _meta: {
      id: '42',
      logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
      version,
      title: 'Classics Department'
    },
    name: {hidden: false, disabled: nameDisabled, get: name},
    code: {hidden: codeHidden, disabled: null, get: code}
  };
}

export function graphQLObjectResponse(options = {}) {
  return {
    data: {
      rich: {
        [DEPARTMENT_OBJECT_FIELD]: departmentObjectData(options)
      }
    }
  };
}

export function partialPropertyErrorResponse() {
  return {
    data: {
      rich: {
        [DEPARTMENT_OBJECT_FIELD]: {
          _meta: departmentObjectData()._meta,
          name: departmentObjectData().name,
          code: null
        }
      }
    },
    errors: [{
      message: 'Code is not readable.',
      path: ['rich', DEPARTMENT_OBJECT_FIELD, 'code', 'get'],
      extensions: {classification: 'DataFetchingException'}
    }]
  };
}

export function createRichSchemaFixtureExecutor({types = createRichSchemaTypes(), readResponses = [graphQLObjectResponse()]} = {}) {
  const calls = [];
  const introspectionCalls = [];
  const readCalls = [];
  let readIndex = 0;
  const executor = async request => {
    if (request.signal?.aborted) {
      throw abortError();
    }
    calls.push(request);
    if (request.operationName === 'CausewayDescribeTypes') {
      introspectionCalls.push(request);
      const data = {};
      Object.entries(request.variables).forEach(([variable, typeName]) => {
        const index = Number(variable.slice('type'.length));
        data[`describedType${index}`] = types.get(typeName) ?? null;
      });
      return {data};
    }
    if (request.operationName === 'CausewayReadObject') {
      readCalls.push(request);
      const response = readResponses[Math.min(readIndex, readResponses.length - 1)];
      readIndex += 1;
      return typeof response === 'function' ? response(request) : response;
    }
    throw new Error(`Unexpected fixture operation '${request.operationName}'.`);
  };
  Object.assign(executor, {calls, introspectionCalls, readCalls, types});
  return executor;
}

export function waitFor(predicate, {timeout = 2000, interval = 5} = {}) {
  return new Promise((resolve, reject) => {
    const started = Date.now();
    const poll = () => {
      if (predicate()) {
        resolve();
      } else if (Date.now() - started >= timeout) {
        reject(new Error('Timed out waiting for fixture condition.'));
      } else {
        setTimeout(poll, interval);
      }
    };
    poll();
  });
}

function propertyType(name) {
  return objectType(name, null, [
    field('hidden', null, scalar('Boolean')),
    field('disabled', null, scalar('String')),
    field('get', null, scalar('String')),
    field('validate', null, scalar('String'), [argument('value', scalar('String'))]),
    field('datatype', null, scalar('String'))
  ]);
}

function objectType(name, description, fields) {
  return {kind: 'OBJECT', name, description, fields};
}

function field(name, description, type, args = []) {
  return {name, description, args, type};
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

function abortError() {
  const error = new Error('Aborted');
  error.name = 'AbortError';
  return error;
}
