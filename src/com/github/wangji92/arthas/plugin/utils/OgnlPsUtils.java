package com.github.wangji92.arthas.plugin.utils;

import com.github.wangji92.arthas.plugin.common.exception.CompilerFileNotFoundException;
import com.github.wangji92.arthas.plugin.constants.ArthasCommandConstants;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 获取Java类型构造ognl的默认值信息
 *
 * @author 汪小哥
 * @date 22-12-2019
 */
public class OgnlPsUtils {

    private static final Logger LOG = Logger.getInstance(OgnlPsUtils.class);

    /**
     * 是否为匿名类
     *
     * @param psiElement
     * @return
     */
    public static boolean isAnonymousClass(PsiElement psiElement) {
        boolean result = false;
        if (isPsiFieldOrMethodOrClass(psiElement)) {
            if (getCommonOrInnerOrAnonymousClassName(psiElement).contains("*$*")) {
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
                String defaultParamValue = OgnlPsUtils.getDefaultString(parameter.getType());
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
            defaultFieldValue = OgnlPsUtils.getDefaultString(psiField.getType());
        }
        return defaultFieldValue;
    }

    /**
     * 构造ognl 的默认值
     *
     * @param psiType
     * @return
     */
    public static String getDefaultString(PsiType psiType) {
        String result = " ";
        String canonicalText = psiType.getCanonicalText();

        //基本类型  boolean
        if (PsiType.BOOLEAN.equals(psiType) || "java.lang.Boolean".equals(canonicalText)) {
            result = "true";
            return result;
        }

        //基本类型  String
        if (canonicalText.endsWith("java.lang.String")) {
            result = "\" \"";
            return result;
        }

        if (PsiType.LONG.equals(psiType) || "java.lang.Long".equals(canonicalText)) {
            result = "0L";
            return result;
        }

        if (PsiType.DOUBLE.equals(psiType) || "java.lang.Double".equals(canonicalText)) {
            result = "0D";
            return result;
        }

        if (PsiType.FLOAT.equals(psiType) || "java.lang.Float".equals(canonicalText)) {
            result = "0F";
            return result;
        }

        //基本类型  数字
        if (PsiType.INT.equals(psiType) || "java.lang.Integer".equals(canonicalText)
                ||
                PsiType.BYTE.equals(psiType) || "java.lang.Byte".equals(canonicalText)
                ||
                PsiType.SHORT.equals(psiType) || "java.lang.Short".equals(canonicalText)) {
            result = "0";
            return result;
        }


        //Class xx 特殊class 字段的判断
        //java.lang.Class
        if ("java.lang.Class".equals(canonicalText)) {
            result = "@java.lang.Object@class";
            return result;
        }
        //Class<XXX> x
        //java.lang.Class<com.wangji92.arthas.plugin.demo.controller.user>
        if (canonicalText.startsWith("java.lang.Class")) {
            result = "@" + canonicalText.substring(canonicalText.indexOf("<") + 1, canonicalText.length() - 1) + "@class";
            return result;
        }


        //常见的List 和Map
        if (canonicalText.startsWith("java.util.")) {
            if (canonicalText.contains("Map")) {
                result = "#{\" \": null }";
                return result;
            }
            if (canonicalText.contains("List")) {
                result = "{}";
                return result;
            }
        }

        //...
        if (psiType instanceof PsiEllipsisType) {
            String arrayCanonicalText = ((PsiEllipsisType) psiType).getDeepComponentType().getCanonicalText();
            return "new " + arrayCanonicalText + "[]{}";
        }

        //原生的数组
        if (canonicalText.contains("[]")) {
            result = "new " + canonicalText + "{}";
            return result;
        }

        //不管他的构造函数了，太麻烦了
        result = "new " + canonicalText + "()";
        return result;

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
