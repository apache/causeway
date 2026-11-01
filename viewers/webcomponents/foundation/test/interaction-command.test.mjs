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

import assert from 'node:assert/strict';
import test from 'node:test';
import {
  argumentsFromValues,
  collectionResultSelectionForType,
  normalizeInteractionInput
} from '../src/interaction-operations.mjs';
import {ObjectContextController} from '../src/object-context-controller.mjs';

const scalar = name => ({kind: 'SCALAR', name, ofType: null});
const named = name => ({kind: 'OBJECT', name, ofType: null});
const list = ofType => ({kind: 'LIST', name: null, ofType});
const argument = (name, type) => ({name, description: null, defaultValue: null, type});
const field = (name, type, args = []) => ({name, description: null, args, type});
const graphType = (name, fields) => ({name, kind: 'OBJECT', description: null, fields, inputFields: [], enumValues: []});

test('collection result selection merges identity with supported declared property wrappers', () => {
  const ownerType = 'rich__petclinic_PetOwner';
  const metadataType = `${ownerType}__gqlv_meta`;
  const nameWrapper = `${ownerType}__name__gqlv_property`;
  const types = new Map([
    [ownerType, graphType(ownerType, [
      field('_meta', named(metadataType)),
      field('name', named(nameWrapper))
    ])],
    [metadataType, graphType(metadataType, [
      field('id', scalar('String')),
      field('logicalTypeName', scalar('String')),
      field('title', scalar('String')),
      field('icon', scalar('String'))
    ])],
    [nameWrapper, graphType(nameWrapper, [
      field('hidden', scalar('Boolean')),
      field('disabled', scalar('String')),
      field('datatype', scalar('String')),
      field('get', scalar('String'))
    ])]
  ]);

  assert.deepEqual(collectionResultSelectionForType(list(named(ownerType)), [
    {member: 'name'},
    {member: 'missing'}
  ], types), {
    _meta: {id: true, logicalTypeName: true, title: true, icon: true},
    name: {hidden: true, disabled: true, datatype: true, get: true}
  });
});

function fixtureDescription() {
  const generatedTypeName = 'rich__example_Object';
  const generatedInputTypeName = `${generatedTypeName}__gqlv_input`;
  const safeInvokeType = `${generatedTypeName}__inspect__gqlv_action_invoke`;
  const property = {
    id: 'name', kind: 'property', description: 'Name', generatedTypeName: `${generatedTypeName}__name__gqlv_property`,
    fields: new Map([
      ['get', field('get', scalar('String'))],
      ['validate', field('validate', scalar('String'), [argument('name', scalar('String'))])]
    ]),
    value: {typeRef: scalar('String'), namedTypeName: 'String', typeKind: 'SCALAR', typeDescription: null}
  };
  const safeAction = {
    id: 'inspect', kind: 'action', description: 'Inspect', generatedTypeName: `${generatedTypeName}__inspect__gqlv_action`,
    fields: new Map([
      ['validate', field('validate', scalar('String'))],
      ['invoke', field('invoke', named(safeInvokeType))]
    ]), value: null
  };
  const mutatingAction = {
    id: 'rename', kind: 'action', description: 'Rename', generatedTypeName: `${generatedTypeName}__rename__gqlv_action`,
    fields: new Map([['validate', field('validate', scalar('String'), [argument('newName', scalar('String'))])]]), value: null
  };
  return {
    logicalTypeName: 'example.Object', generatedTypeName, generatedInputTypeName, generatedFieldName: 'example_Object',
    members: new Map([['name', property], ['inspect', safeAction], ['rename', mutatingAction]]), metadata: null,
    types: new Map([
      [generatedTypeName, graphType(generatedTypeName, [])],
      [safeInvokeType, graphType(safeInvokeType, [field('results', scalar('String'))])]
    ])
  };
}

