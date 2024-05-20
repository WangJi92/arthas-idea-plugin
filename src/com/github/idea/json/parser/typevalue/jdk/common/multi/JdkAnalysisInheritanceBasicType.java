package com.github.idea.json.parser.typevalue.jdk.common.multi;

import com.github.idea.json.parser.typevalue.MultiTypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.github.idea.json.parser.typevalue.jdk.time.ZonedDateTimeTypeValue;
import com.intellij.psi.PsiType;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 需要判断是否继承的基本类型
 *
 * @author wangji
 * @date 2024/5/20 22:11
 */
public class JdkAnalysisInheritanceBasicType implements MultiTypeDefaultValue {


    @Override
    public void init() {
        Map<String, Object> container = this.getContainer();
        container.put(Number.class.getName(), 0);
        container.put(CharSequence.class.getName(), " ");
        container.put(BigDecimal.class.getName(), 0.0);
        container.put(java.lang.Throwable.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(java.lang.Runnable.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(Callable.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(Future.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(Thread.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(ThreadGroup.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(Optional.class.getName(), TypeDefaultValue.DEFAULT_NULL);
        container.put(Charset.class.getName(), StandardCharsets.UTF_8);
        container.put(TimeZone.class.getName(), TimeZone.getDefault().getID());
        container.put(Appendable.class.getName(), " ");
        //这个很不常见
        container.put(Enumeration.class.getName(), List.of());

        ZonedDateTimeTypeValue zonedDateTimeTypeValue = new ZonedDateTimeTypeValue();
        Object value = zonedDateTimeTypeValue.getValue(null);
        container.put(Calendar.class.getName(), value);
        container.put(Clob.class.getName(), TypeDefaultValue.DEFAULT_NULL);
    }

    @Override
    public boolean isSupport(TypeValueContext context) {
        PsiType type = context.getType();
        if (type == null) {
            return false;
        }
        for (Map.Entry<String, Object> entry : getContainer().entrySet()) {
            if (context.isInheritor(entry.getKey())) {
                Object result = getContainer().get(entry.getKey());
                context.put(TypeValueContext.RESULT, result);
                return true;
            }
        }
        return false;
    }
}
