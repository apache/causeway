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
  captureDeclarativeCollectionPreviews,
  collectionPreviewDeclaration,
  CausewayCollectionElement,
  CausewayPreviewElement,
  defineCausewayWebComponents,
  normalizePreviewPresentation,
  OBJECT_CONTEXT_REQUEST_EVENT
} = await import('../src/index.mjs');
const {
  createPreviewToggleIcon,
  PREVIEW_TOGGLE_ICON_CLASS,
  PREVIEW_TOGGLE_ICON_MARKUP,
  PREVIEW_TOGGLE_ICON_PATH,
  PREVIEW_TOGGLE_ICON_VIEW_BOX
} = await import('../src/preview-toggle-icon.mjs');
defineCausewayWebComponents();

test('shared preview toggle icon has equivalent markup and DOM geometry', () => {
  assert.match(PREVIEW_TOGGLE_ICON_MARKUP, new RegExp(`class="${PREVIEW_TOGGLE_ICON_CLASS}"`));
  assert.match(PREVIEW_TOGGLE_ICON_MARKUP, new RegExp(`viewBox="${PREVIEW_TOGGLE_ICON_VIEW_BOX}"`));
  assert.match(PREVIEW_TOGGLE_ICON_MARKUP, new RegExp(`d="${PREVIEW_TOGGLE_ICON_PATH}"`));

  const icon = createPreviewToggleIcon(document);
  assert.equal(icon.getAttribute('class'), PREVIEW_TOGGLE_ICON_CLASS);
  assert.equal(icon.getAttribute('aria-hidden'), 'true');
  assert.equal(icon.getAttribute('focusable'), 'false');
  assert.equal(icon.getAttribute('viewBox'), PREVIEW_TOGGLE_ICON_VIEW_BOX);
  assert.equal(icon.childNodes[0].getAttribute('d'), PREVIEW_TOGGLE_ICON_PATH);
});

test('registers only the preview custom element name', () => {
  assert.equal(globalThis.customElements.get('cw-preview'), CausewayPreviewElement);
  assert.equal(globalThis.customElements.get('cw-peek'), undefined);
});

test('captures one inert inline preview declaration without connecting its descendants', () => {
  const collection = new CausewayCollectionElement();
  const preview = document.createElement('cw-preview');
  preview.innerHTML = '<cw-property id="name"></cw-property>';
  collection.appendChild(preview);

  captureDeclarativeCollectionPreviews({querySelectorAll: () => [collection]});

  assert.deepEqual(collectionPreviewDeclaration(collection), {
    count: 1,
    presentation: {html: '<cw-property id="name"></cw-property>', inline: true}
  });
  assert.equal(preview.innerHTML, '');
  assert.equal(preview.hidden, true);
});

test('empty, duplicate, and unsupported presentation values fail closed', () => {
  const emptyCollection = new CausewayCollectionElement();
  emptyCollection.appendChild(document.createElement('cw-preview'));
  captureDeclarativeCollectionPreviews({querySelectorAll: () => [emptyCollection]});
  assert.deepEqual(collectionPreviewDeclaration(emptyCollection), {
    count: 1,
    presentation: {html: '', inline: false}
  });

  const duplicateCollection = new CausewayCollectionElement();
  duplicateCollection.appendChild(document.createElement('cw-preview'));
  duplicateCollection.appendChild(document.createElement('cw-preview'));
  assert.deepEqual(collectionPreviewDeclaration(duplicateCollection), {count: 2, presentation: null});
  assert.equal(normalizePreviewPresentation({html: '  <!-- empty --> '}), null);
  assert.deepEqual(normalizePreviewPresentation({html: '<cw-property id="name"></cw-property>'}), {
    html: '<cw-property id="name"></cw-property>'
  });
  assert.equal(normalizePreviewPresentation({html: `<p>${'x'.repeat(65536)}</p>`}), null);
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
  const declaration = document.createElement('cw-preview');
  declaration.innerHTML = '<p>Inline preview</p>';
  collection.appendChild(declaration);
  document.body.appendChild(collection);

  await waitFor(() => collection.collectionState.status === 'ready');
  assert.match(collection.innerHTML, /data-causeway-preview-toggle="example\.Staff:1"/);
  assert.match(collection.innerHTML, /aria-expanded="false"/);
  assert.match(collection.innerHTML, /<svg class="causeway-collection-preview-icon" aria-hidden="true" focusable="false" viewBox="0 0 20 20">/);
  assert.match(collection.innerHTML, /<path d="M7 4l6 6-6 6"><\/path><\/svg>/);
  assert.doesNotMatch(collection.innerHTML, /[▸▾]/);
  assert.equal(hydrationCount, 1);
});

test('empty collection preview resolves a type default and missing defaults disclose nothing', async () => {
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
  collection.appendChild(document.createElement('cw-preview'));
  document.body.appendChild(collection);

  await waitFor(() => collection.collectionState.status === 'ready');
  assert.deepEqual(requested.sort(), ['example.Missing', 'example.Staff']);
  assert.equal((collection.innerHTML.match(/data-causeway-preview-toggle=/g) ?? []).length, 1);
  delete globalThis.causewayCollectionRowPreviewResolver;
});

test('live preview provides only its dedicated context and Escape requests collapse', () => {
  const context = {identity: {logicalTypeName: 'example.Type', id: '1'}, disconnect() {}};
  let collapsed = 0;
  let provided = null;
  const preview = new CausewayPreviewElement();
  preview.configureLive({context, label: 'Preview Example', collapse: () => collapsed += 1});
  const child = document.createElement('span');
  preview.appendChild(child);
  document.body.appendChild(preview);

  child.dispatchEvent(new CustomEvent(OBJECT_CONTEXT_REQUEST_EVENT, {
    bubbles: true,
    detail: {provide: candidate => provided = candidate}
  }));
  assert.equal(provided, context);
  assert.equal(preview.getAttribute('role'), 'region');
  assert.equal(preview.getAttribute('aria-label'), 'Preview Example');

  const escape = new Event('keydown', {bubbles: true, cancelable: true});
  escape.key = 'Escape';
  child.dispatchEvent(escape);
  assert.equal(collapsed, 1);
  assert.equal(escape.defaultPrevented, true);
});
