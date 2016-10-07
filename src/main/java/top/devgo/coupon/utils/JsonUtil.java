package top.devgo.coupon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * json字符串处理的工具类
 * 
 * @author DD
 *
 */
public class JsonUtil {
	
	private JsonUtil() {
	}
	
	/**
	 * 将【内容】中的双引号 " 替换为 ' , 以确保jsonStr的正确解析<br> 
	 * 不影响jsonStr key和value前后的 "<br> 
	 * 【注意不要滥用，否则也会影响json解析】
	 * @param jsonStr
	 * @return
	 */
	public static String formateDoubleQuotationMarks(String jsonStr) {
		char[] temp = jsonStr.toCharArray();
		int n = temp.length;
		for (int i = 0; i < n; i++) {
			if (temp[i] == ':' && temp[i + 1] == '"') {
				for (int j = i + 2; j < n; j++) {
					if (temp[j] == '"') {
						if (temp[j + 1] != ',' && temp[j + 1] != '}') {
							temp[j] = '\'';
						} else if (temp[j + 1] == ',' || temp[j + 1] == '}') {
							break;
						}
					}
				}
			}
		}
		return new String(temp);
	}
	
	/**
	 * 用ScriptEngine的方式解析str->jsonArray，并通过JSON.stringify规格化jsonArray中的元素<br>
	 * 主要用于在Jackson Json解析报错的情况下
	 * @param jsonStr 如果str是jsonObject则包装成[str]
	 * @return
	 * @throws ScriptException
	 * @throws NoSuchMethodException
	 */
	public static String formateJsonArrays(String jsonStr) throws ScriptException, NoSuchMethodException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();  
        engine.put("res", list);  
        
        if (!jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
        	jsonStr = "[" + jsonStr + "]";
		}
        
        //利用script.eval把字符串转换成js的数组
        String str2jsonArr = "function str2jsonArr() { return "+ jsonStr +";}"; 
		//将数组中的对象重新规格化
        String reformatArr = ""
//				+ "importPackage(java.util); "
				+ "function reformatArr(arr){ "
				+ "for(var i=0; i<arr.length; i++){ "
				+ "    var item = arr[i]; "
//				+ "    var itm = new HashMap(); "
//				+ "    for(var t in item){ "
//				+ "        itm.put(t, JSON.stringify(item[t]));"
//				+ "    };"
				+ "    res.add(JSON.stringify(item));"
				+ "};"
				+ "};";
		//如果可以的话 使用编译模式以提高执行效率
		if (engine instanceof Compilable) {
			Compilable compEngine = (Compilable)engine;
			CompiledScript funStr2jsonArr = compEngine.compile(str2jsonArr);
			funStr2jsonArr.eval();
			CompiledScript funReformatArr = compEngine.compile(reformatArr);
			funReformatArr.eval();
		}else{
		  	engine.eval(str2jsonArr);  
			engine.eval(reformatArr);
		}

		Invocable invocable = (Invocable) engine;
		Object json2array = invocable.invokeFunction("str2jsonArr"); 
		invocable.invokeFunction("reformatArr", json2array);
			
		return list.toString();
	}

}
