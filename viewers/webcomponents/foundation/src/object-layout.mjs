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

import {escapeHtml} from './rendering.mjs';
import {
  MAX_STRUCTURAL_DIAGNOSTICS,
  MAX_STRUCTURAL_XML_CHARACTERS,
  MAX_STRUCTURAL_XML_DEPTH,
  MAX_STRUCTURAL_XML_NODES,
  parseStructuralXml
} from './structural-xml.mjs';

export const CAUSEWAY_COMPONENT_NAMESPACE = 'https://causeway.apache.org/applib/layout/component';
export const CAUSEWAY_GRID_NAMESPACE = 'https://causeway.apache.org/applib/layout/grid/bootstrap3';
export const MAX_GRID_XML_CHARACTERS = MAX_STRUCTURAL_XML_CHARACTERS;
export const MAX_GRID_XML_DEPTH = MAX_STRUCTURAL_XML_DEPTH;
export const MAX_GRID_XML_NODES = MAX_STRUCTURAL_XML_NODES;
export const MAX_LAYOUT_DIAGNOSTICS = MAX_STRUCTURAL_DIAGNOSTICS;
export const MAX_MULTI_LINE_ROWS = 50;

const MEMBER_KINDS = Object.freeze(['action', 'property', 'collection']);
const STRUCTURAL_KINDS = Object.freeze(['grid', 'row', 'col', 'tabGroup', 'tab', 'fieldSet', 'domainObject']);
const PRESENTATION_KINDS = Object.freeze(['named', 'describedAs']);

export class CausewayGridError extends Error {
  constructor(code, message) {
    super(message);
    this.name = 'CausewayGridError';
    this.code = code;
  }
}

export function parseCausewayGridXml(xml, {members = new Map(), maxDiagnostics = MAX_LAYOUT_DIAGNOSTICS} = {}) {
  const diagnostics = createDiagnosticCollector(maxDiagnostics);
  const root = parseStructuralXml(xml, {
    codePrefix: 'GRID',
    resourceLabel: 'effective grid resource',
    ErrorType: CausewayGridError
  });
  if (elementKind(root) !== 'grid') {
    throw new CausewayGridError('GRID_ROOT_REQUIRED', 'The effective grid resource does not contain a supported grid root.');
  }
  diagnoseAttributes(root, diagnostics);
  const normalizedMembers = normalizeMembers(members);
  const explicitClaims = claimExplicitMembers(root, normalizedMembers, diagnostics);
  const allocated = new Set();
  const children = buildChildren(root, {
    members: normalizedMembers,
    explicitClaims,
    allocated,
    diagnostics,
    path: 'grid'
  });
  const semanticCount = countSemanticNodes(children);
  return deepFreeze({
    usable: semanticCount > 0,
    plan: {
      source: 'grid',
      regions: children
    },
    diagnostics: diagnostics.values()
  });
}

export function createFallbackLayoutPlan(members = new Map(), diagnostics = []) {
  const normalizedMembers = normalizeMembers(members);
  const byKind = kind => [...normalizedMembers.values()]
    .filter(member => member.kind === kind)
    .map(member => memberNode(member));
  const actions = byKind('action');
  const properties = byKind('property');
  const collections = byKind('collection');
  const mainColumns = [];
  if (properties.length > 0) {
    mainColumns.push(columnNode(4, [groupNode('Properties', 'properties', properties)]));
  }
  if (collections.length > 0) {
    mainColumns.push(columnNode(8, [tabsForCollections(collections)]));
  }
  const regions = [rowNode([columnNode(12, [{kind: 'header'}, ...actions])])];
  if (mainColumns.length > 0) {
    regions.push(rowNode(mainColumns));
  }
  return deepFreeze({
    source: 'fallback',
    regions,
    diagnostics: [...diagnostics].slice(0, MAX_LAYOUT_DIAGNOSTICS)
  });
}

export function renderObjectLayoutPlan(plan, {idPrefix = 'causeway-object', editable = false} = {}) {
  const state = {idPrefix: safeId(idPrefix), editable: Boolean(editable), sequence: 0};
  return (plan?.regions ?? []).map(region => renderNode(region, state)).join('');
}

