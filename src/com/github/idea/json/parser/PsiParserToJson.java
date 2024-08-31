package com.github.idea.json.parser;

import com.github.idea.json.parser.toolkit.ParserContext;
import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.github.idea.json.parser.toolkit.model.JPsiTypeContext;
import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.github.idea.json.parser.typevalue.TypeValueAnalysisFactory;
import com.github.idea.json.parser.typevalue.TypeValueContext;
import com.google.common.collect.Lists;
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
public class PsiParserToJson {

    private static final Logger LOG = Logger.getInstance(PsiParserToJson.class);

    private final TypeValueAnalysisFactory typeValueAnalysisFactory = TypeValueAnalysisFactory.getInstance();

    private static class SingletonHolder {
        private static final PsiParserToJson INSTANCE = new PsiParserToJson();
    }

    public static PsiParserToJson getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PsiParserToJson() {
    }

    /**
     * 转换为json 字符
     *
     * @param psiType
     * @return
     */
    public String toJSONString(@NotNull final PsiType psiType, ParserContext context) {
        try {
            JPsiTypeContext JPsiTypeContext = new JPsiTypeContext(psiType, true);
            Object object = parseVariableValue(JPsiTypeContext);
            if (!Objects.equals(TypeDefaultValue.DEFAULT_NULL, object)) {
                return context.toJsonString(object);
            }
            return null;
        } catch (Exception e) {
            LOG.error("to json error", e);
        }
        return null;
    }


