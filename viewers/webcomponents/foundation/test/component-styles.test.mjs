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
import {readFile} from 'node:fs/promises';
import test from 'node:test';

import {CAUSEWAY_COMPONENT_STYLES} from '../src/component-styles.mjs';

test('external structural stylesheet remains synchronized with the installable styles', async () => {
    const external = await readFile(new URL('../src/component-styles.css', import.meta.url), 'utf8');
    const withoutLicense = external.slice(external.indexOf('cw-parameter {')).trim();
    assert.equal(withoutLicense, CAUSEWAY_COMPONENT_STYLES.trim());
});

test('declarative action parameters remain non-visual configuration elements', () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /^\s*cw-parameter \{\s+display: none;\s+\}/);
});

test('action result content is bounded above a persistent dismiss control', () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-action-results[\s\S]*?display: flex;[\s\S]*?flex-direction: column;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /data-causeway-presentation-style="INLINE"[\s\S]*?--causeway-action-results-content-max-block-size[\s\S]*?overflow: auto;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-action-results-surface > \.causeway-result-dismiss \{[\s\S]*?align-self: end;[\s\S]*?order: 2;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-action-results-surface > :not\(\.causeway-result-dismiss\) \{[\s\S]*?--causeway-action-results-content-max-block-size[\s\S]*?overflow: auto;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-action-results-dialog \{[\s\S]*?max-block-size: calc\(100dvh - 2rem\);[\s\S]*?inline-size: min\(var\(--causeway-action-results-dialog-width, 60rem\)/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-action-results-dialog::backdrop \{[\s\S]*?--causeway-action-results-backdrop/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-action-results-sidebar \{[\s\S]*?block-size: 100dvh;[\s\S]*?position: fixed;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /@media \(max-width: 36rem\) \{[\s\S]*?\.causeway-action-results-sidebar \{[\s\S]*?inline-size: 100vw;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /@media \(prefers-reduced-motion: reduce\) \{[\s\S]*?transition: none;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /@media \(forced-colors: active\) \{[\s\S]*?border: 2px solid CanvasText;/);
});

test('PDF reader styles expose bounded sizing, scrolling, and visible keyboard focus', () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-pdf-viewport \{[\s\S]*?--causeway-pdf-viewport-height[\s\S]*?overflow: auto;[\s\S]*?overflow-anchor: none;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-pdf-pages \{[\s\S]*?flex-direction: column;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-pdf-control:focus-visible,[\s\S]*?outline: var\(--causeway-pdf-focus/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-pdf-toolbar > \.causeway-value-lob \{[\s\S]*?flex: 1 1 14rem;[\s\S]*?overflow-wrap: anywhere;/);
    assert.doesNotMatch(CAUSEWAY_COMPONENT_STYLES, /causeway-pdf-accessibility-note/);
});

test('property edit affordance remains a compact deterministic icon control', async () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-edit \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?padding: 0\.35rem;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-edit-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    assert.match(theme, /\.causeway-property-edit \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?min-height: 0;/);
    assert.match(theme, /\.causeway-property-edit-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
});

test('property editor actions remain compact deterministic icon controls', async () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-editor-action \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?padding: 0\.35rem;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-editor-action-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    assert.match(theme, /\.causeway-property-editor-action \{[\s\S]*?block-size: 2rem;[\s\S]*?inline-size: 2rem;[\s\S]*?min-height: 0;/);
    assert.match(theme, /\.causeway-property-editor-action-icon \{[\s\S]*?block-size: 1rem;[\s\S]*?inline-size: 1rem;[\s\S]*?stroke: currentColor;/);
    assert.match(theme, /\.causeway-property-editor-actions button:first-child[\s\S]*?background: var\(--causeway-action-background\)/);
});

test('object-link icons remain bounded, decorative markup is themeable and header identity is not duplicated', () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-object-link-icon \{[\s\S]*?block-size: var\(--causeway-object-icon-size, 1\.5rem\);[\s\S]*?inline-size: var\(--causeway-object-icon-size, 1\.5rem\);[\s\S]*?object-fit: var\(--causeway-object-icon-fit, contain\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-breadcrumbs \.causeway-object-link-identity,\s+\.causeway-object-header-link \.causeway-object-link-identity \{\s+display: none;/);
});

test('sign-out appearance remains visibly bounded in native and Vaadin menu controls', () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-service-action-control\[data-action-appearance="sign-out"\],[\s\S]*?vaadin-menu-bar-item\[data-causeway-action-appearance="sign-out"\] \{[\s\S]*?border: var\(--causeway-sign-out-border, 1px solid currentColor\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /vaadin-menu-bar-item\[data-causeway-action-appearance="sign-out"\]:focus-within \{[\s\S]*?outline: var\(--causeway-menubar-focus, 0\.2rem solid LinkText\);/);
});

test('action icons retain component-owned spacing in native and Vaadin controls', () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /gap: var\(--causeway-action-icon-gap, 0\.75rem\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-action-control > vaadin-button > \.causeway-action-icon:first-child \{\s+margin-inline-end: var\(--causeway-action-icon-gap, 0\.75rem\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-action-control > vaadin-button > \.causeway-action-icon:last-child \{\s+margin-inline-start: var\(--causeway-action-icon-gap, 0\.75rem\);/);
});

test('member tooltips preserve sections, responsive bounds and pointer and focus access', async () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-disabled-tooltip \{[\s\S]*?cursor: help;[\s\S]*?position: relative;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-member-tooltip,\s+\.causeway-action-control-tooltip \{[\s\S]*?cursor: help;[\s\S]*?position: relative;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-member-tooltip::after,\s+\.causeway-action-control-tooltip::after \{[\s\S]*?content: attr\(data-tooltip\);[\s\S]*?max-inline-size: min\(24rem, calc\(100vw - 2rem\)\);[\s\S]*?white-space: pre-line;/);
    assert.equal([...CAUSEWAY_COMPONENT_STYLES.matchAll(/background: var\(--causeway-tooltip-background, #f8fafc\);/g)].length, 2);
    assert.equal([...CAUSEWAY_COMPONENT_STYLES.matchAll(/border: 1px solid var\(--causeway-tooltip-border, #cbd5e1\);/g)].length, 2);
    assert.equal([...CAUSEWAY_COMPONENT_STYLES.matchAll(/box-shadow: var\(--causeway-tooltip-shadow, 0 0\.25rem 0\.75rem rgb\(15 23 42 \/ 0\.18\)\);/g)].length, 2);
    assert.equal([...CAUSEWAY_COMPONENT_STYLES.matchAll(/color: var\(--causeway-tooltip-color, #111827\);/g)].length, 2);
    assert.equal([...CAUSEWAY_COMPONENT_STYLES.matchAll(/inset-block-start: calc\(100% \+ 0\.4rem\);/g)].length, 2);
    assert.equal([...CAUSEWAY_COMPONENT_STYLES.matchAll(/transform: translateY\(-0\.25rem\);/g)].length, 2);
    assert.doesNotMatch(CAUSEWAY_COMPONENT_STYLES, /inset-block-end: calc\(100% \+ 0\.4rem\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-member-tooltip:hover::after,[\s\S]*?\.causeway-member-tooltip:focus-visible::after/);
    assert.doesNotMatch(CAUSEWAY_COMPONENT_STYLES, /causeway-property-disabled-indicator/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-property-value-string \{\s+text-align: start;/);
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    assert.match(theme, /--causeway-tooltip-background: #f8fafc;/);
    assert.match(theme, /--causeway-tooltip-color: #111827;/);
    assert.match(theme, /--causeway-tooltip-border: #cbd5e1;/);
    assert.match(theme, /--causeway-tooltip-shadow: 0 0\.25rem 0\.75rem rgb\(15 23 42 \/ 0\.18\);/);
    assert.match(theme, /\.causeway-property-value-string \{\s+text-align: start;/);
    assert.match(theme, /\.causeway-action-description \{[\s\S]*?position: absolute !important;[\s\S]*?clip: rect\(0, 0, 0, 0\) !important;/);
});

test('collection descriptions remain quiet text directly below their labels', async () => {
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    assert.match(theme, /\.causeway-collection-description \{\s+margin: 0;\s+padding: var\(--causeway-space-2\) var\(--causeway-space-4\);\s+color: var\(--causeway-muted\);\s+font-size: var\(--causeway-font-size-sm\);/);
    assert.doesNotMatch(theme, /\.causeway-property-description,\s+\.causeway-collection-description/);
});

test('collection criteria, rows and paging retain compact aligned spacing', () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-content \{[\s\S]*?padding-block: var\(--causeway-collection-content-padding, 0\.75rem\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-search \{\s+align-items: center;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-search :is\(input, button\) \{\s+block-size: var\(--causeway-control-height, 2\.35rem\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-collection-grid \{[\s\S]*?font-size: var\(--causeway-font-size-sm, 0\.82rem\);[\s\S]*?--lumo-font-size-m: var\(--causeway-font-size-sm, 0\.82rem\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-collection-grid button\[data-causeway-collection-sort\] \{[\s\S]*?min-height: 0;[\s\S]*?padding: 0;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-sort-indicator \{[\s\S]*?margin-inline-start: 0\.25rem;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-collection-table \{\s+border-collapse: collapse;\s+table-layout: fixed;/);
});

test('application native-control chrome does not cross into toolkit-owned slotted field inputs', async () => {
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    const nativeInput = 'input:not([slot="input"])';
    const nativeSelect = 'select:not([slot="input"])';
    const nativeTextarea = 'textarea:not([slot])';

    assert.ok(theme.includes(`button,\n${nativeInput},\n${nativeSelect},\n${nativeTextarea} {\n  max-width: 100%;\n  border: 1px solid var(--causeway-border-strong);`));
    assert.ok(theme.includes(`${nativeInput},\n${nativeSelect},\n${nativeTextarea} {\n  padding: 0.45rem 0.6rem;`));
    assert.ok(theme.includes(`${nativeTextarea} {\n  min-height: 6rem;\n  resize: vertical;`));
    assert.ok(theme.includes(`:where(a, button, ${nativeInput}, ${nativeSelect}, ${nativeTextarea}, [tabindex]):focus-visible`));
    assert.match(theme, /cw-field-editor > vaadin-text-area:focus-visible \{\s+outline: none;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-field-editor \{[\s\S]*?--vaadin-focus-ring-color: var\(--causeway-focus, Highlight\);/);
    assert.doesNotMatch(theme, /textarea:not\(\[slot="input"\]\)/);
    assert.doesNotMatch(theme, /cw-field-editor > textarea:focus-visible/);
    assert.doesNotMatch(theme, /\ninput,\nselect,\ntextarea \{\n  max-width:/);
    assert.doesNotMatch(theme, /:where\(a, button, input, select, textarea, \[tabindex\]\):focus-visible/);
});

test('direct collection associations share one compact responsive heading row', async () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-property\[data-causeway-action-group\]/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-collection\[data-causeway-collection-heading-actions\] \{[\s\S]*?align-items: center;[\s\S]*?justify-content: flex-start;[\s\S]*?row-gap: 0;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /> \[data-causeway-collection-heading\] \{\s+flex: 1 1 12rem;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /--vaadin-button-height: var\(--causeway-collection-heading-action-height, 2\.25rem\);/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /@container cw-collection \(max-width: 32rem\)[\s\S]*?> \[data-causeway-collection-heading\] \{\s+flex-basis: 100%;/);
    assert.doesNotMatch(CAUSEWAY_COMPONENT_STYLES, /data-causeway-associated-kind="collection"/);
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    assert.match(theme, /cw-collection\[data-causeway-collection-heading-actions\] \{[\s\S]*?border: 1px solid var\(--causeway-border\);[\s\S]*?background: var\(--causeway-surface-subtle\);/);
    assert.match(theme, /> \.causeway-member-primary \{\s+border-block-start: 1px solid var\(--causeway-border\);/);
    assert.match(theme, /> \.causeway-member-primary > \.causeway-collection \{[\s\S]*?border: 0;[\s\S]*?box-shadow: none;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /--causeway-associated-action-gap/);
});

test('property-associated actions align with the effective field column responsively', async () => {
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-property\[data-causeway-action-group\] \{[\s\S]*?display: grid;[\s\S]*?grid-template-columns: var\(--causeway-property-label-column/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 2;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /> cw-action\[data-causeway-associated-action\] > \.causeway-action \{\s+margin-block-start: var\(--causeway-associated-action-margin-block-start, 0\);/);
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    assert.match(theme, /cw-property\[data-causeway-action-group\] \{\s+grid-template-columns: minmax\(7rem, var\(--causeway-label-width\)\) minmax\(0, 1fr\) auto;/);
    assert.match(theme, /cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 2 \/ -1;/);
    assert.match(theme, /@container \(max-width: 32rem\)[\s\S]*?cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 1 \/ -1;/);
    assert.match(theme, /@media \(max-width: 48rem\)[\s\S]*?cw-property\[data-causeway-action-group\] > cw-action\[data-causeway-associated-action\] \{\s+grid-column: 1 \/ -1;/);
});

test('action prompt styles expose inline, movable modal and vertical sidebar surfaces', async () => {
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\.causeway-action-prompt-inline,[\s\S]*?inline-size: 100%;[\s\S]*?max-inline-size: none;/);
    assert.match(CAUSEWAY_COMPONENT_STYLES, /\[data-causeway-inline-action-prompt\] > \.causeway-inline-action-prompt-portal \{\s+grid-column: 1 \/ -1;/);
    assert.match(theme, /cw-interaction-controller:has\(\.causeway-action-prompt-modal, \.causeway-action-prompt-sidebar\)::before/);
    assert.match(theme, /\.causeway-action-prompt-modal \{[\s\S]*?inset: 50% auto auto 50%;[\s\S]*?transform: translate\(-50%, -50%\);/);
    assert.match(theme, /\.causeway-action-prompt-sidebar \{[\s\S]*?inset: 0 0 0 auto;[\s\S]*?max-height: 100dvh;/);
    assert.match(theme, /\.causeway-action-prompt-modal \.causeway-action-prompt-title\[data-causeway-dialog-drag-handle\] \{[\s\S]*?cursor: move;/);
});

test('standard theme gives effective multiline properties explicit responsive placement', async () => {
    const theme = await readFile(new URL('../src/theme.css', import.meta.url), 'utf8');
    const effectiveMultiline = '\\.causeway-property\\[data-multi-line\\]\\[data-label-position="LEFT"\\]';
    assert.match(theme, new RegExp(`${effectiveMultiline} \\.causeway-property-description \\{\\s+grid-column: 1;\\s+grid-row: 2;`));
    assert.match(theme, new RegExp(`${effectiveMultiline} \\.causeway-property-field \\{\\s+grid-column: 2;\\s+grid-row: 1 / span 2;`));
    assert.match(theme, new RegExp(`@container \\(max-width: 32rem\\)[\\s\\S]+${effectiveMultiline} \\.causeway-property-field \\{\\s+align-items: flex-start;\\s+grid-column: 1 / -1;\\s+grid-row: 3;`));
    assert.match(theme, new RegExp(`@media \\(max-width: 48rem\\)[\\s\\S]+${effectiveMultiline} \\.causeway-property-field \\{\\s+align-items: flex-start;\\s+grid-column: 1 / -1;\\s+grid-row: 3;`));
    assert.doesNotMatch(theme, /cw-property\[multiline\]/);
});
