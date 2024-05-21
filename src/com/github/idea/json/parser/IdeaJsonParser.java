package com.github.idea.json.parser;

import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.github.idea.json.parser.toolkit.model.JPsiType;
import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueAnalysisFactory;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 解析 默认的json 字符串信息
 *
 * @author wangji
 * @date 2024/5/19 22:08
 */
public class IdeaJsonParser {

    private static final Logger LOG = Logger.getInstance(IdeaJsonParser.class);

    private final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

    private final TypeValueAnalysisFactory typeValueAnalysisFactory = TypeValueAnalysisFactory.getInstance();

    private static class SingletonHolder {
        private static final IdeaJsonParser INSTANCE = new IdeaJsonParser();
    }

    public static IdeaJsonParser getInstance() {
        return IdeaJsonParser.SingletonHolder.INSTANCE;
    }

    private IdeaJsonParser() {
    }

    /**
     * 转换为json 字符
     *
     * @param psiParameter
     * @return
     */
    public String toJSONString(@NotNull final PsiParameter psiParameter) {
        try {
            PsiType type = psiParameter.getType();
            JPsiType jPsiType = new JPsiType(type, getPsiClassGenerics(type), null, -1);
            Object object = parseVariableValue(jPsiType);
            if (!Objects.equals(TypeDefaultValue.DEFAULT_NULL, object)) {
                return gsonBuilder.create().toJson(object);
            }
            return null;
        } catch (Exception e) {
            LOG.error("to json error", e);
        }
        return null;
    }

    /**
     * 解析clazz
     *
     * @param currentParseJPsiClazzType
     * @return
     */
    private Object parseClass(JPsiType currentParseJPsiClazzType) {
        if(!(currentParseJPsiClazzType.getPsiType() instanceof  PsiClassType)){
            return TypeDefaultValue.DEFAULT_NULL;
        }
        PsiClass psiClass = ((PsiClassType) currentParseJPsiClazzType.getPsiType()).resolve();
        assert psiClass != null;
        if (checkClassIgnore(psiClass)) {
            return TypeDefaultValue.DEFAULT_NULL;
        }
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        for (PsiField field : psiClass.getAllFields()) {
            try {
                if (checkIgnoreModifierP(field)) {
                    continue;
                }

                if (checkIgnoreField(field)) {
                    continue;
                }
                // key
                String fieldKey = checkGetFieldName(field);

                //region 字母常量的默认值
                PsiExpression psiExpression = field.getInitializer();
                Object fieldValue = null;
                if (psiExpression instanceof PsiLiteralExpression) {
                    // 这个字段有默认值信息，且为字面量信息，直接获取结果。
                    fieldValue = ((PsiLiteralExpression) psiExpression).getValue();
                }
                //endregion

                if (fieldValue == null) {
                    JPsiType jPsiType = currentParseJPsiClazzType.copyNew(field);
                    fieldValue = parseVariableValue(jPsiType);
                }

                if (!Objects.equals(TypeDefaultValue.DEFAULT_NULL, fieldValue)) {
                    linkedHashMap.put(fieldKey, fieldValue);
                }
            } catch (Exception e) {
                LOG.error("get file json error " + field.getName(), e);
            }
        }
        if (currentParseJPsiClazzType.getRecursionLevel() > 0 && psiClass.getAllFields().length == 0) {
            return linkedHashMap.isEmpty() ? null : linkedHashMap;
        }
        return linkedHashMap;
    }

    /**
     * 获取字段的名称
     *
     * @param field
     * @return
     */
    private String checkGetFieldName(PsiField field) {
        if (field.getAnnotations().length > 0) {
            // 存在注解才需要去处理 获取注解的值信息
            return parseFieldName(field);
        }
        return field.getName();
    }

