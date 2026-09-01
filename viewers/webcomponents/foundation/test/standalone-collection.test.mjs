/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';
import {installDomShim} from './dom-shim.mjs';

const {document} = installDomShim();
const {
  COLLECTION_STATE_EVENT,
  captureDeclarativeStandaloneCollectionColumns,
  CausewayCollectionColumnElement,
  CausewayStandaloneCollectionElement,
  configureCausewayGridWidgets,
  defineCausewayWebComponents
} = await import('../src/index.mjs');

defineCausewayWebComponents();

function object(id, title = id, extra = {}) {
  return {
    _meta: {
      logicalTypeName: 'example.Result',
      id,
      title,
      icon: `/graphql/object/example.Result:${id}/_meta/icon`
    },
    ...extra
  };
}

test('standalone collection captures declarations before browser upgrade replaces source children', () => {
  const collection = new CausewayStandaloneCollectionElement();
  const column = new CausewayCollectionColumnElement();
  column.id = 'name';
  column.label = 'Name';
  column.setAttribute('data-testid', 'column-name');
  collection.appendChild(column);
  captureDeclarativeStandaloneCollectionColumns({querySelectorAll: () => [collection]});
  collection.removeChild(column);
  document.body.appendChild(collection);

  assert.deepEqual(collection.columns, [{member: 'name', label: 'Name', testId: 'column-name'}]);
  document.body.removeChild(collection);
});

test('standalone collection snapshots normalized results and publishes bounded lifecycle state', () => {
  const collection = new CausewayStandaloneCollectionElement();
  collection.named = 'Matching owners';
  collection.describedAs = 'Owners returned by the action';
  collection.descriptionAs = 'tooltip';
  document.body.appendChild(collection);
  const states = [];
  collection.addEventListener(COLLECTION_STATE_EVENT, event => states.push(event.detail));

  const source = [object('1', 'Ada <Lovelace>')];
  collection.result = {kind: 'collection', value: source};
  source.push(object('2'));

  assert.equal(collection.result.value.length, 1);
  assert.equal(collection.resultState.status, 'ready');
  assert.equal(collection.resultState.totalCount, 1);
  assert.equal(collection.hasAttribute('result'), false);
  assert.match(collection.innerHTML, /Matching owners/);
  assert.match(collection.innerHTML, /Owners returned by the action/);
  assert.match(collection.innerHTML, /class="causeway-collection-description causeway-visually-hidden"/);
  assert.match(collection.innerHTML, /logical-type="example.Result"/);
  assert.match(collection.innerHTML, /title="Ada &lt;Lovelace&gt;"/);
  assert.match(collection.innerHTML, /icon="\/graphql\/object\/example.Result:1\/_meta\/icon"/);
  assert.equal(states.length, 1);
  assert.equal(states[0].standalone, true);
  assert.equal(states[0].member, null);
  assert.equal(states[0].state.status, 'ready');

  collection.result = {kind: 'collection', value: []};
  assert.equal(collection.resultState.status, 'empty');
  assert.match(collection.innerHTML, /No items/);
  assert.doesNotMatch(collection.innerHTML, /causeway-collection-pager/);

  collection.result = null;
  assert.equal(collection.resultState.status, 'idle');
  assert.match(collection.innerHTML, /No result/);

  collection.result = {kind: 'scalar', value: 'wrong'};
  assert.equal(collection.result, null);
  assert.equal(collection.resultState.status, 'unsupported');
  assert.match(collection.innerHTML, /normalized collection action result is required/);
  assert.equal(states.at(-1).state.status, 'unsupported');
});

