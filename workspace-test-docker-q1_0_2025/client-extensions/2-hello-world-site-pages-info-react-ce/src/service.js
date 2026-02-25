async function liferayFetch(path, options = {}) {
  const headers = {
    Accept: "application/json",
    ...(options.headers || {}),
  };

  if (window.Liferay?.authToken) {
    headers["x-csrf-token"] = window.Liferay.authToken;
  }

  const res = await fetch(path, {
    method: "GET",
    credentials: "include",
    ...options,
    headers,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} ${res.statusText} ${text}`);
  }

  return res.json();
}

export function getSitePages(siteId) {
  return liferayFetch(`/o/headless-delivery/v1.0/sites/${siteId}/site-pages?pageSize=10`);
}