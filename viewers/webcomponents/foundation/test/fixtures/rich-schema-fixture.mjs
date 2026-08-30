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
const MEMBER_METADATA_TYPE = 'RichMemberMetadata';
const BREADCRUMB_TYPE = 'RichNavigableBreadcrumb';
const NAME_TYPE = `${OBJECT_TYPE}__name__gqlv_property`;
const CODE_TYPE = `${OBJECT_TYPE}__code__gqlv_property`;
const STATUS_TYPE = `${OBJECT_TYPE}__status__gqlv_property`;
const NOTES_TYPE = `${OBJECT_TYPE}__notes__gqlv_property`;
const CHAIR_TYPE = `${OBJECT_TYPE}__chair__gqlv_property`;
const BLOB_TYPE = `${OBJECT_TYPE}__prospectus__gqlv_property`;
const BLOB_GET_TYPE = `${BLOB_TYPE}__gqlv_get`;
const CLOB_TYPE = `${OBJECT_TYPE}__history__gqlv_property`;
const CLOB_GET_TYPE = `${CLOB_TYPE}__gqlv_get`;
const UNSUPPORTED_TYPE = `${OBJECT_TYPE}__unsupportedValue__gqlv_property`;
const UNSUPPORTED_GET_TYPE = `${UNSUPPORTED_TYPE}__gqlv_get`;
const COLLECTION_TYPE = `${OBJECT_TYPE}__staffMembers__gqlv_collection`;
const COLLECTION_WINDOW_TYPE = `${COLLECTION_TYPE}_window`;
const COLLECTION_WINDOW_ORDERING_TYPE = 'rich__gqlv_collection_window_ordering';
const COLLECTION_WINDOW_SORT_DIRECTION_TYPE = 'rich__gqlv_collection_window_sort_direction';
const EMPTY_COLLECTION_TYPE = `${OBJECT_TYPE}__formerStaff__gqlv_collection`;
const ACTION_TYPE = `${OBJECT_TYPE}__changeName__gqlv_action`;
const ACTION_PARAMS_TYPE = `${OBJECT_TYPE}__changeName__gqlv_action_params`;
const ACTION_PARAM_TYPE = `${OBJECT_TYPE}__changeName__newName__gqlv_action_parameter`;
const AUTOCOMPLETE_WINDOW_ORDERING_TYPE = 'rich__gqlv_autocomplete_window_ordering';
const CHAIR_AUTOCOMPLETE_WINDOW_TYPE = `${CHAIR_TYPE}_autocomplete_window`;
const ACTION_PARAM_AUTOCOMPLETE_WINDOW_TYPE = `${ACTION_PARAM_TYPE}_autocomplete_window`;
const STAFF_OBJECT_TYPE = 'rich__university_staff_StaffMember';
const STAFF_META_TYPE = `${STAFF_OBJECT_TYPE}__gqlv_meta`;
const STAFF_NAME_TYPE = `${STAFF_OBJECT_TYPE}__name__gqlv_property`;
const STAFF_CODE_TYPE = `${STAFF_OBJECT_TYPE}__code__gqlv_property`;

export const DEPARTMENT_LOGICAL_TYPE = 'university.dept.Department';
export const DEPARTMENT_OBJECT_FIELD = 'university_dept_Department';
export const STAFF_LOGICAL_TYPE = 'university.staff.StaffMember';
export const STAFF_OBJECT_FIELD = 'university_staff_StaffMember';

