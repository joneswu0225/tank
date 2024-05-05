package com.jones.tank.object;


import java.util.HashMap;
import java.util.Map;


public enum ErrorCode {

	OK("0", "成功"),

	//注册错误
	REGIST_MOBILE_EXISTS("1001", "手机号已被注册"),

	//登录错误
	LOGIN_MOBILE_NOTEXISTS("1002", "手机号不存在"),
	LOGIN_FAIL("1003", "验证码或密码错误"),
	WECHAT_DECRYPT_MOBILE_FAIL("1004", "解密手机号失败, code失效或者session_key失效"),
	WECHAT_LOGIN_VERIFY_FAIL("1005", "微信小程序登录验证失败"),
	WECHAT_NO_PASSWD("1006", "微信小程序注册没有密码"),
	WECHAT_OPENID_NOTEXISTS("1008", "未找到用户，需要授权手机号"),
	VERIFY_CODE_FAILED("1006", "验证码错误"),
	ADMIN_LOGIN_DENIED("1007", "非管理员无法登录管理系统"),

	//请求错误
	VALIDATION_FAILED("1011", "参数校验失败"),
	BAD_REQUEST("1012", "请求参数错误 [%s]"),
	UPLOAD_FAILED("1013", "文件上传失败"),

	//API验
	API_PATH_INVALID("1021","请求路径不存在"),
	API_METHOD_INVALID("1022","请求方法不匹配"),

	API_ENTITY_INVALID("1023", "参数校验失败"),
	API_PARAM_INVALID("1024", "当前版本仅支持单个实体的接口请求"),
	API_UPSERT_DUPLICATE_ENTITY("1025", "upsert接口的实体个数不能超过一个"),
	API_SQL_LIMIT_NOT_ENOUGH("1026", "提醒：更新删除需要包含有效条件"),

	//权限不够
	AUTH_PROJECT_UNPUBLIC_NOLOGIN("2001", "非登录状态无权访问非公开项目"),
	AUTH_PROJECT_UNAUTH("2002", "无权访问该项目"),
	AUTH_PROJECT_EDIT_UNAUTH("2003", "无权修改该项目"),
	AUTH_PROJECT_EDIT_NOTPARTNER("2004", "您不是该项目的共建人，无权修改该项目"),
	//业务错误
	ENT_USER_EXISTS("3001", "企业用户已存在"),
	//角色错误
	ROLE_DELETE_EXIST_USER("5101", "该角色仍存在被授予的用户，无法删除"),
	ROLE_PERMISSION_DELETE_NOAUTH("5101", "请联系管理员进行权限删除"),
	//部门错误
	DEPARTMENT_DELETE_EXIST_USER("6001", "该部门存在其他用户，无法删除"),

	// KRPANO
	KRPANO_SOURCE_IMAGE_NOT_EXIST("7001", "切图文件不存在"),
	KRPANO_SLICE_PROCESS_INTERRUPTED("7002", "切图流程意外中止"),
	KRPANO_SLICE_PROCESS_TIMEOUT("7003", "图片过大，切图流程超时"),
	KRPANO_SLICE_PARAM_ERROR("7004", "切图参数不能全空"),
	KRPANO_SLICE_TMPFILE_MOVE_ERROR("7005", "切图内部问题，联系管理员处理"),
	INTERNAL_ERROR("9000", "系统内部错误"),
	NEED_LOGIN("9001", "请登录后操作"),
	ROOM_MODIFY_DENIED("1101", "无权限操作聊天室"),
	ROOM_INVITE_DENIED("1102", "无权邀请用户"),
	ROOM_REMOVE_OWNER_DENIED("1103", "无权移除群创建人"),
	ROOM_REMOVE_USER_DENIED("1104", "无权移除群成员"),
	ROOM_USER_STATUS_WAITING("1105", "入群申请已提交，请等待审批"),
	ROOM_USER_STATUS_IN("1106", "已经入群无需重复加入"),
	;

	public final String key;

	public final String description;

	private ErrorCode(String key, String description) {
		this.key = key;
		this.description = description;
	}

	public boolean isSucceeded() {
		return this == OK;
	}

	public static boolean isSucceeded(String thatKey) {
		return OK.key.equals(thatKey);
	}

	// public static boolean isBadRequest(String keyName) {
	// // 原则：如果是未知原因，就认为是我们系统自己的问题
	//
	// if (keyName == null)
	// return false;
	// ErrorCode code = ErrorCodeMap.byKey(keyName);
	// if (code == null)
	// return false;
	// return code.INTERNAL_ERROR;
	// }

	private static class ErrorCodeMap {

		private static final Map<String, ErrorCode> mapByKey = new HashMap<>();

		static {
			for (ErrorCode er : ErrorCode.values()) {
				String key = er.key;
				if (mapByKey.containsKey(key)) {
					throw new InnerException(ErrorCode.INTERNAL_ERROR,
							"duplicated error code key: " + key);
				}

				mapByKey.put(key, er);
			}
		}

		private ErrorCodeMap() {
			// dummy
		}

		public static ErrorCode byKey(String key) {
			return mapByKey.get(key);
		}

	}

}
