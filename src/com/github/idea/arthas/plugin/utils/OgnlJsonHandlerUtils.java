package com.github.idea.arthas.plugin.utils;

import com.github.idea.json.parser.PsiParserToJson;
import com.github.idea.json.parser.toolkit.ParserContext;
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

            @Override
            public ParserContext.ParserJsonType getType() {
                return ParserContext.ParserJsonType.FASTJSON;
            }
        },
        FASTJSON_2 {
            @Override
            public String getTemplate() {
                return "@com.alibaba.fastjson2.JSON@parseObject(\"%s\",%s)";
            }

            @Override
            public ParserContext.ParserJsonType getType() {
                return ParserContext.ParserJsonType.FASTJSON_2;
            }
        },
        JACKSON {
            @Override
            public String getTemplate() {
                return "new com.fasterxml.jackson.databind.ObjectMapper().readValue(\"%s\",%s)";
            }

            @Override
            public ParserContext.ParserJsonType getType() {
                return ParserContext.ParserJsonType.JACKSON;
            }
        },
        GSON {
            @Override
            public String getTemplate() {
                return "new com.google.gson.Gson().fromJson(\"%s\",%s)";
            }

            @Override
            public ParserContext.ParserJsonType getType() {
                return ParserContext.ParserJsonType.GSON;
            }
        };


        /**
         * 获取模板
         *
         * @return
         */
        public abstract String getTemplate();

        public abstract ParserContext.ParserJsonType getType();
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
        boolean fastJson2 = PsiToolkit.findClass("com.alibaba.fastjson2.JSON", project);
        if (fastJson2) {
            DEFAULT_JSON = JsonType.FASTJSON_2;
            return DEFAULT_JSON;
        }
        boolean jackson = PsiToolkit.findClass("com.fasterxml.jackson.databind.ObjectMapper", project);
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
     * 获取这些基本类型的ognl 表达式
     *
     * @param psiType
     * @return
     */
    public static String getPsiClassJDKBaseTypeDefaultOgnl(PsiType psiType) {
        String canonicalText = PsiToolkit.getPsiTypeSimpleName(psiType);
        return switch (canonicalText) {
            case "java.lang.StringBuilder", "java.lang.StringBuffer" ->
                    "(#p=new " + psiType.getCanonicalText() + "(),#p.append(\" \"),#p)";
            case "java.math.BigInteger" -> "(#p=new java.math.BigInteger(\"0\"),#p)";
            case "java.math.BigDecimal" -> "(#p=@java.math.BigDecimal@valueOf(1L),#p)";
            case "java.net.URL", "java.net.URI" ->
                    "(#p=new " + psiType.getCanonicalText() + "(\"https://arthas.aliyun.com\"),#p)";
            case "java.util.Date" -> "(#p=new java.util.Date(),#p)";
            case "java.sql.Date" -> "(#p=new java.sql.Date(@java.lang.System@currentTimeMillis()),#p)";
            case "java.sql.Timestamp" -> "(#p=new java.sql.Timestamp(@java.lang.System@currentTimeMillis()),#p)";
            case "java.util.concurrent.atomic.AtomicBoolean" ->
                    "(#p=new java.util.concurrent.atomic.AtomicBoolean(true),#p)";
            case "java.util.concurrent.atomic.AtomicInteger" ->
                    "(#p=new java.util.concurrent.atomic.AtomicInteger(1),#p)";
            case "java.util.concurrent.atomic.AtomicLong" -> "(#p=new java.util.concurrent.atomic.AtomicLong(1L),#p)";
            case "java.util.concurrent.atomic.AtomicIntegerArray" ->
                    "(#p=new java.util.concurrent.atomic.AtomicIntegerArray(1),#p.set(0,1),#p)";
            case "java.util.concurrent.atomic.DoubleAdder" ->
                    "(#p=new java.util.concurrent.atomic.DoubleAdder(),#p.add(1.0d),#p)";
            case "java.util.concurrent.atomic.LongAdder" ->
                    "(#p=new java.util.concurrent.atomic.LongAdder(),#p.add(1L),#p)";
            case "java.util.concurrent.atomic.AtomicReference" -> "(#p=null,#p)";
            case "java.lang.Object", "java.lang.Runnable", "java.util.concurrent.Callable",
                 "java.util.concurrent.Future", "java.lang.Thread", "java.lang.ThreadGroup", "java.util.Optional" ->
                    "(#p=null,#p)";
            case "java.lang.Enum" -> "(#p=null,#p)";
            case "java.lang.ref.WeakReference" -> "(#p=null,#p)";
            case "java.lang.ref.SoftReference", "java.sql.Clob" -> "(#p=null,#p)";
            case "java.util.UUID" -> "(#p=@java.util.UUID@randomUUID(),#p)";
            case "java.text.SimpleDateFormat" -> "(#p=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\"),#p)";
            case "java.text.DateFormat" -> "(#p=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\"),#p)";
            case "java.util.Locale" -> "(#p=@java.util.Locale@CHINA,#p)";
            case "java.util.Currency" -> "(#p=@java.util.Currency@getInstance(@java.util.Locale@CHINA),#p)";
            case "java.net.InetAddress" -> "(#p=@java.net.InetAddress@getByName(\"127.0.0.1\"),#p)";
            case "java.net.Inet4Address" -> "(#p=@java.net.InetAddress@getByName(\"127.0.0.1\"),#p)";
            case "java.net.Inet6Address" ->
                    "(#p=@java.net.Inet6Address@Inet6Address(\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\"),#p)";
            case "java.util.regex.Pattern" ->
                    "(#p=@java.util.regex.Pattern@compile(\"^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$\"),#p)";
            case "java.lang.Throwable", "java.lang.RuntimeException" ->
                    "(#p=new java.lang.RuntimeException(\"message\"),#p)";
            case "java.nio.charset.Charset" -> "(#p=@java.nio.charset.StandardCharsets@UTF_8,#p)";
            case "java.util.TimeZone" -> "(#p=@java.util.TimeZone@getDefault(),#p)";
            case "java.util.Calendar" -> "(#p=@java.util.Calendar@getInstance(),#p)";
            case "java.time.DayOfWeek" -> "(#p=@java.time.LocalDate@now().getDayOfWeek(),#p)";
            case "java.time.Duration" -> "(#p=@java.time.Duration@ofHours(1L),#p)";
            case "java.time.Instant" -> "(#p=@java.time.Instant@now(),#p)";
            case "java.time.LocalDateTime" -> "(#p=@java.time.LocalDateTime@now(),#p)";
            case "java.time.LocalDate" -> "(#p=@java.time.LocalDate@now(),#p)";
            case "java.time.MonthDay" -> "(#p=@java.time.MonthDay@now(),#p)";
            case "java.time.OffsetDateTime" -> "(#p=@java.time.OffsetDateTime@now(),#p)";
            case "java.time.OffsetTime" -> "(#p=@java.time.OffsetTime@now(),#p)";
            case "java.time.YearMonth" -> "(#p=@java.time.YearMonth@now(),#p)";
            case "java.time.Year" -> "(#p=@java.time.Year@now(),#p)";
            case "java.time.Period" -> "(#p=@java.time.Period@ofDays(1),#p)";
            case "java.time.ZonedDateTime" -> "(#p=@java.time.ZonedDateTime@now(),#p)";
            case "java.time.ZoneId" -> "(#p=@java.time.ZoneId@systemDefault(),#p)";
            case "java.time.ZoneOffset" -> "(#p=@java.time.ZoneOffset@UTC,#p)";
            default -> null;
        };
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

        //如果基本类型可以处理，不需要走json 字符串去处理了
        String psiClassJDKBaseTypeDefaultOgnl = getPsiClassJDKBaseTypeDefaultOgnl(currentPsiType);
        if (psiClassJDKBaseTypeDefaultOgnl != null) {
            return psiClassJDKBaseTypeDefaultOgnl;
        }
        JsonType jsonType = getJsonType(project);
        if (jsonType == null) {
            return null;
        }

        // 根据ognl的json 类型 处理具体的类型
        ParserContext parserContext = new ParserContext();
        parserContext.setPretty(false);
        parserContext.setJsonType(jsonType.getType());

        String jsonString = PsiParserToJson.getInstance().toJSONString(currentPsiType, parserContext);
        String escapeJson = StringEscapeUtils.escapeJson(jsonString);

        String psiTypeSimpleName = PsiToolkit.getPsiTypeQualifiedNameClazzName((PsiClassType) currentPsiType);
        if(StringUtils.isBlank(jsonString) || StringUtils.isBlank(psiTypeSimpleName)){
            // https://github.com/WangJi92/arthas-idea-plugin/issues/156
            return null;
        }
        return jsonType.getTemplate().formatted(escapeJson, "@" + psiTypeSimpleName + "@class");
    }
}