export function createRichSchemaTypes() {
  return new Map([
    [OBJECT_TYPE, objectType(OBJECT_TYPE, 'A university department.', [
      field('_meta', 'Object metadata', named(META_TYPE)),
      field('name', 'Department name', named(NAME_TYPE)),
      field('code', 'Department code', named(CODE_TYPE)),
      field('status', 'Department status', named(STATUS_TYPE)),
      field('notes', 'Department notes', named(NOTES_TYPE)),
      field('chair', 'Department chair', named(CHAIR_TYPE)),
      field('prospectus', 'Department prospectus', named(BLOB_TYPE)),
      field('history', 'Department history', named(CLOB_TYPE)),
      field('unsupportedValue', 'Unsupported value', named(UNSUPPORTED_TYPE)),
      field('staffMembers', 'Staff members', named(COLLECTION_TYPE)),
      field('formerStaff', 'Former staff', named(EMPTY_COLLECTION_TYPE)),
      field('changeName', 'Change the department name', named(ACTION_TYPE))
    ])],
    [META_TYPE, objectType(META_TYPE, 'Object metadata', [
      field('id', null, scalar('ID')),
      field('logicalTypeName', null, scalar('String')),
      field('version', null, scalar('String')),
      field('title', null, scalar('String')),
      field('breadcrumbs', null, list(named(BREADCRUMB_TYPE))),
      field('grid', null, scalar('String')),
      field('layout', null, scalar('String')),
      field('cssClass', null, scalar('String'))
    ])],
    [BREADCRUMB_TYPE, objectType(BREADCRUMB_TYPE, 'Navigable breadcrumb', [
      field('logicalTypeName', null, scalar('String')),
      field('id', null, scalar('ID')),
      field('title', null, scalar('String'))
    ])],
    [MEMBER_METADATA_TYPE, objectType(MEMBER_METADATA_TYPE, 'Member metadata', [
      field('friendlyName', null, scalar('String')),
      field('description', null, scalar('String')),
      field('multiLine', null, scalar('Int')),
      field('labelPosition', null, scalar('String'))
    ])],
    [NAME_TYPE, propertyType(NAME_TYPE)],
    [CODE_TYPE, propertyType(CODE_TYPE)],
    [STATUS_TYPE, propertyType(STATUS_TYPE, enumeration('DepartmentStatus'))],
    [NOTES_TYPE, propertyType(NOTES_TYPE)],
    [CHAIR_TYPE, propertyType(CHAIR_TYPE, named('rich__university_staff_StaffMember'))],
    [BLOB_TYPE, propertyType(BLOB_TYPE, named(BLOB_GET_TYPE))],
    [BLOB_GET_TYPE, objectType(BLOB_GET_TYPE, null, [
      field('name', null, scalar('String')),
      field('mimeType', null, scalar('String')),
      field('bytes', null, scalar('String'))
    ])],
    [CLOB_TYPE, propertyType(CLOB_TYPE, named(CLOB_GET_TYPE))],
    [CLOB_GET_TYPE, objectType(CLOB_GET_TYPE, null, [
      field('name', null, scalar('String')),
      field('mimeType', null, scalar('String')),
      field('chars', null, scalar('String'))
    ])],
    [UNSUPPORTED_TYPE, propertyType(UNSUPPORTED_TYPE, named(UNSUPPORTED_GET_TYPE))],
    [UNSUPPORTED_GET_TYPE, objectType(UNSUPPORTED_GET_TYPE, null, [
      field('nested', null, named('ArbitraryNestedValue'))
    ])],
    [COLLECTION_TYPE, objectType(COLLECTION_TYPE, null, [
      field('hidden', null, scalar('Boolean')),
      field('disabled', null, scalar('String')),
      field('metadata', null, named(MEMBER_METADATA_TYPE)),
      field('get', null, list(named(STAFF_OBJECT_TYPE))),
      field('datatype', null, scalar('String'))
    ])],
    [EMPTY_COLLECTION_TYPE, objectType(EMPTY_COLLECTION_TYPE, null, [
      field('hidden', null, scalar('Boolean')),
      field('disabled', null, scalar('String')),
      field('metadata', null, named(MEMBER_METADATA_TYPE)),
      field('get', null, list(named(STAFF_OBJECT_TYPE))),
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
    ])],
    [STAFF_OBJECT_TYPE, objectType(STAFF_OBJECT_TYPE, 'A staff member.', [
      field('_meta', 'Object metadata', named(STAFF_META_TYPE)),
      field('name', 'Staff name', named(STAFF_NAME_TYPE)),
      field('code', 'Staff code', named(STAFF_CODE_TYPE))
    ])],
    [STAFF_META_TYPE, objectType(STAFF_META_TYPE, 'Object metadata', [
      field('id', null, scalar('ID')),
      field('logicalTypeName', null, scalar('String')),
      field('version', null, scalar('String')),
      field('title', null, scalar('String')),
      field('breadcrumbs', null, list(named(BREADCRUMB_TYPE)))
    ])],
    [STAFF_NAME_TYPE, propertyType(STAFF_NAME_TYPE)],
    [STAFF_CODE_TYPE, propertyType(STAFF_CODE_TYPE)]
  ]);
}

