package com.intellij.ide.plugins.isa;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.sql.psi.impl.SqlCompositeElement;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


/**
 *
 */
public class AssistWithSqlInsertStatementAction extends AnAction {

    private Language language = Language.findLanguageByID("SQL");
    private Editor editor;
    private JComponent editorComponent;
    private LanguageTextField languageTextField;
    private PsiElement columnsList;
    private PsiElement valuesList;
    private CaretPositionToTextRangeMapper caretPositionToTextRangeMapper;

    public void actionPerformed(AnActionEvent e) {
        PsiElement currentPsiElement = getPsiElementFromContext(e);

        if (editor == null) {
            return;
        }

        SqlCompositeElement fullSqlInsertStatement = findTopmostSqlCompositeElement(currentPsiElement);

        if (fullSqlInsertStatement == null) {
            return;
        }

        editorComponent = editor.getComponent();

        if (isSqlInsertStatement(fullSqlInsertStatement)) {
            columnsList = extractColumnsListFrom(fullSqlInsertStatement);
            valuesList = extractValuesListFrom(fullSqlInsertStatement);

            caretPositionToTextRangeMapper = createCaretPositionToTextRangeMapper();

            languageTextField = new LanguageTextField(language, e.getProject(), columnsList.getText(), true);

            editor.getCaretModel().addCaretListener(new CaretListener() {
                @Override
                public void caretPositionChanged(final CaretEvent e) {
                    updateSelectionInPopup(e);
                }
            });

            showColumnListPopup();
        } else {
            PopupUtil.showBalloonForComponent(editorComponent, "Current line is not an SQL INSERT statement", MessageType.ERROR, true, null);
        }

    }

    private CaretPositionToTextRangeMapper createCaretPositionToTextRangeMapper() {
        return new CaretPositionToTextRangeMapper(new TextElement(columnsList), new TextElement(valuesList));
    }

    private void showColumnListPopup() {
        ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(languageTextField, editorComponent);
        JBPopup popup = componentPopupBuilder.createPopup();
        popup.showInBestPositionFor(editor);
    }

    private void updateSelectionInPopup(CaretEvent caretEvent) {
        final int newColumn = caretEvent.getNewPosition().column;
        TextRange editorSelection = caretPositionToTextRangeMapper.getNewColumnListSelection(newColumn);

        editor.getSelectionModel().setSelection(editorSelection.getStartOffset(), editorSelection.getEndOffset());
    }

    private PsiElement extractValuesListFrom(SqlCompositeElement fullSqlInsertStatement) {
        return fullSqlInsertStatement.getLastChild();
    }

    private PsiElement extractColumnsListFrom(SqlCompositeElement fullSqlInsertStatement) {
        return fullSqlInsertStatement.getChildren()[1];
    }

    @Nullable
    private SqlCompositeElement findTopmostSqlCompositeElement(@Nullable PsiElement psiElement) {
        return PsiTreeUtil.getTopmostParentOfType(psiElement, SqlCompositeElement.class);
    }

    private boolean isSqlInsertStatement(@NotNull SqlCompositeElement psiElement) {
        return psiElement.getFirstChild().getText().startsWith("INSERT INTO");
    }

    private PsiElement getPsiElementFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        return psiFile.findElementAt(offset);
    }
}
