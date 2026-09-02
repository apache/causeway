/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';
import {installDomShim} from './dom-shim.mjs';
import {waitFor} from './fixtures/rich-schema-fixture.mjs';

const {document} = installDomShim();
const {
  captureDeclarativeCollectionPeeks,
  collectionPeekDeclaration,
  CausewayCollectionElement,
  CausewayPeekElement,
  defineCausewayWebComponents,
  normalizePeekPresentation,
  OBJECT_CONTEXT_REQUEST_EVENT
} = await import('../src/index.mjs');
defineCausewayWebComponents();

test('captures one inert inline peek declaration without connecting its descendants', () => {
  const collection = new CausewayCollectionElement();
  const peek = document.createElement('cw-peek');
  peek.innerHTML = '<cw-property id="name"></cw-property>';
  collection.appendChild(peek);

  captureDeclarativeCollectionPeeks({querySelectorAll: () => [collection]});

  assert.deepEqual(collectionPeekDeclaration(collection), {
    count: 1,
    presentation: {html: '<cw-property id="name"></cw-property>', inline: true}
  });
  assert.equal(peek.innerHTML, '');
  assert.equal(peek.hidden, true);
});

test('empty, duplicate, and unsupported presentation values fail closed', () => {
  const emptyCollection = new CausewayCollectionElement();
  emptyCollection.appendChild(document.createElement('cw-peek'));
  captureDeclarativeCollectionPeeks({querySelectorAll: () => [emptyCollection]});
  assert.deepEqual(collectionPeekDeclaration(emptyCollection), {
    count: 1,
    presentation: {html: '', inline: false}
  });

  const duplicateCollection = new CausewayCollectionElement();
  duplicateCollection.appendChild(document.createElement('cw-peek'));
  duplicateCollection.appendChild(document.createElement('cw-peek'));
  assert.deepEqual(collectionPeekDeclaration(duplicateCollection), {count: 2, presentation: null});
  assert.equal(normalizePeekPresentation({html: '  <!-- empty --> '}), null);
  assert.deepEqual(normalizePeekPresentation({html: '<cw-property id="name"></cw-property>'}), {
    html: '<cw-property id="name"></cw-property>'
  });
  assert.equal(normalizePeekPresentation({html: `<p>${'x'.repeat(65536)}</p>`}), null);
});

test('collection resolves an inline preview lazily for an eligible row', async () => {
  let hydrationCount = 0;
  const row = {_meta: {logicalTypeName: 'example.Staff', id: '1', title: 'Ada'}};
  const context = {
    registerRequirement(_requirement, listener) {
      listener({status: 'ready', descriptor: {id: 'staff'}, data: {hidden: false}, errors: [], generation: 1});
      return () => {};
    },
    async loadCollection() {
      return {data: {get: [row]}, rows: [row], errors: [], rowSelection: {_meta: {id: true}}};
    },
    createHydratedRowContext() {
      hydrationCount += 1;
      return {disconnect() {}};
    }
  };
  const collection = document.createElement('cw-collection');
  collection.id = 'staff';
  collection.active = true;
  collection.context = context;
  const declaration = document.createElement('cw-peek');
  declaration.innerHTML = '<p>Inline preview</p>';
  collection.appendChild(declaration);
  document.body.appendChild(collection);

  await waitFor(() => collection.collectionState.status === 'ready');
  assert.match(collection.innerHTML, /data-causeway-peek-toggle="example\.Staff:1"/);
  assert.match(collection.innerHTML, /aria-expanded="false"/);
  assert.equal(hydrationCount, 1);
});

test('empty collection peek resolves a type default and missing defaults disclose nothing', async () => {
  const requested = [];
  globalThis.causewayCollectionRowPreviewResolver = async ({logicalTypeName}) => {
    requested.push(logicalTypeName);
    return logicalTypeName === 'example.Staff' ? {html: '<p>Default preview</p>'} : null;
  };
  const rows = [
    {_meta: {logicalTypeName: 'example.Staff', id: '1', title: 'Ada'}},
    {_meta: {logicalTypeName: 'example.Missing', id: '2', title: 'Grace'}}
  ];
  const context = {
    registerRequirement(_requirement, listener) {
      listener({status: 'ready', descriptor: {id: 'staff'}, data: {hidden: false}, errors: [], generation: 1});
      return () => {};
    },
    async loadCollection() {
      return {data: {get: rows}, rows, errors: [], rowSelection: {_meta: {id: true}}};
    },
    createHydratedRowContext() { return {disconnect() {}}; }
  };
  const collection = document.createElement('cw-collection');
  collection.id = 'staff';
  collection.active = true;
  collection.context = context;
  collection.appendChild(document.createElement('cw-peek'));
  document.body.appendChild(collection);

  await waitFor(() => collection.collectionState.status === 'ready');
  assert.deepEqual(requested.sort(), ['example.Missing', 'example.Staff']);
  assert.equal((collection.innerHTML.match(/data-causeway-peek-toggle=/g) ?? []).length, 1);
  delete globalThis.causewayCollectionRowPreviewResolver;
});

test('live peek provides only its dedicated context and Escape requests collapse', () => {
  const context = {identity: {logicalTypeName: 'example.Type', id: '1'}, disconnect() {}};
  let collapsed = 0;
  let provided = null;
  const peek = new CausewayPeekElement();
  peek.configureLive({context, label: 'Preview Example', collapse: () => collapsed += 1});
  const child = document.createElement('span');
  peek.appendChild(child);
  document.body.appendChild(peek);

  child.dispatchEvent(new CustomEvent(OBJECT_CONTEXT_REQUEST_EVENT, {
    bubbles: true,
    detail: {provide: candidate => provided = candidate}
  }));
  assert.equal(provided, context);
  assert.equal(peek.getAttribute('role'), 'region');
  assert.equal(peek.getAttribute('aria-label'), 'Preview Example');

  const escape = new Event('keydown', {bubbles: true, cancelable: true});
  escape.key = 'Escape';
  child.dispatchEvent(escape);
  assert.equal(collapsed, 1);
  assert.equal(escape.defaultPrevented, true);
});
