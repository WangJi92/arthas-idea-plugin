package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.setting.AppSettingsState;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * 剪切板工具类
 *
 * @author 汪小哥
 * @date 20-11-2019
 */
public class ClipboardUtils {

    public static final String CLIPBOARD_TEXT = "%s 已复制到剪切板";

    /**
     * 从剪贴板中获取文本（粘贴）
     */
    public static String getClipboardString() {
        // 获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // 获取剪贴板中的内容
        Transferable trans = clipboard.getContents(null);

        if (trans != null) {
            // 判断剪贴板中的内容是否支持文本
            if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    // 获取剪贴板中的文本内容
                    String text = (String) trans.getTransferData(DataFlavor.stringFlavor);
                    return text;
                } catch (Exception e) {
                    //
                }
            }
        }

        return null;
    }

    /**
     * 把文本设置到剪贴板（复制）
     */
    public static void setClipboardString(String text) {
        try {
            // 获取系统剪贴板
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            // 将中文转换为 unicode  这里写法 通过断点跟踪其实这个 转换的逻辑是ognl 解析表达式的时候搞定的
            // https://github.com/alibaba/arthas/blob/master/site/src/site/sphinx/faq.md#%E8%BE%93%E5%85%A5%E4%B8%AD%E6%96%87unicode%E5%AD%97%E7%AC%A6
            String command = ChineseUnicodeConvert.chineseToUnicode(text);
            // 增加开关控制...
            Project projectName = AppSettingsState.getProject();
            if (projectName != null) {
                AppSettingsState instance = AppSettingsState.getInstance(projectName);
                if (!instance.autoToUnicode) {
                    command = text;
                }
            }
            // 封装文本内容
            Transferable trans = new StringSelection(command);
            // 把文本内容设置到系统剪贴板
            clipboard.setContents(trans, null);
        } catch (Exception e) {
            //
        }
    }


}
