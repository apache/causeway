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
    CausewayPdfDocumentReaderController,
    resolvePdfAssetBase
} from '../src/pdf-document-reader.mjs';

class FakeControl extends EventTarget {
    constructor() {
        super();
        this.disabled = false;
        this.textContent = '';
    }

    click() {
        this.focus();
        this.dispatchEvent(new Event('click'));
    }

    focus() {
        if (FakeControl.active) FakeControl.active.focused = false;
        FakeControl.active = this;
        this.focused = true;
    }
}

class FakePage {
    constructor() {
        this.dataset = {};
        this.style = {};
        this.children = [];
        this.isConnected = true;
    }

    setAttribute(name, value) {
        this[name] = String(value);
    }

    replaceChildren(...children) {
        this.children = children;
        this.innerHTML = '';
    }

    scrollIntoView() {
        this.scrolled = true;
        this.outerScroller.scrollTop = 0;
    }
}

function fixture({pageCount = 5, password = false, deferredModule = null, declaredBytes = null} = {}) {
    const outerScroller = {scrollTop: 421};
    FakePage.prototype.outerScroller = outerScroller;
    const controls = Object.fromEntries([
        'activate', 'previous', 'next', 'zoom-out', 'zoom-in', 'status', 'zoom-status', 'viewport'
    ].map(name => [name, new FakeControl()]));
    controls.viewport.clientWidth = 636;
    controls.viewport.clientHeight = 816;
    controls.viewport.clientTop = 1;
    controls.viewport.scrollTop = 0;
    controls.viewport.getBoundingClientRect = () => ({top: 100});
    controls.viewport.scrollTo = options => {
        controls.viewport.scrollCalls ??= [];
        controls.viewport.scrollCalls.push(options);
        controls.viewport.scrollTop = options.top;
    };
    const pages = new FakePage();
    pages.ownerDocument = {
        createElement(name) {
            const element = new FakePage();
            element.localName = name;
            element.ownerDocument = pages.ownerDocument;
            element.getBoundingClientRect = () => ({
                top: 101 + (Number(element.dataset.causewayPdfPage || 1) - 1) * 800 - controls.viewport.scrollTop
            });
            if (name === 'canvas') element.getContext = () => ({});
            return element;
        }
    };
    pages.append = page => pages.children.push(page);
    const container = {
        dataset: {},
        isConnected: true,
        querySelector(selector) {
            const controlMatch = /data-causeway-pdf-([^=\]]+)/.exec(selector);
            if (selector === '[data-causeway-pdf-pages]') return pages;
            const pageMatch = /data-causeway-pdf-page="(\d+)"/.exec(selector);
            if (pageMatch) return pages.children.find(page => page.dataset.causewayPdfPage === pageMatch[1]);
            return controls[controlMatch?.[1]] ?? null;
        },
        querySelectorAll(selector) {
            return selector === '[data-causeway-pdf-page]' ? pages.children : [];
        }
    };
    const rendered = [];
    const documentProxy = {
        numPages: pageCount,
        async getPage(number) {
            return {
                getViewport({scale}) {
                    return {width: 612 * scale, height: 792 * scale};
                },
                render() {
                    rendered.push(number);
                    return {
                        promise: Promise.resolve(), cancel() {
                        }
                    };
                }
            };
        },
        destroy() {
        }
    };
    let moduleLoads = 0;
    let fetches = 0;
    let documentOptions = null;
    const moduleValue = {
        GlobalWorkerOptions: {},
        getDocument(options) {
            documentOptions = options;
            let rejectPromise;
            const task = {
                promise: password
                        ? new Promise((resolve, reject) => {
                            rejectPromise = reject;
                        })
                        : Promise.resolve(documentProxy),
                destroy() {
                }
            };
            if (password) queueMicrotask(() => {
                task.onPassword?.(() => {
                }, 1);
                const error = new Error('password required');
                error.name = 'PasswordException';
                rejectPromise(error);
            });
            return task;
        }
    };
    const host = {
        states: [],
        diagnostics: [],
        dispatchPdfState(detail) {
            this.states.push(detail);
        },
        dispatchPdfDiagnostic(detail) {
            this.diagnostics.push(detail);
        }
    };
    const controller = new CausewayPdfDocumentReaderController(host, {
        moduleLoader: async () => {
            moduleLoads += 1;
            return deferredModule ? deferredModule.promise : moduleValue;
        },
        fetcher: async () => {
            fetches += 1;
            return {
                ok: true,
                status: 200,
                headers: {get: name => name === 'content-length' ? declaredBytes : null},
                arrayBuffer: async () => new ArrayBuffer(16)
            };
        },
        intersectionObserver: null,
        resizeObserver: null
    });
    return {
        controller,
        container,
        controls,
        pages,
        rendered,
        host,
        moduleValue,
        counts: () => ({moduleLoads, fetches}),
        documentOptions: () => documentOptions,
        outerScroller
    };
}

