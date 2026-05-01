const QUILL_VERSION = "1.3.7";
const QUILL_CDN = `https://cdn.jsdelivr.net/npm/quill@${QUILL_VERSION}/dist`;

function loadQuill() {
    return new Promise((resolve) => {
        if (window.Quill) { resolve(); return; }

        // Snow-tyylinen teema
        const link = document.createElement("link");
        link.rel = "stylesheet";
        link.href = `${QUILL_CDN}/quill.snow.css`;
        document.head.appendChild(link);

        // Quill bundle
        const script = document.createElement("script");
        script.src = `${QUILL_CDN}/quill.min.js`;
        script.onload = resolve;
        script.onerror = () => console.error("Quill CDN script ei latautunut oikein");
        document.body.appendChild(script);
    });
}

class QuillEditor extends HTMLElement {
    constructor() { super(); }

    async connectedCallback() {
        // Ei ladata uudestaan, jos Quill on jo alustettu
        if (this._quill) return;

        await loadQuill();

        this.style.display = "block";
        this.style.minHeight = "200px";
        this.style.overflow = "visible";

        const toolbar = document.createElement("div");
        toolbar.innerHTML = `
            <span class="ql-formats">
                <select class="ql-header">
                    <option value="1"></option>
                    <option value="2"></option>
                    <option selected></option>
                </select>
            </span>
            <span class="ql-formats">
                <button class="ql-bold"></button>
                <button class="ql-italic"></button>
                <button class="ql-underline"></button>
            </span>
            <span class="ql-formats">
                <button class="ql-list" value="ordered"></button>
                <button class="ql-list" value="bullet"></button>
            </span>
            <span class="ql-formats">
                <button class="ql-link"></button>
            </span>
        `;

        const editor = document.createElement("div");
        editor.style.height = "160px";

        this.appendChild(toolbar);
        this.appendChild(editor);

        this._quill = new Quill(editor, {
            modules: { toolbar: toolbar },
            theme: "snow"
        });

        // Suoritetaan custom tapahtuma aina, kun editorin sisältö muuttuu, jotta
        // QuillEditorField.kaa voi synkronoida
        this._quill.on("text-change", () => {
            this.dispatchEvent(new CustomEvent("quill-change", {
                detail: { html: this._quill.root.innerHTML },
                bubbles: true,
                composed: true
            }));
        });
    }

    // Java kutsuu tätä (element.callJsFunction("setValue", html)) päivittääkseen editorin sisällön
    setValue(html) {
        if (this._quill) this._quill.root.innerHTML = html || "";
    }

    // Java kutsuu tätä (element.callJsFunction("getValue")) hakeakseen editorin sisällön
    getValue() {
        return this._quill ? this._quill.root.innerHTML : "";
    }
}

customElements.define("quill-editor", QuillEditor);