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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.ui.UIUtil;


/**
 *
 */
public class AssistWithSqlInsertStatementAction extends AnAction {

    private Editor editor;
    private LanguageTextField languageTextField;

    private CaretPositionToTextRangeMapper caretPositionToTextRangeMapper;
    private final CaretListener updateSelectionOnCaretChangeListener = new MyCaretListener();
    private final JBPopupListener destroyListener = new MyJBPopupListener();

    public void actionPerformed(final AnActionEvent e) {
        final PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        editor = e.getData(PlatformDataKeys.EDITOR);

        if (psiFile == null || editor == null) {
            return;
        }

        final SqlInsertStatementExtractor sqlInsertStatementExtractor = new SqlInsertStatementExtractor(psiFile, editor);

        if (sqlInsertStatementExtractor.isInsertStatementUnderCaret()) {

            caretPositionToTextRangeMapper = new CaretPositionToTextRangeMapper(sqlInsertStatementExtractor.getColumnList(),
                    sqlInsertStatementExtractor.getValuesList());

            languageTextField = createLanguageTextField(e.getProject(), sqlInsertStatementExtractor.getColumnListText());

            editor.getCaretModel().addCaretListener(updateSelectionOnCaretChangeListener);

            final JBPopup popup = createPopup();
            popup.showInBestPositionFor(editor);
        } else {
            PopupUtil.showBalloonForComponent(editor.getComponent(), "Current line is not an SQL INSERT statement", MessageType.ERROR, true, null);
        }

    }

    private LanguageTextField createLanguageTextField(final Project project, final String columnListText) {
        final LanguageTextField languageTextField = new LanguageTextField(Language.findLanguageByID("SQL"), project, columnListText, true);
        languageTextField.setAsRendererWithSelection(UIUtil.getListSelectionBackground(), UIUtil.getListSelectionForeground());
        return languageTextField;
    }

    private JBPopup createPopup() {
        final ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(languageTextField, editor.getComponent());

        return componentPopupBuilder
                .setCancelOnClickOutside(false)
                .setCancelKeyEnabled(true)
                .addListener(destroyListener)
                .setCancelOnOtherWindowOpen(false)
                .setShowShadow(true)
                .setResizable(true)
                .createPopup();
    }

    private class MyJBPopupListener implements JBPopupListener {
        @Override
        public void beforeShown(final LightweightWindowEvent event) {
            // NOP
        }

        @Override
        public void onClosed(final LightweightWindowEvent event) {
            editor.getCaretModel().removeCaretListener(updateSelectionOnCaretChangeListener);
        }
    }

    private class MyCaretListener implements CaretListener {
        @Override
        public void caretPositionChanged(final CaretEvent e) {
            updateSelectionInPopup(e);
        }
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
}
