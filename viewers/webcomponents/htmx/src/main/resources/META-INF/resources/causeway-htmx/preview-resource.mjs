/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0.
 */

const LOGICAL_TYPE_NAME = /^[A-Za-z_][A-Za-z0-9_$-]*(?:\.[A-Za-z_][A-Za-z0-9_$-]*)*$/;
const MAXIMUM_PREVIEW_BYTES = 65536;
const SUPPORTED_ELEMENTS = new Set([
  'cw-preview', 'cw-object-header', 'cw-property', 'cw-action', 'cw-collection', 'cw-collection-column', 'cw-object-link',
  'cw-menubar', 'cw-menubar-primary', 'cw-menubar-secondary', 'cw-menubar-tertiary',
  'section', 'article', 'header', 'footer', 'main', 'nav', 'div', 'span', 'p', 'ul', 'ol', 'li',
  'dl', 'dt', 'dd', 'h2', 'h3', 'h4', 'h5', 'h6', 'strong', 'em', 'small', 'hr'
]);
const FORBIDDEN_ATTRIBUTES = new Set([
  'href', 'src', 'srcdoc', 'action', 'formaction', 'logical-type', 'object-id'
]);

export function createCausewayPreviewResolver({
  basePath,
  resourcePageMode = 'cached',
  fetchImpl = globalThis.fetch,
  documentRef = globalThis.document,
  parse = html => parseCausewayPreview(html, {documentRef}),
  diagnostic = () => {}
} = {}) {
  const cache = new Map();
  return async function resolveCausewayPreview({logicalTypeName} = {}) {
    const type = String(logicalTypeName ?? '').trim();
    if (!LOGICAL_TYPE_NAME.test(type) || typeof fetchImpl !== 'function') return null;
    if (resourcePageMode !== 'reload' && cache.has(type)) return cache.get(type);
    const pending = fetchImpl(`${basePath}/_previews/${encodeURIComponent(type)}`, {
      headers: {Accept: 'text/html'}
    }).then(async response => {
      if (response.status === 404) return null;
      if (!response.ok) throw new Error(`Preview lookup failed (${response.status}).`);
      const presentation = parse(await response.text());
      diagnostic(null);
      return presentation;
    });
    if (resourcePageMode !== 'reload') cache.set(type, pending);
    try {
      return await pending;
    } catch (error) {
      if (resourcePageMode !== 'reload') cache.delete(type);
      diagnostic('resolution');
      throw error;
    }
  };
}

export function parseCausewayPreview(html, {documentRef = globalThis.document} = {}) {
  const source = String(html ?? '');
  if (new TextEncoder().encode(source).length > MAXIMUM_PREVIEW_BYTES) {
    throw new Error('A preview exceeds the client size limit.');
  }
  if (!documentRef?.createElement) throw new Error('Preview parsing is unavailable.');
  const template = documentRef.createElement('template');
  template.innerHTML = source;
  const roots = [...(template.content?.children ?? [])];
  if (roots.length !== 1 || roots[0].localName !== 'cw-preview') {
    throw new Error('A preview requires one cw-preview root.');
  }
  return validateCausewayPreviewRoot(roots[0]);
}

export function validateCausewayPreviewRoot(root) {
  if (root.localName !== 'cw-preview') throw new Error('A preview requires one cw-preview root.');
  if ([...(root.attributes ?? [])].length > 0) throw new Error('A preview root cannot declare attributes.');
  const elements = [root, ...(root.querySelectorAll?.('*') ?? [])];
  if (elements.some(element => !SUPPORTED_ELEMENTS.has(element.localName)
      || [...(element.attributes ?? [])].some(attribute => /^on/i.test(attribute.name)
        || FORBIDDEN_ATTRIBUTES.has(attribute.name)))) {
    throw new Error('A preview contains unsupported markup.');
  }
  return Object.freeze({html: String(root.innerHTML ?? '')});
}