    /**
     * 检测 忽略的字段
     *
     * @param field
     * @return
     */
    private static boolean checkIgnoreField(PsiField field) {
        if (field.getAnnotations().length > 0) {
            // jackson 不需要序列化
            PsiAnnotation annotationJsonIgnore = field.getAnnotation("com.fasterxml.jackson.annotation.JsonIgnore");
            if (annotationJsonIgnore != null) {
                return true;
            }

            // fastjson 不需要序列化
            PsiAnnotation annotationJSONField = field.getAnnotation("com.alibaba.fastjson.annotation.JSONField");
            if (annotationJSONField != null) {
                String serialize = Objects.requireNonNull(annotationJSONField.findAttributeValue("serialize")).getText();
                if (StringUtils.isNotBlank(serialize)) {
                    if (Objects.equals(serialize, "false")) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    /**
     * 检测忽略的属性
     *
     * @param field
     * @return
     */
    private static boolean checkIgnoreModifierP(PsiField field) {
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            return true;
        } else if (field.hasModifierProperty(PsiModifier.TRANSIENT)) {
            return true;
        } else if (field.hasModifierProperty(PsiModifier.NATIVE)) {
            return true;
        }
        return false;
    }

    /**
     * 检测是否需要忽略
     *
     * @param psiClass
     * @return
     */
    private static Boolean checkClassIgnore(PsiClass psiClass) {
        if (psiClass.getAnnotations().length > 0) {
            PsiAnnotation annotation = psiClass.getAnnotation("com.fasterxml.jackson.annotation.JsonIgnoreType");
            if (annotation != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取字段的名称
     *
     * @param field
     * @return
     */
    private String parseFieldName(PsiField field) {
        PsiAnnotation annotationJacksonJsonProperty = field.getAnnotation("com.fasterxml.jackson.annotation.JsonProperty");
        if (annotationJacksonJsonProperty != null) {
            String fieldName = Objects.requireNonNull(annotationJacksonJsonProperty.findAttributeValue("value")).getText();
            if (StringUtils.isNotBlank(fieldName)) {
                //""fileName""
                return fieldName.substring(1, fieldName.length() - 1);
            }
        }
        PsiAnnotation annotationFastJsonJSONField = field.getAnnotation("com.alibaba.fastjson.annotation.JSONField");
        ;
        if (annotationFastJsonJSONField != null) {
            String fieldName = Objects.requireNonNull(annotationFastJsonJSONField.findAttributeValue("name")).getText();
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName.substring(1, fieldName.length() - 1);
            }
        }
        return field.getName();
    }

    private Object parseVariableValue(JPsiType currenParsetJpsiType) {
        if (currenParsetJpsiType.getRecursionLevel() >= 200) {
            //递归太多了次数直接返回 null
            return TypeDefaultValue.DEFAULT_NULL;
        }
        PsiType type = currenParsetJpsiType.getPsiType();
        if (type instanceof PsiPrimitiveType) {
            //primitive Type
            TypeValueContext value = typeValueAnalysisFactory.getValue(type);
            return value.getResult();
        } else if (type instanceof PsiArrayType) {
            //array type also support PsiEllipsisType
            PsiType typeToDeepType = type.getDeepComponentType();
            Object obj = parseVariableValue(currenParsetJpsiType.copyNew(typeToDeepType, getPsiClassGenerics(typeToDeepType)));
            return obj != null ? List.of(obj) : List.of();
        } else if (type instanceof PsiClassType currentParseIdeaPsiClassType) {
            TypeValueContext quickProcessValue = typeValueAnalysisFactory.getValue(type);
            if (quickProcessValue.getSupport()) {
                // 快速处理获取结果，比如一些常见的数据类型
                // 之前已经定义的一些数据
                //Enum 处理
                return quickProcessValue.getResult();
            }
            //reference Type
            PsiClass psiClass = currentParseIdeaPsiClassType.resolve();
            if (psiClass == null) {
                return TypeDefaultValue.DEFAULT_NULL;
            }

            // 检测泛型参数
            PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
            if (typeParameters.length == 1) {
                PsiClassType rawType = currentParseIdeaPsiClassType.rawType();
                JPsiType rawJPsiType = new JPsiType(rawType);
                if (rawJPsiType.isInheritor(Collection.class.getName())) {
                    // Set<String> List<Demo<String>> ..why not startsWith("java.")?
                    PsiType[] parameters = currentParseIdeaPsiClassType.getParameters();
                    if (parameters.length == 1) {
                        Object obj = parseVariableValue(currenParsetJpsiType.copyNew(parameters[0], getPsiClassGenerics(parameters[0])));
                        return obj != null ? List.of(obj) : List.of();
                    }
                    // List 没有写泛型..
                    return List.of();
                }

                if (type.getCanonicalText().startsWith("java.")) {
                    // 提速
                    if (rawJPsiType.isInheritor(Class.class.getName())) {
                        // Class clazz  ,Class<User> clazz2
                        PsiType[] parameters = currentParseIdeaPsiClassType.getParameters();
                        if (parameters.length == 0) {
                            //没有泛型类型，为空直接 null
                            return TypeDefaultValue.DEFAULT_NULL;
                        }
                        // clazz 直接返回这个类的字符串
                        PsiClassType classType = (PsiClassType) parameters[0];
                        PsiClass currentContainingClass = classType.resolve();
                        assert currentContainingClass != null;
                        PsiClass nextContainingClass = currentContainingClass.getContainingClass();
                        if (nextContainingClass == null) {
                            // 不是内部类
                            return parameters[0].getCanonicalText();
                        }
                        // 内部类的处理 OutClass$InnerClass
                        List<String> qualifiedClassNameArray = Lists.newArrayList();
                        qualifiedClassNameArray.add(Objects.requireNonNull(currentContainingClass.getNameIdentifier()).getText());
                        currentContainingClass = currentContainingClass.getContainingClass();
                        while (currentContainingClass != null) {
                            qualifiedClassNameArray.add("$");
                            String name = Objects.requireNonNull(currentContainingClass.getNameIdentifier()).getText();
                            nextContainingClass = currentContainingClass.getContainingClass();
                            if (nextContainingClass == null) {
                                qualifiedClassNameArray.add(currentContainingClass.getQualifiedName());
                            } else {
                                qualifiedClassNameArray.add(name);
                            }
                            currentContainingClass = nextContainingClass;
                        }
                        Collections.reverse(qualifiedClassNameArray);
                        return String.join("", qualifiedClassNameArray);
                    }
                }

            } else if (typeParameters.length == 2) {
                PsiClassType rawType = currentParseIdeaPsiClassType.rawType();
                // 特殊处理Map
                JPsiType rawJPsiType = new JPsiType(rawType);
                if (rawJPsiType.isInheritor(Map.class.getName())) {
                    PsiType[] parameters = currentParseIdeaPsiClassType.getParameters();
                    if (parameters.length == 2) {
                        Object obj = parseVariableValue(currenParsetJpsiType.copyNew(parameters[1], getPsiClassGenerics(parameters[1])));
                        return obj != null ? Map.of(" ", obj) : new HashMap<>();
                    }
                    // Map 没有写泛型..
                    return new HashMap<>();
                }
            }

            //not standard Map、List  CustomMap extends HashMap<String,String>
            // current psiClazz not type generics
            // simple handler ignore type generics
            if (currenParsetJpsiType.isInheritor(Map.class.getName())) {
                return Map.of();
            } else if (currenParsetJpsiType.isInheritor(Collection.class.getName())) {
                return List.of();
            }

            if (currenParsetJpsiType.getPsiTypeGenerics() != null) {
                PsiType typeToDeepType = currenParsetJpsiType.getPsiTypeGenerics().get(psiClass.getName());
                if (typeToDeepType != null) {
                    return parseVariableValue(currenParsetJpsiType.copyNew(typeToDeepType, getPsiClassGenerics(typeToDeepType)));
                }
            }
            if (typeParameters.length == 0) {
                // 没有泛型参数
                return parseClass(currenParsetJpsiType.copyNew(currentParseIdeaPsiClassType,null));
            }
            // Test<User,String> ..
            return parseClass(currenParsetJpsiType.copyNew(currentParseIdeaPsiClassType, getPsiClassGenerics(type)));
        }
        return TypeDefaultValue.DEFAULT_NULL;
    }


    /**
     * 获取 type的泛型信息
     *
     * @param type
     * @return
     */
    private Map<String, PsiType> getPsiClassGenerics(PsiType type) {
        return PsiToolkit.getPsiClassGenerics(type);
    }


}
