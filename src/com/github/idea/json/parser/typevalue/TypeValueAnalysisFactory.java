package com.github.idea.json.parser.typevalue;

import com.intellij.psi.PsiType;
import org.reflections.Reflections;

import java.util.*;

/**
 *
 * @author wangji
 * @date 2024/5/19 16:32
 */
public class TypeValueAnalysisFactory {
    /**
     * 和class 一一对应的类
     */
    private final Map<String, TypeDefaultValue> singleTypeValueMap = new HashMap<>(30);
    /**
     * 一个类可以处理多个类型
     */
    private final List<MultiTypeDefaultValue> multiTypeValueList = new LinkedList<>();

    private TypeValueAnalysisFactory() {
        initialize();
    }

    private void initialize() {
        Reflections reflections = new Reflections(TypeDefaultValue.class.getPackageName());
        Set<Class<? extends TypeDefaultValue>> typeDefaultValueClazz = reflections.getSubTypesOf(TypeDefaultValue.class);
        for (Class<? extends TypeDefaultValue> clazz : typeDefaultValueClazz) {
            try {
                TypeDefaultValue typeDefaultValue = clazz.getDeclaredConstructor().newInstance();
                if (typeDefaultValue instanceof MultiTypeDefaultValue) {
                    multiTypeValueList.add((MultiTypeDefaultValue) typeDefaultValue);
                    // 批量里面也有单个的数据，方便加快速度
                    for (String qualifiedName : ((MultiTypeDefaultValue) typeDefaultValue).getQualifiedNames()) {
                        singleTypeValueMap.put(qualifiedName, typeDefaultValue);
                    }
                } else {
                    singleTypeValueMap.put(typeDefaultValue.getQualifiedName(), typeDefaultValue);
                }
            } catch (Exception e) {
            }
        }
    }

    private static class SingletonHolder {
        private static final TypeValueAnalysisFactory INSTANCE = new TypeValueAnalysisFactory();
    }

    public static TypeValueAnalysisFactory getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 获取值的信息
     *
     * @param type
     * @return
     */
    public TypeValueContext getValue(PsiType type) {
        //如果基本类型能够处理
        TypeDefaultValue typeDefaultValue = singleTypeValueMap.get(type.getCanonicalText());
        TypeValueContext context = new TypeValueContext(type);
        if (typeDefaultValue != null) {
            Object value = typeDefaultValue.getValue(context);
            context.setResult(value);
            return context;
        }
        for (MultiTypeDefaultValue multiTypeValue : multiTypeValueList) {
            if (multiTypeValue.isSupport(context)) {
                Object value = multiTypeValue.getValue(context);
                context.setResult(value);
                return context;
            }
        }
        return context;
    }


}
