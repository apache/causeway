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
import {installDomShim} from './dom-shim.mjs';

const {document} = installDomShim();
const {
  CausewayActionElement,
  CausewayCollectionColumnElement,
  CausewayCollectionElement,
  CausewayObjectContextElement,
  CausewayPropertyElement,
  CausewaySemanticEvent,
  defineCausewayWebComponents
} = await import('../src/index.mjs');
const {associatedActions} = await import('../src/member-composition.mjs');

defineCausewayWebComponents();

test('property preserves direct actions across owner states and ignores descendant actions', () => {
  const context = recordingContext();
  const ownerContext = objectContext(context);
  const property = new CausewayPropertyElement();
  property.id = 'name';
  const direct = action('updateName');
  const wrapper = document.createElement('div');
  const descendant = action('ignoredPresentationAssociation');
  wrapper.appendChild(descendant);
  property.appendChild(direct);
  property.appendChild(wrapper);
  ownerContext.appendChild(property);
  document.body.appendChild(ownerContext);

  assert.equal(property.childNodes[0].getAttribute('data-causeway-member-primary'), '');
  assert.deepEqual(associatedActions(property), [direct]);
  assert.equal(direct.getAttribute('data-causeway-associated-action'), '');
  assert.equal(descendant.getAttribute('data-causeway-associated-action'), null);
  assert.equal(property.getAttribute('data-causeway-associated-member'), 'name');
  assert.equal(context.count('property:name'), 1);
  assert.equal(context.count('action:updateName'), 1);
  assert.equal(context.count('action:ignoredPresentationAssociation'), 1);

  context.publish('action:updateName', actionState({hidden: true}));
  assert.equal(direct.hidden, true);
  assert.equal(property.childNodes[0].hidden, false);
  context.publish('property:name', propertyState({hidden: true}));
  context.publish('action:updateName', actionState());
  assert.equal(property.hidden, false);
  assert.equal(property.childNodes[0].hidden, true);
  assert.equal(direct.hidden, false);
  assert.equal(direct.isConnected, true);
  assert.equal(context.count('action:updateName'), 1);
  context.publish('action:updateName', actionState({disabled: 'Action is locked'}));
  assert.equal(direct.activate(), false);

  context.publish('property:name', propertyState({disabled: 'Owner is locked'}));
  assert.equal(property.childNodes[0].hidden, false);
  assert.match(property.innerHTML, /Owner is locked/);
  assert.equal(property.childNodes[1], direct);
  assert.equal(context.count('action:updateName'), 1);

  document.body.removeChild(ownerContext);
  assert.equal(context.releaseCount('property:name'), 1);
  assert.equal(context.releaseCount('action:updateName'), 1);
});

test('property editing and cancellation preserve the associated action node', async () => {
  const context = recordingContext();
  const ownerContext = objectContext(context);
  const property = new CausewayPropertyElement();
  property.id = 'name';
  property.editable = true;
  const nestedAction = action('updateName');
  property.appendChild(nestedAction);
  ownerContext.appendChild(property);
  document.body.appendChild(ownerContext);

  assert.equal(await property.beginEdit(), true);
  assert.equal(property.childNodes[1], nestedAction);
  assert.equal(context.count('action:updateName'), 1);
  property.setPendingValue('Updated');
  assert.equal((await property.validatePending()).status, 'success');
  assert.equal(context.commandCount('validateProperty'), 1);
  property.cancelEdit();
  assert.equal(property.childNodes[1], nestedAction);
  assert.equal(context.count('action:updateName'), 1);
  assert.equal(context.commandCount('updateProperty'), 0);
  document.body.removeChild(ownerContext);
});