async function settle() {
    await new Promise(resolve => setTimeout(resolve, 10));
}

test('manual mode loads neither PDF.js nor bytes until accessible activation', async () => {
    const value = fixture({pageCount: 3});
    value.controller.mount(value.container, {
        url: '/document.pdf',
        name: 'Document',
        mode: 'manual',
        initialPage: 2,
        zoom: 'page-fit'
    });
    assert.deepEqual(value.counts(), {moduleLoads: 0, fetches: 0});
    assert.equal(value.container.dataset.causewayPdfState, undefined);

    value.controls.activate.click();
    await settle();
    assert.deepEqual(value.counts(), {moduleLoads: 1, fetches: 1});
    assert.equal(value.container.dataset.causewayPdfState, 'ready');
    assert.equal(value.pages.children.length, 3);
    assert.match(value.controls.status.textContent, /Page 2 of 3/);
    assert.equal(value.controls.previous.disabled, false);
    assert.equal(value.controls.next.disabled, false);
    assert.deepEqual([...new Set(value.rendered)].sort(), [1, 2, 3]);
    assert.equal(value.controls.previous.focused, true);
    assert.equal(value.controls.viewport.scrollTop, 800);
    assert.equal(value.outerScroller.scrollTop, 421);
    assert.ok(value.pages.children.every(page => !page.scrolled));
});

test('manual activation at the first page focuses the available next control', async () => {
    const value = fixture({pageCount: 3});
    value.controller.mount(value.container, {
        url: '/document.pdf',
        name: 'Document',
        mode: 'manual',
        initialPage: 1,
        zoom: 'page-fit'
    });

    value.controls.activate.click();
    await settle();

    assert.equal(value.controls.previous.disabled, true);
    assert.equal(value.controls.next.focused, true);
});

test('same-origin PDF assets retain an application context path', () => {
    assert.equal(
            resolvePdfAssetBase('https://example.test/app/causeway-webcomponents/pdf-document-reader.mjs').href,
            'https://example.test/app/causeway-webcomponents/pdfjs/'
    );
});