test('standalone collection preserves direct child columns and renders only supplied values', () => {
  const collection = new CausewayStandaloneCollectionElement();
  const name = document.createElement('cw-collection-column');
  name.id = 'name';
  name.label = 'Owner name';
  name.setAttribute('data-testid', 'owner-name-column');
  const hidden = document.createElement('cw-collection-column');
  hidden.id = 'secret';
  const disabled = document.createElement('cw-collection-column');
  disabled.id = 'code';
  const missing = document.createElement('cw-collection-column');
  missing.id = 'missing';
  collection.appendChild(name);
  collection.appendChild(hidden);
  collection.appendChild(disabled);
  collection.appendChild(missing);
  document.body.appendChild(collection);

  collection.result = {kind: 'collection', value: [object('1', 'Ada', {
    name: {get: 'Ada Lovelace'},
    secret: {hidden: true, get: 'classified'},
    code: {get: null, disabled: 'Read only'}
  })]};

  assert.equal(collection.columns.length, 4);
  assert.equal(collection.children.includes(name), true);
  assert.equal(name.parentNode, collection);
  assert.match(collection.innerHTML, /Owner name/);
  assert.match(collection.innerHTML, /data-testid="owner-name-column"/);
  assert.match(collection.innerHTML, /Ada Lovelace/);
  assert.match(collection.innerHTML, /data-causeway-grid-member="secret"[^>]* hidden/);
  assert.match(collection.innerHTML, /aria-disabled="true" title="Read only"/);
  assert.match(collection.innerHTML, /data-causeway-grid-member="missing"[\s\S]*?causeway-unavailable">Unavailable/);

  collection.removeChild(missing);
  assert.equal(collection.columns.some(column => column.member === 'missing'), false);
  assert.equal(collection.children.includes(name), true);
});

test('standalone collection contains renderer failures as local error state', () => {
  const collection = new CausewayStandaloneCollectionElement();
  collection.rendererRegistry = {render() { throw new Error('Renderer failed safely.'); }};
  document.body.appendChild(collection);
  collection.result = {kind: 'collection', value: ['value']};

  assert.equal(collection.resultState.status, 'error');
  assert.match(collection.innerHTML, /causeway-error/);
  assert.match(collection.innerHTML, /Renderer failed safely/);
});

test('standalone collection renders supported scalar rows without inventing identity', () => {
  const collection = new CausewayStandaloneCollectionElement();
  document.body.appendChild(collection);
  collection.result = {kind: 'collection', value: ['alpha', 42, true, null]};

  assert.match(collection.innerHTML, /alpha/);
  assert.match(collection.innerHTML, /42/);
  assert.match(collection.innerHTML, /true/i);
  assert.match(collection.innerHTML, /causeway-value-null/);
  assert.doesNotMatch(collection.innerHTML, /cw-object-link/);
  assert.equal(collection.gridQualification.qualified, false);
});

test('standalone collection qualifies finite object snapshots and rejects superseded ranges', async () => {
  configureCausewayGridWidgets({enabled: true});
  const collection = new CausewayStandaloneCollectionElement();
  collection.resizableColumns = true;
  collection.reorderableColumns = true;
  document.body.appendChild(collection);
  collection.result = {kind: 'collection', value: [object('1'), object('2')]};
  collection.acceptGridResponsiveState(true);

  assert.equal(collection.gridQualification.qualified, true);
  assert.equal(collection.gridQualification.presentation, 'grid-virtual');
  assert.equal(collection.resizableColumns, true);
  assert.equal(collection.reorderableColumns, true);
  assert.equal(collection.gridProjection.rows.length, 2);
  assert.equal(collection.dataset.causewayGridCount, 'available');
  const current = await collection.requestResultRange({offset: 1, size: 1});
  assert.equal(current.rows.length, 1);
  assert.equal(current.rows[0].key, 'example.Result:2');

  const obsolete = collection.requestResultRange({offset: 0, size: 1});
  collection.result = {kind: 'collection', value: [object('3')]};
  await assert.rejects(obsolete, error => error.name === 'AbortError');
  assert.equal(collection.gridProjection.rows[0].key, 'example.Result:3');

  document.body.removeChild(collection);
  await assert.rejects(
    collection.requestResultRange({offset: 0, size: 1}),
    error => error.name === 'AbortError'
  );
  configureCausewayGridWidgets({enabled: true});
});

test('standalone collection keeps member-only attributes inert and supports native Grid policy', () => {
  configureCausewayGridWidgets({enabled: false});
  const collection = new CausewayStandaloneCollectionElement();
  collection.setAttribute('id', 'not-a-member');
  collection.setAttribute('active', '');
  collection.setAttribute('paged', '5');
  collection.setAttribute('sortable', '');
  collection.setAttribute('filterable', '');
  document.body.appendChild(collection);
  collection.result = {kind: 'collection', value: [object('1')]};
  collection.acceptGridResponsiveState(true);

  assert.equal(collection.gridQualification.qualified, false);
  assert.equal(collection.gridQualification.reason, 'policy-native');
  assert.match(collection.innerHTML, /causeway-collection-rows/);
  assert.doesNotMatch(collection.innerHTML, /data-causeway-activate|causeway-collection-pager|causeway-collection-search|causeway-collection-sort/);
  configureCausewayGridWidgets({enabled: true});
});
