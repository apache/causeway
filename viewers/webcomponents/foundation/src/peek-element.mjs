/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

import {CausewayHostClass} from './component-contracts.mjs';
import {OBJECT_CONTEXT_REQUEST_EVENT} from './context-events.mjs';

const HTMLElementBase = globalThis.HTMLElement ?? class extends EventTarget {};
const MAX_PEEK_TEMPLATE_LENGTH = 65536;
const declarations = new WeakMap();

export function captureDeclarativeCollectionPeeks(root = globalThis.document) {
  if (!root?.querySelectorAll) return;
  const collections = root.localName === 'cw-collection' ? [root] : root.querySelectorAll('cw-collection');
  for (const collection of collections) {
    for (const peek of directPeekChildren(collection)) capturePeekDeclaration(peek);
  }
}

export function collectionPeekDeclaration(collection) {
  const peeks = directPeekChildren(collection);
  for (const peek of peeks) capturePeekDeclaration(peek);
  if (peeks.length !== 1) {
    return Object.freeze({count: peeks.length, presentation: null});
  }
  return Object.freeze({count: 1, presentation: declarations.get(peeks[0]) ?? emptyPresentation()});
}

export function normalizePeekPresentation(value) {
  const html = boundedTemplate(value?.html ?? '');
  if (html == null || !hasMeaningfulMarkup(html)) return null;
  return Object.freeze({html});
}

export class CausewayPeekElement extends HTMLElementBase {
  constructor() {
    super();
    this._context = null;
    this._collapse = null;
    this._live = false;
    this.addEventListener(OBJECT_CONTEXT_REQUEST_EVENT, event => {
      if (!this._live || !this._context || !event.detail?.provide) return;
      event.detail.provide(this._context);
      event.stopPropagation();
    });
    this.addEventListener('keydown', event => {
      if (!this._live || event.key !== 'Escape' || typeof this._collapse !== 'function') return;
      event.preventDefault?.();
      event.stopPropagation?.();
      this._collapse();
    }, true);
  }

  get context() {
    return this._context;
  }

  get live() {
    return this._live;
  }

  configureLive({context, label = 'Object preview', collapse = null} = {}) {
    this._context = context ?? null;
    this._collapse = typeof collapse === 'function' ? collapse : null;
    this._live = true;
    this.hidden = false;
    this.setAttribute('data-causeway-peek-live', '');
    this.setAttribute('role', 'region');
    this.setAttribute('aria-label', String(label || 'Object preview'));
    return this;
  }

  connectedCallback() {
    this.classList?.add?.(CausewayHostClass.PEEK);
    if (this.parentElement?.localName === 'cw-collection' && !this._live) {
      capturePeekDeclaration(this);
      this.hidden = true;
      this.setAttribute('aria-hidden', 'true');
    }
  }

  disconnectedCallback() {
    if (!this._live) return;
    this._context?.disconnect?.();
    this._context = null;
    this._collapse = null;
    this._live = false;
  }
}

function directPeekChildren(collection) {
  return [...(collection?.children ?? collection?.childNodes ?? [])]
    .filter(child => child?.localName === 'cw-peek');
}

function capturePeekDeclaration(peek) {
  if (!peek || declarations.has(peek)) return declarations.get(peek) ?? emptyPresentation();
  const source = String(peek.innerHTML ?? '');
  const inline = hasMeaningfulContent(peek, source);
  const bounded = boundedTemplate(source);
  const presentation = Object.freeze({
    html: bounded ?? '',
    inline,
    ...(inline && bounded == null ? {invalid: true} : {})
  });
  declarations.set(peek, presentation);
  peek.replaceChildren?.();
  peek.hidden = true;
  peek.setAttribute?.('aria-hidden', 'true');
  return presentation;
}

function hasMeaningfulContent(peek, html) {
  if ([...(peek?.children ?? [])].length > 0) return true;
  if (String(peek?.textContent ?? '').trim()) return true;
  return hasMeaningfulMarkup(html);
}

function hasMeaningfulMarkup(html) {
  const withoutComments = String(html ?? '').replace(/<!--[\s\S]*?-->/g, '');
  return /<[^>]+>|\S/.test(withoutComments);
}

function boundedTemplate(value) {
  const text = String(value ?? '');
  const length = typeof TextEncoder === 'function' ? new TextEncoder().encode(text).length : text.length;
  return length <= MAX_PEEK_TEMPLATE_LENGTH ? text : null;
}

function emptyPresentation() {
  return Object.freeze({html: '', inline: false});
}
