package com.github.idea.arthas.plugin.common.enums;

import com.github.idea.arthas.plugin.common.enums.base.EnumCodeMsg;

/**
 * 一些常见的命令  通过;分号判断是否可以直接复制命令
 *
 * @author 汪小哥
 * @date 05-05-2021
 */
public enum ShellScriptConstantEnum implements EnumCodeMsg<String> {
    /**
     * https://www.yuque.com/arthas-idea-plugin/help/zkuar3
     */
    WATCH_SPRING_HTTP("watch org.springframework.web.servlet.DispatcherServlet doService '{params[0].getRequestURI()+\" \"+ #cost}'  -n 5  -x 3 '#cost>1'", "watch spring http request cost", "https://github.com/WangJi92/arthas-idea-plugin/issues/80"),
    WATCH_SPRING_HTTP_ALL_HEADER("watch org.springframework.web.servlet.DispatcherServlet doService '#httpRequest=params[0],#allHeaders={},#forEnumeration = :[#this.hasMoreElements()? (#headerName=#this.nextElement(),#allHeaders.add(#headerName+\"=\"+#httpRequest.getHeader(#headerName)),#forEnumeration(#this)):null],#forEnumeration(#httpRequest.getHeaderNames()),#allHeaders'  -n 5  -x 3", "watch spring http all header", "https://github.com/WangJi92/arthas-idea-plugin/issues/102"),
    WATCH_SPRING_HTTP_HEADER("watch org.springframework.web.servlet.DispatcherServlet doService '{params[0].getRequestURI()+\"  header=\"+params[1].getHeaders(\"trace-id\")}'  -n 10  -x 3 -f", "watch spring http response header", "https://github.com/WangJi92/arthas-idea-plugin/issues/80"),
    WATCH_MYBATIS_BOUND_SQL("watch org.apache.ibatis.mapping.BoundSql getSql '{returnObj,target.parameterObject,throwExp}'  -n 5  -x 3", "watch mybatis boundSql", "https://github.com/WangJi92/arthas-idea-plugin/issues/80"),
    WATCH_JDBC_REQUEST("watch java.sql.Connection prepareStatement '{params,throwExp}'  -n 5  -x 3  'clazz.getName().startsWith(\"com.mysql\") and params.length==1' and #cost>1 ", "watch jdbc sql", "https://github.com/WangJi92/arthas-idea-plugin/issues/80"),
    VM_TOOL_FORCE_GC("vmtool --action forceGc", "force gc", "https://arthas.aliyun.com/doc/vmtool.html#%E5%BC%BA%E5%88%B6-gc"),
    VMTOOL_INTERRUPT_THREAD("vmtool --action interruptThread -t 1", "vmtool interrupt thread by thread id(thread command get)", "https://arthas.aliyun.com/doc/vmtool.html#interrupt-%E6%8C%87%E5%AE%9A%E7%BA%BF%E7%A8%8B"),
    BATCH_THREAD("thread -i 3000 -n 5;thread -b;thread --state BLOCKED;", "batch thread", "https://arthas.aliyun.com/doc/thread.html"),
    PROFILER("profiler start --event cpu --interval 10000000 --format jfr --duration 180", "see link arthas+jprofiler分析 采集的cpu jfr 火焰图", "https://github.com/alibaba/arthas/issues/1416"),
    PROFILER_ALLOC("profiler start --event alloc --interval 10000000 --threads --format html --duration 180 --threads", "采集alloc火焰图 ", "https://arthas.aliyun.com/doc/profiler.html#profiler-%E6%94%AF%E6%8C%81%E7%9A%84-events"),
    PROFILER_CPU("profiler start --event cpu --interval 10000000 --threads --format html --duration 180 --threads", "采集cpu 火焰图", "https://arthas.aliyun.com/doc/profiler.html#%E9%85%8D%E7%BD%AE-include-exclude-%E6%9D%A5%E8%BF%87%E6%BB%A4%E6%95%B0%E6%8D%AE"),
    HEAPDUMP("heapdump /tmp/dump.hprof", "heap dump,heapdump --live /tmp/dump.hprof", "https://arthas.aliyun.com/doc/heapdump.html"),
    JVM("jvm", "display the target jvm information", "https://arthas.aliyun.com/doc/jvm.html"),
    /**
     * Arthas发布3.5.6版本：应用排包瘦身不再烦恼 https://mp.weixin.qq.com/s/ePc807eHs5vVMbuCfGpLzQ
     */
    CLASSLOADER_URL_STAT("classloader --url-stat", "应用排包瘦身不再烦恼", "https://mp.weixin.qq.com/s/ePc807eHs5vVMbuCfGpLzQ"),
    CLASSLOADER("classloader;classloader -l;classloader -t;", "show classloader info", "https://arthas.aliyun.com/doc/classloader.html"),
    MEMORY("memory", "memory命令可以查看JVM内存信息", "https://mp.weixin.qq.com/s/ePc807eHs5vVMbuCfGpLzQ"),
    MBEAN_MEMORY_THREAD("mbean java.lang:type=Memory;mbean java.lang:type=MemoryPool,name=* Usage;mbean java.nio:type=BufferPool,name=*;mbean java.lang:type=Threading;mbean java.lang:type=OperatingSystem;", "use mbean to show memory pool and thread", "https://arthas.aliyun.com/doc/mbean.html"),
    MBEAN_GARBAGE("mbean java.lang:type=GarbageCollector,name=*;mbean java.lang:type=Compilation;", "use mbean to show garbage and compilation", "https://arthas.aliyun.com/doc/mbean.html#%E4%BD%BF%E7%94%A8%E5%8F%82%E8%80%83"),
    OPTIONS_UN_SAFE("options unsafe true", "enhance classes under the java.* package", "https://arthas.aliyun.com/doc/options.html#%E6%89%93%E5%BC%80-unsafe-%E5%BC%80%E5%85%B3-%E6%94%AF%E6%8C%81-jdk-package-%E4%B8%8B%E7%9A%84%E7%B1%BB"),
    OPTIONS_JSON_FORMAT("options json-format true", "format object output with json", "https://arthas.aliyun.com/doc/options.html#view-all-options"),
    DASHBOARD_N_1("dashboard -n 1", "dashboard info", "https://arthas.aliyun.com/doc/dashboard.html"),
    SYS_PROP_ENV("sysprop;sysenv;", "sysprop and sysenv", "https://arthas.aliyun.com/doc/sysenv.html"),
    VM_OPTION("vmoption", "display and update the vm diagnostic options", "https://arthas.aliyun.com/doc/vmoption.html"),
    /**
     * https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA
     */
    VM_OPTION_PRINT_GC("vmoption PrintGC true", "print gc info", "https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA"),
    VM_OPTION_PRINT_GC_DETAIL("vmoption PrintGCDetails true", " print gc details", "https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA"),
    VM_OPTION_PRINT_GC_DATE("vmoption PrintGCDateStamps true", " print gc time", "https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA"),
    VM_OPTION_PRINT_GC_BEFORE_DUMP("vmoption HeapDumpBeforeFullGC true", "GC前生成heapdump文件", "https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA"),
    VM_OPTION_PRINT_GC_AFTER_DUMP("vmoption HeapDumpAfterFullGC true", "GC结束后生成heapdump文件", "https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA"),
    VM_OPTION_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC("vmoption PrintClassHistogramBeforeFullGC true", "GC前打印类直方图", "https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA"),
    VM_OPTION_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC("vmoption PrintClassHistogramAfterFullGC true", "GC结束后打印类直方图", "https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA"),
    PERF_COUNTER("perfcounter -d", "display the perf counter information.", "https://arthas.aliyun.com/doc/perfcounter.html");


    ShellScriptConstantEnum(String code, String message, String url) {
        this.code = code;
        this.message = message;
        this.url = url;
    }

    private String code;

    private String message;

    private String url;

    @Override
    public String getEnumMsg() {
        return message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    public String getUrl() {
        return url;
    }
}