test('parser-late direct actions are recognized once in declaration order', () => {
  const context = recordingContext();
  const ownerContext = objectContext(context);
  const property = new CausewayPropertyElement();
  property.id = 'name';
  ownerContext.appendChild(property);
  document.body.appendChild(ownerContext);

  context.publish('property:name', propertyState({hidden: true}));
  assert.equal(property.hidden, true);
  const first = action('firstAction');
  const second = action('secondAction');
  property.appendChild(first);
  property.appendChild(second);

  assert.equal(property.hidden, false);
  assert.equal(property.childNodes[0].hidden, true);
  assert.deepEqual(associatedActions(property), [first, second]);
  assert.equal(context.count('action:firstAction'), 1);
  assert.equal(context.count('action:secondAction'), 1);
  context.publish('property:name', propertyState({value: 'Changed'}));
  assert.deepEqual(associatedActions(property), [first, second]);
  assert.equal(context.count('action:firstAction'), 1);
  assert.equal(context.count('action:secondAction'), 1);

  property.removeChild(first);
  assert.deepEqual(associatedActions(property), [second]);
  assert.equal(first.getAttribute('data-causeway-associated-action'), null);
  assert.equal(context.releaseCount('action:firstAction'), 1);
  document.body.removeChild(ownerContext);
});

test('nested action activation bypasses property owner controls and publishes one request', () => {
  const context = recordingContext();
  const ownerContext = objectContext(context);
  const property = new CausewayPropertyElement();
  property.id = 'name';
  const nestedAction = action('updateName');
  property.appendChild(nestedAction);
  ownerContext.appendChild(property);
  document.body.appendChild(ownerContext);
  let requests = 0;
  ownerContext.addEventListener(CausewaySemanticEvent.ACTION_REQUEST, event => {
    requests += 1;
    assert.equal(event.detail.actionId, 'updateName');
  });

  const button = document.createElement('button');
  nestedAction.appendChild(button);
  button.dispatchEvent(new Event('click', {bubbles: true, composed: true}));
  assert.equal(requests, 1);
  assert.equal(context.count('action:updateName'), 1);
  document.body.removeChild(ownerContext);
});

