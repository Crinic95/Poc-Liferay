import React from "react";
import { createRoot } from "react-dom/client";
import { getSitePages } from "./service";

function App() {
  const [loading, setLoading] = React.useState(false);
  const [sitePages, setSitePages] = React.useState(null);
  const [err, setErr] = React.useState("");

  const onCall = async () => {
    setErr("");
    setSitePages(null);
    setLoading(true);

    try {
      const sitePages = await getSitePages(20117);
      setSitePages(sitePages);
    } catch (e) {
      setErr(e?.message || String(e));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: 16, border: "1px solid #ddd", borderRadius: 8 }}>
      <h3>Hello “API”</h3>

      <button onClick={onCall} disabled={loading}>
        {loading ? "Chiamo..." : "Chiama API Liferay"}
      </button>

      {err && <p style={{ color: "crimson", marginTop: 12 }}>{err}</p>}

      {sitePages && (
        <div style={{ marginTop: 12 }}>
          {sitePages.items.map(({ id, title, friendlyUrlPath }) => (
            <div key={id}>
              {title} — {friendlyUrlPath}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

class HelloWorldSitePagesInfoReactCE extends HTMLElement {
  connectedCallback() {
    if (this.__mounted) return;
    this.__mounted = true;

    this.style.display = "block";
    this.style.width = "100%";
    this.style.minHeight = "1px";

    const mount = document.createElement("div");
    mount.style.minHeight = "40px";
    mount.style.width = "100%";
    this.appendChild(mount);

    this.__root = createRoot(mount);
    this.__root.render(<App />);
  }

  disconnectedCallback() {
    try { this.__root?.unmount(); } catch (_) {}
    this.__root = null;
  }
}

if (!customElements.get("hello-world-site-pages-info-react-ce")) {
  customElements.define("hello-world-site-pages-info-react-ce", HelloWorldSitePagesInfoReactCE);
}