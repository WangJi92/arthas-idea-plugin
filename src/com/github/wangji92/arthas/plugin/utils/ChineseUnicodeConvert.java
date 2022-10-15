package com.github.wangji92.arthas.plugin.utils;

import org.apache.commons.text.translate.UnicodeEscaper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将命令中含有中文的字符串进行转换为 Unicode 编码
 */
public class ChineseUnicodeConvert {

    /**
     * 将命令中含有中文的字符串进行转换为 Unicode 编码
     *
     * @param command
     * @return
     */
    public static String chineseToUnicode(String command) {
        UnicodeEscaper ue = new UnicodeEscaper();
        String regEx = "[\u4e00-\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(command);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(ue.translate(m.group())));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
