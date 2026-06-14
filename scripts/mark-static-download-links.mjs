#!/usr/bin/env node

import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const buildRoot = path.join(repoRoot, 'pages-build');
const downloadExtensions = new Set([
  '.zip',
  '.config',
  '.conf',
  '.properties',
  '.yml',
  '.yaml',
  '.json',
]);

const htmlFiles = await collectHtmlFiles(buildRoot);
let updatedFiles = 0;
let updatedLinks = 0;

for (const htmlFile of htmlFiles) {
  const original = await fs.readFile(htmlFile, 'utf8');
  const {html, count} = addDownloadAttributes(original);

  if (count > 0) {
    await fs.writeFile(htmlFile, html);
    updatedFiles += 1;
    updatedLinks += count;
  }
}

console.log(`Marked static download links: ${updatedLinks} links in ${updatedFiles} HTML files.`);

async function collectHtmlFiles(directory) {
  const entries = await fs.readdir(directory, {withFileTypes: true});
  const files = [];

  for (const entry of entries.sort((a, b) => a.name.localeCompare(b.name))) {
    const entryPath = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      files.push(...(await collectHtmlFiles(entryPath)));
    } else if (entry.isFile() && entry.name.endsWith('.html')) {
      files.push(entryPath);
    }
  }

  return files;
}

function addDownloadAttributes(html) {
  let count = 0;
  const rewritten = html.replace(/<a\b([^>]*?)>/g, (anchor, attributes) => {
    if (/\bdownload(?:\s|=|>)/i.test(anchor)) {
      return anchor;
    }

    const href = getHref(attributes);
    if (!href || !shouldDownload(href)) {
      return anchor;
    }

    count += 1;
    return `<a${attributes} download>`;
  });

  return {html: rewritten, count};
}

function getHref(attributes) {
  const quoted = attributes.match(/\bhref=(["'])(.*?)\1/i);
  if (quoted) {
    return quoted[2];
  }

  const unquoted = attributes.match(/\bhref=([^\s>]+)/i);
  return unquoted ? unquoted[1] : null;
}

function shouldDownload(href) {
  if (!isLocalHref(href)) {
    return false;
  }

  const pathname = stripQueryAndHash(href);
  const extension = path.posix.extname(pathname).toLowerCase();
  return downloadExtensions.has(extension);
}

function isLocalHref(href) {
  return href.startsWith('/') || href.startsWith('./') || href.startsWith('../');
}

function stripQueryAndHash(href) {
  const suffixIndex = [...href]
    .map((character, index) => (character === '?' || character === '#' ? index : -1))
    .filter((index) => index !== -1)
    .sort((a, b) => a - b)[0];

  return suffixIndex === undefined ? href : href.slice(0, suffixIndex);
}
