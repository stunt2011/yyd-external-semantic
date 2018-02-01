package com.yyd.external.semantic;

import java.util.Map;



public class ExternalSemanticResult {
	
	public static enum OperationEx {
		/**
		 * 播放器播放
		 */
		PLAY,
		/**
		 * 文本转音频播放
		 */
		SPEAK,
		/**
		 * 机器人移动
		 */
		MOVE,
		/**
		 * 打开第三方APP
		 */
		APP
	}

	public static enum ParamTypeEx {
		/**
		 * 文本
		 */
		T,
		/**
		 * 资源URL
		 */
		U,
		/**
		 * 文本+资源URL
		 */
		TU,
		/**
		 * 图片URL+文本
		 */
		IT,
		/**
		 * 图片URL+资源URL
		 */
		IU,
		/**
		 * 图片URL+文本+资源URL
		 */
		ITU		
	}	
	
	//语义处理结果(错误码)
	/**
	 * 语义处理返回码，
	 */
	private Integer ret;
	/**
	 * 语义处理返回信息，当返回的错误信息指示为第三文外接语义错误时，需再根据semanticRet和semanticMsg字段
	 * 排查错误
	 */
	private String msg;
	/**
	 * 第三方外接语义的返回码，无错误时为空
	 */
	private String semanticRet;	
	/**
	 * 第三方外接语义的返回码，无错误时为空
	 */
	private String semanticMsg;
	
	//语义理解相关
	/**
	 * 用户输入，可能和原始文本不一致
	 */
	private String text;
	/**
	 * 技能或场景，不为空
	 */
	private String service;
	/**
	 * 意图，可能为空
	 */
	private String intent;
	private Long time;	
	private Map<String,Object> slots;
	
	//语义回复内容相关
	private OperationEx operation;
	private ParamTypeEx paramType;	
	private String answer;	
	
	
	//其他
	/**
	 * 第三方语义的服务标识，讯飞语义有此字段
	 */
	private String sid;
	/**
	 * 第三方外接语义返回的原始数据
	 */
	private String srcResult;
	

	public String getSrcResult() {
		return srcResult;
	}
	public void setSrcResult(String srcResult) {
		this.srcResult = srcResult;
	}
	public OperationEx getOperation() {
		return operation;
	}
	public void setOperation(OperationEx operation) {
		this.operation = operation;
	}
	public ParamTypeEx getParamType() {
		return paramType;
	}
	public void setParamType(ParamTypeEx paramType) {
		this.paramType = paramType;
	}
	
	public Map<String, Object> getSlots() {
		return slots;
	}
	public void setSlots(Map<String, Object> slots) {
		this.slots = slots;
	}
	
	public String getIntent() {
		return intent;
	}
	public void setIntent(String intent) {
		this.intent = intent;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}	
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	public Integer getRet() {
		return ret;
	}
	public void setRet(Integer ret) {
		this.ret = ret;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}	
	public String getSemanticRet() {
		return semanticRet;
	}
	public void setSemanticRet(String semanticRet) {
		this.semanticRet = semanticRet;
	}
	public String getSemanticMsg() {
		return semanticMsg;
	}
	public void setSemanticMsg(String semanticMsg) {
		this.semanticMsg = semanticMsg;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
			
}
