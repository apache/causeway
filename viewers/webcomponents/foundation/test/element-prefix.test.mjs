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
import {readdir, readFile} from 'node:fs/promises';
import {extname} from 'node:path';
import {fileURLToPath} from 'node:url';
import test from 'node:test';

import {installDomShim} from './dom-shim.mjs';

installDomShim();
const {CausewayElementName, CausewayHostClass, CausewaySemanticEvent} = await import('../src/component-contracts.mjs');
const {CAUSEWAY_FIELD_EDITOR} = await import('../src/field-widget.mjs');

const WEB_COMPONENTS_ROOT = fileURLToPath(new URL('../../', import.meta.url));
const PROJECT_ROOT = fileURLToPath(new URL('../../../../', import.meta.url));
const AUDIT_ROOTS = [
  WEB_COMPONENTS_ROOT,
  `${PROJECT_ROOT}/openspec/planned-changes`,
  `${PROJECT_ROOT}/regressiontests/referenceapp/htmx`,
  `${PROJECT_ROOT}/viewers/graphql/adoc`
];
const SOURCE_EXTENSIONS = new Set(['.adoc', '.css', '.html', '.java', '.js', '.md', '.mjs', '.yaml', '.yml']);
const OLD_PREFIX = `causeway${'-'}`;
const OLD_SUFFIXES = [
  'graphql-client',
  'object-context',
  'object',
  'object-header',
  'property',
  'value',
  'object-link',
  'action',
  'interaction-controller',
  'field-editor',
  'reference-editor',
  'collection',
  'collection-column',
  'menubars',
  'menubar-primary',
  'menubar-secondary',
  'menubar-tertiary'
];
const SUFFIX_PATTERN = [...OLD_SUFFIXES]
  .sort((left, right) => right.length - left.length)
  .join('|');
const OLD_TAG = new RegExp(`(?:<|&lt;)\\/?${OLD_PREFIX}(?:${SUFFIX_PATTERN})(?![a-z0-9-])`);
const OLD_API_NAME = new RegExp(`(?<![.\\w-])${OLD_PREFIX}(?:${SUFFIX_PATTERN})(?![a-z0-9-])`);
const OLD_TYPE_SELECTOR = new RegExp(`(?:^|[>,(]\\s*)${OLD_PREFIX}(?:${SUFFIX_PATTERN})(?=[\\s\\[{:>,])`);
const MEMBER_BEARING_SUFFIX_PATTERN = 'property|action|collection|collection-column';
const OLD_MEMBER_TAG = new RegExp(`<cw-(?:${MEMBER_BEARING_SUFFIX_PATTERN})\\b[^>]*\\bmember\\s*=`);
const OLD_MEMBER_SELECTOR = new RegExp(`cw-(?:${MEMBER_BEARING_SUFFIX_PATTERN})[^\\n]*\\[member(?:[~|^$*]?=|\\])`);
const OLD_MEMBER_DOM_API = /(?:get|set)Attribute(?:\?\.)?\(\s*['"]member['"]/;
const OLD_MEMBER_ACCESSOR = /\b(?:get member\(\)|set member\()/;
const OLD_MEMBER_PROPERTY = /\b(?:property|action|collection|element|host)\.member\b/;
const INTENTIONAL_OBSOLETE_MEMBER_API = 'intentional-obsolete-member-api';
const AUDIT_FILE = fileURLToPath(import.meta.url);
const ELEMENT_API_MARKERS = [
  'closest(',
  'createElement',
  'customElements',
  'fallbackElement',
  'getElementsByTagName',
  'localName',
  'locator(',
  'matches',
  'querySelector',
  'waitForFunction'
];

async function sourceFiles(directory) {
  const files = [];
  for (const entry of await readdir(directory, {withFileTypes: true})) {
    if (entry.name === 'node_modules' || entry.name === 'target') continue;
    const path = `${directory}/${entry.name}`;
    if (entry.isDirectory()) {
      files.push(...await sourceFiles(path));
    } else if (SOURCE_EXTENSIONS.has(extname(entry.name))) {
      files.push(path);
    }
  }
  return files;
}

test('source contains only the compact custom-element namespace', async () => {
  const stale = [];
  for (const root of AUDIT_ROOTS) {
    for (const path of await sourceFiles(root)) {
      const lines = (await readFile(path, 'utf8')).split('\n');
      for (const [index, line] of lines.entries()) {
        if (OLD_TAG.test(line)
            || (ELEMENT_API_MARKERS.some(marker => line.includes(marker)) && OLD_API_NAME.test(line))
            || ((['.adoc', '.css', '.html'].includes(extname(path)) || path.endsWith('/component-styles.mjs')) && OLD_TYPE_SELECTOR.test(line.trim()))) {
          stale.push(`${path.slice(PROJECT_ROOT.length + 1)}:${index + 1}: ${line.trim()}`);
        }
      }
    }
  }
  assert.deepEqual(stale, []);
});

test('member-bearing elements use native id without stale member DOM APIs', async () => {
  const stale = [];
  for (const root of AUDIT_ROOTS) {
    for (const path of await sourceFiles(root)) {
      if (path === AUDIT_FILE) continue;
      const lines = (await readFile(path, 'utf8')).split('\n');
      for (const [index, line] of lines.entries()) {
        if (!line.includes(INTENTIONAL_OBSOLETE_MEMBER_API)
            && (OLD_MEMBER_TAG.test(line)
              || OLD_MEMBER_SELECTOR.test(line)
              || OLD_MEMBER_DOM_API.test(line)
              || OLD_MEMBER_ACCESSOR.test(line)
              || OLD_MEMBER_PROPERTY.test(line))) {
          stale.push(`${path.slice(PROJECT_ROOT.length + 1)}:${index + 1}: ${line.trim()}`);
        }
      }
    }
  }
  assert.deepEqual(stale, []);
});

test('compact element names retain established non-element Causeway contracts', () => {
  assert.equal(Object.values(CausewayElementName).length, 16);
  assert.ok(Object.values(CausewayElementName).every(name => name.startsWith('cw-')));
  assert.equal(CAUSEWAY_FIELD_EDITOR, 'cw-field-editor');
  assert.equal(CausewayHostClass.PROPERTY, 'causeway-property');
  assert.equal(CausewaySemanticEvent.PROPERTY_UPDATED, 'causeway-property-updated');
});
