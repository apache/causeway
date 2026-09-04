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

const DEFAULT_ASSET_BASE = resolvePdfAssetBase();
const MAX_DOCUMENT_BYTES = 50 * 1024 * 1024;
const MAX_PAGES = 500;
const MAX_CANVAS_DIMENSION = 8192;
const MAX_IMAGE_PIXELS = 16 * 1024 * 1024;
const MAX_CANVAS_PIXELS = 32 * 1024 * 1024;
const DEVICE_PIXEL_RATIO_LIMIT = 2;
const NEARBY_PAGE_RADIUS = 1;
const ZOOM_STEP = 25;

export function resolvePdfAssetBase(moduleUrl = import.meta.url) {
    return new URL('./pdfjs/', moduleUrl);
}

export class CausewayPdfDocumentReaderController {
    constructor(host, {
        assetBase = DEFAULT_ASSET_BASE,
        moduleLoader = () => import(new URL('pdf.min.mjs', assetBase).href),
        fetcher = (...args) => globalThis.fetch(...args),
        intersectionObserver = globalThis.IntersectionObserver,
        resizeObserver = globalThis.ResizeObserver
    } = {}) {
        this.host = host;
        this.assetBase = new URL(assetBase);
        this.moduleLoader = moduleLoader;
        this.fetcher = fetcher;
        this.IntersectionObserver = intersectionObserver;
        this.ResizeObserver = resizeObserver;
        this.generation = 0;
        this.renderTasks = new Map();
        this.renderedPages = new Set();
        this.visiblePages = new Set();
        this.pageProxies = new Map();
    }

    mount(container, configuration) {
        this.retire();
        if (!container || !configuration) return;
        const generation = this.generation;
        this.container = container;
        this.configuration = Object.freeze({...configuration});
        this.currentPage = configuration.initialPage;
        this.zoom = configuration.zoom;
        this.failed = false;
        this.#bindControls(generation);
        if (configuration.mode === 'auto') void this.#initialize(generation);
    }

    retire() {
        this.generation += 1;
        this.abortController?.abort();
        this.abortController = null;
        clearTimeout(this.resizeTimer);
        this.intersection?.disconnect?.();
        this.resize?.disconnect?.();
        this.intersection = null;
        this.resize = null;
        for (const task of this.renderTasks.values()) task?.cancel?.();
        this.renderTasks.clear();
        this.renderedPages.clear();
        this.visiblePages.clear();
        this.pageProxies.clear();
        const loadingTask = this.loadingTask;
        const documentProxy = this.documentProxy;
        this.loadingTask = null;
        this.documentProxy = null;
        if (loadingTask?.destroy) void Promise.resolve(loadingTask.destroy()).catch(() => {
        });
        else if (documentProxy?.destroy) void Promise.resolve(documentProxy.destroy()).catch(() => {
        });
        if (this.container) {
            const pages = this.container.querySelector?.('[data-causeway-pdf-pages]');
            pages?.replaceChildren?.();
        }
        this.container = null;
        this.configuration = null;
        this.failed = false;
    }