export function createAutoCompleteWindowRichSchemaTypes() {
  const types = createRichSchemaTypes();
  const windowFields = () => [
    field('items', null, list(named(STAFF_OBJECT_TYPE))),
    field('offset', null, nonNull(scalar('Int'))),
    field('requestedSize', null, nonNull(scalar('Int'))),
    field('returnedCount', null, nonNull(scalar('Int'))),
    field('totalCount', null, nonNull(scalar('Int'))),
    field('maximumSize', null, nonNull(scalar('Int'))),
    field('hasPrevious', null, nonNull(scalar('Boolean'))),
    field('hasNext', null, nonNull(scalar('Boolean'))),
    field('ordering', null, nonNull(enumeration(AUTOCOMPLETE_WINDOW_ORDERING_TYPE)))
  ];
  const windowArguments = [
    argument('search', nonNull(scalar('String'))),
    argument('offset', nonNull(scalar('Int')), {defaultValue: '0'}),
    argument('size', nonNull(scalar('Int')), {defaultValue: '5'})
  ];
  const chairType = types.get(CHAIR_TYPE);
  types.set(CHAIR_TYPE, objectType(CHAIR_TYPE, null, [
    ...chairType.fields,
    field('autoComplete', null, list(named(STAFF_OBJECT_TYPE)), [argument('search', nonNull(scalar('String')))]),
    field('autoCompleteWindow', null, named(CHAIR_AUTOCOMPLETE_WINDOW_TYPE), windowArguments)
  ]));
  const parameterType = types.get(ACTION_PARAM_TYPE);
  types.set(ACTION_PARAM_TYPE, objectType(ACTION_PARAM_TYPE, null, [
    ...parameterType.fields,
    field('autoComplete', null, list(named(STAFF_OBJECT_TYPE)), [argument('search', nonNull(scalar('String')))]),
    field('autoCompleteWindow', null, named(ACTION_PARAM_AUTOCOMPLETE_WINDOW_TYPE), [
      argument('preceding', scalar('String')),
      ...windowArguments
    ])
  ]));
  types.set(CHAIR_AUTOCOMPLETE_WINDOW_TYPE, objectType(
    CHAIR_AUTOCOMPLETE_WINDOW_TYPE, null, windowFields()));
  types.set(ACTION_PARAM_AUTOCOMPLETE_WINDOW_TYPE, objectType(
    ACTION_PARAM_AUTOCOMPLETE_WINDOW_TYPE, null, windowFields()));
  types.set(AUTOCOMPLETE_WINDOW_ORDERING_TYPE, {
    kind: 'ENUM', name: AUTOCOMPLETE_WINDOW_ORDERING_TYPE, description: null,
    fields: [], enumValues: [{name: 'APPLICATION', description: null}], possibleTypes: []
  });
  return types;
}

export function createCriteriaWindowedRichSchemaTypes() {
  const types = createWindowedRichSchemaTypes();
  const collectionType = types.get(COLLECTION_TYPE);
  types.set(COLLECTION_TYPE, objectType(COLLECTION_TYPE, null, collectionType.fields.map(candidate =>
    candidate.name === 'window'
      ? field(candidate.name, candidate.description, candidate.type, [
          ...candidate.args,
          argument('sortBy', scalar('String')),
          argument('sortDirection', nonNull(enumeration(COLLECTION_WINDOW_SORT_DIRECTION_TYPE)), {defaultValue: 'ASCENDING'}),
          argument('search', scalar('String'))
        ])
      : candidate)));
  const windowType = types.get(COLLECTION_WINDOW_TYPE);
  types.set(COLLECTION_WINDOW_TYPE, objectType(COLLECTION_WINDOW_TYPE, windowType.description, [
    ...windowType.fields,
    field('sortableMembers', null, nonNull(list(nonNull(scalar('String'))))),
    field('searchSupported', null, nonNull(scalar('Boolean'))),
    field('searchPrompt', null, scalar('String'))
  ]));
  types.set(COLLECTION_WINDOW_ORDERING_TYPE, {
    ...types.get(COLLECTION_WINDOW_ORDERING_TYPE),
    enumValues: [
      {name: 'CONFIGURED', description: 'A Causeway configured comparator was applied.'},
      {name: 'REQUESTED', description: 'A requested accepted sort was applied.'},
      {name: 'ENCOUNTER', description: 'Encounter order was retained.'}
    ]
  });
  types.set(COLLECTION_WINDOW_SORT_DIRECTION_TYPE, {
    kind: 'ENUM',
    name: COLLECTION_WINDOW_SORT_DIRECTION_TYPE,
    description: 'Direction for a requested collection-window sort.',
    fields: [],
    inputFields: [],
    enumValues: [
      {name: 'ASCENDING', description: null},
      {name: 'DESCENDING', description: null}
    ],
    possibleTypes: []
  });
  return types;
}

