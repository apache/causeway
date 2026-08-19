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

export function encodeRouteSegment(value) {
  const text = String(value ?? '');
  if (!text || text.length > 1024 || text === '.' || text === '..' || /[\\/\u0000-\u001f\u007f]/u.test(text)) {
    throw new Error('The requested application route is invalid.');
  }
  return encodeURIComponent(text).replace(/[!'()*]/g, character =>
    `%${character.charCodeAt(0).toString(16).toUpperCase()}`);
}

export function canonicalObjectPath(basePath, target) {
  const base = normalizeBasePath(basePath);
  const logicalTypeName = target?.logicalTypeName;
  const objectId = target?.id ?? target?.objectId;
  return `${base}/object/${encodeRouteSegment(logicalTypeName)}/${encodeRouteSegment(objectId)}`;
}

export function resultObjectIdentity(result) {
  if (result?.kind !== 'object') {
    return null;
  }
  const metadata = result.value?._meta;
  if (!metadata?.logicalTypeName || !metadata?.id) {
    return null;
  }
  return Object.freeze({logicalTypeName: metadata.logicalTypeName, id: metadata.id, title: metadata.title ?? metadata.id});
}

export function homeObjectIdentity(applicationEntry) {
  if (applicationEntry?.home?.kind !== 'OBJECT') {
    return null;
  }
  const metadata = applicationEntry.home.object?._meta;
  const logicalTypeName = metadata?.logicalTypeName ?? applicationEntry.home.logicalTypeName;
  if (!logicalTypeName || !metadata?.id) {
    return null;
  }
  return Object.freeze({logicalTypeName, id: metadata.id, title: metadata.title ?? metadata.id});
}

export function normalizeBasePath(basePath) {
  let value = String(basePath ?? '').trim();
  if (!value.startsWith('/') || value.startsWith('//')) {
    throw new Error('The HTMX viewer base path is invalid.');
  }
  while (value.length > 1 && value.endsWith('/')) {
    value = value.slice(0, -1);
  }
  return value;
}
