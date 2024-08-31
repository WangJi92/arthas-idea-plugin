package com.github.idea.json.parser.typevalue;

/**
 * 获取类型的默认值信息
 * {@link  TypeValueAnalysisFactory#initialize() 初始化在这里}
 *
 * @author wangji
 * @date 2024/5/19 13:13
 */
public interface TypeDefaultValue {


    String DEFAULT_NULL = "__default_null__";

    String DEFAULT_MAP_KEY = "_key_";

    /**
     * 获取值的信息 context 可能为空
     *
     * @return
     */
    Object getValue(TypeValueContext context);


    /**
     * 获取包结构信息 (only isSingle need)
     *
     * @return
     */
    default String getQualifiedName() {
        return "";
    }
}