export function createVersionlessRichSchemaTypes({windowed = false} = {}) {
  const types = windowed ? createWindowedRichSchemaTypes() : createRichSchemaTypes();
  const metadataType = types.get(STAFF_META_TYPE);
  types.set(STAFF_META_TYPE, objectType(STAFF_META_TYPE, metadataType.description,
    metadataType.fields.filter(candidate => candidate.name !== 'version')));
  return types;
}

export function createWindowedRichSchemaTypes() {
  const types = createRichSchemaTypes();
  const collectionType = types.get(COLLECTION_TYPE);
  types.set(COLLECTION_TYPE, objectType(COLLECTION_TYPE, null, [
    ...collectionType.fields,
    field(
      'window',
      'Returns a bounded zero-based collection window; the configured maximum size is 100.',
      named(COLLECTION_WINDOW_TYPE),
      [
        argument('offset', nonNull(scalar('Int')), {description: 'Zero-based row offset.', defaultValue: '0'}),
        argument('size', nonNull(scalar('Int')), {description: 'Positive row count.', defaultValue: '20'})
      ]
    )
  ]));
  types.set(COLLECTION_WINDOW_TYPE, objectType(
    COLLECTION_WINDOW_TYPE,
    'A bounded execution-time view of a Causeway collection association.',
    [
      field('rows', null, list(named(STAFF_OBJECT_TYPE))),
      field('offset', null, nonNull(scalar('Int'))),
      field('requestedSize', null, nonNull(scalar('Int'))),
      field('returnedCount', null, nonNull(scalar('Int'))),
      field('totalCount', null, scalar('Int')),
      field('maximumSize', null, nonNull(scalar('Int'))),
      field('hasPrevious', null, nonNull(scalar('Boolean'))),
      field('hasNext', null, nonNull(scalar('Boolean'))),
      field('ordering', null, nonNull(enumeration(COLLECTION_WINDOW_ORDERING_TYPE)))
    ]
  ));
  types.set(COLLECTION_WINDOW_ORDERING_TYPE, {
    kind: 'ENUM',
    name: COLLECTION_WINDOW_ORDERING_TYPE,
    description: 'How rows were ordered before selecting a collection window.',
    fields: [],
    inputFields: [],
    enumValues: [
      {name: 'CONFIGURED', description: 'A Causeway configured comparator was applied.'},
      {name: 'ENCOUNTER', description: 'Encounter order was retained.'}
    ]
  });
  return types;
}

