package com.intellij.ide.plugins.isa;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.sql.psi.impl.SqlCompositeElement;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.ui.UIUtil;
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
    private final CaretListener updateSelectionOnCaretChangeListener = new MyCaretListener();
    private final JBPopupListener destroyListener = new MyJBPopupListener();

    public void actionPerformed(final AnActionEvent e) {
        final PsiElement currentPsiElement = getPsiElementFromContext(e);

        if (editor == null) {
            return;
        }

        final SqlCompositeElement fullSqlInsertStatement = findTopmostSqlCompositeElement(currentPsiElement);

        if (fullSqlInsertStatement == null) {
            return;
        }

        editorComponent = editor.getComponent();

        if (isSqlInsertStatement(fullSqlInsertStatement)) {
            columnsList = extractColumnsListFrom(fullSqlInsertStatement);
            valuesList = extractValuesListFrom(fullSqlInsertStatement);

            caretPositionToTextRangeMapper = createCaretPositionToTextRangeMapper();

            languageTextField = new LanguageTextField(language, e.getProject(), columnsList.getText(), true);
            languageTextField.setForeground(UIUtil.getLabelTextForeground());
            languageTextField.setAsRendererWithSelection(UIUtil.getListSelectionBackground(), UIUtil.getListSelectionForeground());

            editor.getCaretModel().addCaretListener(updateSelectionOnCaretChangeListener);

            showColumnListPopup();
        } else {
            PopupUtil.showBalloonForComponent(editorComponent, "Current line is not an SQL INSERT statement", MessageType.ERROR, true, null);
        }

    }

    private CaretPositionToTextRangeMapper createCaretPositionToTextRangeMapper() {
        return new CaretPositionToTextRangeMapper(new TextElement(columnsList), new TextElement(valuesList));
    }

    private void showColumnListPopup() {
        final ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(languageTextField, editorComponent);
        final JBPopup popup = componentPopupBuilder
                .setCancelOnClickOutside(false)
                .setTitle("test")
                .setCancelKeyEnabled(true)
                .addListener(destroyListener)
                .setCancelOnOtherWindowOpen(false)
                .setShowShadow(true)
                .setAdText("asd")
                .setResizable(true)
                .createPopup();
        popup.showInBestPositionFor(editor);
    }

    private void updateSelectionInPopup(final CaretEvent caretEvent) {
        final Editor languageTextFieldEditor = languageTextField.getEditor();

        if (languageTextFieldEditor != null) {
            final int newColumn = caretEvent.getNewPosition().column;
            final TextRange editorSelection = caretPositionToTextRangeMapper.getNewColumnListSelection(newColumn);
            final SelectionModel selectionModel = languageTextFieldEditor.getSelectionModel();
            selectionModel.setSelection(editorSelection.getStartOffset(), editorSelection.getEndOffset());
        }
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

    private PsiElement getPsiElementFromContext(final AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        return psiFile.findElementAt(offset);
    }

    private class MyJBPopupListener implements JBPopupListener {
        @Override
        public void beforeShown(LightweightWindowEvent event) {
            // NOP
        }

        @Override
        public void onClosed(LightweightWindowEvent event) {
            editor.getCaretModel().removeCaretListener(updateSelectionOnCaretChangeListener);
        }
    }

    private class MyCaretListener implements CaretListener {
        @Override
        public void caretPositionChanged(final CaretEvent e) {
            updateSelectionInPopup(e);
        }
    }
}
