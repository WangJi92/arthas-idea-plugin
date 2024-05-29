package com.github.idea.json.parser.typevalue.jdk.common.multi;

import com.github.idea.json.parser.typevalue.MultiTypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.InheritanceUtil;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.net.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.regex.Pattern;

/**
 * @author wangji
 * @date 2024/5/19 14:36
 */
public class JdkBasicTypeValue implements MultiTypeDefaultValue {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public JdkBasicTypeValue() {
        this.init();
    }

    private final Map<String, Object> container = new HashMap<>(16);

    @Override
    public Map<String, Object> getContainer() {
        return container;
    }

    public void init() {
        Map<String, Object> container = this.getContainer();
        container.put(Boolean.class.getName(), true);
        container.put("boolean", true);

        container.put(Byte.class.getName(), 0);
        container.put("byte", 0);

        container.put(Short.class.getName(), 0);
        container.put("short", 0);

        container.put(Integer.class.getName(), 0);
        container.put("int", 0);

        container.put(Long.class.getName(), 0L);
        container.put("long", 0L);

        container.put(Float.class.getName(), 0.0F);
        container.put("float", 0.0F);

        container.put(Double.class.getName(), 0.0D);
        container.put("double", 0.0D);

        container.put("java.lang.Character", 'c');
        container.put("char", 'c');

        container.put(String.class.getName(), " ");
        container.put(StringBuilder.class.getName(), " ");
        container.put(StringBuffer.class.getName(), " ");
        container.put(BigInteger.class.getName(), "0");
        try {
            URL url = new URL("https://arthas.aliyun.com/");
            container.put("java.net.URL", url);

            URI uri = new URI("https://arthas.aliyun.com/");
            container.put("java.net.URI", uri);
        } catch (Exception e) {
        }
        container.put(Date.class.getName(), new Date());
        container.put(java.sql.Date.class.getName(), new java.sql.Date(System.currentTimeMillis()));
        container.put(java.sql.Timestamp.class.getName(), new Timestamp(System.currentTimeMillis()));

        container.put(AtomicBoolean.class.getName(), new AtomicBoolean(true));
        container.put(AtomicReference.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(AtomicInteger.class.getName(), new AtomicInteger(1));
        AtomicIntegerArray atomicArray = new AtomicIntegerArray(1);
        atomicArray.set(0, 1);
        container.put(AtomicIntegerArray.class.getName(), atomicArray);

        container.put(AtomicLong.class.getName(), new AtomicLong(1L));
        AtomicLongArray atomicLongArray = new AtomicLongArray(1);
        atomicLongArray.set(0, 1L);
        container.put(AtomicLongArray.class.getName(), atomicLongArray);

        DoubleAdder doubleAdder = new DoubleAdder();
        doubleAdder.add(1.0d);
        container.put(DoubleAdder.class.getName(), doubleAdder);

        LongAdder longAdder = new LongAdder();
        longAdder.add(1L);
        container.put(LongAdder.class.getName(), longAdder);
        container.put(java.lang.Object.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(java.lang.Enum.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(WeakReference.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(SoftReference.class.getName(), TypeDefaultValue.DEFAULT_NULL);

        container.put(UUID.class.getName(), UUID.randomUUID());

        container.put(SimpleDateFormat.class.getName(), DATE_FORMAT);
        Locale locale = Locale.CHINA;
        container.put(Locale.class.getName(), locale);

        Currency currency = Currency.getInstance(locale);
        container.put(Currency.class.getName(), currency);
        container.put(InetAddress.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(Inet4Address.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(Inet6Address.class.getName(), TypeDefaultValue.DEFAULT_NULL);

        container.put(InetSocketAddress.class.getName(), TypeDefaultValue.DEFAULT_NULL);

        container.put(Pattern.class.getName(),Pattern.compile( "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$"));


    }

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        String canonicalText = type.getCanonicalText();
        if (canonicalText.startsWith("java") || canonicalText.startsWith("jdk") || canonicalText.startsWith("sun")) {
            if (canonicalText.startsWith("java.io")
                    || canonicalText.startsWith("java.lang.reflect")
                    //Optional 序列化不稳定，eg fastjson 需要自定义..
                    || canonicalText.startsWith("java.util.Optional")
                    || canonicalText.startsWith("java.util.concurrent.locks")
                    || canonicalText.startsWith("java.text")
                    || canonicalText.startsWith("java.sql")
                    || canonicalText.startsWith("java.security")
                    || canonicalText.startsWith("java.rmi")
                    || canonicalText.startsWith("java.nio")
                    || canonicalText.startsWith("java.net")
                    || canonicalText.startsWith("java.math")
                    || canonicalText.startsWith("java.beans")
                    || canonicalText.startsWith("java.awt")
                    || canonicalText.startsWith("java.apple")
                    || canonicalText.startsWith("jdk")
                    || canonicalText.startsWith("sun")
                    || InheritanceUtil.isInheritor(context.getType(), Throwable.class.getName())) {
                //io 的包忽略 异常的忽略
                context.setResult(TypeDefaultValue.DEFAULT_NULL);
                return true;
            }
        }
        return false;
    }
}