function claimExplicitMembers(root, members, diagnostics) {
  const claims = new Set();
  walk(root, node => {
    const kind = elementKind(node);
    if (!MEMBER_KINDS.includes(kind)) {
      return;
    }
    diagnoseAttributes(node, diagnostics);
    const id = node.attributes.get('id') ?? '';
    if (!id) {
      diagnostics.add('MEMBER_ID_REQUIRED', `A ${kind} layout reference has no semantic member ID.`);
      return;
    }
    const member = members.get(id);
    if (!member) {
      diagnostics.add('STALE_MEMBER_REFERENCE', `The layout references unknown ${kind} member '${id}'.`);
      return;
    }
    if (member.kind !== kind) {
      diagnostics.add('WRONG_MEMBER_KIND', `Layout ${kind} '${id}' resolves to a ${member.kind} member.`);
      return;
    }
    if (claims.has(id)) {
      diagnostics.add('DUPLICATE_MEMBER_REFERENCE', `Member '${id}' is referenced more than once.`);
      return;
    }
    claims.add(id);
    node.claim = memberNode(member, labelFrom(node), memberPresentation(node, diagnostics));
  });
  return claims;
}

function buildChildren(node, context) {
  const result = [];
  for (let index = 0; index < node.children.length; index += 1) {
    const child = node.children[index];
    const childPath = `${context.path}/${child.localName}[${index + 1}]`;
    const built = buildNode(child, {...context, path: childPath});
    if (Array.isArray(built)) {
      result.push(...built);
    } else if (built) {
      result.push(built);
    }
  }
  appendUnreferenced(node, result, context);
  return result;
}

function buildNode(node, context) {
  const kind = elementKind(node);
  diagnoseAttributes(node, context.diagnostics);
  if (!kind) {
    context.diagnostics.add('UNSUPPORTED_LAYOUT_NODE', `Unsupported layout element '${node.qName}' was ignored.`, context.path);
    return buildChildren(node, context);
  }
  if (PRESENTATION_KINDS.includes(kind)) {
    return null;
  }
  if (MEMBER_KINDS.includes(kind)) {
    const children = buildChildren(node, context);
    if (node.claim && !context.allocated.has(node.claim.memberId)) {
      context.allocated.add(node.claim.memberId);
      return {...node.claim, children};
    }
    return children;
  }
  if (kind === 'domainObject') {
    return {kind: 'header'};
  }
  if (kind === 'row') {
    return rowNode(buildChildren(node, context));
  }
  if (kind === 'col') {
    return columnNode(spanFrom(node, context.diagnostics, context.path), buildChildren(node, context));
  }
  if (kind === 'fieldSet') {
    return groupNode(labelFrom(node) || humanize(node.attributes.get('id') || 'Properties'), node.attributes.get('id') || '', buildChildren(node, context));
  }
  if (kind === 'tab') {
    return tabNode(labelFrom(node) || 'Tab', buildChildren(node, context));
  }
  if (kind === 'tabGroup') {
    const children = buildChildren(node, context);
    const tabs = [];
    const loose = [];
    for (const child of children) {
      if (child.kind === 'tab') {
        tabs.push(child);
      } else if (child.kind === 'member' && child.memberKind === 'collection') {
        tabs.push(tabNode(child.label || humanize(child.memberId), [child]));
      } else {
        loose.push(child);
      }
    }
    if (loose.length > 0) {
      tabs.push(tabNode('Details', loose));
    }
    return {kind: 'tabs', tabs};
  }
  if (kind === 'grid') {
    return buildChildren(node, context);
  }
  return null;
}

function appendUnreferenced(node, target, context) {
  const attributesByKind = {
    action: 'unreferencedActions',
    property: 'unreferencedProperties',
    collection: 'unreferencedCollections'
  };
  for (const kind of MEMBER_KINDS) {
    const attribute = attributesByKind[kind];
    if (!booleanAttribute(node, attribute, context.diagnostics, context.path)) {
      continue;
    }
    for (const member of context.members.values()) {
      if (member.kind !== kind || context.explicitClaims.has(member.id) || context.allocated.has(member.id)) {
        continue;
      }
      context.allocated.add(member.id);
      target.push(memberNode(member));
    }
  }
}

