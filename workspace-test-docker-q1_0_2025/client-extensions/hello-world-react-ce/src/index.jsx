import React from "react";
import { createRoot } from "react-dom/client";

function App() {
  return (
    <div style={{ padding: 16, border: "1px solid #ddd", borderRadius: 8 }}>
      <h3>Hello World!</h3>
      <p>React dentro una Client Extension (customElement).</p>
    </div>
  );
}

class ReactHelloCE extends HTMLElement {
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
    try {
      this.__root?.unmount();
    } catch (_) {}
  }
}

if (!customElements.get("hello-world-react-ce")) {
  customElements.define("hello-world-react-ce", ReactHelloCE);
}