    #current(generation) {
        return generation === this.generation && Boolean(this.container?.isConnected ?? true);
    }

    #bindControls(generation) {
        const activate = this.container.querySelector?.('[data-causeway-pdf-activate]');
        activate?.addEventListener?.('click', () => {
            if (!this.#current(generation) || activate.disabled) return;
            activate.disabled = true;
            void this.#initialize(generation).then(() => {
                if (this.#current(generation)) {
                    const previous = this.container.querySelector?.('[data-causeway-pdf-previous]');
                    const next = this.container.querySelector?.('[data-causeway-pdf-next]');
                    (previous?.disabled ? next : previous)?.focus?.();
                }
            });
        }, {once: true});
        const previous = this.container.querySelector?.('[data-causeway-pdf-previous]');
        const next = this.container.querySelector?.('[data-causeway-pdf-next]');
        previous?.addEventListener?.('click', () => {
            this.#goToPage(this.currentPage - 1, generation);
            if (previous.disabled) next?.focus?.();
        });
        next?.addEventListener?.('click', () => {
            this.#goToPage(this.currentPage + 1, generation);
            if (next.disabled) previous?.focus?.();
        });
        this.container.querySelector?.('[data-causeway-pdf-zoom-out]')?.addEventListener?.('click', () => this.#changeZoom(-ZOOM_STEP, generation));
        this.container.querySelector?.('[data-causeway-pdf-zoom-select]')?.addEventListener?.('change', event => this.#selectZoom(event.target?.value, generation));
        this.container.querySelector?.('[data-causeway-pdf-zoom-in]')?.addEventListener?.('click', () => this.#changeZoom(ZOOM_STEP, generation));
    }

    async #initialize(generation) {
        if (!this.#current(generation) || this.loadingTask || this.documentProxy) return;
        this.#setState('loading', 'Loading document…', generation);
        this.abortController = new AbortController();
        try {
            const pdfjs = await this.moduleLoader();
            if (!this.#current(generation)) return;
            pdfjs.GlobalWorkerOptions.workerSrc = new URL('pdf.worker.min.mjs', this.assetBase).href;
            const response = await this.fetcher(this.configuration.url, {
                credentials: 'same-origin',
                signal: this.abortController.signal,
                headers: {Accept: 'application/pdf'}
            });
            if (!this.#current(generation)) return;
            if (!response.ok) throw new Error(`Document request failed (${response.status}).`);
            const declaredBytes = Number(response.headers?.get?.('content-length'));
            if (Number.isFinite(declaredBytes) && declaredBytes > MAX_DOCUMENT_BYTES) throw new PdfSafetyError('Document exceeds the inline reader size limit.');
            const bytes = await response.arrayBuffer();
            if (!this.#current(generation)) return;
            if (bytes.byteLength > MAX_DOCUMENT_BYTES) throw new PdfSafetyError('Document exceeds the inline reader size limit.');
            const loadingTask = pdfjs.getDocument({
                data: new Uint8Array(bytes),
                cMapUrl: new URL('cmaps/', this.assetBase).href,
                cMapPacked: true,
                standardFontDataUrl: new URL('standard_fonts/', this.assetBase).href,
                iccUrl: new URL('iccs/', this.assetBase).href,
                wasmUrl: new URL('wasm/', this.assetBase).href,
                maxImageSize: MAX_IMAGE_PIXELS,
                useWasm: false,
                isEvalSupported: false,
                enableXfa: false
            });
            this.loadingTask = loadingTask;
            let passwordRequested = false;
            loadingTask.onPassword = () => {
                passwordRequested = true;
                void loadingTask.destroy?.();
            };
            const documentProxy = await loadingTask.promise;
            if (!this.#current(generation)) {
                void documentProxy.destroy?.();
                return;
            }
            if (passwordRequested) throw new PdfPasswordError();
            if (!Number.isSafeInteger(documentProxy.numPages) || documentProxy.numPages < 1 || documentProxy.numPages > MAX_PAGES) {
                throw new PdfSafetyError(`Document exceeds the inline reader page limit of ${MAX_PAGES}.`);
            }
            this.documentProxy = documentProxy;
            this.currentPage = Math.min(this.configuration.initialPage, documentProxy.numPages);
            this.#createPagePlaceholders(documentProxy.numPages, generation);
            this.#observePages(generation);
            this.#setState('ready', `Page ${this.currentPage} of ${documentProxy.numPages}`, generation);
            this.#goToPage(this.currentPage, generation, {scroll: this.currentPage !== 1});
        } catch (error) {
            if (!this.#current(generation) || error?.name === 'AbortError') return;
            const password = error instanceof PdfPasswordError || /password/i.test(String(error?.name ?? ''));
            const status = password ? 'unsupported-password' : error instanceof PdfSafetyError ? 'safety-limit' : 'failed';
            const message = password
                    ? 'Password-protected documents are not supported by the inline reader.'
                    : error instanceof PdfSafetyError ? error.message : 'This document could not be displayed inline.';
            this.failed = true;
            if (this.loadingTask?.destroy) void Promise.resolve(this.loadingTask.destroy()).catch(() => {
            });
            this.loadingTask = null;
            this.documentProxy = null;
            this.#setState(status, message, generation);
            this.host?.dispatchPdfDiagnostic?.({status, message});
        }
    }

    #createPagePlaceholders(pageCount, generation) {
        if (!this.#current(generation)) return;
        const pages = this.container.querySelector?.('[data-causeway-pdf-pages]');
        if (!pages) return;
        pages.replaceChildren?.();
        const ownerDocument = pages.ownerDocument ?? globalThis.document;
        for (let pageNumber = 1; pageNumber <= pageCount; pageNumber += 1) {
            const page = ownerDocument.createElement('section');
            page.className = 'causeway-pdf-page';
            page.dataset.causewayPdfPage = String(pageNumber);
            page.setAttribute('aria-label', `PDF page ${pageNumber} of ${pageCount}`);
            page.setAttribute('tabindex', '-1');
            page.innerHTML = `<span class="causeway-pdf-page-status" role="status">Page ${pageNumber} waiting to render</span>`;
            pages.append?.(page);
        }
    }

    #observePages(generation) {
        const pageElements = [...(this.container.querySelectorAll?.('[data-causeway-pdf-page]') ?? [])];
        if (typeof this.IntersectionObserver === 'function') {
            this.intersection = new this.IntersectionObserver(entries => {
                if (!this.#current(generation)) return;
                for (const entry of entries) {
                    const pageNumber = Number(entry.target?.dataset?.causewayPdfPage);
                    if (!pageNumber) continue;
                    if (entry.isIntersecting) this.visiblePages.add(pageNumber);
                    else this.visiblePages.delete(pageNumber);
                }
                this.#updateCurrentPageFromVisibility(generation);
                this.#scheduleVisiblePages(generation);
            }, {root: this.container.querySelector?.('[data-causeway-pdf-viewport]'), rootMargin: '100% 0px'});
            for (const page of pageElements) this.intersection.observe(page);
        } else {
            this.visiblePages.add(this.currentPage);
            this.#scheduleVisiblePages(generation);
        }
        if (typeof this.ResizeObserver === 'function') {
            this.resize = new this.ResizeObserver(() => {
                clearTimeout(this.resizeTimer);
                this.resizeTimer = setTimeout(() => {
                    if (!this.#current(generation) || !['page-width', 'page-height', 'page-fit'].includes(this.zoom)) return;
                    this.#invalidateRenderedPages();
                    this.#scheduleVisiblePages(generation);
                }, 100);
            });
            this.resize.observe(this.container.querySelector?.('[data-causeway-pdf-viewport]') ?? this.container);
        }
    }

    #updateCurrentPageFromVisibility(generation) {
        if (!this.visiblePages.size) return;
        const next = [...this.visiblePages].sort((left, right) => Math.abs(left - this.currentPage) - Math.abs(right - this.currentPage))[0];
        this.currentPage = next;
        this.#updateControls(generation);
    }

    #scheduleVisiblePages(generation) {
        if (this.failed || !this.documentProxy) return;
        const wanted = new Set();
        for (const visible of this.visiblePages) {
            for (let page = Math.max(1, visible - NEARBY_PAGE_RADIUS); page <= Math.min(this.documentProxy.numPages, visible + NEARBY_PAGE_RADIUS); page += 1) wanted.add(page);
        }
        if (!wanted.size) wanted.add(this.currentPage);
        for (const page of [...wanted].sort((left, right) => Math.abs(left - this.currentPage) - Math.abs(right - this.currentPage))) {
            if (this.renderTasks.size >= 2) break;
            if (!this.renderedPages.has(page) && !this.renderTasks.has(page)) void this.#renderPage(page, generation);
        }
    }

    async #renderPage(pageNumber, generation) {
        const reservation = {
            cancel() {
            }
        };
        this.renderTasks.set(pageNumber, reservation);
        let renderTask;
        try {
            const page = this.pageProxies.get(pageNumber) ?? await this.documentProxy.getPage(pageNumber);
            if (!this.#current(generation)) return;
            this.pageProxies.set(pageNumber, page);
            const placeholder = this.container.querySelector?.(`[data-causeway-pdf-page="${pageNumber}"]`);
            if (!placeholder) return;
            const scale = this.#scaleFor(page, placeholder);
            const viewport = page.getViewport({scale});
            const pixelRatio = Math.min(Number(globalThis.devicePixelRatio) || 1, DEVICE_PIXEL_RATIO_LIMIT);
            const pixelWidth = Math.ceil(viewport.width * pixelRatio);
            const pixelHeight = Math.ceil(viewport.height * pixelRatio);
            if (pixelWidth > MAX_CANVAS_DIMENSION || pixelHeight > MAX_CANVAS_DIMENSION || pixelWidth * pixelHeight > MAX_CANVAS_PIXELS) {
                throw new PdfSafetyError('A document page exceeds the inline reader canvas limit.');
            }
            const canvas = (placeholder.ownerDocument ?? globalThis.document).createElement('canvas');
            canvas.className = 'causeway-pdf-page-canvas';
            canvas.width = pixelWidth;
            canvas.height = pixelHeight;
            canvas.style.width = `${Math.ceil(viewport.width)}px`;
            canvas.style.height = `${Math.ceil(viewport.height)}px`;
            canvas.setAttribute('role', 'img');
            canvas.setAttribute('aria-label', `Rendered PDF page ${pageNumber} of ${this.documentProxy.numPages}; canvas content has no semantic text alternative`);
            const context = canvas.getContext('2d');
            renderTask = page.render({
                canvasContext: context,
                viewport,
                transform: pixelRatio === 1 ? null : [pixelRatio, 0, 0, pixelRatio, 0, 0]
            });
            this.renderTasks.set(pageNumber, renderTask);
            await renderTask.promise;
            if (!this.#current(generation) || this.renderTasks.get(pageNumber) !== renderTask) return;
            placeholder.replaceChildren(canvas);
            this.renderedPages.add(pageNumber);
        } catch (error) {
            if (!this.#current(generation) || error?.name === 'RenderingCancelledException') return;
            const message = error instanceof PdfSafetyError ? error.message : `Page ${pageNumber} could not be displayed.`;
            this.failed = true;
            for (const task of this.renderTasks.values()) if (task !== renderTask) task?.cancel?.();
            this.intersection?.disconnect?.();
            this.resize?.disconnect?.();
            if (this.loadingTask?.destroy) void Promise.resolve(this.loadingTask.destroy()).catch(() => {
            });
            this.loadingTask = null;
            this.documentProxy = null;
            this.container.querySelector?.('[data-causeway-pdf-pages]')?.replaceChildren?.();
            this.#setState(error instanceof PdfSafetyError ? 'safety-limit' : 'failed', message, generation);
            this.host?.dispatchPdfDiagnostic?.({
                status: error instanceof PdfSafetyError ? 'safety-limit' : 'failed',
                message,
                page: pageNumber
            });
        } finally {
            if ([renderTask, reservation].includes(this.renderTasks.get(pageNumber))) this.renderTasks.delete(pageNumber);
            if (this.#current(generation) && !this.failed) this.#scheduleVisiblePages(generation);
        }
    }

    #scaleFor(page, placeholder) {
        if (typeof this.zoom === 'number') return this.zoom / 100;
        if (this.zoom === 'actual-size') return 1;
        const base = page.getViewport({scale: 1});
        const viewport = this.container.querySelector?.('[data-causeway-pdf-viewport]');
        const availableWidth = Math.max(1, (viewport?.clientWidth ?? placeholder?.clientWidth ?? base.width) - 24);
        const availableHeight = Math.max(1, (viewport?.clientHeight ?? base.height) - 24);
        if (this.zoom === 'page-height') return availableHeight / base.height;
        if (this.zoom === 'page-fit') return Math.min(availableWidth / base.width, availableHeight / base.height);
        return availableWidth / base.width;
    }

    #goToPage(pageNumber, generation, {scroll = true} = {}) {
        if (!this.#current(generation) || !this.documentProxy) return;
        const bounded = Math.max(1, Math.min(this.documentProxy.numPages, pageNumber));
        this.currentPage = bounded;
        this.visiblePages.add(bounded);
        const target = this.container.querySelector?.(`[data-causeway-pdf-page="${bounded}"]`);
        if (scroll && target) this.#scrollPageIntoViewport(target);
        this.#updateControls(generation);
        this.#scheduleVisiblePages(generation);
    }

    #scrollPageIntoViewport(target) {
        const viewport = this.container.querySelector?.('[data-causeway-pdf-viewport]');
        if (!viewport) return;
        const viewportTop = viewport.getBoundingClientRect?.().top ?? 0;
        const targetTop = target.getBoundingClientRect?.().top;
        const top = Number.isFinite(targetTop)
                ? (viewport.scrollTop ?? 0) + targetTop - viewportTop - (viewport.clientTop ?? 0)
                : target.offsetTop ?? 0;
        if (typeof viewport.scrollTo === 'function') viewport.scrollTo({top: Math.max(0, top), behavior: 'auto'});
        else viewport.scrollTop = Math.max(0, top);
    }

    #changeZoom(delta, generation) {
        const current = typeof this.zoom === 'number' ? this.zoom : 100;
        this.#applyZoom(Math.max(25, Math.min(400, current + delta)), generation);
    }

    #selectZoom(value, generation) {
        const percentage = /^\d{1,3}$/.test(String(value ?? '')) ? Number(value) : null;
        const zoom = percentage !== null && percentage >= 25 && percentage <= 400
                ? percentage
                : ['page-width', 'page-height', 'page-fit', 'actual-size'].includes(value) ? value : null;
        if (zoom !== null) this.#applyZoom(zoom, generation);
    }

    #applyZoom(zoom, generation) {
        if (!this.#current(generation) || !this.documentProxy || zoom === this.zoom) return;
        this.zoom = zoom;
        this.#invalidateRenderedPages();
        this.#updateControls(generation);
        this.#scheduleVisiblePages(generation);
    }

    #invalidateRenderedPages() {
        for (const task of this.renderTasks.values()) task?.cancel?.();
        this.renderTasks.clear();
        this.renderedPages.clear();
        for (const page of this.container.querySelectorAll?.('[data-causeway-pdf-page]') ?? []) {
            const number = page.dataset.causewayPdfPage;
            page.innerHTML = `<span class="causeway-pdf-page-status" role="status">Page ${number} waiting to render</span>`;
        }
    }

    #setState(status, message, generation) {
        if (!this.#current(generation)) return;
        this.container.dataset.causewayPdfState = status;
        const statusElement = this.container.querySelector?.('[data-causeway-pdf-status]');
        if (statusElement) statusElement.textContent = message;
        this.#updateControls(generation);
        this.host?.dispatchPdfState?.({
            status,
            message,
            page: this.currentPage,
            pageCount: this.documentProxy?.numPages ?? 0,
            zoom: this.zoom
        });
    }

    #updateControls(generation) {
        if (!this.#current(generation)) return;
        const pageCount = this.documentProxy?.numPages ?? 0;
        const previous = this.container.querySelector?.('[data-causeway-pdf-previous]');
        const next = this.container.querySelector?.('[data-causeway-pdf-next]');
        if (previous) previous.disabled = !pageCount || this.currentPage <= 1;
        if (next) next.disabled = !pageCount || this.currentPage >= pageCount;
        const zoomOut = this.container.querySelector?.('[data-causeway-pdf-zoom-out]');
        const zoomIn = this.container.querySelector?.('[data-causeway-pdf-zoom-in]');
        const numericZoom = typeof this.zoom === 'number' ? this.zoom : 100;
        if (zoomOut) zoomOut.disabled = !pageCount || numericZoom <= 25;
        if (zoomIn) zoomIn.disabled = !pageCount || numericZoom >= 400;
        const zoomSelect = this.container.querySelector?.('[data-causeway-pdf-zoom-select]');
        if (zoomSelect) {
            const value = typeof this.zoom === 'number' ? String(this.zoom) : this.zoom;
            for (const option of zoomSelect.querySelectorAll?.('[data-causeway-pdf-dynamic-zoom]') ?? []) option.remove?.();
            const optionValues = [...(zoomSelect.options ?? [])].map(option => option.value);
            if (!optionValues.includes(value)) {
                const option = zoomSelect.ownerDocument?.createElement?.('option');
                if (option) {
                    option.value = value;
                    option.textContent = typeof this.zoom === 'number' ? `${this.zoom}%` : this.zoom.replaceAll('-', ' ');
                    option.setAttribute('data-causeway-pdf-dynamic-zoom', '');
                    zoomSelect.append?.(option);
                }
            }
            zoomSelect.value = value;
            zoomSelect.disabled = !pageCount;
        }
        if (pageCount) {
            const status = this.container.querySelector?.('[data-causeway-pdf-status]');
            if (status && this.container.dataset.causewayPdfState === 'ready') status.textContent = `Page ${this.currentPage} of ${pageCount}`;
        }
    }
}

class PdfPasswordError extends Error {
}

class PdfSafetyError extends Error {
}
