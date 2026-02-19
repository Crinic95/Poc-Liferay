(function () {
  'use strict';

  class HelloWorldCE extends HTMLElement {
    connectedCallback() {
      this.innerHTML = `
        <div style="padding:16px;border:1px solid #ddd;border-radius:8px">
          <h3>Hello World!</h3>
          <p>Client Extension Custom Element attiva!</p>
        </div>
      `;
    }
  }

  if (!customElements.get('hello-world-ce')) {
    customElements.define('hello-world-ce', HelloWorldCE);
  }
})();