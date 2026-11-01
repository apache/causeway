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

import {LOCAL_RESOURCE_STRATEGIES} from './action-result.mjs';

export class LocalResourceNavigationError extends Error {
  constructor(code, message) {
    super(message);
    this.name = 'LocalResourceNavigationError';
    this.code = code;
  }
}

export function resolveLocalResourceTarget(value, {
  location = globalThis.location,
  applicationBase = '/'
} = {}) {
  const path = typeof value?.path === 'string' ? value.path.trim() : '';
  const strategy = value?.openUrlStrategy;
  if (!path || !LOCAL_RESOURCE_STRATEGIES.includes(strategy)) {
    throw navigationError('LOCAL_RESOURCE_INVALID', 'The local-resource result is incomplete.');
  }
  if (/^[a-z][a-z\d+.-]*:/i.test(path) || path.startsWith('//') || path.includes('\\') || /[\u0000-\u001f\u007f]/.test(path)) {
    throw navigationError('LOCAL_RESOURCE_TARGET_UNSAFE', 'The local-resource target is not an application-local path.');
  }
  const origin = new URL(location?.href ?? String(location));
  const base = resolveApplicationBase(applicationBase, origin);
  const basePath = normalizedBasePath(base.pathname);
  const supplied = path.startsWith('/') ? path : `/${path}`;
  const alreadyBased = basePath !== '/' && (supplied === basePath || supplied.startsWith(`${basePath}/`) || supplied.startsWith(`${basePath}?`) || supplied.startsWith(`${basePath}#`));
  const relative = alreadyBased ? supplied : `${basePath === '/' ? '' : basePath}/${supplied.replace(/^\/+/, '')}`;
  const target = new URL(relative, origin.origin);
  if (target.origin !== origin.origin || target.username || target.password || !withinBase(target.pathname, basePath)) {
    throw navigationError('LOCAL_RESOURCE_TARGET_UNSAFE', 'The local-resource target escapes the configured application boundary.');
  }
  return Object.freeze({url: target, openUrlStrategy: strategy});
}

export function navigateLocalResource(value, {
  location = globalThis.location,
  applicationBase = '/',
  open = globalThis.open?.bind(globalThis)
} = {}) {
  const target = resolveLocalResourceTarget(value, {location, applicationBase});
  if (target.openUrlStrategy === 'SAME_WINDOW') {
    if (typeof location?.assign !== 'function') {
      throw navigationError('LOCAL_RESOURCE_NAVIGATION_UNAVAILABLE', 'Same-window navigation is unavailable.');
    }
    location.assign(target.url.href);
    return target;
  }
  if (typeof open !== 'function') {
    throw navigationError('LOCAL_RESOURCE_NAVIGATION_UNAVAILABLE', 'New-window navigation is unavailable.');
  }
  const opened = open(target.url.href, '_blank', 'noopener,noreferrer');
  if (opened && 'opener' in opened) opened.opener = null;
  return target;
}

function resolveApplicationBase(applicationBase, origin) {
  const text = String(applicationBase ?? '/').trim() || '/';
  if (text.startsWith('//') || /[\u0000-\u001f\u007f]/.test(text)) {
    throw navigationError('LOCAL_RESOURCE_BASE_INVALID', 'The application-local resource base is invalid.');
  }
  const base = new URL(text.endsWith('/') ? text : `${text}/`, origin.origin);
  if (base.origin !== origin.origin || base.username || base.password) {
    throw navigationError('LOCAL_RESOURCE_BASE_INVALID', 'The application-local resource base must be same-origin.');
  }
  return base;
}

function normalizedBasePath(pathname) {
  const normalized = pathname.replace(/\/+$/, '');
  return normalized || '/';
}

function withinBase(pathname, basePath) {
  return basePath === '/' || pathname === basePath || pathname.startsWith(`${basePath}/`);
}

function navigationError(code, message) {
  return new LocalResourceNavigationError(code, message);
}
