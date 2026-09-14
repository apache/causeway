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

import type {CausewayLocalResourceTarget} from './contracts';

export class LocalResourceNavigationError extends Error {
  readonly code: string;

  constructor(code: string, message: string) {
    super(message);
    this.name = 'LocalResourceNavigationError';
    this.code = code;
  }
}

interface LocationTarget {
  readonly href: string;
  assign(url: string): void;
}

interface NavigateOptions {
  readonly location?: LocationTarget;
  readonly applicationBase?: string;
  readonly open?: (url?: string | URL, target?: string, features?: string) => Window | null;
}

export function resolveLocalResourceTarget(
  value: CausewayLocalResourceTarget | undefined,
  options: Pick<NavigateOptions, 'location' | 'applicationBase'> = {}
): Readonly<{url: URL; openUrlStrategy: 'SAME_WINDOW' | 'NEW_WINDOW'}> {
  const location = options.location ?? window.location;
  const path = typeof value?.path === 'string' ? value.path.trim() : '';
  const strategy = value?.openUrlStrategy;
  if (!path || !['SAME_WINDOW', 'NEW_WINDOW'].includes(strategy ?? '')) {
    throw failure('LOCAL_RESOURCE_INVALID', 'The local-resource result is incomplete.');
  }
  if (/^[a-z][a-z\d+.-]*:/i.test(path) || path.startsWith('//') || path.includes('\\') || /[\u0000-\u001f\u007f]/.test(path)) {
    throw failure('LOCAL_RESOURCE_TARGET_UNSAFE', 'The local-resource target is not an application-local path.');
  }
  const origin = new URL(location.href);
  const base = resolveApplicationBase(options.applicationBase ?? '/', origin);
  const basePath = base.pathname.replace(/\/+$/, '') || '/';
  const supplied = path.startsWith('/') ? path : `/${path}`;
  const alreadyBased = basePath !== '/'
    && (supplied === basePath || supplied.startsWith(`${basePath}/`) || supplied.startsWith(`${basePath}?`) || supplied.startsWith(`${basePath}#`));
  const relative = alreadyBased ? supplied : `${basePath === '/' ? '' : basePath}/${supplied.replace(/^\/+/, '')}`;
  const target = new URL(relative, origin.origin);
  if (target.origin !== origin.origin || target.username || target.password
      || (basePath !== '/' && target.pathname !== basePath && !target.pathname.startsWith(`${basePath}/`))) {
    throw failure('LOCAL_RESOURCE_TARGET_UNSAFE', 'The local-resource target escapes the configured application boundary.');
  }
  return Object.freeze({url: target, openUrlStrategy: strategy as 'SAME_WINDOW' | 'NEW_WINDOW'});
}

export function navigateLocalResource(value: CausewayLocalResourceTarget | undefined, options: NavigateOptions = {}): void {
  const location = options.location ?? window.location;
  const target = resolveLocalResourceTarget(value, {location, applicationBase: options.applicationBase});
  if (target.openUrlStrategy === 'SAME_WINDOW') {
    location.assign(target.url.href);
    return;
  }
  const open = options.open ?? window.open.bind(window);
  const opened = open(target.url.href, '_blank', 'noopener,noreferrer');
  if (opened) opened.opener = null;
}

function resolveApplicationBase(applicationBase: string, origin: URL): URL {
  const text = String(applicationBase).trim() || '/';
  if (text.startsWith('//') || /[\u0000-\u001f\u007f]/.test(text)) {
    throw failure('LOCAL_RESOURCE_BASE_INVALID', 'The application-local resource base is invalid.');
  }
  const base = new URL(text.endsWith('/') ? text : `${text}/`, origin.origin);
  if (base.origin !== origin.origin || base.username || base.password) {
    throw failure('LOCAL_RESOURCE_BASE_INVALID', 'The application-local resource base must be same-origin.');
  }
  return base;
}

function failure(code: string, message: string): LocalResourceNavigationError {
  return new LocalResourceNavigationError(code, message);
}