function diagnoseAttributes(node, diagnostics) {
  const kind = elementKind(node);
  const supported = new Set(['xmlns', 'xsi:schemaLocation']);
  for (const name of node.attributes.keys()) {
    if (name.startsWith('xmlns:')) {
      supported.add(name);
    }
  }
  for (const name of supportedAttributes(kind)) {
    supported.add(name);
  }
  for (const name of node.attributes.keys()) {
    if (!supported.has(name)) {
      diagnostics.add('UNSUPPORTED_LAYOUT_ATTRIBUTE', `Unsupported attribute '${name}' on '${node.qName}' was ignored.`);
    }
  }
}

function supportedAttributes(kind) {
  if (kind === 'col') {
    return ['span', 'size', 'unreferencedActions', 'unreferencedProperties', 'unreferencedCollections'];
  }
  if (kind === 'fieldSet') {
    return ['id', 'name', 'unreferencedActions', 'unreferencedProperties', 'unreferencedCollections'];
  }
  if (kind === 'tabGroup') {
    return ['unreferencedActions', 'unreferencedProperties', 'unreferencedCollections'];
  }
  if (kind === 'tab') {
    return ['name'];
  }
  if (kind === 'domainObject') {
    return ['bookmarking'];
  }
  if (MEMBER_KINDS.includes(kind)) {
    const attributes = [
      'id', 'position', 'defaultView', 'paged', 'hidden',
      'labelPosition', 'dateRenderAdjustDays', 'typicalLength'
    ];
    return kind === 'property' ? [...attributes, 'multiLine'] : attributes;
  }
  return [];
}

function elementKind(node) {
  const local = node?.localName;
  if (!local) {
    return null;
  }
  const component = node.namespaceURI === CAUSEWAY_COMPONENT_NAMESPACE || ['c', 'cpt'].includes(node.prefix);
  const grid = node.namespaceURI === CAUSEWAY_GRID_NAMESPACE || node.prefix === 'bs';
  if (grid && ['grid', 'row', 'col', 'tabGroup', 'tab'].includes(local)) {
    return local;
  }
  if (component && [...MEMBER_KINDS, 'fieldSet', 'domainObject', ...PRESENTATION_KINDS].includes(local)) {
    return local;
  }
  return null;
}

function spanFrom(node, diagnostics, path) {
  const value = node.attributes.get('span');
  if (value == null) {
    return 12;
  }
  const span = Number(value);
  if (!Number.isSafeInteger(span) || span < 1 || span > 12) {
    diagnostics.add('INVALID_COLUMN_SPAN', `Column span '${value}' is outside the supported range 1 through 12; span 12 was used.`, path);
    return 12;
  }
  return span;
}

function booleanAttribute(node, name, diagnostics, path) {
  const value = node.attributes.get(name);
  if (value == null) {
    return false;
  }
  if (value === 'true') {
    return true;
  }
  if (value === 'false') {
    return false;
  }
  diagnostics.add('INVALID_BOOLEAN_ATTRIBUTE', `Attribute '${name}' on '${node.qName}' must be true or false and was ignored.`, path);
  return false;
}

function labelFrom(node) {
  const named = node.children.find(child => elementKind(child) === 'named');
  return named ? textContent(named).trim() : node.attributes.get('name') ?? '';
}

function textContent(node) {
  return `${node.text ?? ''}${node.children.map(textContent).join('')}`;
}

function normalizeMembers(members) {
  const result = new Map();
  for (const [candidateId, candidate] of members instanceof Map ? members : Object.entries(members ?? {})) {
    const id = candidate?.id ?? candidateId;
    const kind = candidate?.kind;
    if (typeof id === 'string' && id && MEMBER_KINDS.includes(kind)) {
      result.set(id, Object.freeze({...candidate, id, kind}));
    }
  }
  return result;
}

function memberNode(member, label = '', presentation = {}) {
  return {
    kind: 'member',
    memberKind: member.kind,
    memberId: member.id,
    label: label || humanize(member.id),
    presentation
  };
}

