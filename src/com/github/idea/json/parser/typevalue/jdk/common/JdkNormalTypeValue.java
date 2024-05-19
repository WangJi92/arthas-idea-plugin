package com.github.idea.json.parser.typevalue.jdk.common;

import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.InheritanceUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.*;

/**
 * @author wangji
 * @date 2024/5/19 14:36
 */
public class JdkNormalTypeValue implements TypeDefaultValue {

    private static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, Object> NORMAL_TYPES = new HashMap<>();

    static {
        NORMAL_TYPES.put(Boolean.class.getName(), true);
        NORMAL_TYPES.put("boolean", true);

        NORMAL_TYPES.put(Byte.class.getName(), 0);
        NORMAL_TYPES.put("byte", 0);

        NORMAL_TYPES.put(Short.class.getName(), 0);
        NORMAL_TYPES.put("short", 0);

        NORMAL_TYPES.put(Integer.class.getName(), 0);
        NORMAL_TYPES.put("int", 0);

        NORMAL_TYPES.put(Long.class.getName(), 0L);
        NORMAL_TYPES.put("long", 0L);

        NORMAL_TYPES.put(Float.class.getName(), 0.0F);
        NORMAL_TYPES.put("float", 0.0F);

        NORMAL_TYPES.put(Double.class.getName(), 0.0D);
        NORMAL_TYPES.put("double", 0.0D);

        NORMAL_TYPES.put(Number.class.getName(), 0);

        NORMAL_TYPES.put("java.lang.Character", 'c');
        NORMAL_TYPES.put("char", 'c');

        NORMAL_TYPES.put(String.class.getName(), " ");
        NORMAL_TYPES.put(CharSequence.class.getName(), " ");
        NORMAL_TYPES.put(StringBuilder.class.getName(), " ");
        NORMAL_TYPES.put(StringBuffer.class.getName(), " ");
        NORMAL_TYPES.put(BigDecimal.class.getName(), 0.0);
        NORMAL_TYPES.put(BigInteger.class.getName(), "0");
        try {
            URL url = new URL("https://arthas.aliyun.com/");
            NORMAL_TYPES.put("java.net.URL", url);

            URI uri = new URI("https://arthas.aliyun.com/");
            NORMAL_TYPES.put("java.net.URI", uri);
        } catch (Exception e) {
        }
        NORMAL_TYPES.put(Date.class.getName(), DATEFORMAT.format(new Date()));
        NORMAL_TYPES.put(java.sql.Date.class.getName(), DATEFORMAT.format(new Date()));
        NORMAL_TYPES.put(java.sql.Timestamp.class.getName(), System.currentTimeMillis());

        NORMAL_TYPES.put(AtomicBoolean.class.getName(), new AtomicBoolean(true));
        NORMAL_TYPES.put(AtomicInteger.class.getName(), new AtomicInteger(1));
        AtomicIntegerArray atomicArray = new AtomicIntegerArray(1);
        atomicArray.set(0, 1);
        NORMAL_TYPES.put(AtomicIntegerArray.class.getName(), atomicArray);

        NORMAL_TYPES.put(AtomicLong.class.getName(), new AtomicLong(1L));
        AtomicLongArray atomicLongArray = new AtomicLongArray(1);
        atomicLongArray.set(0, 1L);
        NORMAL_TYPES.put(AtomicLongArray.class.getName(), atomicLongArray);

        DoubleAdder doubleAdder = new DoubleAdder();
        doubleAdder.add(1.0d);
        NORMAL_TYPES.put(DoubleAdder.class.getName(), doubleAdder);

        LongAdder longAdder = new LongAdder();
        longAdder.add(1L);
        NORMAL_TYPES.put(LongAdder.class.getName(), longAdder);
        // 异常的不处理
        NORMAL_TYPES.put(java.lang.Throwable.class.getName(), null);
        NORMAL_TYPES.put(java.lang.Object.class.getName(), null);
        NORMAL_TYPES.put(java.lang.Enum.class.getName(), null);
        NORMAL_TYPES.put(java.lang.Runnable.class.getName(), null);
        NORMAL_TYPES.put(Callable.class.getName(), null);
        NORMAL_TYPES.put(Future.class.getName(), null);
        NORMAL_TYPES.put(Thread.class.getName(), null);
        NORMAL_TYPES.put(ThreadGroup.class.getName(), null);
        NORMAL_TYPES.put(UUID.class.getName(), UUID.randomUUID().toString());
        NORMAL_TYPES.put(Charset.class.getName(), StandardCharsets.UTF_8);

    }

    @Override
    public Object getValue(TypeValueContext context) {
        return context.get(TypeValueContext.RESULT);
    }

    @Override
    public String getQualifiedName(TypeValueContext context) {
        return (String) context.get(TypeValueContext.QUALIFIED_NAME);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        if (type == null) {
            return false;
        }
        String canonicalText = type.getCanonicalText();
        Object result = NORMAL_TYPES.get(canonicalText);
        if (result != null) {
            context.put(TypeValueContext.RESULT, result);
            context.put(TypeValueContext.QUALIFIED_NAME, canonicalText);
            return true;
        }
        if (canonicalText.startsWith("java") || canonicalText.startsWith("jdk") || canonicalText.startsWith("sun")) {
            if (canonicalText.startsWith("java.io") || canonicalText.startsWith("java.lang.reflect") || canonicalText.startsWith("java.util.concurrent.locks") || canonicalText.startsWith("javax") || canonicalText.startsWith("java.text") || canonicalText.startsWith("java.sql") || canonicalText.startsWith("java.security") || canonicalText.startsWith("java.rmi") || canonicalText.startsWith("java.nio") || canonicalText.startsWith("java.net") || canonicalText.startsWith("java.math") || canonicalText.startsWith("java.beans") || canonicalText.startsWith("java.awt") || canonicalText.startsWith("java.apple") || canonicalText.startsWith("jdk") || canonicalText.startsWith("sun") || InheritanceUtil.isInheritor(context.getType(), Throwable.class.getName())) {
                //io 的包忽略 异常的忽略
                context.put(TypeValueContext.RESULT, null);
                context.put(TypeValueContext.QUALIFIED_NAME, canonicalText);
                return true;
            }
        }

        if (InheritanceUtil.isInheritor(context.getType(), Number.class.getName())) {
            //number 的处理
            context.put(TypeValueContext.RESULT, NORMAL_TYPES.get(Number.class.getName()));
            context.put(TypeValueContext.QUALIFIED_NAME, canonicalText);
            return true;
        }

        if (InheritanceUtil.isInheritor(context.getType(), CharSequence.class.getName())) {
            // 字符串的处理
            context.put(TypeValueContext.RESULT, NORMAL_TYPES.get(CharSequence.class.getName()));
            context.put(TypeValueContext.QUALIFIED_NAME, canonicalText);
            return true;
        }

        return false;
    }
}
