package com.yyd.external.semantic;

import java.util.HashMap;
import java.util.Map;

public class ExternalSemanticError {
	public static Map<Integer,String> allErrorMsg = new HashMap<Integer,String>();
	public static final Integer ERROR_SUCCESS = 0;
	public static final Integer ERROR_EXTERNAL_SEMANTIC_ERROR = 1;
	public static final Integer ERROR_PARSE_RESULT_ERROR = 2;
	public static final Integer ERROR_LINGJU_NO_TOKEN = 3;
	public static final Integer ERROR_INVALID_RESULT_DATA = 4;
	public static final Integer ERROR_WRONG_INTERFACE = 5;
	public static final Integer ERROR_INPUT_PARAM_EMPTY = 6;
	public static final Integer ERROR_NO_REQUIRED_PARAM = 7;
	
	static {
		allErrorMsg.put(ERROR_SUCCESS, "ok");
		allErrorMsg.put(ERROR_EXTERNAL_SEMANTIC_ERROR, "外接语义返回错误信息");
		allErrorMsg.put(ERROR_PARSE_RESULT_ERROR, "解析返回数据错误");
		allErrorMsg.put(ERROR_LINGJU_NO_TOKEN, "无灵聚用户的token");
		allErrorMsg.put(ERROR_INVALID_RESULT_DATA, "语义返回数据为空或无效");
		allErrorMsg.put(ERROR_WRONG_INTERFACE, "调用错误的接口");
		allErrorMsg.put(ERROR_INPUT_PARAM_EMPTY, "输入参数为空");
		allErrorMsg.put(ERROR_NO_REQUIRED_PARAM, "缺省必须的参数");
	}
	
	public static String get(Integer errorCode) {
		String msg =  allErrorMsg.get(errorCode);
		if(null != msg) {
			return msg;
		}
		return "未知的错误码:"+errorCode;
	}
}
