/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {createReadStream, existsSync, statSync} from 'node:fs';
import {createServer} from 'node:http';
import {extname, normalize, resolve, sep} from 'node:path';
import {fileURLToPath} from 'node:url';

const harnessDir = fileURLToPath(new URL('.', import.meta.url));
const repositoryRoot = resolve(harnessDir, '../../../../..');
const port = Number.parseInt(process.env.PORT ?? '4173', 10);
const host = process.env.HOST ?? '127.0.0.1';

const contentTypes = new Map([
  ['.css', 'text/css; charset=utf-8'],
  ['.html', 'text/html; charset=utf-8'],
  ['.js', 'text/javascript; charset=utf-8'],
  ['.json', 'application/json; charset=utf-8'],
  ['.mjs', 'text/javascript; charset=utf-8'],
  ['.svg', 'image/svg+xml'],
  ['.woff2', 'font/woff2']
]);

function safePath(pathname) {
  const decoded = decodeURIComponent(pathname.split('?')[0]);
  const relative = normalize(decoded).replace(/^[/\\]+/, '');
  const candidate = resolve(repositoryRoot, relative);
  return candidate === repositoryRoot || candidate.startsWith(repositoryRoot + sep) ? candidate : null;
}

const server = createServer((request, response) => {
  const requestUrl = new URL(request.url ?? '/', `http://${request.headers.host ?? `${host}:${port}`}`);
  if (requestUrl.pathname === '/') {
    response.writeHead(302, {location: '/openspec/changes/analyze-web-component-theming-kit/evidence/harness/index.html'});
    response.end();
    return;
  }
  let candidate;
  try {
    candidate = safePath(requestUrl.pathname);
  } catch {
    candidate = null;
  }
  if (!candidate || !existsSync(candidate) || !statSync(candidate).isFile()) {
    response.writeHead(404, {'content-type': 'text/plain; charset=utf-8'});
    response.end('Not found');
    return;
  }
  response.writeHead(200, {
    'access-control-allow-origin': '*',
    'cache-control': 'no-store',
    'content-type': contentTypes.get(extname(candidate)) ?? 'application/octet-stream'
  });
  createReadStream(candidate).pipe(response);
});

server.listen(port, host, () => {
  console.log(`Causeway theming analysis harness: http://${host}:${port}/`);
  console.log(`Serving repository root: ${repositoryRoot}`);
});