    public String toJSONString(@NotNull final PsiElement psiElement, ParserContext context) {
        if (psiElement instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) psiElement;
            PsiType psiType = PsiToolkit.getPsiTypeByPisClazz(psiClass);
            return toJSONString(psiType, context);
        } else if (psiElement instanceof PsiField) {
            PsiField field = (PsiField) psiElement;
            return toJSONString(field.getType(), context);
        } else if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            // 构造函数特殊处理
            if (psiMethod.isConstructor()) {
                PsiClass containingClass = psiMethod.getContainingClass();
                if (containingClass != null) {
                    PsiType psiType = PsiToolkit.getPsiTypeByPisClazz(containingClass);
                    return toJSONString(psiType, context);
                }
            }
            // 如果是方法、获取返回值的JSON 数据结构
            PsiType returnType = psiMethod.getReturnType();
            if (returnType != null) {
                return toJSONString(returnType, context);
            }
        } else if (psiElement instanceof PsiParameter) {
            PsiParameter psiParameter = (PsiParameter) psiElement;
            PsiType type = psiParameter.getType();
            return toJSONString(type, context);
        } else if (psiElement instanceof PsiJavaFile) {
            PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
            PsiClass[] classes = psiJavaFile.getClasses();
            if (classes.length > 0) {
                return toJSONString(classes[0], context);
            }
        } else if (psiElement instanceof PsiLocalVariable) {
            PsiLocalVariable psiLocalVariable = (PsiLocalVariable) psiElement;
            PsiType type = psiLocalVariable.getType();
            return toJSONString(type, context);
        } else if (psiElement instanceof PsiNewExpression) {
            PsiNewExpression psiNewExpression = (PsiNewExpression) psiElement;
            if (psiNewExpression.getReference() != null) {
                PsiElement resolve = psiNewExpression.getReference().resolve();
                if (resolve != null) {
                    return toJSONString(resolve, context);
                }
            }
        } else if (psiElement instanceof PsiReferenceExpression) {
            PsiReferenceExpression referenceExpression = (PsiReferenceExpression) psiElement;
            if (referenceExpression.getReference() != null) {
                PsiElement resolve = referenceExpression.getReference().resolve();
                if (resolve != null) {
                    return toJSONString(resolve, context);
                }
            }
        } else if (psiElement instanceof PsiJavaCodeReferenceElement) {
            PsiJavaCodeReferenceElement psiJavaCodeReferenceElement = (PsiJavaCodeReferenceElement) psiElement;
            if (psiJavaCodeReferenceElement.getReference() != null) {
                PsiElement resolve = psiJavaCodeReferenceElement.getReference().resolve();
                if (resolve != null) {
                    return toJSONString(resolve, context);
                }
            }
        }
        return null;
    }


    /**
     * 解析clazz
     *
     * @param context
     * @return
     */
    private Object parseClass(JPsiTypeContext context) {
        // 1、查看缓存有没有数据
        Object parsedJsonObject = context.getCache(context.getOwner());
        if (parsedJsonObject != null) {
            return parsedJsonObject;
        }
        PsiClass psiClass = null;
        if (context.getOwner() instanceof PsiClassType) {
            psiClass = ((PsiClassType) context.getOwner()).resolve();
        }
        assert psiClass != null;

        if (checkClassIgnore(psiClass)) {
            return TypeDefaultValue.DEFAULT_NULL;
        }
        LinkedHashMap<String, Object> linkedHashMap = new LinkedHashMap<>();
        // 循环依赖，返回null 空数据~ Gson 解析会异常..
        // https://github.com/WangJi92/arthas-idea-plugin/issues/131
        context.putCache(context.getOwner(), TypeDefaultValue.DEFAULT_NULL);
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
                    JPsiTypeContext jPsiTypeContext = context.copy(field.getType(), context.getPsiTypeGenerics());
                    fieldValue = parseVariableValue(jPsiTypeContext);
                }

                if (!Objects.equals(TypeDefaultValue.DEFAULT_NULL, fieldValue)) {
                    linkedHashMap.put(fieldKey, fieldValue);
                }
            } catch (Exception e) {
                LOG.error("get file json error " + field.getName(), e);
            }
        }
        if (context.getRecursionLevel() > 0 && psiClass.getAllFields().length == 0) {
            return linkedHashMap.isEmpty() ? null : linkedHashMap;
        }

        if (!linkedHashMap.isEmpty()) {
            context.putCache(context.getOwner(), linkedHashMap);
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
        if (annotationFastJsonJSONField != null) {
            String fieldName = Objects.requireNonNull(annotationFastJsonJSONField.findAttributeValue("name")).getText();
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName.substring(1, fieldName.length() - 1);
            }
        }
        return field.getName();
    }

    private Object parseVariableValue(JPsiTypeContext context) {
        if (context.getRecursionLevel() >= 200) {
            //递归太多了次数直接返回 null
            return TypeDefaultValue.DEFAULT_NULL;
        }
        PsiType type = context.getOwner();
        if (type instanceof PsiPrimitiveType) {
            //primitive Type
            TypeValueContext value = typeValueAnalysisFactory.getValue(type);
            return value.getResult();
        } else if (type instanceof PsiArrayType) {
            //array type also support PsiEllipsisType
            PsiType typeToDeepType = type.getDeepComponentType();
            Object obj = parseVariableValue(context.copy(typeToDeepType, PsiToolkit.getPsiClassGenerics(typeToDeepType)));
            if (Objects.equals(obj, TypeDefaultValue.DEFAULT_NULL) || obj == null) {
                return new ArrayList<>();
            }
            return new ArrayList<>();
        } else if (type instanceof PsiClassType) {
            PsiClassType currentParseIdeaPsiClassType = (PsiClassType) type;
            TypeValueContext quickProcessValue = typeValueAnalysisFactory.getValue(type);
            if (quickProcessValue.getSupport()) {
                // 快速处理获取结果，比如一些常见的数据类型 Enum 处理
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
                JPsiTypeContext rawJPsiTypeJPsiTypeContext = context.copy(rawType, context.getPsiTypeGenerics());
                if (rawJPsiTypeJPsiTypeContext.isInheritor(Collection.class.getName())) {
                    // Set<String> List<Demo<String>> ..why not startsWith("java.")?
                    PsiType[] parameters = currentParseIdeaPsiClassType.getParameters();
                    if (parameters.length == 1) {
                        //List<?> List<? extend XXX>
                        PsiType psiTypeGenericsType = PsiToolkit.getPsiTypeGenericsType(parameters[0]);
                        if (psiTypeGenericsType != null) {
                            Object obj = parseVariableValue(context.copy(psiTypeGenericsType, getPsiClassGenerics(psiTypeGenericsType)));
                            if (Objects.equals(obj, TypeDefaultValue.DEFAULT_NULL) || obj == null) {
                                return new ArrayList<>();
                            }
                            return Lists.newArrayList(obj);
                        }
                    }
                    // List 没有写泛型..
                    return new ArrayList<>();
                }

                if (type.getCanonicalText().startsWith("java.")) {
                    // 提速
                    if (rawJPsiTypeJPsiTypeContext.isInheritor(Class.class.getName())) {
                        // Class clazz  ,Class<User> clazz2
                        PsiType[] parameters = currentParseIdeaPsiClassType.getParameters();
                        if (parameters.length == 0) {
                            //没有泛型类型，为空直接 null
                            return Class.class.getName();
                        }
                        // https://github.com/WangJi92/arthas-idea-plugin/issues/130
                        // List<?>  Class<? extends LanguageDriver>
                        if (parameters[0] instanceof PsiClassType) {
                            PsiClassType psiClassType = (PsiClassType) parameters[0];
                            // clazz 直接返回这个类的字符串
                            return PsiToolkit.getPsiTypeQualifiedNameClazzName(psiClassType);
                        } else if (parameters[0] instanceof PsiWildcardType) {
                            PsiWildcardType wildcardType = (PsiWildcardType) parameters[0];
                            if (wildcardType.isExtends()) {
                                // 获取上界限定的类型 上界限定通配符 (? extends T): 指定了类型的上界，表示该类型可以是 T 或 T 的子类。
                                PsiType extendsBound = wildcardType.getExtendsBound();
                                if (extendsBound instanceof PsiClassType) {
                                    PsiClassType extendsBoundPsiClassType = (PsiClassType) extendsBound;
                                    return PsiToolkit.getPsiTypeQualifiedNameClazzName(extendsBoundPsiClassType);
                                }
                            } else if (wildcardType.isSuper()) {
                                // 获取下界限定的类型 下界限定通配符 (? super T): 指定了类型的下界，表示该类型可以是 T 或 T 的超类。
                                PsiType superBound = wildcardType.getSuperBound();
                                if (superBound instanceof PsiClassType) {
                                    PsiClassType superBoundPsiClassType = (PsiClassType) superBound;
                                    return PsiToolkit.getPsiTypeQualifiedNameClazzName(superBoundPsiClassType);
                                }
                            }
                        }
                        return Class.class.getName();
                    }
                }

            } else if (typeParameters.length == 2) {
                PsiClassType rawType = currentParseIdeaPsiClassType.rawType();
                // 特殊处理Map
                JPsiTypeContext rawJPsiTypeJPsiTypeContext = context.copy(rawType, context.getPsiTypeGenerics());
                if (rawJPsiTypeJPsiTypeContext.isInheritor(Map.class.getName())) {
                    PsiType[] parameters = currentParseIdeaPsiClassType.getParameters();
                    if (parameters.length == 2) {
                        //Map<String,<? extends LanguageDriver> ?
                        PsiType psiTypeGenericsType = PsiToolkit.getPsiTypeGenericsType(parameters[1]);
                        if (psiTypeGenericsType != null) {
                            Object obj = parseVariableValue(context.copy(psiTypeGenericsType, getPsiClassGenerics(psiTypeGenericsType)));
                            if (Objects.equals(obj, TypeDefaultValue.DEFAULT_NULL) || obj == null) {
                                return new HashMap<>();
                            }
                            HashMap<Object, Object> map = new HashMap<>();
                            map.put(TypeDefaultValue.DEFAULT_MAP_KEY, obj);
                            return map;
                        }
                    }
                    // Map 没有写泛型..
                    return new HashMap<>();
                }
            }

            //not standard Map、List  CustomMap extends HashMap<String,String>
            // current psiClazz not type generics
            // simple handler ignore type generics
            if (context.isInheritor(Map.class.getName())) {
                return new HashMap<>();
            } else if (context.isInheritor(Collection.class.getName())) {
                return new HashMap<>();
            }

            if (context.getPsiTypeGenerics() != null) {
                PsiType typeToDeepType = context.getPsiTypeGenerics().get(psiClass.getName());
                if (typeToDeepType != null) {
                    return parseVariableValue(context.copy(typeToDeepType, getPsiClassGenerics(typeToDeepType)));
                }
            }
            if (typeParameters.length == 0) {
                // 没有泛型参数
                return parseClass(context.copy(currentParseIdeaPsiClassType, null, 0));
            }
            // Test<User,String> ..
            return parseClass(context.copy(currentParseIdeaPsiClassType, getPsiClassGenerics(type), 0));
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
