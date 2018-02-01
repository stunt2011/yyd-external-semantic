package com.yyd.external.semantic;

import java.util.Map;

public interface ExternalSemanticService {
	
	/**
	 * 
	 * @param text:问句
	 * @param userId：用户id，用户自定义，无数量限制，但迅飞语义有调用次数限制(每天20000)。
	 * @param params:认证参数，灵聚：userId字段，userIp字段，token字段，讯飞:userId字段，appid字段，apiKey字段,三角兽：userId字段
	 * @return
	 * @throws Exception
	 */
	ExternalSemanticResult handleSemantic(String text,Map<String,String> params)throws Exception;
	
		
	/**
	 * 获取灵聚用户token接口
	 * @param userId：用户id
	 * @param userIp：用户ip
	 * @return
	 * @throws Exception
	 */
	String  getToken(String userId,String userIp)throws Exception;
}
