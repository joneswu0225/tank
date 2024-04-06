/**
 * 
 */
package com.jones.tank.object;


public class RequestException extends InnerException {

	/**
	 * @param code 错误代码
	 */
	public RequestException(ErrorCode code) {
		this(code, code.description);
	}

	/**
	 * @param reason 作为格式化ErrorCode.BAD_REQUEST消息的参数
	 */
	public RequestException(String reason) {
		this(ErrorCode.BAD_REQUEST, reason);
	}

	/**
	 * @param args code.description作为格式化消息的参数
	 */
	public RequestException(Object... args) {
		super(ErrorCode.BAD_REQUEST, String.format(ErrorCode.BAD_REQUEST.description, args));
	}

	/**
	 * @param code 错误代码
	 * @param args code.description作为格式化消息的参数
	 */
	public RequestException(ErrorCode code, Object... args) {
		super(code, String.format(code.description, args));
	}

	/**
	 * @param code 错误代码
	 * @param cause 导致错误的异常
	 */
	public RequestException(ErrorCode code, Throwable cause) {
		super(code, code.description, cause);
	}

}
