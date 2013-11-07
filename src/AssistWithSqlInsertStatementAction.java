import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.LanguageTextField;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;


/**
 *
 */
public class AssistWithSqlInsertStatementAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        PsiElement psiElement = getPsiElementFromContext(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (isSqlInsertStatement(psiElement)) {
            LanguageTextField languageTextField = new LanguageTextField(psiElement.getLanguage(), e.getProject(), psiElement.getText(), true);
            languageTextField.setVisible(true);
            languageTextField.setBackground(UIUtil.getTextFieldBackground());
            JComponent component = editor.getComponent();
            ComponentPopupBuilder componentPopupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(languageTextField, component);
            JBPopup popup = componentPopupBuilder.createPopup();
            popup.showInBestPositionFor(editor);
        } else {
            PopupUtil.showBalloonForComponent(editor.getContentComponent(), "Current line is not an SQL INSERT statement", MessageType.ERROR, true, null);
        }

    }

    private boolean isSqlInsertStatement(PsiElement psiElement) {
        if (psiElement == null) {
            return false;
        }
        Language language = psiElement.getLanguage();

        return language.is(Language.findLanguageByID("SQL"));
    }

    private PsiElement getPsiElementFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        return psiFile.findElementAt(offset);
    }















}
