package com.intellij.ide.plugins.isa;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

/**
 *
 */
public class TextElement {
    private final String text;
    private final TextRange textRange;

    public TextElement(final PsiElement psiElement) {
        this(psiElement.getText(), psiElement.getTextRange());
    }

    public TextElement(final String text, final TextRange textRange) {
        this.text = text;
        this.textRange = textRange;
    }

    public String getText() {
        return text;
    }

    public int getStartOffset() {
        return textRange.getStartOffset();
    }
}
