/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {mkdirSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {build} from 'esbuild';

const directory = fileURLToPath(new URL('.', import.meta.url));
const outputDirectory = resolve(directory, 'generated');
mkdirSync(outputDirectory, {recursive: true});

await build({
  entryPoints: [resolve(directory, 'webawesome-entry.mjs')],
  bundle: true,
  format: 'esm',
  minify: true,
  outfile: resolve(outputDirectory, 'webawesome-selective.js'),
  platform: 'browser',
  sourcemap: false,
  treeShaking: true
});

await build({
  entryPoints: [resolve(directory, 'node_modules/@awesome.me/webawesome/dist/styles/webawesome.css')],
  bundle: true,
  loader: {'.woff2': 'dataurl'},
  minify: true,
  outfile: resolve(outputDirectory, 'webawesome-selective.css')
});

console.log('Built generated/webawesome-selective.js and generated/webawesome-selective.css');
