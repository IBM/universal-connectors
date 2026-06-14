import React, {useEffect} from 'react';

const downloadExtensions = [
  '.zip',
  '.config',
  '.conf',
  '.properties',
  '.yml',
  '.yaml',
  '.json',
];

function isDownloadLink(anchor) {
  const href = anchor.getAttribute('href');
  if (!href) {
    return false;
  }

  let url;
  try {
    url = new URL(href, window.location.href);
  } catch {
    return false;
  }

  if (url.origin !== window.location.origin) {
    return false;
  }

  const pathname = url.pathname.toLowerCase();
  return downloadExtensions.some((extension) => pathname.endsWith(extension));
}

function markDownloadLinks() {
  for (const anchor of document.querySelectorAll('a[href]')) {
    if (isDownloadLink(anchor)) {
      anchor.setAttribute('download', '');
    }
  }
}

export default function Root({children}) {
  useEffect(() => {
    markDownloadLinks();

    const observer = new MutationObserver(() => markDownloadLinks());
    observer.observe(document.documentElement, {childList: true, subtree: true});

    return () => observer.disconnect();
  }, []);

  return <>{children}</>;
}
