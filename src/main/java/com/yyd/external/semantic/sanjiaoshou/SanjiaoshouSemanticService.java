package com.yyd.external.semantic.sanjiaoshou;

import java.util.Map;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yyd.external.semantic.ExternalSemanticError;
import com.yyd.external.semantic.ExternalSemanticResult;
import com.yyd.external.semantic.ExternalSemanticService;
import com.yyd.external.semantic.ExternalSemanticResult.OperationEx;
import com.yyd.external.semantic.ExternalSemanticResult.ParamTypeEx;
import com.yyd.external.util.Http;
import com.yyd.external.util.StringTool;

/**
 * 访问三角兽语义
 * @author pc
 *
 */
public class SanjiaoshouSemanticService implements ExternalSemanticService{
	private String qaUrl = "http://triotest.sanjiaoshou.net/Qa/Qa.php";	
	//测试使用
	//private String testUrl = "http://triotest.sanjiaoshou.net/TrioRobot/index.php";
	private String childChat = "http://service.sanjiaoshou.net/TrioRobot/index.php";
	
	
	@Override
	public ExternalSemanticResult handleSemantic(String text,Map<String,String> params)throws Exception{
		ExternalSemanticResult result = null;
		
		if(StringTool.isEmpty(text) || params == null) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_INPUT_PARAM_EMPTY);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INPUT_PARAM_EMPTY));
			return result;
		}
		String userId = params.get("userId");
		if(StringTool.isEmpty(userId)) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM));
			return result;
		}
		
		long start = System.currentTimeMillis();		
		result = getAnswer(userId,text);			
		long total = System.currentTimeMillis() - start;
		result.setTime(total);
		//三角兽统一定为闲聊
		result.setService("chat");
		result.setOperation(OperationEx.SPEAK);
		result.setParamType(ParamTypeEx.T);
		
		return result;
	}
	
	@Override
	public String  getToken(String userId,String userIp)throws Exception{
		return null;
	}
	
	
	/**
	 * 
	 * @param question:问题文本
	 * @param rid:暂时没限制，用户id，随便填,
	 * @return
	 */
	private ExternalSemanticResult getAnswer(String rid,String question) {
		ExternalSemanticResult semanticResult = new ExternalSemanticResult();
		String result = null;
		String answer = null;
		try {
			String url = qaUrl+"?userid="+rid+"&query=";
			Http http = new Http(url + question);
			http.setCharset("utf-8");
			http.setRequestProperty("Content-type", "text/plain;charset=UTF-8");
			
			//1.第一次查询,通用问答接口，像百科知识或十万个为什么之类的
			//回复内容是纯文本，不是json格式
			answer = http.get();
			if (!StringTool.isEmpty(answer)) {
				semanticResult.setAnswer(answer);
				semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
				semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));
				semanticResult.setSrcResult(answer);
				semanticResult.setIntent("qa");
				return semanticResult;
			}

			//2.第二次查询，聊天意图接口
			http = new Http(childChat);//childChat,testUrl
			http.setCharset("utf-8");
			http.setRequestProperty("Content-type", "text/plain;charset=UTF-8");
			JSONObject stat_info = new JSONObject();
			stat_info.put("device_id", rid);
			stat_info.put("user_group", "user");
			stat_info.put("os", "android");
			stat_info.put("os_ver", "1.4.0");

			JSONObject json = new JSONObject();
			/*
			 * json.put("service_ver", "1.0"); json.put("bot_name",
			 * "yongyida_xiaoyong_normal"); json.put("bot_ver", "1");
			 * json.put("user_id", "rokid_test"); json.put("bot_mode", 0);
			 * json.put("query", s); json.put("req_type", "chat");
			 * json.put("send_time_ms", "1454319650000");
			 * json.put("query_time_ms", "1454319650000"); json.put("debug", 1);
			 * json.put("callback_msg", s);
			 */

			json.put("service_ver", "1.0");
			json.put("bot_name", "yongyida_xiaoyong");
			json.put("bot_ver", "1");
			json.put("user_id", "rokid_test");
			json.put("bot_mode", 0);
			json.put("query", question);
			json.put("req_type", "intent"); //指定接口类型
			json.put("send_time_ms", "1454319650000");
			json.put("query_time_ms", "1454319650000");
			json.put("debug", 1);
			json.put("callback_msg", question);
			json.put("stat_info", stat_info);

			result = http.post(json.toString());
			semanticResult.setSrcResult(result);
			answer = parseResult(result,semanticResult);
			if (!StringTool.isEmpty(answer)) {
				semanticResult.setAnswer(answer);
				semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
				semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));
				return semanticResult;
			}
			
			//3.第三次查询
			json.put("req_type", "chat"); //成人聊天接口
			result = http.post(json.toString());
			semanticResult.setSrcResult(result);
			answer = parseResult(result,semanticResult);
			//System.out.println("3 成人聊天接口="+answer);
			if (!StringTool.isEmpty(answer)) {
				semanticResult.setAnswer(answer);
				semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
				semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));
				return semanticResult;
			}				

		} catch (Exception e) {			
			
		}
		
		semanticResult.setRet(ExternalSemanticError.ERROR_INVALID_RESULT_DATA);
		semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INVALID_RESULT_DATA));
		
		return semanticResult;
	}
	
	
	private String parseResult(String jsonResult,ExternalSemanticResult semanticResult){
		if(null == jsonResult || jsonResult.isEmpty()){
			return null;
		}
		
		String result = null;
		JsonObject obj = new JsonParser().parse(jsonResult).getAsJsonObject();
		int status = obj.get("status").getAsInt();
		if(0 != status){
			semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));
			semanticResult.setSemanticRet(obj.get("status").getAsString());
			return null;
		}
		
		JsonArray dataArray = obj.get("data").getAsJsonArray();
		if(null == dataArray || dataArray.size() <= 0){
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));
			return null;
		}
		
		String intent = dataArray.get(0).getAsJsonObject().get("intent").getAsString();
		semanticResult.setIntent(intent);
		
		JsonArray resultsArray = dataArray.get(0).getAsJsonObject().get("results").getAsJsonArray();
		if(null == resultsArray || resultsArray.size() <= 0){
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));
			return null;
		}
		
		JsonObject replyObj = resultsArray.get(0).getAsJsonObject().get("reply").getAsJsonObject();
		if(null == replyObj){
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));
			return null;
		}
		
		result = replyObj.get("content").getAsString();
		
		return result;
	}
}