function versionlessPreparationDescription() {
  const description = fixtureDescription();
  const choiceTypeName = 'rich__example_VersionlessChoice';
  const metadataTypeName = `${choiceTypeName}__gqlv_meta`;
  const paramsTypeName = `${description.generatedTypeName}__choose__gqlv_action_params`;
  const parameterTypeName = `${description.generatedTypeName}__choose__choice__gqlv_action_parameter`;
  const invokeTypeName = `${description.generatedTypeName}__choose__gqlv_action_invoke`;
  const choiceInputType = named(`${choiceTypeName}__gqlv_input`);
  const action = {
    id: 'choose', kind: 'action', description: 'Choose', generatedTypeName: `${description.generatedTypeName}__choose__gqlv_action`,
    fields: new Map([
      ['params', field('params', named(paramsTypeName))],
      ['validate', field('validate', scalar('String'), [argument('choice', choiceInputType)])],
      ['invoke', field('invoke', named(invokeTypeName), [argument('choice', choiceInputType)])]
    ]), value: null
  };
  return {
    ...description,
    members: new Map(description.members).set('choose', action),
    types: new Map([
      ...description.types,
      [paramsTypeName, graphType(paramsTypeName, [field('choice', named(parameterTypeName))])],
      [parameterTypeName, graphType(parameterTypeName, [
        field('hidden', scalar('Boolean')),
        field('default', named(choiceTypeName)),
        field('choices', list(named(choiceTypeName))),
        field('autoComplete', list(named(choiceTypeName)), [argument('search', scalar('String'))]),
        field('datatype', scalar('String'))
      ])],
      [choiceTypeName, graphType(choiceTypeName, [field('_meta', named(metadataTypeName))])],
      [metadataTypeName, graphType(metadataTypeName, [
        field('id', scalar('ID')),
        field('logicalTypeName', scalar('String')),
        field('title', scalar('String'))
      ])],
      [invokeTypeName, graphType(invokeTypeName, [field('results', scalar('String'))])]
    ])
  };
}

function fixtureMutation(description) {
  return graphType('Mutation', [
    field('example_Object__name', named(description.generatedTypeName), [
      argument('_target', named(description.generatedInputTypeName)), argument('name', scalar('String'))
    ]),
    field('example_Object__rename', named(description.generatedTypeName), [
      argument('_target', named(description.generatedInputTypeName)), argument('newName', scalar('String'))
    ])
  ]);
}

test('interaction arguments reduce object choices to public input identities recursively', () => {
  const parameterField = field('invoke', scalar('String'), [
    argument('pet', named('rich__petclinic_Pet__gqlv_input')),
    argument('structured', named('StructuredInput'))
  ]);
  const pet = {_meta: {id: 's_pet-turing', logicalTypeName: 'petclinic.Pet', title: 'Turing · dog'}};

  assert.deepEqual(normalizeInteractionInput(pet), {id: 's_pet-turing'});
  assert.deepEqual(argumentsFromValues(parameterField, {
    pet,
    structured: {label: 'Visit', nested: pet, __typename: 'StructuredValue'}
  }), {
    pet: {id: 's_pet-turing'},
    structured: {label: 'Visit', nested: {id: 's_pet-turing'}}
  });
});

test('object context executes property validation and update through semantic commands', async () => {
  const description = fixtureDescription();
  const calls = [];
  const client = {
    describeObject: async () => description,
    describeMutation: async () => fixtureMutation(description),
    executeObjectInteraction: async request => {
      calls.push(request);
      return {data: {name: {validate: null}}, errors: [], operation: {operationName: request.operationName}};
    },
    executeMutationInteraction: async request => {
      calls.push(request);
      return {data: {_meta: {id: '42'}}, errors: [], operation: {operationName: request.operationName}};
    }
  };
  const context = new ObjectContextController({client, logicalTypeName: 'example.Object', objectId: '42'});
  const prepared = await context.prepareProperty('name');
  assert.equal(prepared.status, 'success');
  assert.equal(prepared.data.capabilities.editable, true);
  assert.equal((await context.validateProperty('name', 'Updated')).status, 'success');
  assert.equal((await context.updateProperty('name', 'Updated')).status, 'success');
  assert.deepEqual(calls.at(-1).args, {_target: {id: '42'}, name: 'Updated'});
  context.disconnect();
});

