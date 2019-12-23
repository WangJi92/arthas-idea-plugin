# Idea arthas 插件

<a name="eaba0"></a>
# 一、背景
目前Arthas 官方的工具还不够足够的简单，需要记住一些命令，特别是一些扩展性特别强的高级语法，比如ognl获取spring context 为所欲为，watch、trace 不够简单，需要构造一些命令工具的信息，因此只需要一个能够简单处理字符串信息的插件即可使用。当在处理线上问题的时候需要最快速、最便捷的命令，因此Idea arthas 插件还是有存在的意义和价值的。
<a name="cE2LQ"></a>
## 
<a name="vwK8h"></a>
# 二、支持的功能
支持的功能都是平时处理最常用的一些功能，一些快捷的链接，在处理紧急问题时候不需要到处查找，都是一些基本的功能,自动复制到剪切板中去，方便快捷。<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577110207183-951efa06-26ba-4201-abc9-46efb3836c41.png#align=left&display=inline&height=286&name=image.png&originHeight=572&originWidth=1074&size=241010&status=done&style=none&width=537)

![arthas command.svg](https://cdn.nlark.com/yuque/0/2019/svg/171220/1577110958822-5fd46571-d616-4bef-8432-f94992ceab43.svg#align=left&display=inline&height=660&name=arthas%20command.svg&originHeight=660&originWidth=917&size=2375450&status=done&style=none&width=917)

<a name="pwJKx"></a>
## 2.1 watch
![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577111133387-260aa3ff-1a8d-441f-ab5a-3786c585e618.png#align=left&display=inline&height=230&name=image.png&originHeight=460&originWidth=1570&size=326956&status=done&style=none&width=785)
```bash
watch com.command.idea.plugin.utils.StringUtils toLowerFristChar '{params,returnObj,throwExp}' -n 5 -x 3
```

<a name="yrbvX"></a>
## 2.2 trace 

```bash
trace com.command.idea.plugin.utils.StringUtils toLowerFristChar -n 5
```

<a name="M5hj0"></a>
## 2.3 static ognl (字段或者方法)
![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577111229892-122cacbb-1f25-47cc-bab2-df5cc10cebc0.png#align=left&display=inline&height=234&name=image.png&originHeight=468&originWidth=1410&size=304620&status=done&style=none&width=705)

<a name="qFcTH"></a>
### 2.3.1 右键static ognl
<a name="Mj3eY"></a>
### 2.3.2 获取classload命令
必须要获取，不然会找不到classload，arthas 官方获取问题系统的classload，spring 项目应该无法获取到这个class的信息，因此首先执行一下这个命令

```bash
sc -d com.command.idea.plugin.utils.StringUtils
```

![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577111475711-5ffa5492-9099-4b50-9ac0-18b03631f03c.png#align=left&display=inline&height=196&name=image.png&originHeight=392&originWidth=1346&size=218372&status=done&style=none&width=673)

<a name="6Q1ae"></a>
### 2.3.2 复制到界面，获取命令，执行即可
![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577111519442-aee9d985-5d0c-4f67-a8fc-bd4f2d9c3ff5.png#align=left&display=inline&height=126&name=image.png&originHeight=252&originWidth=1290&size=161506&status=done&style=none&width=645)

```bash
ognl  -x  3  '@com.command.idea.plugin.utils.StringUtils@toLowerFristChar(" ")' -c 8bed358
```

<a name="DzhKQ"></a>
## 2.4 Invoke Bean Method
 实际上就是根据当前的spring项目中的获取静态的spring context这样可以直接根据这个context直接获取任何的Bean方法，一般在Java后端服务中都有这样的Utils类，因此这个可以看为一个常量! 可以参考:[http://www.dcalabresi.com/blog/java/spring-context-static-class/](http://www.dcalabresi.com/blog/java/spring-context-static-class/) 有了这个，我们可以跟进一步的进行数据简化，由于在idea这个环节中，可以获取方法参数，spring bean的名称等等，非常的方便。

```java
public class ApplicationContextProvider implements ApplicationContextAware {
    
    private static ApplicationContext context;
 
    public ApplicationContext getApplicationContext() {
        return context;
    }
 
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        context = ctx;
    }
}
```
<a name="ofj0b"></a>
### 2.4.1 设置获取spring context的上下文
> ps 这里可以使用@applicationContextProvider@context 这样，比如还行进行函数调用可以直接使用ognl逗号分割可以继续执行的语法，比如 @applicationContextProvider@context,#springContext.getBean("name").todo() 后续我们需要在界面调用任何的方法都会添加上这句话。

![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577111904672-c0b779ec-06eb-45fb-b95e-c1bb9ae743b1.png#align=left&display=inline&height=701&name=image.png&originHeight=1402&originWidth=2152&size=547914&status=done&style=none&width=1076)

<a name="KuN43"></a>
### 2.4.2 右键点击需要调用的方法
这里的策略和static ognl 一样的，本质还是ognl的调用。<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577112169623-5535dd8a-47d2-4fbb-8631-f785406c6057.png#align=left&display=inline&height=191&name=image.png&originHeight=382&originWidth=1632&size=318255&status=done&style=none&width=816)

```bash
ognl  -x  3  '@applicationContextProvider@context,#springContext.getBean("arthasInstallCommandAction").actionPerformed(new com.intellij.openapi.actionSystem.AnActionEvent())' -c desw22
```

**特别说明** 
> 太复杂的参数不太适用于对于线上问题的诊断，因此方法参数尽可能的简单，这里有一套规则，因为ognl的语法和Java类似的，在获取到参数的时候会进行默认的参数构造处理。
> String ——> ""
> Number、Byte 、Char ——> 0
> Map ——> #{"":" "}  ognl 语法
> List ——>{}
> 数组 int[] ——>new int[]{}
> other ——> new XXXClass() 参数太复杂了默认直接new了一个
> Special 你的参数可能是从springContext中获取，你可以修改表达式
> ......,#newParam= #springContext.getBean("beanName").todo(),#springContext.getBean("other").to(#newParam)
> 这样使用参数定义到你的bash脚本中去，这种属于特殊用法，不具有一般的通用性


<a name="Cybim"></a>
## 2.5  install(linux)
安装脚本，可以一键的通过as.sh 进行执行

```bash
curl -sk https://arthas.gitee.io/arthas-boot.jar -o ~/.arthas-boot.jar  && echo "alias as.sh='java -jar ~/.arthas-boot.jar --repo-mirror aliyun --use-http'" >> ~/.bashrc && source ~/.bashrc
```

![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577112830008-8b7d0714-f37d-4425-b343-c7318eaf51ef.png#align=left&display=inline&height=257&name=image.png&originHeight=514&originWidth=2060&size=649699&status=done&style=none&width=1030)

<a name="Is5S6"></a>
## 2.6 常用特殊用法链接

- [special ognl](https://github.com/alibaba/arthas/issues/71)
- [tt get spring context](https://github.com/alibaba/arthas/issues/482)
- [get error filter](https://github.com/alibaba/arthas/issues/429)
- [dubbo 遇上arthas](http://hengyunabc.github.io/dubbo-meet-arthas/)
- [Arthas实践--jad/mc/redefine线上热更新一条龙](http://hengyunabc.github.io/arthas-online-hotswap/)
- [ognl 使用姿势](https://blog.csdn.net/u010634066/article/details/101013479)

<a name="L0Qr4"></a>
# 三、其他
<a name="j4UkL"></a>
## 3.1 安装 
![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577113149941-dd21bf43-446d-4a74-bb6d-46438437bea5.png#align=left&display=inline&height=173&name=image.png&originHeight=346&originWidth=756&size=133740&status=done&style=none&width=378)

<a name="vKsvI"></a>
## 3.2 快捷键设置
![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577113190659-92993f9c-80f6-49f6-a66a-425c585ab616.png#align=left&display=inline&height=326&name=image.png&originHeight=652&originWidth=2126&size=358045&status=done&style=none&width=1063)

<a name="r40qy"></a>
## 3.3 代码地址
[https://github.com/WangJi92/arthas-idea-plugin](https://github.com/WangJi92/arthas-idea-plugin)
<a name="5pfFa"></a>
## 3.4 插件开发，发布
 参考 插件开发： [https://www.jianshu.com/p/722841c6d0a9](https://www.jianshu.com/p/722841c6d0a9)<br />就可以看到编译后的zip包了<br />![image.png](https://cdn.nlark.com/yuque/0/2019/png/171220/1577113324071-634991d1-c0c7-42aa-ad7c-d0bcde8b7a5a.png#align=left&display=inline&height=273&name=image.png&originHeight=546&originWidth=908&size=347771&status=done&style=none&width=454)