function memberPresentation(node, diagnostics) {
  if (elementKind(node) !== 'property') {
    return {};
  }
  const presentation = {};
  const named = labelFrom(node);
  const describedAs = descriptionFrom(node);
  if (named) presentation.named = named;
  if (describedAs) presentation.describedAs = describedAs;
  if (node.attributes.has('labelPosition')) {
    const labelPosition = String(node.attributes.get('labelPosition')).toUpperCase();
    if (['LEFT', 'TOP', 'NONE'].includes(labelPosition)) {
      presentation.labelPosition = labelPosition;
    } else {
      diagnostics.add('INVALID_LABEL_POSITION', `Property labelPosition '${node.attributes.get('labelPosition')}' is unsupported and was ignored.`);
    }
  }
  if (!node.attributes.has('multiLine')) {
    return presentation;
  }
  const candidate = node.attributes.get('multiLine');
  const rows = Number(candidate);
  if (!Number.isSafeInteger(rows) || rows <= 1) {
    diagnostics.add('INVALID_MULTI_LINE', `Property multiLine '${candidate}' must be an integer greater than one and was ignored.`);
    return presentation;
  }
  if (rows > MAX_MULTI_LINE_ROWS) {
    diagnostics.add('MULTI_LINE_CAPPED', `Property multiLine '${candidate}' exceeds ${MAX_MULTI_LINE_ROWS} rows and was capped.`);
    presentation.multiLine = MAX_MULTI_LINE_ROWS;
    return presentation;
  }
  presentation.multiLine = rows;
  return presentation;
}

function descriptionFrom(node) {
  const describedAs = node.children.find(child => elementKind(child) === 'describedAs');
  return describedAs ? textContent(describedAs).trim() : '';
}

function rowNode(children) {
  return {kind: 'row', children: children.filter(Boolean)};
}

function columnNode(span, children) {
  return {kind: 'column', span, children: children.filter(Boolean)};
}

function groupNode(label, id, children) {
  return {kind: 'group', label, id, children: children.filter(Boolean)};
}

function tabNode(label, children) {
  return {kind: 'tab', label, children: children.filter(Boolean)};
}

function tabsForCollections(collections) {
  return {
    kind: 'tabs',
    tabs: collections.map(collection => tabNode(collection.label || humanize(collection.memberId), [collection]))
  };
}

function countSemanticNodes(nodes) {
  let count = 0;
  walkPlan(nodes, node => {
    if (node.kind === 'header' || node.kind === 'member') {
      count += 1;
    }
  });
  return count;
}

function renderNode(node, state) {
  if (node.kind === 'row') {
    return `<div class="causeway-object-row" data-causeway-region="row">${renderChildren(node.children, state)}</div>`;
  }
  if (node.kind === 'column') {
    return `<div class="causeway-object-column" data-causeway-region="column" data-span="${node.span}">${renderChildren(node.children, state)}</div>`;
  }
  if (node.kind === 'group') {
    const id = `${state.idPrefix}-group-${++state.sequence}`;
    return `<section class="causeway-object-group" data-causeway-region="group"${node.id ? ` data-layout-id="${escapeHtml(node.id)}"` : ''} aria-labelledby="${id}"><h2 id="${id}">${escapeHtml(node.label)}</h2>${renderChildren(node.children, state)}</section>`;
  }
  if (node.kind === 'tabs') {
    return renderTabs(node, state);
  }
  if (node.kind === 'header') {
    return '<cw-object-header data-causeway-region="header"></cw-object-header>';
  }
  if (node.kind === 'member') {
    const label = ` label="${escapeHtml(node.label)}"`;
    let memberMarkup;
    if (node.memberKind === 'property') {
      const named = node.presentation?.named ? ` named="${escapeHtml(node.presentation.named)}"` : '';
      const describedAs = node.presentation?.describedAs ? ` described-as="${escapeHtml(node.presentation.describedAs)}"` : '';
      const multiLine = node.presentation?.multiLine ? ` multi-line="${node.presentation.multiLine}"` : '';
      const labelPosition = node.presentation?.labelPosition ? ` label-position="${node.presentation.labelPosition}"` : '';
      memberMarkup = `<cw-property data-causeway-region="property" id="${escapeHtml(node.memberId)}"${named}${describedAs}${multiLine}${labelPosition}${state.editable ? ' editable' : ''}></cw-property>`;
    } else if (node.memberKind === 'action') {
      memberMarkup = `<cw-action data-causeway-region="action" id="${escapeHtml(node.memberId)}"${label}></cw-action>`;
    } else {
      memberMarkup = `<cw-collection data-causeway-region="collection" id="${escapeHtml(node.memberId)}"${label}></cw-collection>`;
    }
    if ((node.children?.length ?? 0) === 0) {
      return memberMarkup;
    }
    return `<section class="causeway-object-member-composition" data-causeway-associated-member="${escapeHtml(node.memberId)}">${memberMarkup}${renderChildren(node.children, state, 'causeway-object-associated-actions')}</section>`;
  }
  return '';
}