test('object context prefers property autocomplete windows and normalizes metadata', async () => {
  const description = fixtureDescription();
  const choiceTypeName = 'rich__example_Choice';
  const metadataTypeName = `${choiceTypeName}__gqlv_meta`;
  const windowTypeName = `${description.generatedTypeName}__name__gqlv_property_autocomplete_window`;
  const originalProperty = description.members.get('name');
  const windowField = field('autoCompleteWindow', named(windowTypeName), [
    {...argument('search', scalar('String')), defaultValue: null},
    {...argument('offset', scalar('Int')), defaultValue: '0'},
    {...argument('size', scalar('Int')), defaultValue: '5'}
  ]);
  const windowedDescription = {
    ...description,
    members: new Map(description.members).set('name', {
      ...originalProperty,
      fields: new Map(originalProperty.fields)
        .set('autoComplete', field('autoComplete', list(named(choiceTypeName)), [argument('search', scalar('String'))]))
        .set('autoCompleteWindow', windowField)
    }),
    types: new Map([
      ...description.types,
      [windowTypeName, graphType(windowTypeName, [
        field('items', list(named(choiceTypeName))), field('offset', scalar('Int')),
        field('requestedSize', scalar('Int')), field('returnedCount', scalar('Int')),
        field('totalCount', scalar('Int')), field('maximumSize', scalar('Int')),
        field('hasPrevious', scalar('Boolean')), field('hasNext', scalar('Boolean')),
        field('ordering', scalar('String'))
      ])],
      [choiceTypeName, graphType(choiceTypeName, [field('_meta', named(metadataTypeName))])],
      [metadataTypeName, graphType(metadataTypeName, [
        field('id', scalar('ID')), field('logicalTypeName', scalar('String')), field('title', scalar('String'))
      ])]
    ])
  };
  const choice = {_meta: {id: 'choice-6', logicalTypeName: 'example.Choice', title: 'Choice 6'}};
  let request;
  const client = {
    describeObject: async () => windowedDescription,
    executeObjectInteraction: async candidate => {
      request = candidate;
      return {data: {name: {autoCompleteWindow: {
        items: [choice], offset: 5, requestedSize: 5, returnedCount: 1, totalCount: 6,
        maximumSize: 5, hasPrevious: true, hasNext: false, ordering: 'APPLICATION'
      }}}, errors: [], operation: {operationName: candidate.operationName}};
    }
  };
  const context = new ObjectContextController({client, logicalTypeName: 'example.Object', objectId: '42'});

  const result = await context.autoCompletePropertyWindow('name', 'Choice', {offset: 5, size: 5});

  assert.equal(result.status, 'success');
  assert.equal(result.data.windowed, true);
  assert.deepEqual(result.data.items, [choice]);
  assert.equal(result.data.totalCount, 6);
  assert.match(JSON.stringify(request.selection), /autoCompleteWindow/);
  assert.deepEqual(request.selection.name.autoCompleteWindow.__args, {search: 'Choice', offset: 5, size: 5});
  context.disconnect();
});

test('query-only and non-compliant variants report or execute discovered capabilities', async () => {
  const description = fixtureDescription();
  const queryOnlyClient = {
    describeObject: async () => description,
    describeMutation: async () => null
  };
  const queryOnly = new ObjectContextController({client: queryOnlyClient, logicalTypeName: 'example.Object', objectId: '42'});
  const unsupported = await queryOnly.prepareProperty('name');
  assert.equal(unsupported.status, 'unsupported');
  queryOnly.disconnect();

  const setField = field('set', named(description.generatedTypeName), [argument('name', scalar('String'))]);
  const originalProperty = description.members.get('name');
  const nonCompliantDescription = {
    ...description,
    members: new Map(description.members).set('name', {
      ...originalProperty,
      fields: new Map(originalProperty.fields).set('set', setField)
    })
  };
  let selection;
  const nonCompliantClient = {
    describeObject: async () => nonCompliantDescription,
    describeMutation: async () => null,
    executeObjectInteraction: async request => {
      selection = request.selection;
      return {data: {name: {set: {_meta: {id: '42'}}}}, errors: [], operation: {}};
    }
  };
  const nonCompliant = new ObjectContextController({client: nonCompliantClient, logicalTypeName: 'example.Object', objectId: '42'});
  assert.equal((await nonCompliant.updateProperty('name', 'Updated')).status, 'success');
  assert.equal(selection.name.set.__args.name, 'Updated');
  nonCompliant.disconnect();
});

