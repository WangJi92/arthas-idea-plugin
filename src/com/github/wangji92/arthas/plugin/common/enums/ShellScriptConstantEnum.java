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
     * thread
     */
    BATCH_THREAD("thread -i 3000 -n 5;thread -b;thread --state BLOCKED;", "batch thread"),
    PROFILER("profiler start --event cpu --interval 10000000 --format jfr -duration 180", "采集cpu jfr，支持jfr格式的工具来查看,JDK Mission Control or JProfiler 执行 180秒自动结束"),
    PROFILER_ALLOC("profiler start --event alloc --interval 10000000 --threads --format svg -duration 180 --threads", "采集alloc 火焰图 执行 180秒自动结束"),
    HEAPDUMP("heapdump /tmp/dump.hprof", "heap dump,heapdump --live /tmp/dump.hprof"),
    JVM("jvm", "display the target jvm information"),
    CLASSLOADER("classloader;classloader -l;classloader -t;", "show classloader info"),
    MBEAN_MEMORY_THREAD("mbean java.lang:type=Memory;mbean java.lang:type=MemoryPool,name=* Usage;mbean java.nio:type=BufferPool,name=*;mbean java.lang:type=Threading;mbean java.lang:type=OperatingSystem;", "use mbean to show memory pool and thread"),
    MBEAN_GARBAGE("mbean java.lang:type=GarbageCollector,name=*;mbean java.lang:type=Compilation;", "use mbean to show garbage and compilation"),
    OPTIONS_UN_SAFE("options unsafe true", "支持对系统级别的类进行增强，打开该开关可能导致把JVM搞挂"),
    OPTIONS_JSON_FORMAT("options json-format true", "支持json化的输出"),
    DASHBOARD_N_1("dashboard -n 1", "dashboard info"),
    SYS_PROP_ENV("sysprop;sysenv;", "sysprop and sysenv"),
    VM_OPTION("vmoption;", "display, and update the vm diagnostic options. vmoption PrintGCDetails true;"),
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
