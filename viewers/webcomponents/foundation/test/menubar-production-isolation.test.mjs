/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import assert from 'node:assert/strict';
import {readdir, readFile} from 'node:fs/promises';
import path from 'node:path';
import test from 'node:test';
import {fileURLToPath} from 'node:url';

const foundation = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const webcomponents = path.resolve(foundation, '..');

async function filesBeneath(directory) {
  const files = [];
  for (const entry of await readdir(directory, {withFileTypes: true})) {
    if (entry.name === 'node_modules' || entry.name === 'target' || entry.name === 'generated') continue;
    const candidate = path.join(directory, entry.name);
    if (entry.isDirectory()) files.push(...await filesBeneath(candidate));
    else if (/\.(?:adoc|css|html|java|js|mjs|properties|xml)$/.test(entry.name)) files.push(candidate);
  }
  return files;
}

test('application-facing production sources contain no raw Menu Bar markup or item APIs', async () => {
  const roots = [
    path.join(foundation, 'demo'),
    path.join(webcomponents, 'htmx', 'src', 'main'),
    path.join(webcomponents, 'sample-html', 'src', 'main'),
    path.join(webcomponents, 'sample-htmx-petclinic', 'src', 'main')
  ];
  for (const root of roots) {
    for (const file of await filesBeneath(root)) {
      const source = await readFile(file, 'utf8');
      assert.doesNotMatch(source, /<\/?vaadin-menu-bar\b/i, file);
      assert.doesNotMatch(source, /\.items\s*=.*(?:menu|action)|item-selected.*(?:GraphQL|navigate)/is, file);
    }
  }
});

test('Menu Bar adapter remains presentation-only and value-safe', async () => {
  const widget = await readFile(path.join(foundation, 'src', 'menubar-widget.mjs'), 'utf8');
  const projection = await readFile(path.join(foundation, 'src', 'menubar-projection.mjs'), 'utf8');
  assert.doesNotMatch(widget, /GraphQL|canonicalObjectPath|location\.|repository|fetch\(/i);
  assert.doesNotMatch(projection, /GraphQL|canonicalObjectPath|location\.|repository|fetch\(/i);
  assert.match(widget, /resolveCausewayMenuAction/);
  assert.match(widget, /this\._activate\?\.\(descriptor\)/);
  assert.match(projection, /disabled: action\.disabled/);
  assert.doesNotMatch(projection, /arguments|parameters|password|protectedValue|serializedSnapshot/i);
});
