package com.intellij.ide.plugins.isa;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;

/**
 *
 */
public class TextElement {
    private String text;
    private TextRange textRange;

    public TextElement(PsiElement psiElement) {
        this(psiElement.getText(), psiElement.getTextRange());
    }

    public TextElement(String text, TextRange textRange) {
        this.text = text;
        this.textRange = textRange;
    }

    public String getText() {
        return text;
    }

    public int getEndOffset() {
        return textRange.getEndOffset();
    }

    public int getStartOffset() {
        return textRange.getStartOffset();
    }
}
