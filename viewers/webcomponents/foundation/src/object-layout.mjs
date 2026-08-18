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

export const CAUSEWAY_COMPONENT_NAMESPACE = 'https://causeway.apache.org/applib/layout/component';
export const CAUSEWAY_GRID_NAMESPACE = 'https://causeway.apache.org/applib/layout/grid/bootstrap3';
export const MAX_GRID_XML_CHARACTERS = 1_048_576;
export const MAX_GRID_XML_DEPTH = 64;
export const MAX_GRID_XML_NODES = 4_096;
export const MAX_LAYOUT_DIAGNOSTICS = 20;

const MEMBER_KINDS = Object.freeze(['action', 'property', 'collection']);
const STRUCTURAL_KINDS = Object.freeze(['grid', 'row', 'col', 'tabGroup', 'tab', 'fieldSet', 'domainObject']);
const PRESENTATION_KINDS = Object.freeze(['named', 'describedAs']);
const XML_ENTITIES = Object.freeze({amp: '&', lt: '<', gt: '>', quot: '"', apos: "'"});

export class CausewayGridError extends Error {
  constructor(code, message) {
    super(message);
    this.name = 'CausewayGridError';
    this.code = code;
  }
}

export function parseCausewayGridXml(xml, {members = new Map(), maxDiagnostics = MAX_LAYOUT_DIAGNOSTICS} = {}) {
  const diagnostics = createDiagnosticCollector(maxDiagnostics);
  const root = parseSafeXml(xml);
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

function parseSafeXml(value) {
  if (typeof value !== 'string' || value.trim().length === 0) {
    throw new CausewayGridError('GRID_XML_EMPTY', 'The effective grid resource is empty.');
  }
  if (value.length > MAX_GRID_XML_CHARACTERS) {
    throw new CausewayGridError('GRID_XML_TOO_LARGE', `The effective grid resource exceeds ${MAX_GRID_XML_CHARACTERS} characters.`);
  }
  if (/<!\s*(?:DOCTYPE|ENTITY)\b/i.test(value)) {
    throw new CausewayGridError('GRID_XML_DECLARATION_FORBIDDEN', 'Document type and entity declarations are not supported.');
  }
  if (/<!\s*\[CDATA\[/i.test(value)) {
    throw new CausewayGridError('GRID_XML_CDATA_FORBIDDEN', 'CDATA sections are not supported in effective grid resources.');
  }
  let xml = value.replace(/^\uFEFF/, '');
  xml = xml.replace(/^\s*<\?xml\s+[\s\S]*?\?>/i, '');
  if (/<\?/.test(xml)) {
    throw new CausewayGridError('GRID_XML_PROCESSING_INSTRUCTION_FORBIDDEN', 'Processing instructions are not supported.');
  }
  xml = xml.replace(/<!--[\s\S]*?-->/g, '');
  if (xml.includes('<!--') || xml.includes('-->')) {
    throw new CausewayGridError('GRID_XML_MALFORMED_COMMENT', 'The effective grid resource contains a malformed comment.');
  }
  const tokens = xml.match(/<[^>]*>|[^<]+/g) ?? [];
  if (tokens.join('') !== xml) {
    throw new CausewayGridError('GRID_XML_MALFORMED', 'The effective grid resource contains malformed markup.');
  }
  const documentNode = {qName: '#document', children: [], text: '', namespaces: new Map()};
  const stack = [documentNode];
  let nodeCount = 0;
  for (const token of tokens) {
    if (!token.startsWith('<')) {
      const text = decodeXmlEntities(token);
      if (text.trim() && stack.length === 1) {
        throw new CausewayGridError('GRID_XML_TEXT_OUTSIDE_ROOT', 'Text is not permitted outside the effective grid root.');
      }
      stack.at(-1).text += text;
      continue;
    }
    if (/^<\//.test(token)) {
      const close = token.match(/^<\/\s*([A-Za-z_][\w.-]*(?::[A-Za-z_][\w.-]*)?)\s*>$/);
      if (!close || stack.length === 1 || stack.at(-1).qName !== close[1]) {
        throw new CausewayGridError('GRID_XML_MISMATCHED_ELEMENT', 'The effective grid resource contains mismatched elements.');
      }
      stack.pop();
      continue;
    }
    if (/^<!/.test(token)) {
      throw new CausewayGridError('GRID_XML_DECLARATION_FORBIDDEN', 'XML declarations beyond comments are not supported.');
    }
    const open = token.match(/^<\s*([A-Za-z_][\w.-]*(?::[A-Za-z_][\w.-]*)?)([\s\S]*?)(\/?)>$/);
    if (!open) {
      throw new CausewayGridError('GRID_XML_MALFORMED_ELEMENT', 'The effective grid resource contains a malformed element.');
    }
    const attributes = parseAttributes(open[2]);
    const namespaces = new Map(stack.at(-1).namespaces);
    for (const [name, attributeValue] of attributes) {
      if (name === 'xmlns') {
        namespaces.set('', attributeValue);
      } else if (name.startsWith('xmlns:')) {
        namespaces.set(name.slice('xmlns:'.length), attributeValue);
      }
      if (/^on/i.test(localName(name))) {
        throw new CausewayGridError('GRID_XML_EXECUTABLE_ATTRIBUTE', 'Executable event attributes are not supported.');
      }
    }
    const [prefix = '', name = ''] = splitName(open[1]);
    if (['script', 'style'].includes(name.toLowerCase())) {
      throw new CausewayGridError('GRID_XML_EXECUTABLE_ELEMENT', 'Executable or styling elements are not supported.');
    }
    nodeCount += 1;
    if (nodeCount > MAX_GRID_XML_NODES) {
      throw new CausewayGridError('GRID_XML_TOO_MANY_NODES', `The effective grid resource exceeds ${MAX_GRID_XML_NODES} elements.`);
    }
    if (stack.length > MAX_GRID_XML_DEPTH) {
      throw new CausewayGridError('GRID_XML_TOO_DEEP', `The effective grid resource exceeds ${MAX_GRID_XML_DEPTH} nested elements.`);
    }
    const node = {
      qName: open[1],
      prefix,
      localName: name,
      namespaceURI: namespaces.get(prefix) ?? null,
      attributes,
      namespaces,
      children: [],
      text: ''
    };
    stack.at(-1).children.push(node);
    if (open[3] !== '/') {
      stack.push(node);
    }
  }
  if (stack.length !== 1 || documentNode.children.length !== 1) {
    throw new CausewayGridError('GRID_XML_MALFORMED', 'The effective grid resource must contain exactly one complete root element.');
  }
  return documentNode.children[0];
}

function parseAttributes(source) {
  const result = new Map();
  let rest = source;
  while (rest.trim().length > 0) {
    const match = rest.match(/^\s+([A-Za-z_][\w.:-]*)\s*=\s*("([^"]*)"|'([^']*)')/);
    if (!match) {
      throw new CausewayGridError('GRID_XML_MALFORMED_ATTRIBUTE', 'The effective grid resource contains a malformed attribute.');
    }
    if (result.has(match[1])) {
      throw new CausewayGridError('GRID_XML_DUPLICATE_ATTRIBUTE', 'The effective grid resource contains a duplicate attribute.');
    }
    result.set(match[1], decodeXmlEntities(match[3] ?? match[4] ?? ''));
    rest = rest.slice(match[0].length);
  }
  return result;
}

function decodeXmlEntities(value) {
  const entityPattern = /&(#x[0-9a-f]+|#\d+|[A-Za-z][\w.-]*);/gi;
  if (value.replace(entityPattern, '').includes('&')) {
    throw new CausewayGridError('GRID_XML_MALFORMED_ENTITY', 'The effective grid resource contains a malformed entity reference.');
  }
  return value.replace(entityPattern, (match, entity) => {
    if (entity.toLowerCase().startsWith('#x')) {
      return validCodePoint(Number.parseInt(entity.slice(2), 16));
    }
    if (entity.startsWith('#')) {
      return validCodePoint(Number.parseInt(entity.slice(1), 10));
    }
    if (Object.prototype.hasOwnProperty.call(XML_ENTITIES, entity)) {
      return XML_ENTITIES[entity];
    }
    throw new CausewayGridError('GRID_XML_UNKNOWN_ENTITY', 'The effective grid resource contains an unknown entity reference.');
  });
}

function validCodePoint(codePoint) {
  if (!Number.isSafeInteger(codePoint)
      || codePoint <= 0
      || codePoint > 0x10FFFF
      || codePoint >= 0xD800 && codePoint <= 0xDFFF) {
    throw new CausewayGridError('GRID_XML_INVALID_CHARACTER', 'The effective grid resource contains an invalid character reference.');
  }
  return String.fromCodePoint(codePoint);
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
    node.claim = memberNode(member, labelFrom(node));
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
    const result = [];
    if (node.claim && !context.allocated.has(node.claim.memberId)) {
      context.allocated.add(node.claim.memberId);
      result.push(node.claim);
    }
    result.push(...buildChildren(node, context));
    return result;
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
    return [
      'id', 'position', 'defaultView', 'paged', 'hidden',
      'labelPosition', 'dateRenderAdjustDays', 'typicalLength'
    ];
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

function memberNode(member, label = '') {
  return {
    kind: 'member',
    memberKind: member.kind,
    memberId: member.id,
    label: label || humanize(member.id)
  };
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
    return `<div class="causeway-object-row" data-causeway-region="row">${node.children.map(child => renderNode(child, state)).join('')}</div>`;
  }
  if (node.kind === 'column') {
    return `<div class="causeway-object-column" data-causeway-region="column" data-span="${node.span}" style="--causeway-column-span:${node.span}">${node.children.map(child => renderNode(child, state)).join('')}</div>`;
  }
  if (node.kind === 'group') {
    const id = `${state.idPrefix}-group-${++state.sequence}`;
    return `<section class="causeway-object-group" data-causeway-region="group"${node.id ? ` data-layout-id="${escapeHtml(node.id)}"` : ''} aria-labelledby="${id}"><h2 id="${id}">${escapeHtml(node.label)}</h2>${node.children.map(child => renderNode(child, state)).join('')}</section>`;
  }
  if (node.kind === 'tabs') {
    return renderTabs(node, state);
  }
  if (node.kind === 'header') {
    return '<causeway-object-header data-causeway-region="header"></causeway-object-header>';
  }
  if (node.kind === 'member') {
    const label = ` label="${escapeHtml(node.label)}"`;
    if (node.memberKind === 'property') {
      return `<causeway-property data-causeway-region="property" member="${escapeHtml(node.memberId)}"${label}${state.editable ? ' editable' : ''}></causeway-property>`;
    }
    if (node.memberKind === 'action') {
      return `<causeway-action data-causeway-region="action" member="${escapeHtml(node.memberId)}"${label}></causeway-action>`;
    }
    return `<causeway-collection data-causeway-region="collection" member="${escapeHtml(node.memberId)}"${label}></causeway-collection>`;
  }
  return '';
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
    return `<section role="tabpanel" id="${panelId}" aria-labelledby="${tabId}" data-causeway-tab-panel="${index}"${index === 0 ? '' : ' hidden'}>${tab.children.map(child => renderNode(child, state)).join('')}</section>`;
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
