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

export const MAX_STRUCTURAL_XML_CHARACTERS = 1_048_576;
export const MAX_STRUCTURAL_XML_DEPTH = 64;
export const MAX_STRUCTURAL_XML_NODES = 4_096;
export const MAX_STRUCTURAL_DIAGNOSTICS = 20;

const XML_ENTITIES = Object.freeze({amp: '&', lt: '<', gt: '>', quot: '"', apos: "'"});

export class StructuralXmlError extends Error {
  constructor(code, message) {
    super(message);
    this.name = 'StructuralXmlError';
    this.code = code;
  }
}

export function parseStructuralXml(value, {
  codePrefix = 'STRUCTURAL',
  resourceLabel = 'structural resource',
  ErrorType = StructuralXmlError,
  maximumCharacters = MAX_STRUCTURAL_XML_CHARACTERS,
  maximumDepth = MAX_STRUCTURAL_XML_DEPTH,
  maximumNodes = MAX_STRUCTURAL_XML_NODES
} = {}) {
  const fail = (suffix, message) => {
    throw new ErrorType(`${codePrefix}_XML_${suffix}`, message);
  };
  const label = `The ${resourceLabel}`;
  if (typeof value !== 'string' || value.trim().length === 0) {
    fail('EMPTY', `${label} is empty.`);
  }
  if (value.length > maximumCharacters) {
    fail('TOO_LARGE', `${label} exceeds ${maximumCharacters} characters.`);
  }
  if (/[\u0000-\u0008\u000B\u000C\u000E-\u001F]/.test(value)) {
    fail('INVALID_CHARACTER', `${label} contains an invalid XML character.`);
  }
  if (/<!\s*(?:DOCTYPE|ENTITY)\b/i.test(value)) {
    fail('DECLARATION_FORBIDDEN', 'Document type and entity declarations are not supported.');
  }
  if (/<!\s*\[CDATA\[/i.test(value)) {
    fail('CDATA_FORBIDDEN', `CDATA sections are not supported in ${resourceLabel}s.`);
  }
  let xml = value.replace(/^\uFEFF/, '');
  xml = xml.replace(/^\s*<\?xml\s+[\s\S]*?\?>/i, '');
  if (/<\?/.test(xml)) {
    fail('PROCESSING_INSTRUCTION_FORBIDDEN', 'Processing instructions are not supported.');
  }
  for (const comment of xml.matchAll(/<!--([\s\S]*?)-->/g)) {
    if (comment[1].includes('--')) {
      fail('MALFORMED_COMMENT', `${label} contains a malformed comment.`);
    }
  }
  xml = xml.replace(/<!--[\s\S]*?-->/g, '');
  if (xml.includes('<!--') || xml.includes('-->')) {
    fail('MALFORMED_COMMENT', `${label} contains a malformed comment.`);
  }
  const tokens = xml.match(/<[^>]*>|[^<]+/g) ?? [];
  if (tokens.join('') !== xml) {
    fail('MALFORMED', `${label} contains malformed markup.`);
  }
  const documentNode = {qName: '#document', children: [], text: '', namespaces: new Map()};
  const stack = [documentNode];
  let nodeCount = 0;
  for (const token of tokens) {
    if (!token.startsWith('<')) {
      const text = decodeXmlEntities(token, fail, resourceLabel);
      if (text.trim() && stack.length === 1) {
        fail('TEXT_OUTSIDE_ROOT', `Text is not permitted outside the ${resourceLabel} root.`);
      }
      stack.at(-1).text += text;
      continue;
    }
    if (/^<\//.test(token)) {
      const close = token.match(/^<\/\s*([A-Za-z_][\w.-]*(?::[A-Za-z_][\w.-]*)?)\s*>$/);
      if (!close || stack.length === 1 || stack.at(-1).qName !== close[1]) {
        fail('MISMATCHED_ELEMENT', `${label} contains mismatched elements.`);
      }
      stack.pop();
      continue;
    }
    if (/^<!/.test(token)) {
      fail('DECLARATION_FORBIDDEN', 'XML declarations beyond comments are not supported.');
    }
    const open = token.match(/^<\s*([A-Za-z_][\w.-]*(?::[A-Za-z_][\w.-]*)?)([\s\S]*?)(\/?)>$/);
    if (!open) {
      fail('MALFORMED_ELEMENT', `${label} contains a malformed element.`);
    }
    const attributes = parseAttributes(open[2], fail, resourceLabel);
    const namespaces = new Map(stack.at(-1).namespaces);
    for (const [name, attributeValue] of attributes) {
      if (name === 'xmlns') {
        namespaces.set('', attributeValue);
      } else if (name.startsWith('xmlns:')) {
        namespaces.set(name.slice('xmlns:'.length), attributeValue);
      }
      if (/^on/i.test(structuralLocalName(name))) {
        fail('EXECUTABLE_ATTRIBUTE', 'Executable event attributes are not supported.');
      }
    }
    const [prefix = '', name = ''] = splitStructuralName(open[1]);
    if (['script', 'style'].includes(name.toLowerCase())) {
      fail('EXECUTABLE_ELEMENT', 'Executable or styling elements are not supported.');
    }
    nodeCount += 1;
    if (nodeCount > maximumNodes) {
      fail('TOO_MANY_NODES', `${label} exceeds ${maximumNodes} elements.`);
    }
    if (stack.length > maximumDepth) {
      fail('TOO_DEEP', `${label} exceeds ${maximumDepth} nested elements.`);
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
    fail('MALFORMED', `${label} must contain exactly one complete root element.`);
  }
  return documentNode.children[0];
}

export function walkStructuralXml(node, visit) {
  visit(node);
  for (const child of node?.children ?? []) {
    walkStructuralXml(child, visit);
  }
}

export function structuralTextContent(node) {
  return `${node?.text ?? ''}${(node?.children ?? []).map(structuralTextContent).join('')}`;
}

export function splitStructuralName(qName) {
  const separator = qName.indexOf(':');
  return separator < 0 ? ['', qName] : [qName.slice(0, separator), qName.slice(separator + 1)];
}

export function structuralLocalName(qName) {
  return splitStructuralName(qName)[1];
}

function parseAttributes(source, fail, resourceLabel) {
  const result = new Map();
  let rest = source;
  while (rest.trim().length > 0) {
    const match = rest.match(/^\s+([A-Za-z_][\w.:-]*)\s*=\s*("([^"]*)"|'([^']*)')/);
    if (!match) {
      fail('MALFORMED_ATTRIBUTE', `The ${resourceLabel} contains a malformed attribute.`);
    }
    if (result.has(match[1])) {
      fail('DUPLICATE_ATTRIBUTE', `The ${resourceLabel} contains a duplicate attribute.`);
    }
    result.set(match[1], decodeXmlEntities(match[3] ?? match[4] ?? '', fail, resourceLabel));
    rest = rest.slice(match[0].length);
  }
  return result;
}

function decodeXmlEntities(value, fail, resourceLabel) {
  const entityPattern = /&(#x[0-9a-f]+|#\d+|[A-Za-z][\w.-]*);/gi;
  if (value.replace(entityPattern, '').includes('&')) {
    fail('MALFORMED_ENTITY', `The ${resourceLabel} contains a malformed entity reference.`);
  }
  return value.replace(entityPattern, (match, entity) => {
    if (entity.toLowerCase().startsWith('#x')) {
      return validCodePoint(Number.parseInt(entity.slice(2), 16), fail, resourceLabel);
    }
    if (entity.startsWith('#')) {
      return validCodePoint(Number.parseInt(entity.slice(1), 10), fail, resourceLabel);
    }
    if (Object.prototype.hasOwnProperty.call(XML_ENTITIES, entity)) {
      return XML_ENTITIES[entity];
    }
    fail('UNKNOWN_ENTITY', `The ${resourceLabel} contains an unknown entity reference.`);
  });
}

function validCodePoint(codePoint, fail, resourceLabel) {
  if (!Number.isSafeInteger(codePoint)
      || codePoint <= 0
      || codePoint > 0x10FFFF
      || codePoint >= 0xD800 && codePoint <= 0xDFFF) {
    fail('INVALID_CHARACTER', `The ${resourceLabel} contains an invalid character reference.`);
  }
  return String.fromCodePoint(codePoint);
}
