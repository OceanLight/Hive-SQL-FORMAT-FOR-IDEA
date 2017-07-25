package sql;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.jgoodies.common.base.Strings;
import sql.util.SQLFormatter;
import sql.util.SQLFormatterV2;

/**
 * Created by duhaiguang on 2017/7/9.
 */
public class SQLFormat extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Project project = e.getData(PlatformDataKeys.PROJECT);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        SelectionModel selectionModel = editor.getSelectionModel();
        String text = selectionModel.getSelectedText();
        if(!Strings.isEmpty(text)){
            System.out.println(text);
            int startOffset = selectionModel.getSelectionStart();
            int endOffset = selectionModel.getSelectionEnd();

            Document document = editor.getDocument();
            Runnable runnable = new Runnable(){
                @Override
                public void run() {
                    SQLFormatterV2 sqlFormatter = new SQLFormatterV2();
                    document.replaceString(startOffset, endOffset, sqlFormatter.format(text));
                }
            };

            //加入任务，由IDEA调度执行这个任务
            WriteCommandAction.runWriteCommandAction(project, runnable);
        }else{
            System.out.println("kong");
        }
    }

    String format(String s){
        return s.toUpperCase();
    }
}


