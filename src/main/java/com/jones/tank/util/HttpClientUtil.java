package com.jones.tank.util;


import com.alibaba.fastjson.JSONObject;
import com.jones.tank.entity.param.WeprogramMsgCheckParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class HttpClientUtil {

    private static final int CONNECT_TIMEOUT = 10 * 1000;// 设置连接建立的超时时间为10s
    private static final int SOCKET_TIMEOUT = 30 * 1000;
    private static final int MAX_CONN = 30; // 最大连接数
    private static final int MAX_PRE_ROUTE = 30;
    private static final int MAX_ROUTE = 30;
    private static CloseableHttpClient httpClient; // 发送请求的客户端单例
    private static PoolingHttpClientConnectionManager manager; //连接池管理类
    private static ScheduledExecutorService monitorExecutor;

    private final static Object syncLock = new Object(); // 相当于线程锁,用于线程安全

    /**
     * 对http请求进行基本设置
     * @param httpRequestBase http请求
     */
    private static void setRequestConfig(HttpRequestBase httpRequestBase){
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(CONNECT_TIMEOUT)
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).build();
        httpRequestBase.setConfig(requestConfig);
    }

    public static CloseableHttpClient getHttpClient(String url){
        String hostName = url.split("/")[2];
        int port = 80;
        if (hostName.contains(":")){
            String[] args = hostName.split(":");
            hostName = args[0];
            port = Integer.parseInt(args[1]);
        }
        if (httpClient == null){
            //多线程下多个线程同时调用getHttpClient容易导致重复创建httpClient对象的问题,所以加上了同步锁
            synchronized (syncLock){
                if (httpClient == null){
                    httpClient = createHttpClient(hostName, port);
                    //开启监控线程,对异常和空闲线程进行关闭
                    monitorExecutor = Executors.newScheduledThreadPool(1);
                    monitorExecutor.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            //关闭异常连接
                            manager.closeExpiredConnections();
                            //关闭5s空闲的连接
                            manager.closeIdleConnections(5 * 1000, TimeUnit.MILLISECONDS);
                            log.info("close expired and idle for over 5s connection");
                        }
                    }, 60 * 1000, 60 * 1000, TimeUnit.MILLISECONDS);
                }
            }
        }
        return httpClient;
    }

    /**
     * 根据host和port构建httpclient实例
     * @param host 要访问的域名
     * @param port 要访问的端口
     * @return
     */
    public static CloseableHttpClient createHttpClient(String host, int port){
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        manager = new PoolingHttpClientConnectionManager(registry);
        //设置连接参数
        manager.setMaxTotal(MAX_CONN); // 最大连接数
        manager.setDefaultMaxPerRoute(MAX_PRE_ROUTE); // 路由最大连接数

        HttpHost httpHost = new HttpHost(host, port);
        manager.setMaxPerRoute(new HttpRoute(httpHost), MAX_ROUTE);

        //请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i > 3){
                    //重试超过3次,放弃请求
                    log.error("retry has more than 3 time, give up request");
                    return false;
                }
                if (e instanceof NoHttpResponseException){
                    //服务器没有响应,可能是服务器断开了连接,应该重试
                    log.error("receive no response from server, retry");
                    return true;
                }
                if (e instanceof SSLHandshakeException){
                    // SSL握手异常
                    log.error("SSL hand shake exception");
                    return false;
                }
                if (e instanceof InterruptedIOException){
                    //超时
                    log.error("InterruptedIOException");
                    return false;
                }
                if (e instanceof UnknownHostException){
                    // 服务器不可达
                    log.error("server host unknown");
                    return false;
                }
                if (e instanceof ConnectTimeoutException){
                    // 连接超时
                    log.error("Connection Time out");
                    return false;
                }
                if (e instanceof SSLException){
                    log.error("SSLException");
                    return false;
                }

                HttpClientContext context = HttpClientContext.adapt(httpContext);
                HttpRequest request = context.getRequest();
                if (!(request instanceof HttpEntityEnclosingRequest)){
                    //如果请求不是关闭连接的请求
                    return true;
                }
                return false;
            }
        };

        CloseableHttpClient client = HttpClients.custom().setConnectionManager(manager).setRetryHandler(handler).build();
        return client;
    }

    /**
     * 设置post请求的参数
     * @param httpPost
     * @param params
     */
    private static void setPostParams(HttpPost httpPost, Map<String, Object> params){
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        Set<String> keys = params.keySet();
        for (String key: keys){
            nvps.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String post(String url, Map<String, Object> params) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        setPostParams(httpPost, params);
        return getRequestResult(httpPost);
    }

    public static JSONObject postJson(String url, Map<String, Object> params) throws Exception {
        String result = post(url, params);
        return JSONObject.parseObject(result);

    }

    public static JSONObject postJson(String url, Serializable object) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new SerializableEntity(object));
        String result = getRequestResult(httpPost);
        return JSONObject.parseObject(result);

    }

    public static String get(String url) throws Exception {
        HttpGet httpget = new HttpGet(url);
        setRequestConfig(httpget);
        return getRequestResult(httpget);
    }

    public static JSONObject getJson(String url) throws Exception {
        String result = getRequestResult(new HttpGet(url));
        return JSONObject.parseObject(result);
    }

    public static String getRequestResult(HttpRequestBase request) throws Exception {
        setRequestConfig(request);
        CloseableHttpResponse response = null;
        String result = null;
        try {
            response = getHttpClient(request.getURI().toString()).execute(request, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "utf-8");
            }
        } catch (IOException e) {
            log.error("Fail to get response from url: " + request.getURI().toString(), e);
            throw new Exception("Fail to get response from url: " + request.getURI().toString());
        } finally {
            try{
                if (response != null) response.close();
            } catch (IOException e) {
                log.error("Fail to close httpclient response", e);
            }
        }
        return result;

    }

    /**
     * 关闭连接池
     */
    public static void closeConnectionPool(){
        try {
            httpClient.close();
            manager.close();
            monitorExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String token = "8FhLKERD1e0D79Y1J8hxWE_32CY2pmJGi3l_CSYOcNEtT_VMk6fEyL7i7tRCcRz3Nz-67r4bWoEfxlqIwbbL22RS77o0QBRpml2mf3TeFdkAMDzJiiFdwcj13yO8QZOgAEAMVK";
        String url = "https://api.weixin.qq.com/wxa/msg_sec_check?access_token=";
        url = url+token;
        JSONObject param = new JSONObject();
        param.put("scene", 1);
        param.put("version", 2);
        param.put("content", "Hello");
        param.put("openid", "o7rt-65CCbcXh3ZA0b7-PHkpJycg");
        Map<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        Map<String, Object> result = HttpUtil.doPost2(url, header, param.toJSONString());
        System.out.println(result.getOrDefault("body", ""));
//
//        WeprogramMsgCheckParam param = WeprogramMsgCheckParam.builder().scene(1).version(2).content("Hello").openid("o7rt-65CCbcXh3ZA0b7-PHkpJycg").build();
//        HttpPost post = new HttpPost(url);
//        post.setEntity(new SerializableEntity(param));
//        post.setHeader("Content-Type", "application/json");
//        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
//                CloseableHttpClient client = httpClientBuilder.build();
//                CloseableHttpResponse response = client.execute(post);
//        System.out.println(response);
    }

}