test('versionless preparation and autocomplete select only advertised identity metadata', async () => {
  const description = versionlessPreparationDescription();
  const calls = [];
  const choice = {_meta: {id: 'choice-1', logicalTypeName: 'example.VersionlessChoice', title: 'Choice one'}};
  const client = {
    describeObject: async () => description,
    describeMutation: async () => null,
    executeObjectInteraction: async request => {
      calls.push(request);
      if (request.operationName === 'CausewayPrepareAction') {
        return {data: {choose: {params: {choice: {hidden: false, default: choice, choices: [choice], datatype: 'example.VersionlessChoice'}}}}, errors: [], operation: {}};
      }
      return {data: {choose: {params: {choice: {autoComplete: [choice]}}}}, errors: [], operation: {}};
    }
  };
  const context = new ObjectContextController({client, logicalTypeName: 'example.Object', objectId: '42'});

  const prepared = await context.prepareAction('choose');
  const completed = await context.autoCompleteActionParameter('choose', 'choice', 'Cho');

  assert.equal(prepared.status, 'success');
  assert.equal(prepared.data.parameters[0].state.default._meta.id, 'choice-1');
  assert.equal(completed.status, 'success');
  for (const call of calls) {
    const serializedSelection = JSON.stringify(call.selection);
    assert.match(serializedSelection, /logicalTypeName/);
    assert.doesNotMatch(serializedSelection, /version/);
  }
  assert.deepEqual(normalizeInteractionInput(choice), {id: 'choice-1'});
  context.disconnect();
});

test('action parameter autocomplete windows preserve declared dependent arguments', async () => {
  const description = versionlessPreparationDescription();
  const parameterTypeName = `${description.generatedTypeName}__choose__choice__gqlv_action_parameter`;
  const windowTypeName = `${parameterTypeName}_autocomplete_window`;
  const parameterType = description.types.get(parameterTypeName);
  const windowField = field('autoCompleteWindow', named(windowTypeName), [
    argument('preceding', scalar('String')), argument('search', scalar('String')),
    {...argument('offset', scalar('Int')), defaultValue: '0'},
    {...argument('size', scalar('Int')), defaultValue: '5'}
  ]);
  description.types.set(parameterTypeName, graphType(parameterTypeName, [
    ...parameterType.fields, windowField
  ]));
  const action = description.members.get('choose');
  const paramsTypeName = `${description.generatedTypeName}__choose__gqlv_action_params`;
  description.types.set(paramsTypeName, graphType(paramsTypeName, [field('choice', named(parameterTypeName))]));
  description.types.set(windowTypeName, graphType(windowTypeName, [
    field('items', list(named('rich__example_VersionlessChoice'))),
    field('offset', scalar('Int')), field('requestedSize', scalar('Int')),
    field('returnedCount', scalar('Int')), field('totalCount', scalar('Int')),
    field('maximumSize', scalar('Int')), field('hasPrevious', scalar('Boolean')),
    field('hasNext', scalar('Boolean')), field('ordering', scalar('String'))
  ]));
  description.members.set('choose', {...action});
  const choice = {_meta: {id: 'choice-6', logicalTypeName: 'example.VersionlessChoice', title: 'Choice six'}};
  let request;
  const client = {
    describeObject: async () => description,
    describeMutation: async () => null,
    executeObjectInteraction: async candidate => {
      request = candidate;
      return {data: {choose: {params: {choice: {autoCompleteWindow: {
        items: [choice], offset: 5, requestedSize: 5, returnedCount: 1, totalCount: 6,
        maximumSize: 5, hasPrevious: true, hasNext: false, ordering: 'APPLICATION'
      }}}}}, errors: [], operation: {}};
    }
  };
  const context = new ObjectContextController({client, logicalTypeName: 'example.Object', objectId: '42'});

  const result = await context.autoCompleteActionParameterWindow(
    'choose', 'choice', 'Cho', {preceding: 'current', ignored: 'omit'}, {offset: 5, size: 5});

  assert.equal(result.status, 'success');
  assert.equal(result.data.items[0]._meta.id, 'choice-6');
  assert.deepEqual(request.selection.choose.params.choice.autoCompleteWindow.__args, {
    preceding: 'current', search: 'Cho', offset: 5, size: 5
  });
  context.disconnect();
});