test('automatic mode creates every placeholder but initially renders only current and nearby pages', async () => {
    const value = fixture({pageCount: 8});
    value.controller.mount(value.container, {
        url: '/document.pdf',
        name: 'Document',
        mode: 'auto',
        initialPage: 1,
        zoom: 'page-width'
    });
    await settle();
    assert.equal(value.pages.children.length, 8);
    assert.deepEqual([...new Set(value.rendered)].sort(), [1, 2]);
    assert.match(value.pages.children[0].children[0]['aria-label'], /Rendered PDF page 1 of 8/);
    assert.equal(value.documentOptions().maxImageSize, 16 * 1024 * 1024);
    assert.equal(value.documentOptions().useWasm, false);
    assert.equal(value.documentOptions().isEvalSupported, false);
    assert.equal(value.documentOptions().enableXfa, false);
    assert.match(value.documentOptions().cMapUrl, /pdfjs\/cmaps\/$/);
    assert.match(value.documentOptions().standardFontDataUrl, /pdfjs\/standard_fonts\/$/);
    assert.match(value.documentOptions().iccUrl, /pdfjs\/iccs\/$/);
    assert.match(value.documentOptions().wasmUrl, /pdfjs\/wasm\/$/);

    assert.equal(value.controls.previous.disabled, true);
    value.controls.next.click();
    await settle();
    assert.equal(value.controls.previous.disabled, false);
    assert.match(value.controls.status.textContent, /Page 2 of 8/);
    assert.equal(value.controls.viewport.scrollTop, 800);
    value.controls.previous.click();
    await settle();
    assert.match(value.controls.status.textContent, /Page 1 of 8/);
    assert.equal(value.controls.viewport.scrollTop, 0);
    assert.equal(value.controls.next.focused, true);
    for (let page = 2; page <= 8; page += 1) value.controls.next.click();
    await settle();
    assert.match(value.controls.status.textContent, /Page 8 of 8/);
    assert.equal(value.controls.next.disabled, true);
    assert.equal(value.controls.previous.focused, true);
    assert.equal(value.controls.viewport.scrollTop, 5600);
    assert.equal(value.outerScroller.scrollTop, 421);
    assert.ok(value.pages.children.every(page => !page.scrolled));
    assert.ok(value.controls.viewport.scrollCalls.every(call => call.behavior === 'auto'));
    value.controls['zoom-in'].click();
    await settle();
    assert.equal(value.controls['zoom-status'].textContent, '125%');
});

test('documents beyond the fixed byte limit fail before PDF.js receives content', async () => {
    const value = fixture({declaredBytes: String(50 * 1024 * 1024 + 1)});
    value.controller.mount(value.container, {
        url: '/large.pdf',
        name: 'Large',
        mode: 'auto',
        initialPage: 1,
        zoom: 'page-width'
    });
    await settle();
    assert.equal(value.container.dataset.causewayPdfState, 'safety-limit');
    assert.match(value.controls.status.textContent, /size limit/);
    assert.equal(value.documentOptions(), null);
});

test('documents beyond the fixed page limit fail before placeholders or canvases are allocated', async () => {
    const value = fixture({pageCount: 501});
    value.controller.mount(value.container, {
        url: '/large.pdf',
        name: 'Large',
        mode: 'auto',
        initialPage: 1,
        zoom: 'page-width'
    });
    await settle();
    assert.equal(value.container.dataset.causewayPdfState, 'safety-limit');
    assert.match(value.controls.status.textContent, /page limit of 500/);
    assert.equal(value.pages.children.length, 0);
});

test('password requests fail locally without a credential callback or stale pages', async () => {
    const value = fixture({password: true});
    value.controller.mount(value.container, {
        url: '/protected.pdf',
        name: 'Protected',
        mode: 'auto',
        initialPage: 1,
        zoom: 100
    });
    await settle();
    assert.equal(value.container.dataset.causewayPdfState, 'unsupported-password');
    assert.match(value.controls.status.textContent, /Password-protected/);
    assert.equal(value.pages.children.length, 0);
    assert.equal(value.host.diagnostics[0].status, 'unsupported-password');
});

test('retiring a reader makes a late module continuation obsolete', async () => {
    let resolveModule;
    const deferredModule = {
        promise: new Promise(resolve => {
            resolveModule = resolve;
        })
    };
    const value = fixture({deferredModule});
    value.controller.mount(value.container, {
        url: '/old.pdf',
        name: 'Old',
        mode: 'auto',
        initialPage: 1,
        zoom: 'actual-size'
    });
    assert.deepEqual(value.counts(), {moduleLoads: 1, fetches: 0});
    value.controller.retire();
    resolveModule(value.moduleValue);
    await settle();
    assert.deepEqual(value.counts(), {moduleLoads: 1, fetches: 0});
    assert.equal(value.pages.children.length, 0);
});
