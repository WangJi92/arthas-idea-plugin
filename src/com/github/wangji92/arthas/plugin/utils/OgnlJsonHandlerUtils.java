package com.github.wangji92.arthas.plugin.utils;

import com.github.idea.json.parser.PsiParserToJson;
import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiWildcardType;
import org.apache.commons.text.StringEscapeUtils;

/**
 * 解析转换json
 *
 * @author wangji
 * @date 2024/5/23 23:12
 */
public class OgnlJsonHandlerUtils {

    public static JsonType DEFAULT_JSON = null;


    public enum JsonType {
        FASTJSON {
            @Override
            public String getTemplate() {
                return "@com.alibaba.fastjson.JSON@parseObject(\"%s\",%s)";
            }
        },
        JACKSON {
            @Override
            public String getTemplate() {
                return "new com.fasterxml.jackson.databind.ObjectMapper().readValue(\"%s\",%s)";
            }
        },
        GSON {
            @Override
            public String getTemplate() {
                return "new com.google.gson.Gson().fromJson(\"%s\",%s)";
            }
        };


        /**
         * 获取模板
         *
         * @return
         */
        public abstract String getTemplate();
    }

    /**
     * 查询是哪种json
     *
     * @param project
     * @return
     */
    public static JsonType getJsonType(Project project) {
        if (DEFAULT_JSON != null) {
            return DEFAULT_JSON;
        }
        boolean fastJson = PsiToolkit.findClass("com.alibaba.fastjson.JSON", project);
        if (fastJson) {
            DEFAULT_JSON = JsonType.FASTJSON;
            return DEFAULT_JSON;
        }
        boolean jackson = PsiToolkit.findClass("com.fasterxml.jackson.databind.ObjectMapper(", project);
        if (jackson) {
            DEFAULT_JSON = JsonType.JACKSON;
            return DEFAULT_JSON;
        }
        boolean gson = PsiToolkit.findClass("com.google.gson.Gson", project);
        if (gson) {
            DEFAULT_JSON = JsonType.GSON;
            return DEFAULT_JSON;
        }
        DEFAULT_JSON = JsonType.FASTJSON;
        return DEFAULT_JSON;
    }


    /**
     * 来到这里了，肯定是非泛型的数据 .. 不然无法转换ognl json，ognl 不支持 泛型
     *
     * @param psiType
     * @return
     */
    public static String getOgnlJsonDefaultValue(PsiType psiType, Project project) {
        //如果基本类型的直接返回了..
        String basicValue = PsiToolkit.getPsiClassBasicTypeDefaultStringValue(psiType);
        if (basicValue != null) {
            return basicValue;
        }
        PsiType currentPsiType = psiType;
        //处理参数里面哟泛型的
        if (psiType instanceof PsiWildcardType wildcardType) {
            if (wildcardType.isExtends()) {
                // 获取上界限定的类型 上界限定通配符 (? extends T): 指定了类型的上界，表示该类型可以是 T 或 T 的子类。
                currentPsiType = wildcardType.getExtendsBound();

            } else if (wildcardType.isSuper()) {
                // 获取下界限定的类型 下界限定通配符 (? super T): 指定了类型的下界，表示该类型可以是 T 或 T 的超类。
                currentPsiType = wildcardType.getSuperBound();
            }
        }
        if (!(currentPsiType instanceof PsiClassType)) {
            return null;
        }
        String jsonString = PsiParserToJson.getInstance().toJSONString(currentPsiType, false);
        String escapeJson = StringEscapeUtils.escapeJson(jsonString);
        JsonType jsonType = getJsonType(project);
        if (jsonType == null) {
            return null;
        }
        String psiTypeSimpleName = PsiToolkit.getPsiTypeQualifiedNameClazzName((PsiClassType) currentPsiType);
        return jsonType.getTemplate().formatted(escapeJson, "@" + psiTypeSimpleName + "@class");
    }
}
