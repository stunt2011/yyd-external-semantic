package com.yyd.external.semantic.xunfei;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.yyd.external.util.Http;
import com.yyd.external.util.StringTool;
import com.yyd.external.semantic.ExternalSemanticError;
import com.yyd.external.semantic.ExternalSemanticResult;
import com.yyd.external.semantic.ExternalSemanticService;

/**
 * 访问讯飞语义
 * @author pc
 *
 */
public class XunfeiSemanticService implements ExternalSemanticService{
	private  String web_api_url_text_semantic = "http://api.xfyun.cn/v1/aiui/v1/text_semantic";	
	private  String XAppid = null;
	private  String apiKey = null;
	
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
		XAppid = params.get("appid");
		apiKey = params.get("apiKey");
		if(StringTool.isEmpty(userId) || StringTool.isEmpty(XAppid) || StringTool.isEmpty(apiKey)) {
			result = new ExternalSemanticResult();
			result.setRet(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM);
			result.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_NO_REQUIRED_PARAM));
			return result;
		}
		
		long start = System.currentTimeMillis();	
		result = xunfeiWebApiTextUnderstand("main",userId,text);
		long total = System.currentTimeMillis() - start;
		result.setTime(total);
		
		return result;
	}
	
		
	@Override
	public String  getToken(String userId,String userIp)throws Exception{
		return null;
	}
	
	/**
	 * 迅飞语义：两个重要字段semantic 和answer字段,问答对类的(开放问答和自定义问答)一般没有semantic字段，技能类的一般会有semantic字段，可能没有answer字段
	 * 
	 */
	public  ExternalSemanticResult xunfeiWebApiTextUnderstand(String scene,String userid,String question){
		ExternalSemanticResult semanticResult = new ExternalSemanticResult();
		
		try {			
			String XCurTime = (System.currentTimeMillis() / 1000) + "";
			
			JSONObject XParam = new JSONObject();
			XParam.put("scene", scene);
			XParam.put("userid", userid);
			String XParamBase64Str = Base64.getEncoder().encodeToString(XParam.toString().getBytes());

			String http_body = "text="+Base64.getEncoder().encodeToString(question.getBytes());
			
			Http http = new Http(web_api_url_text_semantic);
			http.setCharset("utf-8");
			http.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
			http.setRequestProperty("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");			
			//授权信息
			http.setRequestProperty("X-Appid", XAppid);
			http.setRequestProperty("X-CurTime", XCurTime);
			StringBuilder sb = new StringBuilder();
			sb.append(apiKey).append(XCurTime).append(XParamBase64Str).append(http_body);
			http.setRequestProperty("X-CheckSum",EncoderByMd5(sb.toString()).toLowerCase());
			//XParam 
			http.setRequestProperty("X-Param", XParamBase64Str);

			//post请求
			String result = http.post(http_body);
			semanticResult.setSrcResult(result);
			parseResult(result,semanticResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return semanticResult;
	}
	
	private void parseResult(String result,ExternalSemanticResult semanticResult) {
		if(null == result || result.isEmpty()) {
			semanticResult.setRet(ExternalSemanticError.ERROR_INVALID_RESULT_DATA);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_INVALID_RESULT_DATA));	
			return;
		}
		
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));	
			return;
		}
		
		String rcCode = obj.get("code").getAsString();
		String desc  = obj.get("desc").getAsString();
		String sid = obj.get("sid").getAsString();
		semanticResult.setSid(sid);
		
		if(!rcCode.equalsIgnoreCase("00000")) {
			semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));	
			semanticResult.setSemanticRet(rcCode);
			semanticResult.setSemanticMsg(desc);
			return;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			semanticResult.setRet(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_PARSE_RESULT_ERROR));	
			return;
		}
		
		int rc = dataObject.get("rc").getAsInt();
		if(rc != 0) {
			semanticResult.setRet(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR);
			semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_EXTERNAL_SEMANTIC_ERROR));	
			semanticResult.setSemanticRet(rcCode);
			semanticResult.setSemanticMsg("rc ="+Integer.valueOf(rc));
			return;
		}
		
		String service = dataObject.get("service").getAsString();
		String text = dataObject.get("text").getAsString();
		semanticResult.setText(text);
		semanticResult.setService(service);
		
		//提取intent和slot等语义信息		
		JsonArray semanticArray = null;
		JsonElement semanticElem = dataObject.get("semantic");
		if(null != semanticElem) {
			semanticArray = semanticElem.getAsJsonArray();
		}
		if(null != semanticArray && semanticArray.size() >0) {
			//TODO:只提取第一条语义信息
			JsonObject first = semanticArray.get(0).getAsJsonObject();
			String intent = first.get("intent").getAsString();
			semanticResult.setIntent(intent);			
			//要提取语义槽
			JsonArray slotArray = first.get("slots").getAsJsonArray();
			Map<String,Object> slots = new HashMap<String,Object>();
			if(null != slotArray) {
				for(int i =0; i < slotArray.size();i++) {
					JsonObject item = slotArray.get(i).getAsJsonObject();
					String name = item.get("name").getAsString();
					String value = item.get("value").getAsString();
					if(null !=name) {
						slots.put(name, value);
					}
					
				}
			}
		
		  if(slots.size() >0) {
			  semanticResult.setSlots(slots);
		  }
		}
		
		
		//提取语义回复结果
		JsonObject answerObj = null;
		JsonElement answerElem = dataObject.get("answer");
		if(null != answerElem) {
			answerObj = answerElem.getAsJsonObject();
		}
		if(null != answerObj) {
			String answer = answerObj.get("text").getAsString();
			semanticResult.setAnswer(answer);
		}
		
		//处理需要单独合成结果的语义
		String serviceResult = parseServiceResult(semanticResult.getService(),semanticResult.getIntent(),result,semanticResult.getAnswer());
		if(null != serviceResult) {
			semanticResult.setAnswer(serviceResult);
		}
		
		semanticResult.setRet(ExternalSemanticError.ERROR_SUCCESS);
		semanticResult.setMsg(ExternalSemanticError.get(ExternalSemanticError.ERROR_SUCCESS));
	}
	
	private String parseServiceResult(String service,String intent,String result,String answerResult) {
		String answer = null;
		switch(service) {
			case "stock":
			{
				answer = parseStockServiceResult(result);
				break;
			}
			case "translation":
			{
				answer = parseTranslationServiceResult(result);
			}
			case "weather":
			{
				answer = parseWeatherServiceResult(answerResult);
			}
			default:
				break;
		}
		
		return answer;
	}
	
	public String parseWeatherServiceResult(String result) {
		if(null == result || result.isEmpty()) {
			return null;
		}
		String answer = result;
		answer = answer.replace("\"", "");
		return answer;
	}
	
	public String  parseTranslationServiceResult(String result) {
		String answer = null;		
				
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return null;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return null;
		}
		
		JsonObject subDataObject = dataObject.get("data").getAsJsonObject();
		if(null == subDataObject) {
			return null;
		}
		
		JsonArray resultArray = subDataObject.get("result").getAsJsonArray();
		if(null == resultArray || resultArray.size() <= 0) {
			return null;
		}
		
		JsonObject first = resultArray.get(0).getAsJsonObject();
		answer = first.get("translated").getAsString();
		
		return answer;
	}
	
	/**
	 * 解析股票技能返回结果
	 * @param result
	 * @return
	 */
	private String parseStockServiceResult(String result) {
		String answer = null;		
		StringBuilder build = new StringBuilder();
		
		JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
		if(null == obj) {			
			return null;
		}
		
		JsonObject dataObject = obj.get("data").getAsJsonObject();
		if(null == dataObject) {
			return null;
		}
		
		//提取intent和slot等语义信息，主要是为提取股票名称
		Map<String,Object> slots = new HashMap<String,Object>();
		JsonArray semanticArray = null;
		JsonElement semanticElem = dataObject.get("semantic");
		if(null != semanticElem) {
			semanticArray = semanticElem.getAsJsonArray();
		}
		if(null != semanticArray && semanticArray.size() >0) {
			//TODO:只提取第一条语义信息
			JsonObject first = semanticArray.get(0).getAsJsonObject();
			//要提取语义槽
			JsonArray slotArray = first.get("slots").getAsJsonArray();
			if(null != slotArray) {
				for(int i =0; i < slotArray.size();i++) {
					JsonObject item = slotArray.get(i).getAsJsonObject();
					String name = item.get("name").getAsString();
					String value = item.get("value").getAsString();
					if(null !=name) {
						slots.put(name, value);
					}
					
				}
			}		  
		}
		else
		{
			return null;
		}
		
		String stockName = slots.get("name").toString();
		String stockCode = "股票编号"+slots.get("market").toString()+slots.get("code").toString();
		build.append(stockName+",");
		build.append(stockCode+",");
		
		//提取股票详细信息
		//提取intent和slot等语义信息	
		String lastClosePrice = null;
		String openPrice = null;
		String currentPrice = null;
		String highPrice = null;
		String lowPrice = null;
		String riseValue = null;
		String riseRate = null;
		String updateDateTime = null;
		
		
		JsonObject stockDataObject = null;
		JsonElement stockDataElem = dataObject.get("data");
		if(null != stockDataElem) {
			stockDataObject = stockDataElem.getAsJsonObject();
		}
		if(null != stockDataObject) {
			//TODO:只提取第一条语义信息
			JsonArray resultArray = stockDataObject.get("result").getAsJsonArray();
			if(null != resultArray && resultArray.size() >0) {
				//要提取语义槽
				JsonObject first = resultArray.get(0).getAsJsonObject();
				
				lastClosePrice = "昨日收盘价"+first.get("closingPrice").getAsString();
				openPrice = "今日开盘价"+first.get("openingPrice").getAsString();
				currentPrice = "当前价"+first.get("currentPrice").getAsString();
				highPrice = "今日最高价"+first.get("highPrice").getAsString();
				lowPrice = "今日最低价"+first.get("lowPrice").getAsString();
				riseValue = "涨跌额"+first.get("riseValue").getAsString();
				riseRate = "涨跌幅"+first.get("riseRate").getAsString();
				updateDateTime = first.get("updateDateTime").getAsString()+"更新";
				
				build.append(lastClosePrice+",");
				build.append(openPrice+",");
				build.append(currentPrice+",");
				build.append(highPrice+",");
				build.append(lowPrice+",");
				build.append(riseValue+",");
				build.append(riseRate+",");
				build.append(updateDateTime);
			}
					  
		}
		
		answer = build.toString();
		
		return answer;
	}
	
	 /**利用MD5进行加密
     * @param str  待加密的字符串
     * @return  加密后的字符串
     * @throws NoSuchAlgorithmException  没有这种产生消息摘要的算法
     * @throws UnsupportedEncodingException  
     */
    private  String EncoderByMd5(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException{
    	char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
}
