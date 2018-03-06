package com.yyd.external.semantic.lingju;

import org.json.JSONObject;

import com.yyd.external.util.Http;

public class LingjuTokenService {

	private static String tokenUrl = "http://dev.lingju.ai:8999/serverapi/authorize.do";
	/**
	 * 但是目前好像申请token和userid没有必然关系，uerIP也无关吗？不确定
	 * @param userid
	 * @param userip
	 * @return
	 */
	public static String getAccessToken(String appKey,String authCode,String userId, String userIp) {		
		try {

			String httpURL = tokenUrl+"?appkey="+appKey
					+ "&userid=" + userId + "&userip=" + userIp + "&authcode=" + authCode;
			Http http0 = new Http(httpURL);

			http0.setCharset("utf-8");
			http0.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=UTF-8");

			String result0 = http0.get();
			
			JSONObject retJSON = new JSONObject(result0);
			int status = retJSON.optInt("status", -1);
			if (status == 0) {
				return retJSON.optJSONObject("data").optString("accessToken");
			}
			
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			return null;
		}
	}
}
