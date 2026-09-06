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

import {CausewaySemanticEvent} from './component-contracts.mjs';
import {createSemanticEvent, requestObjectMemberAllocation} from './context-events.mjs';
import {CausewayLayoutContainerElement} from './layout-elements.mjs';
import {escapeHtml} from './rendering.mjs';

export const UNREFERENCED_MEMBER_KINDS = Object.freeze(['property', 'collection', 'action']);
export const UNREFERENCED_OWNED_ATTRIBUTE = 'data-causeway-unreferenced-owned';

const UNREFERENCED_ELEMENT_NAMES = Object.freeze([
  'cw-unreferenced-properties',
  'cw-unreferenced-collections',
  'cw-unreferenced-actions'
]);

export class CausewayUnreferencedMemberCoordinator {
  constructor({boundary, context, schedule = queueMicrotask} = {}) {
    this.boundary = boundary;
    this.context = context;
    this.schedule = schedule;
    this.sinks = new Map();
    this.claimSources = new Map();
    this.references = Object.freeze([]);
    this.inventory = null;
    this.inventoryState = 'idle';
    this.inventoryRevision = 0;
    this.scheduled = false;
    this.closed = false;
    this.releaseReferences = context?.subscribeMemberReferences?.(references => {
      this.references = references ?? Object.freeze([]);
      this.scheduleAllocation();
    }) ?? null;
  }

  registerSink(kind, element, listener) {
    const normalizedKind = normalizeKind(kind);
    if (this.closed) throw new Error('Cannot register an unreferenced destination on a disconnected object context.');
    const token = Symbol(`unreferenced:${normalizedKind}`);
    this.sinks.set(token, {kind: normalizedKind, element, listener});
    this.scheduleAllocation();
    let released = false;
    return () => {
      if (released) return;
      released = true;
      this.sinks.delete(token);
      this.scheduleAllocation();
    };
  }

  registerClaimSource(element, kinds = UNREFERENCED_MEMBER_KINDS) {
    if (this.closed) throw new Error('Cannot register a member claim source on a disconnected object context.');
    const normalizedKinds = new Set(kinds.map(normalizeKind));
    const token = Symbol('member-claim-source');
    const source = {element, kinds: normalizedKinds, state: 'pending', claims: new Set()};
    this.claimSources.set(token, source);
    this.scheduleAllocation();
    let released = false;
    return Object.freeze({
      pending: () => {
        if (released) return;
        source.state = 'pending';
        source.claims.clear();
        this.scheduleAllocation();
      },
      resolve: claims => {
        if (released) return;
        source.state = 'resolved';
        source.claims = normalizeClaims(claims, normalizedKinds);
        this.scheduleAllocation();
      },
      settle: () => {
        if (released) return;
        source.state = 'resolved';
        source.claims.clear();
        this.scheduleAllocation();
      },
      release: () => {
        if (released) return;
        released = true;
        this.claimSources.delete(token);
        this.scheduleAllocation();
      }
    });
  }

  scheduleAllocation() {
    if (this.closed || this.scheduled) return;
    this.scheduled = true;
    const schedule = this.schedule;
    schedule(() => {
      this.scheduled = false;
      if (!this.closed) this.flush();
    });
  }

  flush() {
    const activeSinks = [...this.sinks.values()].filter(sink => sink.element?.isConnected !== false);
    if (activeSinks.length === 0) return;
    this.ensureInventory();
    const order = documentOrder(this.boundary);
    activeSinks.sort((left, right) => (order.get(left.element) ?? Number.MAX_SAFE_INTEGER)
      - (order.get(right.element) ?? Number.MAX_SAFE_INTEGER));
    for (const kind of UNREFERENCED_MEMBER_KINDS) {
      const destinations = activeSinks.filter(sink => sink.kind === kind);
      destinations.forEach((sink, index) => {
        if (index > 0) {
          notifySink(sink, allocationState('error', [], {
            code: 'DUPLICATE_UNREFERENCED_DESTINATION',
            message: `Only the first unreferenced ${kind} destination in an object context is effective.`
          }));
          return;
        }
        if (this.inventoryState === 'error') {
          notifySink(sink, allocationState('error', [], {
            code: 'UNREFERENCED_MEMBER_INVENTORY_UNAVAILABLE',
            message: 'The authoritative object member inventory is unavailable.'
          }));
          return;
        }
        if (this.inventoryState !== 'ready' || this.hasPendingSource(kind)) {
          notifySink(sink, allocationState('loading'));
          return;
        }
        const claims = this.explicitClaims(kind);
        const members = [...this.inventory.values()]
          .filter(member => member?.kind === kind && !claims.has(claimKey(kind, member.id)));
        notifySink(sink, allocationState(members.length > 0 ? 'ready' : 'empty', members));
      });
    }
  }

