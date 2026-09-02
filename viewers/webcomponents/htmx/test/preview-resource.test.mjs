/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import test from 'node:test';
import {
  createCausewayPreviewResolver,
  parseCausewayPreview,
  validateCausewayPreviewRoot
} from '../src/main/resources/META-INF/resources/causeway-htmx/preview-resource.mjs';

function element(localName, {attributes = [], children = [], innerHTML = ''} = {}) {
  return {
    localName,
    attributes,
    innerHTML,
    querySelectorAll() {
      const descendants = [];
      const visit = candidates => candidates.forEach(candidate => {
        descendants.push(candidate);
        visit(candidate.children ?? []);
      });
      visit(children);
      return descendants;
    },
    children
  };
}

function attribute(name, value = '') {
  return {name, value};
}

function response(status, body = '') {
  return {status, ok: status >= 200 && status < 300, async text() { return body; }};
}

test('safe preview validation accepts semantic domain layout and returns only inner markup', () => {
  const property = element('cw-property', {attributes: [attribute('id', 'name'), attribute('editable')]});
  const collectionColumn = element('cw-collection-column', {attributes: [attribute('id', 'pets')]});
  const collection = element('cw-collection', {attributes: [attribute('id', 'pets')], children: [collectionColumn]});
  const section = element('section', {attributes: [attribute('aria-label', 'Preview')], children: [property, collection]});
  const root = element('cw-peek', {children: [section], innerHTML: '<section>safe</section>'});

  assert.deepEqual(validateCausewayPreviewRoot(root), {html: '<section>safe</section>'});
});

test('preview validation rejects executable unsupported and identity-bearing markup without values', () => {
  for (const child of [
    element('script', {innerHTML: 'secret-value'}),
    element('div', {attributes: [attribute('onclick', 'secret-value')]}),
    element('cw-object-link', {attributes: [attribute('object-id', 'secret-value')]}),
    element('img', {attributes: [attribute('src', 'secret-value')]})
  ]) {
    const root = element('cw-peek', {children: [child]});
    assert.throws(() => validateCausewayPreviewRoot(root), error => {
      assert.match(error.message, /unsupported/);
      assert.doesNotMatch(error.message, /secret-value/);
      return true;
    });
  }
  assert.throws(() => validateCausewayPreviewRoot(element('cw-peek', {
    attributes: [attribute('data-row-id', 'secret-value')]
  })), /cannot declare attributes/);
});

test('preview parsing requires one bounded cw-peek root', () => {
  const root = element('cw-peek', {innerHTML: '<p>safe</p>'});
  const documentRef = {createElement: () => ({content: {children: [root]}, set innerHTML(_value) {}})};
  assert.deepEqual(parseCausewayPreview('<cw-peek><p>safe</p></cw-peek>', {documentRef}), {html: '<p>safe</p>'});
  assert.throws(() => parseCausewayPreview('x'.repeat(65537), {documentRef}), /size limit/);
  assert.throws(() => parseCausewayPreview('<p>wrong</p>', {
    documentRef: {createElement: () => ({content: {children: [element('p')]}, set innerHTML(_value) {}})}
  }), /one cw-peek root/);
});

test('preview resolver caches immutable lookups and preserves endpoint policy', async () => {
  const requests = [];
  const diagnostics = [];
  const resolver = createCausewayPreviewResolver({
    basePath: '/viewer',
    resourcePageMode: 'cached',
    fetchImpl: async (url, options) => {
      requests.push({url, options});
      return response(200, '<cw-peek>safe</cw-peek>');
    },
    parse: html => Object.freeze({html}),
    diagnostic: value => diagnostics.push(value)
  });
  const first = await resolver({logicalTypeName: 'petclinic.PetOwner'});
  const second = await resolver({logicalTypeName: 'petclinic.PetOwner'});
  assert.equal(first, second);
  assert.equal(requests.length, 1);
  assert.equal(requests[0].url, '/viewer/_previews/petclinic.PetOwner');
  assert.deepEqual(requests[0].options.headers, {Accept: 'text/html'});
  assert.deepEqual(diagnostics, [null]);
  assert.equal(await resolver({logicalTypeName: '../secret'}), null);
  assert.equal(requests.length, 1);
});

test('preview resolver reloads, caches missing resources, and retries bounded failures', async () => {
  let reloadCount = 0;
  const reload = createCausewayPreviewResolver({
    basePath: '/viewer',
    resourcePageMode: 'reload',
    fetchImpl: async () => response(200, `<cw-peek>${++reloadCount}</cw-peek>`),
    parse: html => ({html})
  });
  assert.notDeepEqual(
    await reload({logicalTypeName: 'petclinic.Visit'}),
    await reload({logicalTypeName: 'petclinic.Visit'})
  );

  let missingCount = 0;
  const missing = createCausewayPreviewResolver({
    basePath: '/viewer',
    fetchImpl: async () => { missingCount += 1; return response(404); },
    parse: () => assert.fail('missing previews are not parsed')
  });
  assert.equal(await missing({logicalTypeName: 'petclinic.Missing'}), null);
  assert.equal(await missing({logicalTypeName: 'petclinic.Missing'}), null);
  assert.equal(missingCount, 1);

  let attempts = 0;
  const diagnostics = [];
  const retry = createCausewayPreviewResolver({
    basePath: '/viewer',
    fetchImpl: async () => ++attempts === 1 ? response(500, 'secret body') : response(200, 'safe'),
    parse: html => ({html}),
    diagnostic: value => diagnostics.push(value)
  });
  await assert.rejects(retry({logicalTypeName: 'petclinic.Visit'}), error => {
    assert.match(error.message, /500/);
    assert.doesNotMatch(error.message, /secret/);
    return true;
  });
  assert.deepEqual(await retry({logicalTypeName: 'petclinic.Visit'}), {html: 'safe'});
  assert.deepEqual(diagnostics, ['resolution', null]);
});
