package com.github.idea.json.parser.toolkit;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import lombok.Data;

/**
 * 解析上下文 (为了保证json 序列化和反序列化一致性，需要感知ognl 使用的哪种json工具)
 *
 * @author wangji
 * @date 2024/5/29 23:01
 */
@Data
public class ParserContext {

    private static final Logger LOG = Logger.getInstance(ParserContext.class);

    private ParserJsonType jsonType;

    private Boolean pretty = true;

    /**
     * 解析JSON的类型
     */
    public static enum ParserJsonType {
        FASTJSON {
            @Override
            public String toJsonString(Object object, ParserContext parserContext) {
                if (Boolean.TRUE.equals(parserContext.getPretty())) {
                    return com.alibaba.fastjson.JSON.toJSONString(object, SerializerFeature.PrettyFormat);
                }
                return com.alibaba.fastjson.JSON.toJSONString(object);
            }
        },
        FASTJSON_2 {
            @Override
            public String toJsonString(Object object, ParserContext parserContext) {
                if (Boolean.TRUE.equals(parserContext.getPretty())) {
                    return com.alibaba.fastjson2.JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat);
                }
                return com.alibaba.fastjson2.JSON.toJSONString(object);
            }
        },
        JACKSON {
            private final static ObjectMapper OBJECTMAPPER = new ObjectMapper()
                    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


            @Override
            public String toJsonString(Object object, ParserContext parserContext) {
                try {
                    if (Boolean.TRUE.equals(parserContext.getPretty())) {
                        return OBJECTMAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
                    }
                    return OBJECTMAPPER.writeValueAsString(object);
                } catch (Exception e) {
                    LOG.error("parse json error", e);
                }
                return null;
            }
        },
        GSON {
            private final static Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

            private final static Gson GSON = new GsonBuilder().create();

            @Override
            public String toJsonString(Object object, ParserContext parserContext) {
                if (Boolean.TRUE.equals(parserContext.getPretty())) {
                    return GSON_PRETTY.toJson(object);
                }
                return GSON.toJson(object);
            }
        };

        /**
         * 获取模板
         *
         * @return
         */
        public abstract String toJsonString(Object object, ParserContext parserContext);
    }

    /**
     * 解析JSON字符串
     *
     * @param object
     * @return
     */
    public String toJsonString(Object object) {
        if (this.getJsonType() != null) {
            return this.getJsonType().toJsonString(object, this);
        }
        return null;
    }

}
