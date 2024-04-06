package com.jones.tank.object;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.ToString;


/**
 * @author yue.su
 * @date 2018年3月28日
 */
@JsonSerialize
@ToString
@Data
public class BaseResponse {

	private String code = ErrorCode.OK.key;

	private String message = ErrorCode.OK.description;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Object data;

	public BaseResponse() {
		this(ErrorCode.OK);
	}

	public BaseResponse(ErrorCode errorCode) {
		setErrorCode(errorCode);
	}

	public BaseResponse(ErrorCode code, String message) {
		this.code = code.key;
		this.message = message;
	}
	
	public BaseResponse(ErrorCode code, String message, Object data) {
		this.code = code.key;
		this.message = message;
		this.data = data;
	}

	public boolean isSuceeded() {
		return ErrorCode.OK.key.equals(getCode());
	}

	public void setErrorCode(ErrorCode errorCode) {
		setCode(errorCode.key);
		setMessage(errorCode.description);
	}

	public void setErrorCode(ErrorCode errorCode, Object... args) {
		setCode(errorCode.key);
		setMessage(String.format(errorCode.description, args));
	}
	
	public static BaseResponseBuilder builder() {
 		return new BaseResponseBuilder();
 	}
 	
 	public static class BaseResponseBuilder {
 		private ErrorCode code = ErrorCode.OK;
 		private String message = ErrorCode.OK.description;
 		private Object data;
 		
 		private BaseResponseBuilder() {}
 		
 		public BaseResponseBuilder code(ErrorCode code) {
 			this.code = code;
 			this.message = code.description;
 			return this;
 		}
 		
 		public BaseResponseBuilder message(String message) {
 			this.message = message;
 			return this;
 		}
 		
 		public BaseResponseBuilder data(Object data) {
 			this.data = data;
 			return this;
 		}
 		
 		public BaseResponse build() {
 			return new BaseResponse(code, message, data);
 		}
 	}


}
