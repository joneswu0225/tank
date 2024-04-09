package com.jones.tank.util;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 只能用于接收云通信的消息，不能用于接收其他业务的消息
 * 短信上行消息接收demo
 */
@Slf4j
@Component
public class AliMnsSender {
	//ascClient需要的几个参数
	private static final String product = "Dysmsapi";//短信API产品名称（短信产品名固定，无需修改）
	private static final String domain = "dysmsapi.aliyuncs.com";//短信API产品域名（接口地址固定，无需修改）
	private static String accessKeyId;//你的accessKeyId,参考本文档步骤2
	private static String accessKeySecret;//你的accessKeySecret，参考本文档步骤2
	private static String signname;//必填:短信签名-可在短信控制台中找到
	private static String templateCode;//必填:短信签名-可在短信控制台中找到
	private static String templateParam = "{\"code\":\"%s\"}";

	static{
		//设置超时时间-可自行调整
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");
	}

	public static boolean sendMns(String mobile, String code) {
		//组装请求对象
		SendSmsRequest request = new SendSmsRequest();
//		使用post提交
		request.setMethod(MethodType.POST);
		//必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为国际区号+号码，如“85200000000”
		request.setPhoneNumbers(mobile);
		//必填:短信签名-可在短信控制台中找到
		request.setSignName(signname);
		//必填:短信模板-可在短信控制台中找到，发送国际/港澳台消息时，请使用国际/港澳台短信模版
		request.setTemplateCode(templateCode);
		//可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
		//友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
		request.setTemplateParam(String.format(templateParam, code));
		//可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
		//request.setSmsUpExtendCode("90997");
		//可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
		//request.setOutId("222222");
		//初始化ascClient,暂时不支持多region（请勿修改）
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
		try {
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient acsClient = new DefaultAcsClient(profile);
			//请求失败这里会抛ClientException异常
			log.debug(String.format("阿里云消息发送请求参数： %s", ObjectUtil.toJsonSting(request)));
			SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
			log.info(String.format("阿里云消息发送结果： %s", ObjectUtil.toJsonSting(sendSmsResponse)));
			if(sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
				return true;
			}
		} catch (ClientException e) {
			log.error("阿里云消息发送异常", e);
		} finally {
			return false;
		}
	}

	@Value("${aliyun.mns.accesskey.id:}")
	private void setAccessKeyId(String accessKeyId){
		AliMnsSender.accessKeyId = accessKeyId;
	}
	@Value("${aliyun.mns.accesskey.secret:}")
	private void setAccessKeySecret(String accessKeySecret){
		AliMnsSender.accessKeySecret = accessKeySecret;
	}
	@Value("${aliyun.mns.signname:}")
	private void setSignName(String signname){
		AliMnsSender.signname = signname;
	}
	@Value("${aliyun.mns.template.code:}")
	private void setTemplateCode(String templateCode){
		AliMnsSender.templateCode = templateCode;
	}
	
}
