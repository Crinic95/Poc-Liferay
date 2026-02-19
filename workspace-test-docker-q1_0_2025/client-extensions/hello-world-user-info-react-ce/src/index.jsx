import React from "react";
import { createRoot } from "react-dom/client";
import { getSitePages } from "./service";

function App() {
  const [loading, setLoading] = React.useState(false);
  const [user, setData] = React.useState(null);
  const [err, setErr] = React.useState("");

  const onCall = async () => {
    setErr("");
    setData(null);
    setLoading(true);

    try {
      const data = await getSitePages(20117);
      setData(data);
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

      {user && (
        <pre style={{ marginTop: 12, background: "#f6f6f6", padding: 12, overflow: "auto" }}>
          {JSON.stringify(user, null, 2)}
        </pre>
      )}
    </div>
  );
}

class HelloWorldUserInfoReactCE extends HTMLElement {
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

if (!customElements.get("hello-world-user-info-react-ce")) {
  customElements.define("hello-world-user-info-react-ce", HelloWorldUserInfoReactCE);
}