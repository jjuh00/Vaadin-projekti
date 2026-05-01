package com.prodeca.views.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.JsModule;

public class QuillEditorField extends CustomField<String> {

    @Tag("quill-editor")
    @JsModule("./quill-editor.js")
    private static class QuillEditorElement extends Component implements HasSize {
        public QuillEditorElement() { }
    }

    private final QuillEditorElement editor = new QuillEditorElement();

    public QuillEditorField() {
        add(editor);
    }

    @Override
    protected String generateModelValue() {
        String value = editor.getElement().getProperty("value");
        return value != null ? value : "";
    }

    @Override
    protected void setPresentationValue(String html) {
        editor.getElement().callJsFunction("setValue", html != null ? html : "");
    }
}