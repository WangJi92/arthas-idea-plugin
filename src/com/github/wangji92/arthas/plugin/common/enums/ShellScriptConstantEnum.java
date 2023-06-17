package com.github.wangji92.arthas.plugin.common.enums;

import com.github.wangji92.arthas.plugin.common.enums.base.EnumCodeMsg;

/**
 * 一些常见的命令
 *
 * @author 汪小哥
 * @date 05-05-2021
 */
public enum ShellScriptConstantEnum implements EnumCodeMsg<String> {
    /**
     * https://www.yuque.com/arthas-idea-plugin/help/zkuar3
     */
    WATCH_SPRING_HTTP("watch org.springframework.web.servlet.DispatcherServlet doService '{params[0].getRequestURI()+\" \"+ #cost}'  -n 5  -x 3 '#cost>1'", "watch spring http request cost"),
    WATCH_SPRING_HTTP_HEADER(" watch org.springframework.web.servlet.DispatcherServlet doService '{params[0].getRequestURI()+\"  header=\"+params[1].getHeaders(\"trace-id\")}'  -n 10  -x 3 -f", "watch spring http response header"),
    WATCH_MYBATIS_BOUND_SQL("watch org.apache.ibatis.mapping.BoundSql getSql '{returnObj,target.parameterObject,throwExp}'  -n 5  -x 3", "watch mybatis boundSql"),
    WATCH_JDBC_REQUEST("watch java.sql.Connection prepareStatement '{params,throwExp}'  -n 5  -x 3  'clazz.getName().startsWith(\"com.mysql\") and params.length==1' and #cost>1 ", "watch jdbc sql"),
    VM_TOOL_FORCE_GC("vmtool --action forceGc", "force gc"),
    VMTOOL_INTERRUPT_THREAD("vmtool --action interruptThread -t 1", "vmtool interrupt thread by thread id(thread command get)"),
    BATCH_THREAD("thread -i 3000 -n 5;thread -b;thread --state BLOCKED;", "batch thread"),
    PROFILER("profiler start --event cpu --interval 10000000 --format jfr --duration 180", "采集cpu jfr，支持jfr格式的工具来查看,JDK Mission Control or JProfiler 执行 180秒自动结束"),
    PROFILER_ALLOC("profiler start --event alloc --interval 10000000 --threads --format svg --duration 180 --threads", "采集alloc 火焰图 执行 180秒自动结束"),
    PROFILER_CPU("profiler start --event cpu --interval 10000000 --threads --format svg --duration 180 --threads", "采集cpu 火焰图 执行 180秒自动结束"),
    HEAPDUMP("heapdump /tmp/dump.hprof", "heap dump,heapdump --live /tmp/dump.hprof"),
    JVM("jvm", "display the target jvm information"),
    /**
     * Arthas发布3.5.6版本：应用排包瘦身不再烦恼
     * https://mp.weixin.qq.com/s?__biz=MzkxNDI0ODE0NQ==&mid=2247484030&idx=1&sn=d5314772a68f587b45ff550a11224c51&chksm=c1701ac0f60793d624bcda54340e61f78587bd8081c76417d9a7e3ea547cc40305a9f00e8410&scene=178&cur_album_id=1896318852766973955#rd
     */
    CLASSLOADER_URL_STAT("classloader --url-stat", "URL使用统计功能，方便排除未使用jar包,注意 不代表Unused URLs可以从应用中删掉。"),
    CLASSLOADER("classloader;classloader -l;classloader -t;", "show classloader info"),
    MEMORY("memory", "memory命令可以查看JVM内存信息"),
    MBEAN_MEMORY_THREAD("mbean java.lang:type=Memory;mbean java.lang:type=MemoryPool,name=* Usage;mbean java.nio:type=BufferPool,name=*;mbean java.lang:type=Threading;mbean java.lang:type=OperatingSystem;", "use mbean to show memory pool and thread"),
    MBEAN_GARBAGE("mbean java.lang:type=GarbageCollector,name=*;mbean java.lang:type=Compilation;", "use mbean to show garbage and compilation"),
    OPTIONS_UN_SAFE("options unsafe true", "支持对系统级别的类进行增强，打开该开关可能导致把JVM搞挂"),
    OPTIONS_JSON_FORMAT("options json-format true", "支持json化的输出"),
    DASHBOARD_N_1("dashboard -n 1", "dashboard info"),
    SYS_PROP_ENV("sysprop;sysenv;", "sysprop and sysenv"),
    VM_OPTION("vmoption;", "display, and update the vm diagnostic options. vmoption PrintGCDetails true;"),
    /**
     * https://mp.weixin.qq.com/s/GF3C7RcEPV0f1hDah6CJPA
     */
    VM_OPTION_PRINT_GC("vmoption PrintGC true", " print gc info"),
    VM_OPTION_PRINT_GC_DETAIL("vmoption PrintGCDetails true", " print gc details"),
    VM_OPTION_PRINT_GC_DATE("vmoption PrintGCDateStamps true", " print gc time"),
    VM_OPTION_PRINT_GC_BEFORE_DUMP("vmoption HeapDumpBeforeFullGC true", "打开HeapDumpBeforeFullGC开关，可以在GC前生成heapdump文件"),
    VM_OPTION_PRINT_GC_AFTER_DUMP("vmoption HeapDumpAfterFullGC true", "打开HeapDumpAfterFullGC开关，可以在GC结束后生成heapdump文件"),
    VM_OPTION_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC("vmoption PrintClassHistogramBeforeFullGC true", "打开PrintClassHistogramBeforeFullGC开关，可以在GC前打印类直方图"),
    VM_OPTION_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC("vmoption PrintClassHistogramAfterFullGC true", "打开PrintClassHistogramAfterFullGC开关，可以在GC结束后打印类直方图"),
    PERF_COUNTER("perfcounter -d", "display the perf counter information.");


    ShellScriptConstantEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private String code;

    private String message;

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
}