function renderChildren(nodes, state, actionGroupClass = 'causeway-object-actions') {
  const markup = [];
  for (let index = 0; index < (nodes?.length ?? 0); index += 1) {
    const node = nodes[index];
    if (node.kind === 'member' && node.memberKind === 'action') {
      const actions = [];
      while (index < nodes.length && nodes[index].kind === 'member' && nodes[index].memberKind === 'action') {
        actions.push(renderNode(nodes[index], state));
        index += 1;
      }
      index -= 1;
      markup.push(`<div class="${actionGroupClass}" data-causeway-action-group>${actions.join('')}</div>`);
    } else {
      markup.push(renderNode(node, state));
    }
  }
  return markup.join('');
}

function renderTabs(node, state) {
  const groupId = `${state.idPrefix}-tabs-${++state.sequence}`;
  const tabs = node.tabs ?? [];
  const buttons = tabs.map((tab, index) => {
    const tabId = `${groupId}-tab-${index}`;
    const panelId = `${groupId}-panel-${index}`;
    return `<button type="button" role="tab" id="${tabId}" aria-controls="${panelId}" aria-selected="${index === 0}" tabindex="${index === 0 ? '0' : '-1'}" data-causeway-tab="${index}">${escapeHtml(tab.label)}</button>`;
  }).join('');
  const panels = tabs.map((tab, index) => {
    const tabId = `${groupId}-tab-${index}`;
    const panelId = `${groupId}-panel-${index}`;
    return `<section role="tabpanel" id="${panelId}" aria-labelledby="${tabId}" data-causeway-tab-panel="${index}"${index === 0 ? '' : ' hidden'}>${renderChildren(tab.children, state)}</section>`;
  }).join('');
  return `<section class="causeway-object-tabs" data-causeway-region="tabs" data-causeway-tab-group="${groupId}"><div role="tablist" aria-label="Object sections">${buttons}</div>${panels}</section>`;
}

function createDiagnosticCollector(maximum) {
  const limit = Number.isSafeInteger(maximum) && maximum > 0 ? maximum : MAX_LAYOUT_DIAGNOSTICS;
  const diagnostics = [];
  const keys = new Set();
  return {
    add(code, message, region = null) {
      const key = `${code}:${message}:${region ?? ''}`;
      if (keys.has(key) || diagnostics.length >= limit) {
        return;
      }
      keys.add(key);
      diagnostics.push(Object.freeze({code, message, region, severity: 'warning'}));
    },
    values() {
      return Object.freeze([...diagnostics]);
    }
  };
}

function walk(node, visit) {
  visit(node);
  for (const child of node.children ?? []) {
    walk(child, visit);
  }
}

function walkPlan(nodes, visit) {
  for (const node of nodes ?? []) {
    visit(node);
    walkPlan(node.children, visit);
    walkPlan(node.tabs, visit);
  }
}

function splitName(qName) {
  const index = qName.indexOf(':');
  return index < 0 ? ['', qName] : [qName.slice(0, index), qName.slice(index + 1)];
}

function localName(qName) {
  return splitName(qName)[1];
}

function safeId(value) {
  const safe = String(value || 'causeway-object').replace(/[^A-Za-z0-9_-]/g, '-');
  return /^[A-Za-z]/.test(safe) ? safe : `causeway-${safe}`;
}

function humanize(value) {
  return String(value || '')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/[-_]+/g, ' ')
    .replace(/^./, character => character.toUpperCase());
}

function deepFreeze(value) {
  if (!value || typeof value !== 'object' || Object.isFrozen(value)) {
    return value;
  }
  for (const child of Array.isArray(value) ? value : Object.values(value)) {
    deepFreeze(child);
  }
  return Object.freeze(value);
}
