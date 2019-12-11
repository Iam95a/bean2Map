package com.chen.bean2Map;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author cjw
 * bean生成map的插件
 */
public class Bean2Map extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) return;
        WriteCommandAction.runWriteCommandAction(e.getProject(), () -> {
            Editor editor = e.getData(PlatformDataKeys.EDITOR);
            if (editor == null) return;
            Project project = editor.getProject();
            if (project == null) return;

            PsiElement element = psiFile.findElementAt(editor.getCaretModel().getOffset());
            PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
            if (psiClass == null) return;

            if (psiClass.getNameIdentifier() == null) return;

            PsiField[] psiFields = psiClass.getAllFields();
            String dto2Map = "public Map<String,String> dto2Map(){\n" +
                    "    Map<String,String> map=new HashMap();\n";
            if (psiFields != null) {
                for (PsiField psiField : psiFields) {
                    dto2Map += "if(this." + psiField.getName() + "!=null){\n";
                    if (psiField.getType().getPresentableText().equals("String")) {
                        dto2Map += "    map.put(\"" + psiField.getName() + "\",this." + psiField.getName() + ");\n  }\n";
                    } else {
                        dto2Map += "    map.put(\"" + psiField.getName() + "\",String.valueOf(this." + psiField.getName() + "));\n  }\n";
                    }
                }
            }
            dto2Map += "    return map;";
            dto2Map += "}";
            String map2Dto = "public static " + psiClass.getName() + " map2Dto(Map<String,String> map){\n"
                    + "    " + psiClass.getName() + " a = new " + psiClass.getName() + "();\n";
            if (psiFields != null) {
                for (PsiField psiField : psiFields) {
                    String mapKey = "map" + upperCase(psiField.getName());
                    map2Dto += "String " + mapKey + " = map.get(\"" + psiField.getName() + "\");";
                    map2Dto += "if("+mapKey+"!=null){\n";
                    if (psiField.getType().getPresentableText().equals("String")) {
                        map2Dto += "        a.set" + upperCase(psiField.getName()) + "(" + mapKey + ");\n  }\n";
                    } else {
                        map2Dto += "        a.set" + upperCase(psiField.getName()) + "(" + psiField.getType().getPresentableText() + ".valueOf(map.get(\"" + psiField.getName() + "\")));\n    }\n";
                    }
                }
            }
            map2Dto += "    return a ;";
            map2Dto += "}";
            PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
            PsiMethod psiMethod = psiElementFactory.createMethodFromText(dto2Map, psiClass);
            PsiMethod map2DtoMethod = psiElementFactory.createMethodFromText(map2Dto, psiClass);
            psiClass.add(psiMethod);
            psiClass.add(map2DtoMethod);
        });
    }

    public String upperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


}