export function departmentObjectData({
  name = 'Classics',
  code = 'CLA',
  codeHidden = false,
  nameDisabled = null,
  version = '7',
  breadcrumbs = []
} = {}) {
  return {
    _meta: {
      id: '42',
      logicalTypeName: DEPARTMENT_LOGICAL_TYPE,
      version,
      title: 'Classics Department',
      breadcrumbs,
      grid: '/graphql/object/university.dept.Department:42/_meta/grid',
      layout: null,
      cssClass: 'department'
    },
    name: {
      hidden: false,
      disabled: nameDisabled,
      metadata: {friendlyName: 'Department name', description: 'The department display name.', multiLine: null, labelPosition: 'LEFT'},
      get: name,
      datatype: 'String'
    },
    code: {
      hidden: codeHidden,
      disabled: null,
      metadata: {friendlyName: 'Department code', description: null, multiLine: null, labelPosition: 'TOP'},
      get: code,
      datatype: 'String'
    },
    status: {hidden: false, disabled: null, get: 'ACTIVE', datatype: 'DepartmentStatus'},
    notes: {hidden: false, disabled: null, get: null, datatype: 'String'},
    chair: {
      hidden: false,
      disabled: null,
      get: {_meta: {id: 'staff-1', logicalTypeName: 'university.staff.StaffMember', version: '3', title: 'Dr Ada'}}
    },
    prospectus: {
      hidden: false,
      disabled: null,
      get: {name: 'prospectus.pdf', mimeType: 'application/pdf', bytes: '/graphql/object/prospectus/blobBytes'}
    },
    history: {
      hidden: false,
      disabled: null,
      get: {name: 'history.txt', mimeType: 'text/plain', chars: '/graphql/object/history/clobChars'}
    },
    unsupportedValue: {hidden: false, disabled: null, get: {nested: {value: 'unknown'}}},
    staffMembers: {
      hidden: false,
      disabled: null,
      metadata: {friendlyName: 'Department staff', description: 'Current staff members.'}
    },
    formerStaff: {
      hidden: false,
      disabled: null,
      metadata: {friendlyName: 'Former staff', description: null}
    },
    changeName: {hidden: false, disabled: null}
  };
}

export function collectionWindowResponse({
  rows = [],
  offset = 0,
  requestedSize = 20,
  totalCount = rows.length,
  ordering = 'ENCOUNTER',
  sortableMembers,
  searchSupported,
  searchPrompt
} = {}) {
  return {
    data: {
      rich: {
        [DEPARTMENT_OBJECT_FIELD]: {
          staffMembers: {
            window: {
              rows,
              offset,
              requestedSize,
              returnedCount: rows.length,
              totalCount,
              maximumSize: 100,
              hasPrevious: offset > 0 && totalCount > 0,
              hasNext: offset + rows.length < totalCount,
              ordering,
              ...(sortableMembers === undefined ? {} : {sortableMembers}),
              ...(searchSupported === undefined ? {} : {searchSupported}),
              ...(searchPrompt === undefined ? {} : {searchPrompt})
            }
          }
        }
      }
    }
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

export function createRichSchemaFixtureExecutor({
  types = createRichSchemaTypes(),
  readResponses = [graphQLObjectResponse()],
  windowResponses = [],
  operationRoots = {queryTypeName: 'SimpleAndRich', mutationTypeName: null},
  interactionHandler = null
} = {}) {
  const calls = [];
  const introspectionCalls = [];
  const readCalls = [];
  const windowCalls = [];
  let readIndex = 0;
  let windowIndex = 0;
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
    if (request.operationName === 'CausewayDescribeOperationRoots') {
      return {
        data: {
          __schema: {
            queryType: operationRoots.queryTypeName ? {name: operationRoots.queryTypeName} : null,
            mutationType: operationRoots.mutationTypeName ? {name: operationRoots.mutationTypeName} : null
          }
        }
      };
    }
    if (request.operationName === 'CausewayReadObject') {
      readCalls.push(request);
      const response = readResponses[Math.min(readIndex, readResponses.length - 1)];
      readIndex += 1;
      return typeof response === 'function' ? response(request) : response;
    }
    if (request.operationName === 'CausewayReadCollectionWindow') {
      windowCalls.push(request);
      const response = windowResponses[Math.min(windowIndex, windowResponses.length - 1)];
      windowIndex += 1;
      if (response) {
        return typeof response === 'function' ? response(request) : response;
      }
    }
    if (interactionHandler) {
      return interactionHandler(request);
    }
    throw new Error(`Unexpected fixture operation '${request.operationName}'.`);
  };
  Object.assign(executor, {calls, introspectionCalls, readCalls, windowCalls, types});
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

function propertyType(name, getType = scalar('String')) {
  return objectType(name, null, [
    field('metadata', null, named(MEMBER_METADATA_TYPE)),
    field('hidden', null, scalar('Boolean')),
    field('disabled', null, scalar('String')),
    field('get', null, getType),
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

function argument(name, type, {description = null, defaultValue = null} = {}) {
  return {name, description, defaultValue, type};
}

function scalar(name) {
  return {kind: 'SCALAR', name, ofType: null};
}

function named(name) {
  return {kind: 'OBJECT', name, ofType: null};
}

function enumeration(name) {
  return {kind: 'ENUM', name, ofType: null};
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
