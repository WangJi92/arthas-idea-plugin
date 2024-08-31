package com.github.wangji92.arthas.plugin.utils;

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
        switch (canonicalText) {
            case "java.lang.StringBuilder":
            case "java.lang.StringBuffer": {
                return "(#p=new " + psiType.getCanonicalText() + "(),#p.append(\" \"),#p)";
            }

            case "java.math.BigInteger":
                return "(#p=new java.math.BigInteger(\"0\"),#p)";
            case "java.math.BigDecimal":
                return "(#p=@java.math.BigDecimal@valueOf(1L),#p)";
            case "java.net.URL":
            case "java.net.URI":
                return "(#p=new " + psiType.getCanonicalText() + "(\"https://arthas.aliyun.com\"),#p)";
            case "java.util.Date":
                return "(#p=new java.util.Date(),#p)";
            case "java.sql.Date":
                return "(#p=new java.sql.Date(@java.lang.System@currentTimeMillis()),#p)";
            case "java.sql.Timestamp":
                return "(#p=new java.sql.Timestamp(@java.lang.System@currentTimeMillis()),#p)";
            case "java.util.concurrent.atomic.AtomicBoolean":
                return "(#p=new java.util.concurrent.atomic.AtomicBoolean(true),#p)";
            case "java.util.concurrent.atomic.AtomicInteger":
                return "(#p=new java.util.concurrent.atomic.AtomicInteger(1),#p)";
            case "java.util.concurrent.atomic.AtomicLong":
                return "(#p=new java.util.concurrent.atomic.AtomicLong(1L),#p)";
            case "java.util.concurrent.atomic.AtomicIntegerArray":
                return "(#p=new java.util.concurrent.atomic.AtomicIntegerArray(1),#p.set(0,1),#p)";
            case "java.util.concurrent.atomic.DoubleAdder":
                return "(#p=new java.util.concurrent.atomic.DoubleAdder(),#p.add(1.0d),#p)";
            case "java.util.concurrent.atomic.LongAdder":
                return "(#p=new java.util.concurrent.atomic.LongAdder(),#p.add(1L),#p)";
            case "java.util.concurrent.atomic.AtomicReference":
                return "(#p=null,#p)";
            case "java.lang.Object":
            case "java.lang.Runnable":
            case "java.util.concurrent.Callable":
            case "java.util.concurrent.Future":
            case "java.lang.Thread":
            case "java.lang.ThreadGroup":
            case "java.util.Optional":
                return "(#p=null,#p)";
            case "java.lang.Enum":
                return "(#p=null,#p)";
            case "java.lang.ref.WeakReference":
                return "(#p=null,#p)";
            case "java.lang.ref.SoftReference":
            case "java.sql.Clob":
                return "(#p=null,#p)";
            case "java.util.UUID":
                return "(#p=@java.util.UUID@randomUUID(),#p)";
            case "java.text.SimpleDateFormat":
                return "(#p=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\"),#p)";
            case "java.text.DateFormat":
                return "(#p=new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\"),#p)";
            case "java.util.Locale":
                return "(#p=@java.util.Locale@CHINA,#p)";
            case "java.util.Currency":
                return "(#p=@java.util.Currency@getInstance(@java.util.Locale@CHINA),#p)";
            case "java.net.InetAddress":
                return "(#p=@java.net.InetAddress@getByName(\"127.0.0.1\"),#p)";
            case "java.net.Inet4Address":
                return "(#p=@java.net.InetAddress@getByName(\"127.0.0.1\"),#p)";
            case "java.net.Inet6Address":
                return "(#p=@java.net.Inet6Address@Inet6Address(\"2001:0db8:85a3:0000:0000:8a2e:0370:7334\"),#p)";
            case "java.util.regex.Pattern":
                return "(#p=@java.util.regex.Pattern@compile(\"^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$\"),#p)";
            case "java.lang.Throwable":
            case "java.lang.RuntimeException":
                return "(#p=new java.lang.RuntimeException(\"message\"),#p)";
            case "java.nio.charset.Charset":
                return "(#p=@java.nio.charset.StandardCharsets@UTF_8,#p)";
            case "java.util.TimeZone":
                return "(#p=@java.util.TimeZone@getDefault(),#p)";
            case "java.util.Calendar":
                return "(#p=@java.util.Calendar@getInstance(),#p)";
            case "java.time.DayOfWeek":
                return "(#p=@java.time.LocalDate@now().getDayOfWeek(),#p)";
            case "java.time.Duration":
                return "(#p=@java.time.Duration@ofHours(1L),#p)";
            case "java.time.Instant":
                return "(#p=@java.time.Instant@now(),#p)";
            case "java.time.LocalDateTime":
                return "(#p=@java.time.LocalDateTime@now(),#p)";
            case "java.time.LocalDate":
                return "(#p=@java.time.LocalDate@now(),#p)";
            case "java.time.MonthDay":
                return "(#p=@java.time.MonthDay@now(),#p)";
            case "java.time.OffsetDateTime":
                return "(#p=@java.time.OffsetDateTime@now(),#p)";
            case "java.time.OffsetTime":
                return "(#p=@java.time.OffsetTime@now(),#p)";
            case "java.time.YearMonth":
                return "(#p=@java.time.YearMonth@now(),#p)";
            case "java.time.Year":
                return "(#p=@java.time.Year@now(),#p)";
            case "java.time.Period":
                return "(#p=@java.time.Period@ofDays(1),#p)";
            case "java.time.ZonedDateTime":
                return "(#p=@java.time.ZonedDateTime@now(),#p)";
            case "java.time.ZoneId":
                return "(#p=@java.time.ZoneId@systemDefault(),#p)";
            case "java.time.ZoneOffset":
                return "(#p=@java.time.ZoneOffset@UTC,#p)";
            default:
                return null;
        }
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
        if (psiType instanceof PsiWildcardType) {
            PsiWildcardType wildcardType = (PsiWildcardType) psiType;
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
        return String.format(jsonType.getTemplate(), escapeJson, "@" + psiTypeSimpleName + "@class");
    }
}
