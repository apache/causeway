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
    register({id, test, render, priority = 100, standard = false}) {
        if (!id || typeof test !== 'function' || typeof render !== 'function') {
            throw new TypeError('A value renderer requires id, test, and render.');
        }
        const registration = Object.freeze({id, test, render, priority, standard, sequence: ++this.sequence});
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
            ...rendered,
            rendererId: renderer.id,
            standard: renderer.standard,
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
        presentation: state.presentation ?? null,
        namedTypeName: namedType(typeRef),
        typeKind: innermostType(typeRef)?.kind ?? null
    });
}

function registerStandardRenderers(registry) {
    const register = registration => registry.register({...registration, standard: true});
    register({
        id: 'null',
        priority: 40,
        test: state => state.value === null || state.value === undefined,
        render: () => ({
            kind: 'null',
            html: '<span class="causeway-value causeway-value-null" aria-label="No value">—</span>'
        })
    });
    register({
        id: 'object-reference',
        priority: 30,
        test: state => Boolean(state.value?._meta?.logicalTypeName && state.value?._meta?.id),
        render: state => ({kind: 'object-reference', html: objectLinkMarkup(state.value._meta)})
    });
    register({
        id: 'pdf',
        priority: 25,
        test: state => isPdfBlobValue(state.value)
                && normalizePdfRenderMode(state.presentation?.pdfRender) !== 'link',
        render: state => {
            const presentation = normalizePdfPresentation(state.presentation);
            return {
                kind: 'pdf',
                html: pdfMarkup(state.value, presentation),
                pdf: Object.freeze({
                    url: state.value.bytes,
                    name: state.value.name || 'PDF document',
                    mode: presentation.mode,
                    initialPage: presentation.initialPage,
                    zoom: presentation.zoom
                })
            };
        }
    });
    register({
        id: 'blob',
        priority: 20,
        test: state => isLob(state.value, 'bytes'),
        render: state => ({kind: 'blob', html: lobMarkup(state.value, 'bytes', 'Download')})
    });
    register({
        id: 'clob',
        priority: 20,
        test: state => isLob(state.value, 'chars'),
        render: state => ({kind: 'clob', html: lobMarkup(state.value, 'chars', 'Open text')})
    });
    register({
        id: 'enum',
        priority: 10,
        test: state => state.typeKind === 'ENUM',
        render: state => ({
            kind: 'enum',
            html: `<span class="causeway-value causeway-value-enum">${escapeHtml(state.value)}</span>`
        })
    });
    register({
        id: 'scalar',
        priority: 0,
        test: state => state.typeKind === 'SCALAR'
                || ['string', 'number', 'boolean', 'bigint'].includes(typeof state.value),
        render: state => ({
            kind: 'scalar',
            html: `<span class="causeway-value causeway-value-scalar">${escapeHtml(formatScalar(state.value))}</span>`
        })
    });
    register({
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
    const icon = metadata.icon ? ` icon="${escapeHtml(metadata.icon)}"` : '';
    return `<cw-object-link class="causeway-value causeway-value-object-reference" logical-type="${escapeHtml(metadata.logicalTypeName)}" object-id="${escapeHtml(metadata.id)}" title="${escapeHtml(metadata.title ?? metadata.id)}"${icon}></cw-object-link>`;
}

function isLob(value, contentField) {
    return value && typeof value === 'object' && contentField in value && ('name' in value || 'mimeType' in value);
}

export function isPdfBlobValue(value, baseUrl = globalThis.document?.baseURI ?? globalThis.location?.href) {
    if (!isLob(value, 'bytes') || String(value.mimeType ?? '').trim().toLocaleLowerCase() !== 'application/pdf') return false;
    const href = String(value.bytes ?? '').trim();
    if (!href) return false;
    if (!baseUrl) return /^(?:\/|\.\.?\/)/.test(href) && !href.startsWith('//');
    try {
        const base = new URL(baseUrl);
        const resource = new URL(href, base);
        return ['http:', 'https:'].includes(resource.protocol) && resource.origin === base.origin;
    } catch {
        return false;
    }
}

export function normalizePdfRenderMode(value) {
    const normalized = String(value ?? '').trim().toLocaleLowerCase();
    return ['auto', 'manual', 'link'].includes(normalized) ? normalized : 'auto';
}

export function normalizePdfInitialPage(value) {
    const normalized = String(value ?? '').trim();
    if (!/^[1-9]\d{0,5}$/.test(normalized)) return 1;
    const page = Number(normalized);
    return page <= 100000 ? page : 1;
}

export function normalizePdfZoom(value) {
    const normalized = String(value ?? '').trim().toLocaleLowerCase();
    if (['page-width', 'page-fit', 'actual-size'].includes(normalized)) return normalized;
    const match = /^(\d{1,3})%$/.exec(normalized);
    const percentage = match ? Number(match[1]) : 0;
    return percentage >= 25 && percentage <= 400 ? percentage : 'page-width';
}

export function normalizePdfPresentation(presentation = {}) {
    return Object.freeze({
        mode: normalizePdfRenderMode(presentation?.pdfRender),
        initialPage: normalizePdfInitialPage(presentation?.pdfInitialPage),
        zoom: normalizePdfZoom(presentation?.pdfZoom)
    });
}

function pdfMarkup(value, presentation) {
    const name = value.name || 'PDF document';
    const activate = presentation.mode === 'manual'
            ? '<button type="button" class="causeway-pdf-activate" data-causeway-pdf-activate>Preview document</button>'
            : '';
    const initialStatus = presentation.mode === 'manual' ? 'Document preview is ready to start.' : 'Preparing document reader…';
    return `<section class="causeway-pdf-reader" data-causeway-pdf-reader data-causeway-pdf-state="${presentation.mode === 'manual' ? 'inactive' : 'preparing'}" role="region" aria-label="PDF document reader: ${escapeHtml(name)}">
  <div class="causeway-pdf-toolbar" role="toolbar" aria-label="PDF document controls">
    ${activate}
    <button type="button" class="causeway-pdf-control" data-causeway-pdf-previous aria-label="Previous page" disabled>Previous</button>
    <span class="causeway-pdf-status" data-causeway-pdf-status role="status" aria-live="polite">${initialStatus}</span>
    <button type="button" class="causeway-pdf-control" data-causeway-pdf-next aria-label="Next page" disabled>Next</button>
    <button type="button" class="causeway-pdf-control" data-causeway-pdf-zoom-out aria-label="Zoom out" disabled>−</button>
    <span class="causeway-pdf-zoom-status" data-causeway-pdf-zoom-status>${escapeHtml(typeof presentation.zoom === 'number' ? `${presentation.zoom}%` : presentation.zoom.replace('-', ' '))}</span>
    <button type="button" class="causeway-pdf-control" data-causeway-pdf-zoom-in aria-label="Zoom in" disabled>+</button>
  </div>
  <div class="causeway-pdf-viewport" data-causeway-pdf-viewport tabindex="0" aria-label="PDF pages">
    <div class="causeway-pdf-pages" data-causeway-pdf-pages></div>
  </div>
  <p class="causeway-pdf-accessibility-note">Rendered pages are canvas images and do not provide a semantic text alternative.</p>
  ${lobMarkup(value, 'bytes', 'Open PDF document')}
</section>`;
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
