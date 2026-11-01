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

import {MAX_STRUCTURAL_XML_CHARACTERS} from './structural-xml.mjs';

export class StructuralResourceError extends Error {
  constructor(code, message, {status = null} = {}) {
    super(message);
    this.name = 'StructuralResourceError';
    this.code = code;
    this.status = status;
  }
}

export async function fetchStructuralResource(resourcePath, {
  fetchImpl = globalThis.fetch,
  accept = 'application/xml',
  signal,
  maximumCharacters = MAX_STRUCTURAL_XML_CHARACTERS
} = {}) {
  if (!isSafeStructuralResourcePath(resourcePath)) {
    throw new StructuralResourceError('INVALID_RESOURCE_PATH', 'A structural resource must be an opaque origin-relative path beginning with exactly one slash.');
  }
  if (typeof fetchImpl !== 'function') {
    throw new StructuralResourceError('FETCH_UNAVAILABLE', 'No Fetch API implementation is available for structural resources.');
  }
  try {
    const response = await fetchImpl(resourcePath, {
      method: 'GET',
      headers: {accept},
      credentials: 'same-origin',
      cache: 'no-store',
      redirect: 'error',
      signal
    });
    if (!response?.ok) {
      throw new StructuralResourceError(
        'RESOURCE_REQUEST_FAILED',
        `Structural resource request failed with HTTP ${response?.status ?? 'unknown'}.`,
        {status: response?.status ?? null}
      );
    }
    const contentLength = Number(response.headers?.get?.('content-length'));
    if (Number.isFinite(contentLength) && contentLength > maximumCharacters) {
      throw new StructuralResourceError('RESOURCE_TOO_LARGE', `Structural resource exceeds ${maximumCharacters} characters.`);
    }
    const text = await response.text();
    if (text.length > maximumCharacters) {
      throw new StructuralResourceError('RESOURCE_TOO_LARGE', `Structural resource exceeds ${maximumCharacters} characters.`);
    }
    return Object.freeze({
      path: resourcePath,
      mediaType: response.headers?.get?.('content-type') ?? null,
      text
    });
  } catch (error) {
    if (error?.name === 'AbortError' || error instanceof StructuralResourceError) {
      throw error;
    }
    throw new StructuralResourceError('RESOURCE_REQUEST_FAILED', 'The structural resource request failed.');
  }
}

export function isSafeStructuralResourcePath(value) {
  return typeof value === 'string'
    && value.startsWith('/')
    && !value.startsWith('//')
    && !value.includes('\\')
    && !/[\r\n]/.test(value);
}
