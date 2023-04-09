# arthas idea plugin 
### 联系方式
有问题直接加我微信wj983433479 或者 扫码添加钉钉群 32102545 沟通
### 帮助文档链接
more issue : [https://github.com/WangJi92/arthas-idea-plugin/issues label=documentation ](https://github.com/WangJi92/arthas-idea-plugin/issues?q=label%3Adocumentation+)
   * [arthas idea plugin document](https://www.yuque.com/arthas-idea-plugin)
   * [arthas + arhtas idea plugin 实战视频](https://www.bilibili.com/video/BV1yz4y1f7iz/)
   * [arthas-idea-plugin demo](https://github.com/WangJi92/arthas-plugin-demo)
### 给作者一个鼓励

如果你喜欢这款插件，欢迎给身边的小伙伴推荐,给个github star,谢谢

# 一、背景
目前Arthas 官方的工具还不够足够的简单，需要记住一些命令，特别是一些扩展性特别强的高级语法，比如ognl获取spring context 为所欲为，watch、trace 不够简单，需要构造一些命令工具的信息，因此只需要一个能够简单处理字符串信息的插件即可使用。当在处理线上问题的时候需要最快速、最便捷的命令，因此Idea arthas 插件还是有存在的意义和价值的。
<a name="cE2LQ"></a>
## 
<a name="vwK8h"></a>
# 二、支持的功能
支持的功能都是平时处理最常用的一些功能，一些快捷的链接，在处理紧急问题时候不需要到处查找，都是一些基本的功能,自动复制到剪切板中去，方便快捷。

![image](https://user-images.githubusercontent.com/20874972/77851010-fa211b80-7208-11ea-909c-e4a208f282f6.png)


## 2.1 watch
```bash
watch StringUtils uncapitalize '{params,returnObj,throwExp}' -n 5 -x 3
```

## 2.2 trace 

```bash
trace StringUtils uncapitalize -n 5
```

## 2.3 static ognl (字段或者方法)

### 2.3.1 右键static ognl

### 2.3.2 获取classload命令
必须要获取，不然会找不到classload，arthas 官方获取问题系统的classload，spring 项目应该无法获取到这个class的信息，因此首先执行一下这个命令

```bash
sc -d StringUtils
```

### 2.3.2 复制到界面，获取命令，执行即可

```bash
ognl  -x  3  '@StringUtils@uncapitalize(" ")' -c 8bed358
```

## 2.4 Invoke Bean Method
 实际上就是根据当前的spring项目中的获取静态的spring context这样可以直接根据这个context直接获取任何的Bean方法，一般在Java后端服务中都有这样的Utils类，因此这个可以看为一个常量! 可以参考:[arthas idea demo](https://github.com/WangJi92/arthas-plugin-demo/blob/master/src/main/java/com/wangji92/arthas/plugin/demo/common/ApplicationContextProvider.java) 有了这个，我们可以跟进一步的进行数据简化，由于在idea这个环节中，可以获取方法参数，spring bean的名称等等，非常的方便。

```java
package com.wangji92.arthas.plugin.demo.common;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 提供给arthas ognl 获取context的信息
 *
 * @author 汪小哥
 * @date 30-28-2020
 */
@Component
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

### 2.4.1 设置获取spring context的上下文
 [arthas idea plugin 配置](https://www.yuque.com/arthas-idea-plugin/help/ugrc8n)
 
### 2.4.2 右键点击需要调用的方法
这里的策略和static ognl 一样的，本质还是ognl的调用。

```bash
ognl  -x  3  '#springContext=@applicationContextProvider@context,#springContext.getBean("arthasInstallCommandAction").actionPerformed(new com.intellij.openapi.actionSystem.AnActionEvent())' -c desw22
```


#### 特别说明对于ognl 字段类型的处理
[代码地址 ](https://github.com/WangJi92/arthas-idea-plugin/blob/master/src/com/github/wangji92/arthas/plugin/utils/OgnlPsUtils.java)


## 2.5  install(linux)
安装脚本，可以一键的通过as.sh 进行执行

```bash
curl -sk https://arthas.aliyun.com/arthas-boot.jar  -o ~/.arthas-boot.jar  && echo "alias as.sh='java -jar ~/.arthas-boot.jar --repo-mirror aliyun --use-http 2>&1'" >> ~/.bashrc && source ~/.bashrc && echo "source ~/.bashrc" >> ~/.bash_profile && source ~/.bash_profile
```


![image](https://user-images.githubusercontent.com/20874972/71365779-f7e06d00-25da-11ea-92e9-e3ad5725f1ca.png)


<a name="Is5S6"></a>
## 2.6 常用特殊用法链接

- [special ognl](https://github.com/alibaba/arthas/issues/71)
- [tt get spring context](https://github.com/alibaba/arthas/issues/482)
- [get error filter](https://github.com/alibaba/arthas/issues/429)
- [dubbo 遇上arthas](http://hengyunabc.github.io/dubbo-meet-arthas/)
- [Arthas实践--jad/mc/redefine线上热更新一条龙](http://hengyunabc.github.io/arthas-online-hotswap/)
- [ognl 使用姿势](https://blog.csdn.net/u010634066/article/details/101013479)


