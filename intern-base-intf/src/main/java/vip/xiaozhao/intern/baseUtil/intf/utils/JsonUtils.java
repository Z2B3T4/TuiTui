package vip.xiaozhao.intern.baseUtil.intf.utils;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.getSerializationConfig().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }



    @SuppressWarnings("rawtypes")
    public static Map convert(String jsonText) {
        if (jsonText == null || jsonText.length() == 0) return new LinkedHashMap();
        try {

            return mapper.readValue(jsonText, Map.class);
        } catch (JsonParseException e) {
            return null;
        } catch (JsonMappingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }


    public static Map<String, String> convert2Map(String jsonText) {

        if (jsonText == null || jsonText.length() == 0) return new LinkedHashMap<String, String>();

        try {
            return mapper.readValue(jsonText, new TypeReference<Map<String, String>>() {
            });
        } catch (JsonParseException e) {
            return null;
        } catch (JsonMappingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 解析json数据的单个节点的值
     * @param json
     * @param key
     * @return
     */
    public static String getStringBykey(String json, String key) {
        if (StringUtils.isEmpty(json) || StringUtils.isEmpty(key)) {
            return null;
        }

        JSONObject parseString = (JSONObject) JSONObject.parse(json);
        String res = (String) parseString.get(key);
        return res;
    }

    /**
     * 解析复杂json数据为java对象
     * @param json
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T getdefiniteObject(String json, Class<?> t ){
       T parseObject =(T) JSONObject.parseObject(json, t);
        return  parseObject;
    }


    /**
     * 将对象转换为JSON格式
     *
     * @param model
     * @return
     * @throws IOException
     */
    public static String toStr(Object model) {
        if (model == null) {
            return "";
        }
        try {
            return mapper.writeValueAsString(model);
        } catch (Exception ex) {
            log.error("JsonParseException=" + ex + ",jsonText=" + model);
            return "";
        }
    }

    public static Object convert(String jsonText, Class<?> valueType) {
        if (jsonText == null || jsonText.length() == 0)
            return null;

        try {
            return mapper.readValue(jsonText, valueType);
        } catch (JsonParseException e) {
            log.error("JsonParseException=" + e + ",jsonText=" + jsonText);
            return null;
        } catch (JsonMappingException e) {
            log.error("JsonMappingException=" + e + ",jsonText=" + jsonText);
            return null;
        } catch (IOException e) {
            log.error("IOException=" + e + ",jsonText=" + jsonText);
            return null;
        }
    }

    public static Object convert(File src, Class<?> valueType) {
        if (src == null || src.length() == 0)
            return null;

        try {
            return mapper.readValue(src, valueType);
        } catch (JsonParseException e) {
            log.error("JsonParseException=" + e);
            e.printStackTrace();
            return null;
        } catch (JsonMappingException e) {
            log.error("JsonMappingException=" + e);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            log.error("IOException=" + e);
            e.printStackTrace();
            return null;
        }
    }

    public static String convert(Map map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonGenerationException e) {
            return null;
        } catch (JsonMappingException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(String jsonText, Class<T> valueType) {
        if (jsonText == null || jsonText.length() == 0)
            return null;

        try {
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, valueType);

            //这两句：排除JSON中含有 Bean所没有的属性 的影响
//			ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
//			mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return (List<T>) mapper.readValue(jsonText, javaType);
        } catch (IOException e) {
            log.error("IOException=" + e + ",jsonText=" + jsonText);
            return null;
        }
    }

}