  ensureInventory() {
    if (this.inventoryState !== 'idle') return;
    const revision = ++this.inventoryRevision;
    this.inventoryState = 'loading';
    Promise.resolve(this.context?.describeObject?.())
      .then(description => {
        if (this.closed || revision !== this.inventoryRevision) return;
        const members = description?.['members'];
        if (!(members instanceof Map)) throw new Error('Object description has no member map.');
        this.inventory = members;
        this.inventoryState = 'ready';
        this.scheduleAllocation();
      })
      .catch(() => {
        if (this.closed || revision !== this.inventoryRevision) return;
        this.inventory = null;
        this.inventoryState = 'error';
        this.scheduleAllocation();
      });
  }

  hasPendingSource(kind) {
    return [...this.claimSources.values()].some(source => source.state === 'pending'
      && source.kinds.has(kind)
      && belongsToBoundary(this.boundary, source.element));
  }

  explicitClaims(kind) {
    const claims = new Set();
    for (const reference of this.references) {
      const requirement = reference?.requirement;
      const consumer = reference?.consumer;
      if (requirement?.kind !== kind
          || !belongsToBoundary(this.boundary, consumer)
          || isInsideUnreferencedDestination(this.boundary, consumer)) continue;
      const member = this.inventory.get(requirement.member);
      if (member?.kind === kind) claims.add(claimKey(kind, member.id));
    }
    for (const source of this.claimSources.values()) {
      if (source.state !== 'resolved' || !belongsToBoundary(this.boundary, source.element)) continue;
      for (const claim of source.claims) {
        if (claim.startsWith(`${kind}:`)) claims.add(claim);
      }
    }
    return claims;
  }

  disconnect() {
    if (this.closed) return;
    this.closed = true;
    this.inventoryRevision += 1;
    this.releaseReferences?.();
    this.releaseReferences = null;
    this.sinks.clear();
    this.claimSources.clear();
    this.references = Object.freeze([]);
    this.inventory = null;
  }
}

class CausewayUnreferencedMemberElement extends CausewayLayoutContainerElement {
  constructor(kind, defaultName) {
    super();
    this.unreferencedKind = kind;
    this.defaultName = defaultName;
    this.releaseSink = null;
    this.currentAllocation = null;
    this.renderedAllocationSignature = null;
  }

  static get observedAttributes() {
    return ['name', 'editable'];
  }

  get name() {
    return boundedName(this.getAttribute('name'), this.defaultName);
  }

  set name(value) {
    this.setAttribute('name', String(value ?? ''));
  }

  connectedCallback() {
    super.connectedCallback();
    this.validateAuthoredChildren();
    const coordinator = requestObjectMemberAllocation(this);
    if (!coordinator) {
      this.acceptAllocation(allocationState('error', [], {
        code: 'UNREFERENCED_MEMBER_CONTEXT_UNAVAILABLE',
        message: 'No usable Causeway object-context allocation boundary is available.'
      }));
      return;
    }
    this.releaseSink = coordinator.registerSink(
      this.unreferencedKind,
      this,
      state => this.acceptAllocation(state)
    );
  }

  disconnectedCallback() {
    this.releaseSink?.();
    this.releaseSink = null;
    this.currentAllocation = null;
    this.renderedAllocationSignature = null;
    super.disconnectedCallback();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (oldValue === newValue || !this.isConnected || !this.currentAllocation) return;
    if (name === 'name' || name === 'editable') {
      this.renderedAllocationSignature = null;
      this.acceptAllocation(this.currentAllocation);
    }
  }

  synchronizeLayout() {
    this.validateAuthoredChildren();
  }

  validateAuthoredChildren() {
    for (const child of [...(this.children ?? [])]) {
      if (child.hasAttribute?.(UNREFERENCED_OWNED_ATTRIBUTE)) continue;
      child.setAttribute?.('data-causeway-layout-invalid', '');
      child.hidden = true;
      this.publishLayoutDiagnostic(
        'UNSUPPORTED_LAYOUT_CHILD',
        `<${this.localName}> does not allow authored direct children.`,
        child.localName || null
      );
    }
  }

  acceptAllocation(state) {
    this.currentAllocation = state;
    const signature = JSON.stringify({
      status: state.status,
      members: state.members.map(member => [member.kind, member.id]),
      diagnostic: state.diagnostic?.code ?? null
    });
    if (signature === this.renderedAllocationSignature) return;
    this.renderedAllocationSignature = signature;
    this.setAttribute('data-causeway-unreferenced-state', state.status);
    this.setAttribute('class', mergeClass(
      this.getAttribute('class'),
      `causeway-unreferenced-members causeway-unreferenced-${pluralKind(this.unreferencedKind)}`
    ));
    if (state.status === 'error') {
      this.innerHTML = '';
      if (state.diagnostic) {
        this.publishLayoutDiagnostic(state.diagnostic.code, state.diagnostic.message);
      }
      this.publishAllocationState(state);
      return;
    }
    if (state.status !== 'ready') {
      this.innerHTML = '';
      this.publishAllocationState(state);
      return;
    }
    this.innerHTML = this.renderMembers(state.members);
    this.publishAllocationState(state);
  }

