package com.github.idea.arthas.plugin.utils;

import com.github.idea.arthas.plugin.common.exception.CompilerFileNotFoundException;
import com.github.idea.arthas.plugin.constants.ArthasCommandConstants;
import com.github.idea.json.parser.toolkit.PsiToolkit;
import com.github.idea.json.parser.typevalue.TypeDefaultValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.InheritanceUtil;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 获取Java类型构造ognl的默认值信息
 *
 * @author 汪小哥
 * @date 22-12-2019
 */
public class OgnlPsUtils {

    private static final Logger LOG = Logger.getInstance(OgnlPsUtils.class);
    public static final String DEFAULT_MAP_KEY = TypeDefaultValue.DEFAULT_MAP_KEY;

    /**
     * 是否为匿名类
     *
     * @param psiElement
     * @return
     */
    public static boolean isAnonymousClass(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            String className = getCommonOrInnerOrAnonymousClassName(psiElement);
            if (className != null && className.contains("*$*")) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 是否为静态的字段或者方法
     *
     * @param psiElement
     * @return
     */
    public static boolean isStaticMethodOrField(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiMethod) {
                return isStaticMethod(psiElement);
            } else if (psiElement instanceof PsiField) {
                return isStaticField(psiElement);
            }
        }
        return result;
    }


    /**
     * 静态方法
     *
     * @param psiElement
     * @return
     */
    public static boolean isStaticMethod(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) psiElement;
                if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                    result = true;
                }

            }
        }
        return result;
    }

    /**
     * 静态字段
     *
     * @param psiElement
     * @return
     */
    public static boolean isStaticField(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiField) {
                PsiField psiField = (PsiField) psiElement;
                if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                    result = true;
                }

            }
        }
        return result;
    }

    /**
     * isFinalField
     *
     * @param psiElement
     * @return
     */
    public static boolean isFinalField(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiField) {
                PsiField psiField = (PsiField) psiElement;
                if (psiField.hasModifierProperty(PsiModifier.FINAL)) {
                    result = true;
                }

            }
        }
        return result;
    }

    /**
     * 非静态字段
     *
     * @param psiElement
     * @return
     */
    public static boolean isNonStaticField(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiField) {
                if (!isStaticField(psiElement)) {
                    result = true;
                }

            }
        }
        return result;
    }

    /**
     * 非静态方法
     *
     * @param psiElement
     * @return
     */
    public static boolean isNonStaticMethod(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiMethod) {
                if (!isStaticMethod(psiElement)) {
                    result = true;
                }

            }
        }
        return result;
    }

    /**
     * 是否为构造方法
     *
     * @param psiElement
     * @return
     */
    public static boolean isConstructor(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) psiElement;
                if (psiMethod.isConstructor()) {
                    result = true;
                }

            }
        }
        return result;
    }

    /**
     * 这个是非静态的方法或者字段
     *
     * @param psiElement
     * @return
     */
    public static boolean isNonStaticMethodOrField(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiMethod) {
                if (!isStaticMethod(psiElement)) {
                    result = true;
                }
            } else if (psiElement instanceof PsiField) {
                if (!isStaticField(psiElement)) {
                    result = true;
                }
            }
        }
        return result;
    }


    /**
     * 获取内部类、匿名类的class的 ognl 名称
     *
     * @param psiElement
     * @return
     */
    public static String getCommonOrInnerOrAnonymousClassName(PsiElement psiElement) {
        if (!OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            return "";
        }
        if (psiElement instanceof PsiMethod) {
            return OgnlPsUtils.getCommonOrInnerOrAnonymousClassName((PsiMethod) psiElement);
        }
        if (psiElement instanceof PsiField) {
            return OgnlPsUtils.getCommonOrInnerOrAnonymousClassName((PsiField) psiElement);
        }
        if (psiElement instanceof PsiClass) {
            return OgnlPsUtils.getCommonOrInnerOrAnonymousClassName((PsiClass) psiElement);
        }
        if (psiElement instanceof PsiJavaFile) {
            //only select project file is PsiJavaFile
            String packageName = ((PsiJavaFile) psiElement.getContainingFile()).getPackageName();
            String shortClassName = FilenameUtils.getBaseName(psiElement.getContainingFile().getName());
            return packageName + "." + shortClassName;
        }
        throw new IllegalArgumentException("Illegal parameters");
    }

    /**
     * 获取内部类、匿名类的class的 ognl 名称
     * <p>
     * 内部类  OuterClass$InnerClass
     * 匿名类  Outer*$*
     *
     * @param psiField
     * @return
     */
    public static String getCommonOrInnerOrAnonymousClassName(PsiField psiField) {
        return OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(Objects.requireNonNull(psiField.getContainingClass()));
    }

    /**
     * 获取内部类、匿名类的class的 ognl 名称
     * 内部类  OuterClass$InnerClass
     * 匿名类  Outer*$*
     *
     * @param psiMethod
     * @return
     */
    public static String getCommonOrInnerOrAnonymousClassName(@NotNull PsiMethod psiMethod) {
        return OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(Objects.requireNonNull(psiMethod.getContainingClass()));
    }

    /**
     * 获取内部类、匿名类的class的 ognl 名称
     * 内部类  OuterClass$InnerClass
     * 匿名类  Outer*$*
     *
     * @param currentContainingClass
     * @return
     */
    public static String getCommonOrInnerOrAnonymousClassName(@NotNull PsiClass currentContainingClass) {
        String qualifiedClassName = "";
        if (currentContainingClass instanceof PsiAnonymousClass) {
            //匿名类 eg com.wangji92.arthas.plugin.demo.controller.OuterClass$InnerClass$1 or com.wangji92.arthas.plugin.demo.controller.OuterClass$InnerClass$InnerInnerClass$1
            PsiAnonymousClass anonymousClass = (PsiAnonymousClass) currentContainingClass;

            //region 这样处理直接是错误的
            // anonymousClass.getQualifiedName() return null
            //endregion


            //region 这种有点问题； 匿名类 区分度不够 com.wangji92.arthas.plugin.demo.controller.OuterClass$InnerClass$1
            //而不是 java.lang.Runnable.run
            //            PsiClass[] interfaces = anonymousClass.getInterfaces();
//            qualifiedClassName = interfaces[0].getQualifiedName();
            //endregion

            PsiJavaFile javaFile = (PsiJavaFile) anonymousClass.getContainingFile();
            //这里获取的是文件名哦
            String outClassName = FilenameUtils.getBaseName(javaFile.getName());
            String packageName = javaFile.getPackageName();
            // 匿名内部类 这里获取当前方法对应的方法有点问题.. 先通过匹配处理
            // 匿名内部类 获取到的不是非常准确
            return packageName + "." + outClassName + "*" + ArthasCommandConstants.OGNL_INNER_CLASS_SEPARATOR + "*";
        }
        PsiClass nextContainingClass = currentContainingClass.getContainingClass();
        if (nextContainingClass == null) {
            // 如果当前不是内部类  nextContainingClass= null;
            qualifiedClassName = currentContainingClass.getQualifiedName();
            return qualifiedClassName;
        }

        // 处理内部类的逻辑 Outer$Inner  eg trace Outer$Inner  method  https://github.com/alibaba/arthas/issues/71
        List<String> ognlQualifiedClassNameArray = Lists.newArrayList();
        ognlQualifiedClassNameArray.add(currentContainingClass.getNameIdentifier().getText());
        currentContainingClass = currentContainingClass.getContainingClass();
        while (currentContainingClass != null) {
            ognlQualifiedClassNameArray.add(ArthasCommandConstants.OGNL_INNER_CLASS_SEPARATOR);
            String name = currentContainingClass.getNameIdentifier().getText();
            nextContainingClass = currentContainingClass.getContainingClass();
            if (nextContainingClass == null) {
                ognlQualifiedClassNameArray.add(currentContainingClass.getQualifiedName());
            } else {
                ognlQualifiedClassNameArray.add(name);
            }
            currentContainingClass = nextContainingClass;
        }
        Collections.reverse(ognlQualifiedClassNameArray);
        qualifiedClassName = String.join("", ognlQualifiedClassNameArray);
        return qualifiedClassName;

    }

    /**
     * 字段的名称
     *
     * @param psiElement
     * @return
     */
    public static String getFieldName(PsiElement psiElement) {
        String fieldName = "";
        if (OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            if (psiElement instanceof PsiField) {
                PsiField psiField = (PsiField) psiElement;
                fieldName = psiField.getNameIdentifier().getText();
            }
        }
        return fieldName;
    }

    /**
     * 判斷是否有 setXXField
     *
     * @param psiElement
     * @return
     */
    public static boolean fieldHaveSetMethod(PsiElement psiElement) {
        if (!(psiElement instanceof PsiField)) {
            return false;
        }
        PsiField psiField = (PsiField) psiElement;
        String fieldName = OgnlPsUtils.getFieldName(psiElement);
        String capitalizeFieldName = StringUtils.capitalize(fieldName);
        PsiClass containingClass = psiField.getContainingClass();
        if (containingClass != null) {
            for (PsiMethod method : containingClass.getMethods()) {
                if (method.getName().equals("set" + capitalizeFieldName)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * 获取方法名称
     *
     * @param psiElement
     * @return
     */
    public static String getMethodName(PsiElement psiElement) {
        String methodName = "";
        if (OgnlPsUtils.isPsiFieldOrMethodOrClass(psiElement)) {
            methodName = "*";
            if (psiElement instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) psiElement;
                if (psiMethod.getNameIdentifier() != null) {
                    methodName = psiMethod.getNameIdentifier().getText();
                } else {
                    methodName = psiMethod.getName();
                }
                if (psiMethod.isConstructor()) {
                    methodName = "<init>";
                }
            }
        }
        return methodName;
    }

    /**
     * 获取可以执行的参数
     *
     * @param psiElement
     * @return
     */
    public static String getExecuteInfo(PsiElement psiElement) {
        if (psiElement instanceof PsiField) {
            return ((PsiField) psiElement).getNameIdentifier().getText();
        } else if (psiElement instanceof PsiMethod) {
            return getMethodParameterDefault((PsiMethod) psiElement);
        }
        return "";
    }

    /**
     * 构造方法的参数信息  complexParameterCall(#{" ":" "}) 后面这个部分需要构造
     *
     * @param psiMethod
     * @return
     */
    public static String getMethodParameterDefault(@NotNull PsiMethod psiMethod) {
        // Experimental API method JvmField.getName() is invoked in Action.arthas.ArthasOgnlStaticCommandAction.actionPerformed().
        // This method can be changed in a future release leading to incompatibilities
        String methodName = OgnlPsUtils.getMethodName(psiMethod);
        StringBuilder builder = new StringBuilder(methodName).append("(");
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        if (parameters.length > 0) {
            int index = 0;
            for (PsiParameter parameter : parameters) {
                String defaultParamValue = null;
                try {
                    defaultParamValue = OgnlPsUtils.getDefaultString(parameter.getType(), parameter.getProject());
                } catch (Exception e) {
                    // https://github.com/WangJi92/arthas-idea-plugin/issues/156
                    // 防御性编程
                    LOG.error("get default json",e);
                }
                builder.append(defaultParamValue);
                if (!(index == parameters.length - 1)) {
                    builder.append(",");
                }
                index++;
            }
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 获取字段的默认值
     *
     * @return
     */
    public static String getFieldDefaultValue(PsiElement psiElement) {
        String defaultFieldValue = "";
        if (psiElement instanceof PsiField) {
            PsiField psiField = (PsiField) psiElement;
            defaultFieldValue = OgnlPsUtils.getDefaultString(psiField.getType(), psiField.getProject());
        }
        return defaultFieldValue;
    }

    /**
     * 构造ognl 的默认值
     *
     * @param psiType
     * @return
     */
    public static String getDefaultString(PsiType psiType, Project project) {
        String canonicalText = psiType.getCanonicalText();
        //基本数据类型
        if (psiType instanceof PsiPrimitiveType psiPrimitiveType) {
            return PsiToolkit.getPsiClassBasicTypeDefaultStringValue(psiType);
        } else if (psiType instanceof PsiArrayType psiArrayType) {
            //数组类型信息
            PsiType componentType = psiArrayType.getDeepComponentType();
            if (componentType instanceof PsiPrimitiveType) {
                String basicValue = PsiToolkit.getPsiClassBasicTypeDefaultStringValue(componentType);
                return "(new " + ((PsiPrimitiveType) componentType).getName() + "[]{%s})".formatted(basicValue);
            } else if (componentType instanceof PsiClassType psiClassType) {
                PsiClass resolved = psiClassType.resolve();
                assert resolved != null;
                PsiTypeParameter[] typeParameters = resolved.getTypeParameters();
                if (typeParameters.length == 0) {
                    // 数组没有泛型.. 可以构建json ?
                    String basicValue = PsiToolkit.getPsiClassBasicTypeDefaultStringValue(componentType);
                    if (basicValue != null) {
                        //基本类型，给个默认值
                        return "(new " + componentType.getCanonicalText() + "[]{%s})".formatted(basicValue);
                    }
                    String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(componentType, project);
                    return "(new " + PsiToolkit.getPsiTypeQualifiedNameClazzName((PsiClassType) componentType) + "[]{%s})".formatted(ognlJsonDefaultValue);
                }
            }
            //其他的直接new array..
            return "new " + componentType.getCanonicalText() + "[]{}";
        } else if (psiType instanceof PsiClassType psiClassType) {
            //基本类型
            String basicTypeValue = PsiToolkit.getPsiClassBasicTypeDefaultStringValue(psiType);
            if (basicTypeValue != null) {
                return basicTypeValue;
            }
            PsiClass psiClass = psiClassType.resolve();
            assert psiClass != null;
            // 处理枚举
            if (psiClass.isEnum()) {
                //父类为枚举的就是枚举  过滤第一个是枚举的字段常量
                final PsiField defaultPsiField = Arrays.stream(psiClass.getAllFields()).filter(psiField -> psiField instanceof PsiEnumConstant).findFirst().orElse(null);
                if (defaultPsiField != null) {
                    final String defaultEnumName = OgnlPsUtils.getFieldName(defaultPsiField);
                    return "@" + PsiToolkit.getPsiTypeQualifiedNameClazzName(psiClassType) + "@" + defaultEnumName;
                }
                return "null";
            }

            PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
            if (typeParameters.length == 0) {
                return OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(psiType, project);
            } else if (typeParameters.length == 2) {
                if (canonicalText.startsWith("java.")){
                    if (InheritanceUtil.isInheritor(psiClassType, Map.class.getName())) {
                        return getOgnlMapDefaultValue(psiClassType);
                    }
                }
            } else if (typeParameters.length == 1) {
                PsiType[] parameters = psiClassType.getParameters();
                if (canonicalText.startsWith("java.")) {
                    if (InheritanceUtil.isInheritor(psiClassType, List.class.getName())
                            || canonicalText.contains(Collection.class.getName())) {
                        //list
                        return getListOgnlDefaultValue(psiClassType);
                    }else if(InheritanceUtil.isInheritor(psiClassType,Set.class.getName())){
                        // set
                        return getSetOgnlDefaultValue(psiClassType);
                    } else if (InheritanceUtil.isInheritor(psiClassType, Class.class.getName())) {
                        if (parameters.length == 0) {
                            return "(@java.lang.Object@class)";
                        } else {
                            //https://github.com/WangJi92/arthas-idea-plugin/issues/156
                            PsiType genericsType = PsiToolkit.getPsiTypeGenericsType(parameters[0]);
                            String qualifiedName = PsiToolkit.getPsiTypeQualifiedNameClazzName((PsiClassType) genericsType);
                            return "(@" + qualifiedName + "@class)";
                        }
                    }
                }
            }

            //region 处理这种泛型复杂参数 Test5<User,Map<String,User>> test
            // 1、ognl 本身不支持泛型参数
            // 2、通过Json 构建外层对象 Test5
            // 3、获取Test5所有泛型参数
            // 4、遍历Test5 所有字段 包含了泛型参数 且非基本类型，通过判断是否有set方法进行赋值 同理赋值使用json 构造
            // 5、构建脚本 差异化解决无法处理泛型的问题..
            Map<String, PsiType> psiClassGenerics = PsiToolkit.getPsiClassGenerics(psiClassType);
            StringBuilder builder = new StringBuilder();
            for (PsiField cField : psiClass.getAllFields()) {
                if (cField.getType() instanceof PsiClassType fieldClazzType && psiClassGenerics != null) {
                    PsiType psiClassGenericCurrent = psiClassGenerics.get(fieldClazzType.getName());
                    if (psiClassGenericCurrent != null && PsiToolkit.getPsiClassBasicTypeDefaultStringValue(psiType) == null) {
                        // ognl 不支持泛型、手动通过set设置过去
                        if(fieldHaveSetMethod(cField)){
                            builder.append(",(#p.set")
                                    .append(StringUtils.capitalize(cField.getName()))
                                    .append("(")
                                    .append(OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(psiClassType, project))
                                    .append("))");
                        }
                    }
                }
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(psiClassType, project);
            return "(#p=%s%s,#p)".formatted(ognlJsonDefaultValue, builder.toString());
            //endregion
        }
        return "null";
    }

    /**
     * 获取map的默认值
     * @param psiClassType
     * @return
     */
    private static String getOgnlMapDefaultValue(PsiClassType psiClassType) {
        String  canonicalText = psiClassType.getCanonicalText();
        PsiType[] parameters = psiClassType.getParameters();
        Project project = Objects.requireNonNull(psiClassType.resolve()).getProject();
        PsiType mapValueTypeGenericsType = null;
        if (parameters.length >= 2) {
            //Map<String,T extend User>?
            mapValueTypeGenericsType = PsiToolkit.getPsiTypeGenericsType(parameters[1]);
        }
        if (canonicalText.contains(HashMap.class.getName())
                || canonicalText.contains(Map.class.getName())
                || canonicalText.contains(AbstractMap.class.getName())) {
            if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
                return "(#{\"" + DEFAULT_MAP_KEY + "\": null })";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
            return ("(#{\"" + DEFAULT_MAP_KEY + "\": %s})").formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(LinkedHashMap.class.getName())){
            if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
                return "(#@java.util.LinkedHashMap@{\"" + DEFAULT_MAP_KEY + "\": null })";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
            return ("(#@java.util.LinkedHashMap@{\"" + DEFAULT_MAP_KEY + "\": %s})").formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(Hashtable.class.getName())){
            if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
                return "(#@java.util.Hashtable@{\"" + DEFAULT_MAP_KEY + "\": new java.lang.Object()})";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
            return ("(#@java.util.Hashtable@{\"" + DEFAULT_MAP_KEY + "\": %s})").formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(TreeMap.class.getName())
                || canonicalText.contains(SortedMap.class.getName())
                || canonicalText.contains(NavigableMap.class.getName())){
            if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
                return "(#@java.util.TreeMap@{\"" + DEFAULT_MAP_KEY + "\": new java.lang.Object() })";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
            return ("(#@java.util.TreeMap@{\"" + DEFAULT_MAP_KEY + "\": %s})").formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(ConcurrentHashMap.class.getName())
                || canonicalText.contains(ConcurrentMap.class.getName())
               ){
            if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
                return "(#@java.util.concurrent.ConcurrentHashMap@{\"" + DEFAULT_MAP_KEY + "\": new java.lang.Object() })";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
            return ("(#@java.util.concurrent.ConcurrentHashMap@{\"" + DEFAULT_MAP_KEY + "\": %s})").formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(EnumMap.class.getName())) {
            if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
                return "(#@java.util.EnumMap@{\"" + DEFAULT_MAP_KEY + "\": null })";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
            return ("(#@java.util.EnumMap@{\"" + DEFAULT_MAP_KEY + "\": %s})").formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(WeakHashMap.class.getName())) {
            if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
                return "(#@java.util.WeakHashMap@{\"" + DEFAULT_MAP_KEY + "\": null })";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
            return ("(#@java.util.WeakHashMap@{\"" + DEFAULT_MAP_KEY + "\": %s})").formatted(ognlJsonDefaultValue);
        }

        String qualifiedNameClazzName = PsiToolkit.getPsiTypeQualifiedNameClazzName(psiClassType);
        PsiClass psiClass = psiClassType.resolve();
        assert psiClass != null;
        // todo 这里实现的不完善 这里直接new 非实现类，可能执行失败
        // PsiToolkit.hasNoArgConstructor(psiClass)
        if (parameters.length == 0 || mapValueTypeGenericsType ==null) {
            return "(#map=new "+qualifiedNameClazzName+"(),#map.put(\"" + DEFAULT_MAP_KEY + "\",new java.lang.Object()),#map)";
        }
        String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(mapValueTypeGenericsType, project);
        return ("(#map=new "+qualifiedNameClazzName+"(),#map.put(\"" + DEFAULT_MAP_KEY + "\",%s),#map)").formatted(ognlJsonDefaultValue);
    }

    /**
     * 获取Set的默认值
     * @param psiClassType
     * @return
     */
    private static String getSetOgnlDefaultValue(PsiClassType psiClassType) {
        String  canonicalText = psiClassType.getCanonicalText();
        PsiType[] parameters = psiClassType.getParameters();
        Project project = Objects.requireNonNull(psiClassType.resolve()).getProject();
        PsiType setValueTypeGenericsType = null;
        if (parameters.length >= 1) {
            //List<T extend User>
            setValueTypeGenericsType = PsiToolkit.getPsiTypeGenericsType(parameters[0]);
        }
        if(canonicalText.contains(Set.class.getName())
                || canonicalText.contains(AbstractSet.class.getName())
                || canonicalText.contains(HashSet.class.getName())){
            // Set
            if (parameters.length == 0 || setValueTypeGenericsType==null) {
                return "(#set=new java.util.HashSet(),#set)";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(setValueTypeGenericsType, project);
            return "(#set=new java.util.HashSet(),#set.add(%s),#set)".formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(TreeSet.class.getName())
          || canonicalText.contains(SortedSet.class.getName())){
            if (parameters.length == 0 ||  setValueTypeGenericsType==null) {
                return "(#set=new java.util.TreeSet(),#set)";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(setValueTypeGenericsType, project);
            return "(#set=new java.util.TreeSet(),#set.add(%s),#set)".formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(LinkedHashSet.class.getName())){
            if (parameters.length == 0 || setValueTypeGenericsType==null) {
                return "(#set=new java.util.LinkedHashSet(),#set)";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(setValueTypeGenericsType, project);
            return "(#set=new java.util.LinkedHashSet(),#set.add(%s),#set)".formatted(ognlJsonDefaultValue);
        }
        return "(#set=new java.util.HashSet(),#set)";
    }

    /**
     * 获取List 默认值
     * @param psiClassType
     * @return
     */
    private static String getListOgnlDefaultValue(PsiClassType psiClassType) {
        String  canonicalText = psiClassType.getCanonicalText();
        PsiType[] parameters = psiClassType.getParameters();
        Project project = Objects.requireNonNull(psiClassType.resolve()).getProject();
        PsiType listValueTypeGenericsType = null;
        if (parameters.length >= 1) {
            //List<T extend User>
            listValueTypeGenericsType = PsiToolkit.getPsiTypeGenericsType(parameters[0]);
        }
        if (canonicalText.contains(ArrayList.class.getName())
                || canonicalText.contains(List.class.getName())
                || canonicalText.contains(Collection.class.getName())
                || canonicalText.contains(AbstractList.class.getName())) {
            //ArrayList
            if (parameters.length == 0 || listValueTypeGenericsType==null) {
                return "{}";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(listValueTypeGenericsType, project);
            return "{%s}".formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(LinkedList.class.getName())){
            // LinkedList
            if (parameters.length == 0 || listValueTypeGenericsType==null) {
                return "(#list=new java.util.LinkedList(),#list)";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(listValueTypeGenericsType, project);
            return "(#list=new java.util.LinkedList(),#list.add(%s),#list)".formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(Vector.class.getName())){
            // Vector
            if (parameters.length == 0 || listValueTypeGenericsType==null) {
                return "(#vector=new java.util.Vector(),#vector)";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(listValueTypeGenericsType, project);
            return "(#vector=new java.util.Vector(),#vector.add(%s),#vector)".formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(Stack.class.getName())){
            // stack
            if (parameters.length == 0 || listValueTypeGenericsType==null) {
                return "(#stack=new java.util.Stack(),#stack)";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(listValueTypeGenericsType, project);
            return "(#stack=new java.util.Stack(),#vector.add(%s),#stack)".formatted(ognlJsonDefaultValue);
        }else if(canonicalText.contains(CopyOnWriteArrayList.class.getName())){
            // stack
            if (parameters.length == 0 || listValueTypeGenericsType==null) {
                return "(#list=new java.util.concurrent.CopyOnWriteArrayList(),#list)";
            }
            String ognlJsonDefaultValue = OgnlJsonHandlerUtils.getOgnlJsonDefaultValue(listValueTypeGenericsType, project);
            return "(#list=new java.util.concurrent.CopyOnWriteArrayList(),#list.add(%s),#list)".formatted(ognlJsonDefaultValue);
        }
        return "{}";
    }

    /**
     * 当前元素是否为枚举..
     *
     * @param psiElement
     * @return
     */
    public static boolean psiElementInEnum(PsiElement psiElement) {
        try {
            if (psiElement instanceof PsiClass && ((PsiClass) psiElement).isEnum()) {
                // 当前类为枚举
                return true;
            } else if (psiElement instanceof PsiEnumConstant) {
                // 当前是字段枚举常量
                return true;
            } else if (psiElement instanceof PsiMethod) {
                if (((PsiMethod) psiElement).getContainingClass() != null && ((PsiMethod) psiElement).getContainingClass().isEnum()) {
                    // 枚举里面的方法 非匿名方法
                    return true;
                }
                if (((PsiMethod) psiElement).getParent() instanceof PsiEnumConstantInitializer) {
                    //枚举里面的匿名常量 常量的匿名方法
                    return true;
                }
            } else if (psiElement instanceof PsiField && ((PsiField) psiElement).getContainingClass().isEnum()) {
                return true;
            } else if (psiElement instanceof PsiJavaFile) {
                // psi java file
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiElement;
                final String className = OgnlPsUtils.getCommonOrInnerOrAnonymousClassName(psiJavaFile);
                final PsiClass psiClass = JavaPsiFacade.getInstance(psiJavaFile.getProject()).findClass(className, GlobalSearchScope.allScope(psiJavaFile.getProject()));
                if (psiClass != null && psiClass.isEnum()) {
                    return true;
                }

            }
        } catch (IndexNotReadyException e) {
            LOG.info("[arthas] IndexNotReadyException get is enum  error", e);
            return false;
        } catch (Exception e) {
            LOG.info("[arthas] get is enum  error", e);
        }

        return false;
    }

    /**
     * 获取spring bean 的名称 【不是非常的精确】
     *
     * @param psiElement
     * @return
     */
    public static String getSpringBeanName(PsiElement psiElement) {
        String beanName = "";
        if (!isPsiFieldOrMethodOrClass(psiElement)) {
            return beanName;
        }
        if (psiElement instanceof PsiMethod) {
            beanName = OgnlPsUtils.getClassBeanName(((PsiMethod) psiElement).getContainingClass());
            return beanName;
        }
        if (psiElement instanceof PsiField) {
            beanName = OgnlPsUtils.getClassBeanName(((PsiField) psiElement).getContainingClass());
            return beanName;
        }
        if (psiElement instanceof PsiJavaFile) {
            beanName = OgnlPsUtils.getClassBeanName(((PsiJavaFile) psiElement).getClasses()[0]);
            return beanName;
        }
        if (psiElement instanceof PsiClass) {
            beanName = OgnlPsUtils.getClassBeanName((PsiClass) psiElement);
            return beanName;
        }
        return beanName;
    }

    /**
     * 获取Bean的名称
     *
     * @param psiClass
     * @return
     */
    public static String getClassBeanName(PsiClass psiClass) {
        if (psiClass == null) {
            return "errorBeanName";
        }
        PsiModifierList psiModifierList = psiClass.getModifierList();
        if (psiModifierList == null) {
            return "errorBeanName";
        }
        PsiAnnotation[] annotations = psiModifierList.getAnnotations();
        String beanName = "";
        if (annotations.length > 0) {
            for (PsiAnnotation annotation : annotations) {
                beanName = getAttributeFromAnnotation(annotation, Sets.newHashSet("org.springframework.stereotype.Component", "org.springframework.stereotype.Service", "org.springframework.stereotype.Controller", "org.springframework.stereotype.Repository", "org.springframework.web.bind.annotation.RestController", "org.springframework.context.annotation.Configuration"), "value");
                if (StringUtils.isNotBlank(beanName)) {
                    break;
                }
            }
        }
        //注解上没有获取值，使用默认的名称首字母小写
        if (StringUtils.isBlank(beanName)) {
            beanName = StringUtils.uncapitalize(psiClass.getName());
        }
        return beanName;

    }

    /**
     * 当前是否为spring bean
     *
     * @return
     */
    public static boolean isSpringBean(PsiElement psiElement) {
        boolean result = false;
        if (!isPsiFieldOrMethodOrClass(psiElement)) {
            return result;
        }
        PsiClass psiClass = null;
        if (psiElement instanceof PsiField) {
            PsiField field = (PsiField) psiElement;
            PsiAnnotation[] annotations = field.getAnnotations();
            Set<String> annotationTypes = new HashSet<>();
            annotationTypes.add("org.springframework.beans.factory.annotation.Autowired");
            annotationTypes.add("org.springframework.beans.factory.annotation.Qualifier");
            annotationTypes.add("javax.annotation.Resource");
            annotationTypes.add("org.springframework.beans.factory.annotation.Value");
            for (PsiAnnotation annotation : annotations) {
                if (annotationTypes.contains(annotation.getQualifiedName())) {
                    return true;
                }
            }
            psiClass = field.getContainingClass();

        }
        if (psiElement instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) psiElement;
            PsiAnnotation[] annotations = psiMethod.getAnnotations();
            Set<String> annotationTypes = new HashSet<>();
            annotationTypes.add("javax.annotation.PostConstruct");
            annotationTypes.add("javax.annotation.PreDestroy");
            annotationTypes.add("javax.annotation.Resource");
            annotationTypes.add("org.springframework.beans.factory.annotation.Lookup");
            annotationTypes.add("org.springframework.context.annotation.Bean");
            annotationTypes.add("org.springframework.context.annotation.Conditional");
            annotationTypes.add("org.springframework.context.annotation.Scope");
            for (PsiAnnotation annotation : annotations) {
                if (annotationTypes.contains(annotation.getQualifiedName())) {
                    return true;
                }
            }
            psiClass = psiMethod.getContainingClass();
        }
        psiClass = (PsiClass) psiElement;
        HashSet<String> annotationTypes = Sets.newHashSet();
        annotationTypes.add("org.springframework.stereotype.Service");
        annotationTypes.add("org.springframework.stereotype.Controller");
        annotationTypes.add("org.springframework.stereotype.Repository");
        annotationTypes.add("org.springframework.web.bind.annotation.RestController");
        annotationTypes.add("org.springframework.context.annotation.ComponentScan");
        annotationTypes.add("org.springframework.stereotype.Component");
        annotationTypes.add("org.springframework.context.annotation.Conditional");
        annotationTypes.add("javax.annotation.Resources");
        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
            assert annotation != null;
            assert annotation.getQualifiedName() != null;
            if (annotation.getQualifiedName().startsWith("org.springframework.") || annotationTypes.contains(annotation.getQualifiedName())) {
                return true;
            }
        }

        for (PsiClass anInterface : psiClass.getInterfaces()) {
            assert anInterface != null;
            assert anInterface.getQualifiedName() != null;
            if (anInterface.getQualifiedName().startsWith("org.springframework.")) {
                return true;
            }
            //todo
        }

        if (psiClass.getSuperClass() != null) {
            assert psiClass.getSuperClass().getQualifiedName() != null;
            if (psiClass.getSuperClass().getQualifiedName().startsWith("org.springframework.")) {

            }
        }

        for (PsiMethod method : psiClass.getMethods()) {
            assert method != null;
            //todo

        }

        for (PsiField field : psiClass.getFields()) {
            assert field != null;
            // todo
        }

        return false;


    }

    /**
     * 获取注解的值得信息
     *
     * @param annotation
     * @param annotationTypes
     * @param attribute
     * @return
     */
    private static String getAttributeFromAnnotation(
            PsiAnnotation annotation, Set<String> annotationTypes, final String attribute) {

        String annotationQualifiedName = annotation.getQualifiedName();
        if (annotationQualifiedName == null) {
            return "";
        }
        if (annotationTypes.contains(annotationQualifiedName)) {
            PsiAnnotationMemberValue annotationMemberValue = annotation.findAttributeValue(attribute);
            if (annotationMemberValue == null) {
                return "";
            }

            String httpMethodWithQuotes = annotationMemberValue.getText();
            return httpMethodWithQuotes.substring(1, httpMethodWithQuotes.length() - 1);
        }
        return "";
    }

    /**
     * 找到 编译的出口地址
     *
     * @param project
     * @param psiElement
     * @return
     */
    public static String getCompilerOutputPathV2(Project project, PsiElement psiElement) {

        // https://jetbrains.org/intellij/sdk/docs/basics/project_structure.html
        // https://jetbrains.org/intellij/sdk/docs/reference_guide/project_model/module.html
        Module module = ModuleUtil.findModuleForPsiElement(psiElement);
        if (module == null) {
            throw new CompilerFileNotFoundException(String.format("not find class  %s module in this project", getCommonOrInnerOrAnonymousClassName(psiElement)));
        }

        //找到编译的 出口位置
        VirtualFile compilerOutputVirtualFile = ModuleRootManager.getInstance(module).getModifiableModel().getModuleExtension(CompilerModuleExtension.class).getCompilerOutputPath();
        if (compilerOutputVirtualFile == null) {
            throw new CompilerFileNotFoundException(String.format("not find compile class file %s in target compile class dir", getCommonOrInnerOrAnonymousClassName(psiElement)));
        }
        return compilerOutputVirtualFile.getPath();
    }

    /**
     * 找到 编译的出口地址  这个版本针对一个工程里面打开一个项目里面的多个svn 目录存在问题 查询 是根据类名称来处理的
     *
     * @param project
     * @param ideaClassName
     * @return
     */
    public static String getCompilerOutputPath(Project project, String ideaClassName) {
        //选择了.class 文件 必须要处理 不然获取不到module 的信息,这里重新获取class 原文件的信息
        //根据类的全限定名查询PsiClass，下面这个方法是查询Project域 https://blog.csdn.net/ExcellentYuXiao/article/details/80273448
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(ideaClassName, GlobalSearchScope.projectScope(project));
        if (psiClass == null) {
            throw new CompilerFileNotFoundException(String.format("not find class  %s in this project", ideaClassName));
        }
        // https://jetbrains.org/intellij/sdk/docs/basics/project_structure.html
        // https://jetbrains.org/intellij/sdk/docs/reference_guide/project_model/module.html
        Module module = ModuleUtil.findModuleForPsiElement(psiClass);
        if (module == null) {
            throw new CompilerFileNotFoundException(String.format("not find class  %s module in this project", ideaClassName));
        }

        //找到编译的 出口位置
        VirtualFile compilerOutputVirtualFile = ModuleRootManager.getInstance(module).getModifiableModel().getModuleExtension(CompilerModuleExtension.class).getCompilerOutputPath();
        if (compilerOutputVirtualFile == null) {
            throw new CompilerFileNotFoundException(String.format("not find compile class file %s in target compile class dir", ideaClassName));
        }
        return compilerOutputVirtualFile.getPath();
    }

    /**
     * http://cache.baiducontent.com/c?m=Tnjg0Yh1MVwmR9iP4ZjO9t6Lw-n_-niXq_L9H_1xQzCQD0pv_sDWB6J-X9JKi_UtVCGlZIedbyoLZ_7IjppgHtJZ3dxPAqPe9_uktmcqP4E6hzObIGskfxHC-Cj01eIj758qcQovyc7U9VjxucwQzxU5KXRQR4Zh6eG-JKymkQoBzPnRgv1fYs79X7MHqE0BALGQB_CeklaXd118YvLS2s0btNlD6hoXlD9nxw9UHPX1y-XWRP4Achz2eTsjx4dW9gYgkL4nWsl7lWMU1o1W1a&p=882a9646d2dd5de442acdc2d021496&newp=c3759a46d5c757fc57efd234450582231615d70e3fd4d5126b82c825d7331b001c3bbfb42328170fd6c37d6100a54a5debf03274360927a3dda5c91d9fb4c574799e&s=cfcd208495d565ef&user=baidu&fm=sc&query=idea+plugin+get+path+of+plugin&qid=8a9012d00004780b&p1=9
     * 获取插件的地址  沙箱的地址和 真实的不一样，沙箱是解压的文件，真实的是一个jar包
     *
     * @return
     */

    //arthas idea 2.17 uses deprecated API, which may be removed in future releases leading to binary and source code incompatibilities
//    public static String getPluginPath() {
//        return PluginManager.getPlugin(PluginId.getId("com.github.wangji92.arthas.plugin")).getPath().getPath();
//    }


    /**
     * 当前是psi 的这个几种类型？ psiElement instanceof JvmMember 兼容性不好 修改为这个 Experimental API interface JvmElement is. This interface can be changed in a future release leading to incompatibilities
     *
     * @param psiElement
     * @return
     */
    public static boolean isPsiFieldOrMethodOrClass(PsiElement psiElement) {
        return psiElement instanceof PsiField || psiElement instanceof PsiClass || psiElement instanceof PsiMethod || psiElement instanceof PsiJavaFile;
    }

    /**
     * 当前为psi class
     *
     * @param psiElement
     * @return
     */
    public static boolean isPsiClass(PsiElement psiElement) {
        return psiElement instanceof PsiClass || psiElement instanceof PsiJavaFile;
    }


    /**
     * 获取ContainingPsiJavaFile
     *
     * @param psiElement
     * @return
     */
    public static PsiJavaFile getContainingPsiJavaFile(PsiElement psiElement) {
        if (psiElement instanceof PsiField || psiElement instanceof PsiClass || psiElement instanceof PsiMethod) {
            return (PsiJavaFile) psiElement.getContainingFile();
        } else if (psiElement instanceof PsiJavaFile) {
            return (PsiJavaFile) psiElement;
        }
        throw new IllegalArgumentException("Illegal parameters");
    }

}