test('collection keeps interleaved columns and actions in separate vocabularies', async () => {
  const context = recordingContext();
  const ownerContext = objectContext(context);
  const collection = new CausewayCollectionElement();
  collection.id = 'pets';
  const nameColumn = column('name', 'Name');
  const addPet = action('addPet');
  const speciesColumn = column('species', 'Species');
  const removePet = action('removePet');
  collection.appendChild(nameColumn);
  collection.appendChild(addPet);
  collection.appendChild(speciesColumn);
  collection.appendChild(removePet);
  ownerContext.appendChild(collection);
  document.body.appendChild(ownerContext);
  await new Promise(resolve => setTimeout(resolve, 0));

  assert.deepEqual(collection.columns.map(candidate => candidate.member), ['name', 'species']);
  assert.deepEqual(associatedActions(collection), [addPet, removePet]);
  assert.equal(nameColumn.hidden, true);
  assert.equal(speciesColumn.hidden, true);
  assert.equal(context.count('collection:pets'), 1);
  assert.equal(context.count('action:addPet'), 1);
  assert.equal(context.count('action:removePet'), 1);

  context.publish('collection:pets', collectionState({hidden: true}));
  assert.equal(collection.hidden, false);
  assert.equal(collection.childNodes[0].hidden, true);
  assert.equal(addPet.isConnected, true);
  context.publish('collection:pets', collectionState({disabled: 'Collection is locked'}));
  assert.match(collection.innerHTML, /class="causeway-collection-label causeway-member-tooltip"/);
  assert.match(collection.innerHTML, /data-tooltip="Collection is locked"/);
  assert.match(collection.innerHTML, /causeway-visually-hidden">Collection is locked<\/span>/);
  assert.equal(addPet.isConnected, true);
  assert.equal(removePet.isConnected, true);
  assert.deepEqual(collection.columns.map(candidate => candidate.member), ['name', 'species']);
  assert.equal(context.count('action:addPet'), 1);
  assert.equal(context.count('action:removePet'), 1);

  document.body.removeChild(ownerContext);
  assert.equal(context.releaseCount('collection:pets'), 1);
  assert.equal(context.releaseCount('action:addPet'), 1);
  assert.equal(context.releaseCount('action:removePet'), 1);
});

test('reconnecting a composition registers each existing semantic child once per connection', async () => {
  const context = recordingContext();
  const ownerContext = objectContext(context);
  const collection = new CausewayCollectionElement();
  collection.id = 'pets';
  const nestedAction = action('addPet');
  collection.appendChild(nestedAction);
  ownerContext.appendChild(collection);
  document.body.appendChild(ownerContext);
  await new Promise(resolve => setTimeout(resolve, 0));
  document.body.removeChild(ownerContext);
  document.body.appendChild(ownerContext);
  await new Promise(resolve => setTimeout(resolve, 0));

  assert.equal(context.count('collection:pets'), 2);
  assert.equal(context.count('action:addPet'), 2);
  assert.equal(context.releaseCount('collection:pets'), 1);
  assert.equal(context.releaseCount('action:addPet'), 1);
  assert.deepEqual(associatedActions(collection), [nestedAction]);
  document.body.removeChild(ownerContext);
});

function objectContext(context) {
  const element = new CausewayObjectContextElement();
  element.context = context;
  return element;
}

function action(member) {
  const element = document.createElement('cw-action');
  assert.ok(element instanceof CausewayActionElement);
  element.id = member;
  return element;
}

function column(member, label) {
  const element = document.createElement('cw-collection-column');
  assert.ok(element instanceof CausewayCollectionColumnElement);
  element.id = member;
  element.label = label;
  return element;
}

function recordingContext() {
  const registrations = [];
  const releases = [];
  const listeners = new Map();
  const commands = [];
  return {
    identity: {logicalTypeName: 'example.Owner', id: 'owner-1'},
    subscribe(listener) {
      listener({status: 'ready'});
      return () => {};
    },
    async prepareProperty() {
      commands.push('prepareProperty');
      return {
        status: 'success',
        data: {capabilities: {validate: true, inputType: {kind: 'SCALAR', name: 'String'}, enumValues: []}, choices: []},
        errors: []
      };
    },
    async validateProperty() {
      commands.push('validateProperty');
      return {status: 'success', data: null, errors: []};
    },
    async updateProperty() {
      commands.push('updateProperty');
      return {status: 'success', data: {_meta: {id: 'owner-1'}}, errors: []};
    },
    registerRequirement(requirement, listener) {
      const key = `${requirement.kind}:${requirement.member}`;
      registrations.push(key);
      listeners.set(key, listener);
      if (requirement.kind === 'property') {
        listener(propertyState());
      } else if (requirement.kind === 'collection') {
        listener(collectionState());
      } else {
        listener(actionState());
      }
      return () => releases.push(key);
    },
    count(key) {
      return registrations.filter(candidate => candidate === key).length;
    },
    releaseCount(key) {
      return releases.filter(candidate => candidate === key).length;
    },
    commandCount(command) {
      return commands.filter(candidate => candidate === command).length;
    },
    publish(key, state) {
      listeners.get(key)?.(state);
    }
  };
}

function propertyState({hidden = false, disabled = null, value = 'Mary'} = {}) {
  return Object.freeze({
    status: 'ready',
    descriptor: {id: 'name', value: {typeRef: {kind: 'SCALAR', name: 'String'}}},
    data: {hidden, disabled, get: value},
    errors: [],
    generation: 1
  });
}

function collectionState({hidden = false, disabled = null} = {}) {
  return Object.freeze({
    status: 'ready',
    descriptor: {id: 'pets'},
    data: {hidden, disabled},
    errors: [],
    generation: 1
  });
}

function actionState({hidden = false, disabled = null} = {}) {
  return Object.freeze({
    status: 'ready',
    descriptor: {id: 'action'},
    data: {hidden, disabled},
    errors: [],
    generation: 1
  });
}