  publishAllocationState(state) {
    this.dispatchEvent(createSemanticEvent(
      CausewaySemanticEvent.UNREFERENCED_MEMBER_STATE,
      Object.freeze({element: this, kind: this.unreferencedKind, status: state.status}),
      {bubbles: true, composed: true}
    ));
  }

  renderMembers() {
    return '';
  }
}

export class CausewayUnreferencedPropertiesElement extends CausewayUnreferencedMemberElement {
  constructor() {
    super('property', 'Other');
  }

  get editable() {
    return this.hasAttribute('editable');
  }

  set editable(value) {
    if (value) this.setAttribute('editable', '');
    else this.removeAttribute('editable');
  }

  renderMembers(members) {
    const properties = members.map(member => `<cw-property id="${escapeHtml(member.id)}"${this.editable ? ' editable' : ''}></cw-property>`).join('');
    return `<cw-fieldset ${UNREFERENCED_OWNED_ATTRIBUTE} name="${escapeHtml(this.name)}">${properties}</cw-fieldset>`;
  }
}

export class CausewayUnreferencedCollectionsElement extends CausewayUnreferencedMemberElement {
  constructor() {
    super('collection', 'Other collections');
  }

  renderMembers(members) {
    const tabs = members.map((member, index) => {
      const label = memberLabel(member);
      return `<cw-tab name="${escapeHtml(label)}"${index === 0 ? ' selected' : ''}><cw-row><cw-column span="12"><cw-collection id="${escapeHtml(member.id)}" named="${escapeHtml(label)}"></cw-collection></cw-column></cw-row></cw-tab>`;
    }).join('');
    return `<cw-tabgroup ${UNREFERENCED_OWNED_ATTRIBUTE} name="${escapeHtml(this.name)}">${tabs}</cw-tabgroup>`;
  }
}

export class CausewayUnreferencedActionsElement extends CausewayUnreferencedMemberElement {
  constructor() {
    super('action', 'Other actions');
  }

  renderMembers(members) {
    const actions = members.map(member => `<cw-action id="${escapeHtml(member.id)}" named="${escapeHtml(memberLabel(member))}"></cw-action>`).join('');
    return `<div ${UNREFERENCED_OWNED_ATTRIBUTE} class="causeway-object-actions causeway-unreferenced-action-group" role="group" aria-label="${escapeHtml(this.name)}">${actions}</div>`;
  }
}

function normalizeKind(kind) {
  if (!UNREFERENCED_MEMBER_KINDS.includes(kind)) throw new Error(`Unsupported unreferenced member kind '${kind}'.`);
  return kind;
}

function normalizeClaims(claims, allowedKinds) {
  const result = new Set();
  for (const claim of claims ?? []) {
    const kind = claim?.kind;
    const id = typeof claim?.id === 'string' ? claim.id : '';
    if (allowedKinds.has(kind) && id) result.add(claimKey(kind, id));
  }
  return result;
}

function claimKey(kind, id) {
  return `${kind}:${id}`;
}

function allocationState(status, members = [], diagnostic = null) {
  return Object.freeze({
    status,
    members: Object.freeze([...members]),
    diagnostic: diagnostic ? Object.freeze({...diagnostic}) : null
  });
}

function notifySink(sink, state) {
  sink.listener?.(state);
}

function belongsToBoundary(boundary, element) {
  if (!boundary || !element) return false;
  let current = element;
  while (current) {
    if (current === boundary) return true;
    if (current !== element && current.localName === 'cw-object-context') return false;
    current = current.parentNode ?? current.host ?? null;
  }
  return false;
}

function isInsideUnreferencedDestination(boundary, element) {
  let current = element?.parentNode ?? element?.host ?? null;
  while (current && current !== boundary) {
    if (UNREFERENCED_ELEMENT_NAMES.includes(current.localName)) return true;
    current = current.parentNode ?? current.host ?? null;
  }
  return false;
}

function documentOrder(boundary) {
  const order = new Map();
  let sequence = 0;
  const walk = node => {
    for (const child of [...(node?.children ?? [])]) {
      order.set(child, sequence++);
      if (child.localName !== 'cw-object-context') walk(child);
    }
  };
  walk(boundary);
  return order;
}

function pluralKind(kind) {
  return kind === 'property' ? 'properties' : `${kind}s`;
}

function memberLabel(member) {
  return boundedName(member?.label ?? member?.friendlyName ?? member?.id, 'Member');
}

function boundedName(value, fallback) {
  const normalized = String(value ?? '').replace(/[\u0000-\u001f\u007f]/g, ' ').trim().replace(/\s+/g, ' ');
  return (normalized || fallback).slice(0, 160);
}

function mergeClass(existing, required) {
  return [...new Set(`${existing ?? ''} ${required}`.trim().split(/\s+/).filter(Boolean))].join(' ');
}
