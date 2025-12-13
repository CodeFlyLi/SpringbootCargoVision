package com.app.springbootcargovision.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 数据脱敏序列化器
 */
public class DataMaskingSerializer {

    /**
     * 手机号脱敏序列化器
     * 保留前3位和后4位，中间用 * 代替
     * 示例: 138****1234
     */
    public static class PhoneMaskingSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (!StringUtils.hasText(value) || value.length() < 7) {
                gen.writeString(value);
                return;
            }
            // 保留前3位，后4位，中间用4个*
            String masked = value.substring(0, 3) + "****" + value.substring(value.length() - 4);
            gen.writeString(masked);
        }
    }

    /**
     * 身份证号脱敏序列化器
     * 保留前3位和后4位，中间用 * 代替
     * 示例: 110***********0001
     */
    public static class IdCardMaskingSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (!StringUtils.hasText(value) || value.length() < 7) {
                gen.writeString(value);
                return;
            }
            // 身份证号一般是15或18位
            // 这里统一处理：保留前3位和后4位，中间全部替换为 *
            int length = value.length();
            String prefix = value.substring(0, 3);
            String suffix = value.substring(length - 4);
            
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            for (int i = 0; i < length - 7; i++) {
                sb.append("*");
            }
            sb.append(suffix);
            
            gen.writeString(sb.toString());
        }
    }
}
