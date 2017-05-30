import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * 
 * @author donald
 *
 */
public class JsonUtil {
	 /** 
     * 对象转换成JSON字符串  obj
     * @param obj 需要转换的对象 
     * @return 对象的string字符 
     */  
    public static String toJson(Object obj) {  
        JSONObject jSONObject = JSONObject.fromObject(obj);  
        return jSONObject.toString();  
    }  
    /**
     * just for String[]
     * @param obj
     * @return
     */
    public static String ArraytoJson(Object obj) {  
    	JSONArray jsonArr = JSONArray.fromObject(obj);  
        return jsonArr.toString();  
    }  
    /** 
     * JSON字符串转换成对象 
     * @param jsonString  需要转换的字符串 
     * @param type 需要转换的对象类型 
     * @return 对象 
     */  
    @SuppressWarnings("unchecked")  
    public static <T> T fromJson(String jsonString, Class<T> type) {  
        JSONObject jsonObject = JSONObject.fromObject(jsonString);  
        return (T) JSONObject.toBean(jsonObject, type);  
    }  
    public static <T> String ListToJson(List<T> lobj){
    	JSONArray jsonArr = JSONArray.fromObject(lobj);
    	return jsonArr.toString();
    }
    /**
     * 根据json String 返回List
     * @param json
     * @return
     */
    public static List<Object> jsonToList(String json){
    	JSONArray jsonArr = JSONArray.fromObject(json);
    	return jsonToList(jsonArr);
    }
    /**
     * 将Json String转化为 bean list
     * @param jsonArray
     * @param clazz
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
	public static List jsonToList(String json,Class clazz){
    	JSONArray jsonArr = JSONArray.fromObject(json);
    	return jsonToList(jsonArr,clazz);
    }
    /**
     * 将JSONArray转化为 bean list
     * @param jsonArray
     * @param clazz
     * @return
     */
    @SuppressWarnings({ "rawtypes", "deprecation" })
	public static List jsonToList( JSONArray jsonArray,Class clazz){
        if(jsonArray!=null){
            try{
                List  list =  JSONArray.toList(jsonArray,clazz);
                return list;
            }catch(Exception e){
                return null;
            }
        }else{
            return null;
        }
    }
    /** 
     * 将JSONArray对象转换成list集合 map 
     * @param jsonArr 
     * @return 
     */  
    public static List<Object> jsonToList(JSONArray jsonArr) {  
        List<Object> list = new ArrayList<Object>();  
        for (Object obj : jsonArr) {  
            if (obj instanceof JSONArray) {  
                list.add(jsonToList((JSONArray) obj));  
            } else if (obj instanceof JSONObject) {  
                list.add(jsonToMap((JSONObject) obj));  
            } else {  
                list.add(obj);  
            }  
        }  
        return list;  
    }  
  
    /** 
     * 将json字符串转换成map对象 
     *  
     * @param json 
     * @return 
     */  
    public static Map<String, Object> jsonToMap(String json) {  
        JSONObject obj = JSONObject.fromObject(json);  
        return jsonToMap(obj);  
    }  
  
    /** 
     * 将JSONObject转换成map对象 
     *  
     * @param json 
     * @return 
     */  
    public static Map<String, Object> jsonToMap(JSONObject obj) {  
        Set<?> set = obj.keySet();  
        Map<String, Object> map = new HashMap<String, Object>(set.size());  
        for (Object key : obj.keySet()) {  
            Object value = obj.get(key);  
            if (value instanceof JSONArray) {  
                map.put(key.toString(), jsonToList((JSONArray) value));  
            } else if (value instanceof JSONObject) {  
                map.put(key.toString(), jsonToMap((JSONObject) value));  
            } else {  
                map.put(key.toString(), obj.get(key));  
            }  
  
        }  
        return map;  
    }  
}
