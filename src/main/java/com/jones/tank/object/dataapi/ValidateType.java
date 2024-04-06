package com.jones.tank.object.dataapi;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum ValidateType {
	STRING("", ""),
	NUMBER("数字",""),
	MOBILE("手机号",""),
	EMAIL("邮箱",""),
	IDCARD("身份证", ""),
	PASSWORD("密码", "必须至少包含大写字母、小写字幕、数字、符号中的3个");

	public final String name;
	public final String description;

	ValidateType(String name, String description) {
		this.name = name;
		this.description = description;
	}

	private static ThreadLocal<Pattern> initPattern(String pattern){
		return ThreadLocal.withInitial(() -> Pattern.compile(pattern));
	}
	private static Map<ValidateType, ThreadLocal<Pattern>> VALIDATE_PATTERN = new HashMap<>();
	static {
		VALIDATE_PATTERN.put(NUMBER, initPattern("\\d+"));
		VALIDATE_PATTERN.put(MOBILE, initPattern("^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$"));
		VALIDATE_PATTERN.put(EMAIL, initPattern("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"));
		VALIDATE_PATTERN.put(IDCARD, initPattern("^\\d{15}|\\d{18}$"));
		VALIDATE_PATTERN.put(PASSWORD, initPattern("(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[^a-zA-Z0-9]).{8,32}"));
	}

	public boolean validate(String value){
		if(this.equals(ValidateType.STRING)){
			return value != null;
		}
		return VALIDATE_PATTERN.get(this).get().matcher(value).matches();
	}

	public String getDescription(){
		return String.format("校验%s格式失败! %s", this.name, this.description);
	}
}
