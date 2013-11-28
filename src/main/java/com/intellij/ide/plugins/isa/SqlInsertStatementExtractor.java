package com.intellij.ide.plugins.isa;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.sql.psi.impl.SqlCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class SqlInsertStatementExtractor {
    private PsiElement columnsList;
    private PsiElement valuesList;
    private final SqlCompositeElement fullSqlInsertStatement;

    public SqlInsertStatementExtractor(final @NotNull PsiFile psiFile, final @NotNull Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        final PsiElement currentPsiElement = psiFile.findElementAt(offset);


        fullSqlInsertStatement = findTopmostSqlCompositeElement(currentPsiElement);

        if (fullSqlInsertStatement == null) {
            return;
        }

        columnsList = extractColumnsListFrom(fullSqlInsertStatement);
        valuesList = extractValuesListFrom(fullSqlInsertStatement);
    }

    private PsiElement extractValuesListFrom(final SqlCompositeElement fullSqlInsertStatement) {
        return fullSqlInsertStatement.getLastChild().getChildren()[0];
    }

    private PsiElement extractColumnsListFrom(final SqlCompositeElement fullSqlInsertStatement) {
        return fullSqlInsertStatement.getChildren()[1].getChildren()[0];
    }

    @Nullable
    private SqlCompositeElement findTopmostSqlCompositeElement(@Nullable final PsiElement psiElement) {
        return PsiTreeUtil.getTopmostParentOfType(psiElement, SqlCompositeElement.class);
    }

    private boolean isSqlInsertStatement(@NotNull final SqlCompositeElement psiElement) {
        return psiElement.getFirstChild().getText().startsWith("INSERT INTO");
    }

    public TextElement getColumnList() {
        return new TextElement(columnsList);
    }

    public TextElement getValuesList() {
        return new TextElement(valuesList);
    }

    public boolean isInsertStatementUnderCaret() {
        return isSqlInsertStatement(fullSqlInsertStatement);
    }

    public String getColumnListText() {
        return columnsList.getText();
    }
}
