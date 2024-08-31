package com.github.idea.json.parser.typevalue;

import com.github.idea.json.parser.toolkit.PsiToolkit;

import java.util.Map;
import java.util.Set;

/**
 * 有多个类型
 *
 * @author wangji
 * @date 2024/5/20 22:26
 */
public interface MultiTypeDefaultValue extends TypeDefaultValue {

    /**
     * 多个处理 把静态和动态结合起来了.
     * @param context
     * @return
     */
    @Override
    default Object getValue(TypeValueContext context) {
        if (context.getSupport()) {
            // 动态处理的;
            return context.getResult();
        }
        if (this.getContainer() != null) {
            //静态处理
            Object quicklyResult = this.getContainer().get(PsiToolkit.getPsiTypeSimpleName(context.getType()));
            if (quicklyResult != null) {
                return quicklyResult;
            }
        }
        return DEFAULT_NULL;
    }


    /**
     * 获取容器
     *
     * @return
     */
    default Map<String, Object> getContainer() {
        return null;
    }


    /**
     * 获取多个名称
     *
     * @return
     */
    default Set<String> getQualifiedNames() {
        return this.getContainer() != null ? this.getContainer().keySet() : Set.of();
    }

    /**
     * 动态的部分是否支持
     *
     * @param context
     * @return
     */
    boolean isSupport(TypeValueContext context);
}
