package com.github.wangji92.arthas.plugin.common.enums;

import com.github.wangji92.arthas.plugin.common.enums.base.EnumCodeMsg;

/**
 * https://alibaba.github.io/arthas/tt
 * 常用命令枚举信息  参考：https://blog.csdn.net/u012881904/article/details/104763791
 *
 * 先记录一下这种获取spring context

 watch org.springframework.web.servlet.DispatcherServlet doDispatch '@org.springframework.web.context.support.WebApplicationContextUtils@getWebApplicationContext(params[0].getServletContext()).getBean("controllerTest").test()' -n 5 -x 3
 *
 * @author 汪小哥
 * @date 21-03-2020
 */
public enum TimeTunnelCommandEnum implements EnumCodeMsg<String> {
    START_ONE_TIME("tt -p -i 1000", "重新出发一次, -play(Replay the time fragment specified by index)"),
    START_MANY_TIME("tt -p --replay-times 5 --replay-interval 2000 -i 1000", "重新出发5次，每次时间间隔 2秒"),
    WATCH_EXPRESS("tt -w '{method.name,params,returnObj,throwExp}' -x 3 -i 1000", "观察表达式入参返回值 -w, -watch-express( watch the time fragment by ognl express)"),
    GET_TARGET("tt -w target -i 1000", "获取当前index 拦截的目标对象,target : the object"),
    GET_PARAMS0("tt -w params[0] -i 1000", "获取当前index 第一个参数的值"),
    /**
     * 参考链接 https://github.com/alibaba/arthas/issues/482
     */
    GET_CONTEXT_BEGIN("tt -t org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter invokeHandlerMethod", "调用一次Spring Mvc接口,获取spring context"),
    GET_CONTEXT_END("tt -w 'target.getApplicationContext()' -i 1000", "通过tt 获取spring context,然后getBean 为所欲为"),
    GET_ALL_TIME("tt -l", "List all the time fragments"),
    DELETE_ALL("tt --delete-all", "Delete all time fragments");


    private String code;

    private String msg;


    TimeTunnelCommandEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getEnumMsg() {
        return msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}
