package com.yyd.external.semantic.lingju;

import org.json.JSONObject;

import java.util.Map;

import org.json.JSONException;

import com.yyd.external.semantic.ExternalSemanticError;
import com.yyd.external.semantic.ExternalSemanticResult;
import com.yyd.external.semantic.ExternalSemanticService;
import com.yyd.external.semantic.ExternalSemanticResult.OperationEx;
import com.yyd.external.semantic.ExternalSemanticResult.ParamTypeEx;
import com.yyd.external.util.Http;
import com.yyd.external.util.StringTool;

/**
 * 访问灵聚语义
 * @author pc
 *
 */
public class LingjuSemanticService implements ExternalSemanticService{
	private String authcode = null;  
	private String appKey = null;
	private String tokenUrl = "http://dev.lingju.ai:8999/serverapi/authorize.do";
	private String charUrl = "http://dev.lingju.ai:8999/serverapi/ljchat.do";
	
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
		String userIp = params.get("userIp");
		String token = params.get("token");
		authcode = params.get("authCode");
		appKey = params.get("appKey");
		
		if(StringTool.isEmpty(userId) || StringTool.isEmpty(userIp) || StringTool.isEmpty(token) || 
		  StringTool.isEmpty(authcode) || StringTool.isEmpty(appKey)) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM));
			return result;
		}
		
		long start = System.currentTimeMillis();	
		result = getAnswer(userId,userIp,token,text);		
		long total = System.currentTimeMillis() - start;
		result.setTime(total);
		//灵聚没有技能标识，暂且全部定为闲聊
		result.setService("chat");
		result.setOperation(OperationEx.SPEAK);
		result.setParamType(ParamTypeEx.T);
		
		return result;
	}
	
	@Override
	public String  getToken(String userId,String userIp)throws Exception{
		return getAccessToken(userId,userIp,null);
	}
	
	
	public String getAuthcode() {
		return authcode;
	}

	public void setAuthcode(String authcode) {
		this.authcode = authcode;
	}

	
	/**
	 * 但是目前好像申请token和userid没有必然关系，uerIP也无关吗？不确定
	 * @param userid
	 * @param userip
	 * @return
	 */
	public String getAccessToken(String userid, String userip,ExternalSemanticResult semanticResult) {
	
		try {

			String httpURL = tokenUrl+"?appkey="+appKey
					+ "&userid=" + userid + "&userip=" + userip + "&authcode=" + authcode;
			Http http0 = new Http(httpURL);

			http0.setCharset("utf-8");
			http0.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");

			String result0 = http0.get();
			
			JSONObject retJSON = new JSONObject(result0);
			int status = retJSON.optInt("status", -1);
			if (status == 0) {
				return retJSON.optJSONObject("data").optString("accessToken");
			}
			else
			{
				if(null != semanticResult) {
					semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
					semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));
					semanticResult.setSemanticRet(Integer.toString(status));
					
					String description = retJSON.optString("description");
					semanticResult.setSemanticMsg(description);
				}
				
			}
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	}

	public String getChatResult(String accessToken, String userip, String question) {
		// TODO Auto-generated method stub
		try {
			Http http = new Http(charUrl);
			http.setCharset("utf-8");
			http.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");

			http.setReadTimeout(1200);
			
			JSONObject json = new JSONObject();
			json.put("accessToken", accessToken);
			json.put("userip", userip);
			json.put("input", question);

			
			String result1 = http.post(json.toString());
			return result1;
		} catch (Exception e) {
			
			return null;
		}
	}
	
	/**
	 * 
	 * @param userid:用户ID,自由分配
	 * @param userip：
	 * @param question：问题
	 * @return
	 */
	public ExternalSemanticResult getAnswer(String userid, String userip, String token,String question) {
		ExternalSemanticResult semanticResult = new ExternalSemanticResult();
		
		String accessToken = token;
	
		String result1 =getChatResult(accessToken, userip, question);
		semanticResult.setSrcResult(result1);
		if (StringTool.isEmpty(result1)) {
			semanticResult.setRet(ExternalSemanticError.ERROR_INVALID_RESULT_DATA);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INVALID_RESULT_DATA));
			return semanticResult;
		}
		JSONObject chatJobj = null;
		try {
			chatJobj = new JSONObject(result1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int chatStatus = chatJobj.optInt("status", -1);
		String answer = chatJobj.optString("answer");
		String description = chatJobj.optString("description");

		if (chatStatus != 0) {
			semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));
			semanticResult.setSemanticRet(Integer.toString(chatStatus));
			semanticResult.setSemanticMsg(description);
			return semanticResult;
		}
		
		//anser字段是纯文本
		if(!answer.contains("{")  &&  !answer.contains("|")  &&  !answer.contains("[")) {
			semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));
			semanticResult.setAnswer(answer);
			return semanticResult;
		}
		
		//rtext
		JSONObject answerObj = null;
		try {
			answerObj = new JSONObject(answer);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String rtext = answerObj.optString("rtext","");
		if(StringTool.isEmpty(rtext)){
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));
			return semanticResult;
		}
		
		//TODO:数学运算语义特殊处理，但是可能会影响其他技能
		int index = rtext.indexOf("，那么");
		if(index > 0){
			rtext = rtext.substring(0,index);
		}
		
		semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
		semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));
		semanticResult.setAnswer(rtext);		
		
		return semanticResult;
	}
	
}