test('broad abstract mutation outcomes execute once and remain typename-only', async () => {
  const description = fixtureDescription();
  const unionTypeName = 'rich__example_BroadResultUnion';
  const unionRef = {kind: 'UNION', name: unionTypeName, ofType: null};
  description.types.set(unionTypeName, {
    kind: 'UNION', name: unionTypeName, description: null, fields: [],
    possibleTypes: Array.from({length: 9}, (_, index) => ({kind: 'OBJECT', name: `Result${index}`}))
  });
  const mutationType = graphType('Mutation', [
    field('example_Object__rename', unionRef, [
      argument('_target', named(description.generatedInputTypeName)), argument('newName', scalar('String'))
    ])
  ]);
  let executions = 0;
  const client = {
    describeObject: async () => description,
    describeMutation: async () => mutationType,
    executeMutationInteraction: async request => {
      executions += 1;
      assert.deepEqual(request.resultSelection, {__typename: true});
      return {data: {__typename: 'Result8'}, errors: [], operation: {}};
    }
  };
  const context = new ObjectContextController({client, logicalTypeName: 'example.Object', objectId: '42'});

  const result = await context.invokeAction('rename', {newName: 'Updated'});

  assert.equal(executions, 1);
  assert.equal(result.status, 'success');
  assert.deepEqual(result.data.value, {__typename: 'Result8'});
  context.disconnect();
});

test('transient validation aborts obsolete generations', async () => {
  const description = fixtureDescription();
  let call = 0;
  const client = {
    describeObject: async () => description,
    describeMutation: async () => fixtureMutation(description),
    executeObjectInteraction: request => new Promise((resolve, reject) => {
      const current = ++call;
      const timer = setTimeout(() => resolve({
        data: {name: {validate: current === 1 ? 'obsolete' : null}}, errors: [], operation: {}
      }), current === 1 ? 50 : 5);
      request.signal.addEventListener('abort', () => {
        clearTimeout(timer);
        const error = new Error('Aborted');
        error.name = 'AbortError';
        reject(error);
      }, {once: true});
    })
  };
  const context = new ObjectContextController({client, logicalTypeName: 'example.Object', objectId: '42'});
  const first = context.validateProperty('name', 'Old');
  const second = context.validateProperty('name', 'New');
  assert.equal((await first).status, 'obsolete');
  assert.equal((await second).status, 'success');
  context.disconnect();
});

test('mutations are serialized and action outcomes are normalized', async () => {
  const description = fixtureDescription();
  let concurrent = 0;
  let maximumConcurrent = 0;
  const order = [];
  const client = {
    describeObject: async () => description,
    describeMutation: async () => fixtureMutation(description),
    executeObjectInteraction: async request => ({
      data: {inspect: {invoke: {results: 'Inspection'}}}, errors: [], operation: {operationName: request.operationName}
    }),
    executeMutationInteraction: async request => {
      concurrent += 1;
      maximumConcurrent = Math.max(maximumConcurrent, concurrent);
      order.push(`start:${request.args.name ?? request.args.newName}`);
      await new Promise(resolve => setTimeout(resolve, 10));
      order.push(`end:${request.args.name ?? request.args.newName}`);
      concurrent -= 1;
      return {data: {_meta: {id: '42', title: request.args.name ?? request.args.newName}}, errors: [], operation: {}};
    }
  };
  const context = new ObjectContextController({client, logicalTypeName: 'example.Object', objectId: '42'});
  const first = context.updateProperty('name', 'First');
  const second = context.updateProperty('name', 'Second');
  assert.equal((await first).status, 'success');
  assert.equal((await second).status, 'success');
  assert.equal(maximumConcurrent, 1);
  assert.deepEqual(order, ['start:First', 'end:First', 'start:Second', 'end:Second']);

  const safeResult = await context.invokeAction('inspect');
  assert.deepEqual(safeResult.data, {kind: 'scalar', value: 'Inspection'});
  const objectResult = await context.invokeAction('rename', {newName: 'Renamed'});
  assert.equal(objectResult.data.kind, 'object');
  assert.equal(objectResult.data.value._meta.title, 'Renamed');
  context.disconnect();
});
