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

test('application-facing production sources contain no raw Grid markup or APIs', async () => {
  const roots = [
    path.join(foundation, 'demo'),
    path.join(webcomponents, 'htmx', 'src', 'main'),
    path.join(webcomponents, 'sample-html', 'src', 'main'),
    path.join(webcomponents, 'sample-htmx-petclinic', 'src', 'main')
  ];
  for (const root of roots) {
    for (const file of await filesBeneath(root)) {
      const source = await readFile(file, 'utf8');
      assert.doesNotMatch(source, /<\/?vaadin-grid(?:-column)?\b/i, file);
      assert.doesNotMatch(source, /\.dataProvider\s*=|\.renderer\s*=.*vaadin/i, file);
    }
  }
});

test('Grid adapter remains presentation-only and disables unqualified affordances', async () => {
  const source = await readFile(path.join(foundation, 'src', 'grid-widget.mjs'), 'utf8');
  assert.doesNotMatch(source, /GraphQL|repository|persistence|canonicalObjectPath|navigation-request/i);
  assert.match(source, /column\.sortable = false/);
  assert.match(source, /column\.resizable = presentation\.resizableColumns/);
  assert.match(source, /control\.columnReorderingAllowed = presentation\.reorderableColumns/);
  assert.match(source, /control\.selectedItems = \[\]/);
  assert.match(source, /control\.rowDetailsRenderer = null/);
  assert.doesNotMatch(source, /sortable\s*=\s*true|selectionMode|dragFilter|dropFilter/);
});
