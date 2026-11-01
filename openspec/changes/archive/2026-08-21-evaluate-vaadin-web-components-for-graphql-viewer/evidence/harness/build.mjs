/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0.
 */

import {mkdirSync, readFileSync, rmSync, writeFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {build} from 'esbuild';

const directory = fileURLToPath(new URL('.', import.meta.url));
const generated = resolve(directory, 'generated');
mkdirSync(generated, {recursive: true});

await build({
  entryPoints: [resolve(directory, 'vaadin-entry.mjs')],
  bundle: true,
  format: 'esm',
  minify: true,
  outfile: resolve(generated, 'vaadin-selective.js'),
  platform: 'browser',
  sourcemap: false,
  treeShaking: true
});

const splitDirectory = resolve(generated, 'split');
rmSync(splitDirectory, {recursive: true, force: true});
const split = await build({
  entryPoints: {
    reference: resolve(directory, 'entry-reference.mjs'),
    grid: resolve(directory, 'entry-grid.mjs'),
    fields: resolve(directory, 'entry-fields.mjs')
  },
  bundle: true,
  chunkNames: 'chunks/[name]-[hash]',
  splitting: true,
  format: 'esm',
  minify: true,
  outdir: splitDirectory,
  platform: 'browser',
  sourcemap: false,
  treeShaking: true,
  metafile: true
});
writeFileSync(resolve(generated, 'split-metafile.json'), `${JSON.stringify(split.metafile)}\n`);

const licensesDirectory = resolve(generated, 'licenses');
rmSync(licensesDirectory, {recursive: true, force: true});
mkdirSync(licensesDirectory, {recursive: true});
const manifest = JSON.parse(readFileSync(resolve(directory, 'package.json'), 'utf8'));
for (const name of Object.keys(manifest.dependencies)) {
  const license = readFileSync(resolve(directory, 'node_modules', ...name.split('/'), 'LICENSE'));
  writeFileSync(resolve(licensesDirectory, `${name.replaceAll('/', '__').replaceAll('@', '')}-LICENSE.txt`), license);
}

console.log('Built broad selective bundle, route-split entries, and direct-package licenses');
