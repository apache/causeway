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

import {namedType} from './introspection.mjs';
import {escapeHtml, formatScalar} from './rendering.mjs';

export class CausewayValueRendererRegistry {
  constructor({standard = true} = {}) {
    this.registrations = [];
    this.sequence = 0;
    if (standard) {
      registerStandardRenderers(this);
    }
  }

  /**
   * Registers an application or library renderer.
   *
   * Higher priorities win, with the most recently registered renderer winning ties.
   * Application registrations default above the standard renderer priority of zero.
   */
  register({id, test, render, priority = 100}) {
    if (!id || typeof test !== 'function' || typeof render !== 'function') {
      throw new TypeError('A value renderer requires id, test, and render.');
    }
    const registration = Object.freeze({id, test, render, priority, sequence: ++this.sequence});
    this.registrations.push(registration);
    this.registrations.sort((left, right) => right.priority - left.priority || right.sequence - left.sequence);
    return () => {
      const index = this.registrations.indexOf(registration);
      if (index >= 0) {
        this.registrations.splice(index, 1);
      }
    };
  }

  resolve(state) {
    return this.registrations.find(registration => registration.test(state)) ?? null;
  }

  render(state) {
    const renderer = this.resolve(state);
    if (!renderer) {
      return unsupportedResult(state);
    }
    const rendered = renderer.render(state);
    return Object.freeze({
      rendererId: renderer.id,
      kind: rendered.kind ?? renderer.id,
      html: String(rendered.html ?? '')
    });
  }
}

export const defaultValueRendererRegistry = new CausewayValueRendererRegistry();

export function renderCausewayValue(state, registry = defaultValueRendererRegistry) {
  return registry.render(normalizeValueState(state));
}

export function normalizeValueState(state = {}) {
  const typeRef = state.typeRef ?? state.descriptor?.value?.typeRef ?? state.descriptor?.fields?.get?.type ?? null;
  const typeDescription = state.typeDescription
    ?? state.descriptor?.value?.typeDescription
    ?? null;
  return Object.freeze({
    value: state.value,
    descriptor: state.descriptor ?? null,
    typeRef,
    typeDescription,
    namedTypeName: namedType(typeRef),
    typeKind: innermostType(typeRef)?.kind ?? null
  });
}

function registerStandardRenderers(registry) {
  registry.register({
    id: 'null',
    priority: 40,
    test: state => state.value === null || state.value === undefined,
    render: () => ({kind: 'null', html: '<span class="causeway-value causeway-value-null" aria-label="No value">—</span>'})
  });
  registry.register({
    id: 'object-reference',
    priority: 30,
    test: state => Boolean(state.value?._meta?.logicalTypeName && state.value?._meta?.id),
    render: state => ({kind: 'object-reference', html: objectLinkMarkup(state.value._meta)})
  });
  registry.register({
    id: 'blob',
    priority: 20,
    test: state => isLob(state.value, 'bytes'),
    render: state => ({kind: 'blob', html: lobMarkup(state.value, 'bytes', 'Download')})
  });
  registry.register({
    id: 'clob',
    priority: 20,
    test: state => isLob(state.value, 'chars'),
    render: state => ({kind: 'clob', html: lobMarkup(state.value, 'chars', 'Open text')})
  });
  registry.register({
    id: 'enum',
    priority: 10,
    test: state => state.typeKind === 'ENUM',
    render: state => ({kind: 'enum', html: `<span class="causeway-value causeway-value-enum">${escapeHtml(state.value)}</span>`})
  });
  registry.register({
    id: 'scalar',
    priority: 0,
    test: state => state.typeKind === 'SCALAR'
      || ['string', 'number', 'boolean', 'bigint'].includes(typeof state.value),
    render: state => ({kind: 'scalar', html: `<span class="causeway-value causeway-value-scalar">${escapeHtml(formatScalar(state.value))}</span>`})
  });
  registry.register({
    id: 'unsupported',
    priority: -1000,
    test: () => true,
    render: state => unsupportedResult(state)
  });
}

function innermostType(typeRef) {
  let current = typeRef;
  while (current?.ofType) {
    current = current.ofType;
  }
  return current;
}

function objectLinkMarkup(metadata) {
  return `<causeway-object-link class="causeway-value causeway-value-object-reference" logical-type="${escapeHtml(metadata.logicalTypeName)}" object-id="${escapeHtml(metadata.id)}" title="${escapeHtml(metadata.title ?? metadata.id)}"></causeway-object-link>`;
}

function isLob(value, contentField) {
  return value && typeof value === 'object' && contentField in value && ('name' in value || 'mimeType' in value);
}

function lobMarkup(value, contentField, fallbackLabel) {
  const label = value.name || fallbackLabel;
  const href = value[contentField];
  const type = value.mimeType ? `<span class="causeway-value-lob-type">${escapeHtml(value.mimeType)}</span>` : '';
  const content = href
    ? `<a class="causeway-value-lob-link" href="${escapeHtml(href)}">${escapeHtml(label)}</a>`
    : `<span class="causeway-value-lob-name">${escapeHtml(label)}</span>`;
  return `<span class="causeway-value causeway-value-lob">${content}${type}</span>`;
}

function unsupportedResult(state) {
  const typeName = state.namedTypeName ?? state.typeDescription?.name ?? typeof state.value;
  return Object.freeze({
    rendererId: 'unsupported',
    kind: 'unsupported',
    html: `<span class="causeway-value causeway-unsupported" role="status">Unsupported value type: ${escapeHtml(typeName)}</span>`
  });
}
