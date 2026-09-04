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
import test from 'node:test';
import {
    CausewayValueRendererRegistry,
    isPdfBlobValue,
    normalizePdfInitialPage,
    normalizePdfRenderMode,
    normalizePdfZoom,
    renderCausewayValue
} from '../src/value-renderers.mjs';

const scalar = {kind: 'SCALAR', name: 'String', ofType: null};
const enumeration = {kind: 'ENUM', name: 'DepartmentStatus', ofType: null};
const object = {kind: 'OBJECT', name: 'rich__university_staff_StaffMember', ofType: null};

function descriptor(typeRef) {
    return {value: {typeRef, typeDescription: null}};
}

test('standard value renderers cover scalars, enums, nulls and object references', () => {
    const scalarResult = renderCausewayValue({value: '<Classics>', descriptor: descriptor(scalar)});
    assert.equal(scalarResult.rendererId, 'scalar');
    assert.match(scalarResult.html, /&lt;Classics&gt;/);

    const enumResult = renderCausewayValue({value: 'ACTIVE', descriptor: descriptor(enumeration)});
    assert.equal(enumResult.rendererId, 'enum');
    assert.match(enumResult.html, /ACTIVE/);

    const nullResult = renderCausewayValue({value: null, descriptor: descriptor(scalar)});
    assert.equal(nullResult.rendererId, 'null');
    assert.match(nullResult.html, /No value/);

    const objectResult = renderCausewayValue({
        value: {
            _meta: {
                id: 'staff-1',
                logicalTypeName: 'university.staff.StaffMember',
                title: 'Dr Ada',
                icon: '/graphql/object/university.staff.StaffMember:staff-1/_meta/icon'
            }
        },
        descriptor: descriptor(object)
    });
    assert.equal(objectResult.rendererId, 'object-reference');
    assert.match(objectResult.html, /cw-object-link/);
    assert.match(objectResult.html, /staff-1/);
    assert.match(objectResult.html, /icon="\/graphql\/object\/university\.staff\.StaffMember:staff-1\/_meta\/icon"/);
});

test('standard value renderers cover PDF, Blob and Clob resource representations', () => {
    const pdf = renderCausewayValue({
        value: {name: 'prospectus.pdf', mimeType: 'application/pdf', bytes: '/blobBytes'}
    });
    assert.equal(pdf.rendererId, 'pdf');
    assert.equal(pdf.pdf.mode, 'auto');
    assert.match(pdf.html, /data-causeway-pdf-reader/);
    assert.match(pdf.html, /role="region" aria-label="PDF document reader/);
    assert.match(pdf.html, /role="toolbar" aria-label="PDF document controls/);
    assert.doesNotMatch(pdf.html, /canvas images and do not provide a semantic text alternative/);
    assert.match(pdf.html, /href="\/blobBytes"/);
    assert.match(pdf.html, /application\/pdf/);
    assert.equal(pdf.html.match(/href="\/blobBytes"/g)?.length, 1);
    assert.ok(pdf.html.indexOf('href="/blobBytes"') < pdf.html.indexOf('data-causeway-pdf-viewport'));

    const blob = renderCausewayValue({
        value: {name: 'image.png', mimeType: 'image\/png', bytes: '/blobBytes'}
    });
    assert.equal(blob.rendererId, 'blob');

    const clob = renderCausewayValue({
        value: {name: 'history.txt', mimeType: 'text\/plain', chars: '/clobChars'}
    });
    assert.equal(clob.rendererId, 'clob');
    assert.match(clob.html, /href="\/clobChars"/);
});

test('PDF qualification and authored options are bounded and link mode retains Blob rendering', () => {
    const value = {name: 'not-authoritative.txt', mimeType: ' APPLICATION/PDF ', bytes: '/blobBytes'};
    assert.equal(isPdfBlobValue(value), true);
    assert.equal(isPdfBlobValue({...value, mimeType: 'application/pdf; charset=binary'}), false);
    assert.equal(isPdfBlobValue({
        ...value,
        bytes: 'https://other.example/document.pdf'
    }, 'https://app.example/objects/1'), false);
    assert.equal(isPdfBlobValue({
        ...value,
        bytes: 'https://app.example/document.pdf'
    }, 'https://app.example/objects/1'), true);
    assert.equal(isPdfBlobValue({...value, bytes: ''}), false);

    assert.equal(normalizePdfRenderMode('MANUAL'), 'manual');
    assert.equal(normalizePdfRenderMode('invalid'), 'auto');
    assert.equal(normalizePdfInitialPage('100000'), 100000);
    assert.equal(normalizePdfInitialPage('0'), 1);
    assert.equal(normalizePdfZoom('275%'), 275);
    assert.equal(normalizePdfZoom('401%'), 'page-width');

    const linked = renderCausewayValue({value, presentation: {pdfRender: 'link'}});
    assert.equal(linked.rendererId, 'blob');
    assert.doesNotMatch(linked.html, /data-causeway-pdf-reader/);
    const manual = renderCausewayValue({
        value,
        presentation: {pdfRender: 'manual', pdfInitialPage: '3', pdfZoom: 'page-fit'}
    });
    assert.equal(manual.rendererId, 'pdf');
    assert.deepEqual(manual.pdf, {
        url: '/blobBytes',
        name: 'not-authoritative.txt',
        mode: 'manual',
        initialPage: 3,
        zoom: 'page-fit'
    });
    assert.match(manual.html, /Preview document/);
});

test('application renderers override standards deterministically and can be released', () => {
    const registry = new CausewayValueRendererRegistry();
    const release = registry.register({
        id: 'department-code',
        test: state => state.descriptor?.id === 'code',
        render: state => ({kind: 'application', html: `<strong>${state.value}</strong>`})
    });
    const state = {value: 'CLA', descriptor: {...descriptor(scalar), id: 'code'}};
    assert.equal(registry.render(state).rendererId, 'department-code');
    const pdfOverride = registry.render({
        value: {name: 'guide.pdf', mimeType: 'application/pdf', bytes: '/guide.pdf'},
        descriptor: {...descriptor(object), id: 'code'}
    });
    assert.equal(pdfOverride.rendererId, 'department-code');
    assert.equal(pdfOverride.pdf, undefined);
    release();
    assert.equal(registry.render(state).rendererId, 'scalar');
});

test('unsupported values expose an introspected diagnostic type', () => {
    const result = renderCausewayValue({
        value: {nested: {value: 'unknown'}},
        descriptor: descriptor({kind: 'OBJECT', name: 'ArbitraryValue', ofType: null})
    });
    assert.equal(result.rendererId, 'unsupported');
    assert.match(result.html, /ArbitraryValue/);
});
