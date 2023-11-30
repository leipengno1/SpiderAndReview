package com.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPClientsUtil {
	private static Logger log = LoggerFactory.getLogger(HTTPClientsUtil.class);
	
	public static String doGet(String url ,String type, List<NameValuePair> paras, String ip, int port) {
		String result = "";
		CloseableHttpClient httpclient = null;
		CloseableHttpResponse httpResponse = null;
		int statusCode = 0;
		try {
			SSLContext sslcontext = createIgnoreVerifySSL();
			RequestBuilder requestBuilder = null;
			if("get".equalsIgnoreCase(type)) {
				requestBuilder = RequestBuilder.get().setUri(java.net.URLDecoder.decode(url, "UTF-8"));
			}else {
				requestBuilder = RequestBuilder.post().setUri(java.net.URLDecoder.decode(url, "UTF-8"));
				NameValuePair[] NParas = new NameValuePair[paras.size()];
				requestBuilder.addParameters(paras.toArray(NParas));
			}
			
			RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT)
					.setExpectContinueEnabled(true)
					.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
					.setConnectionRequestTimeout(30000)
					.setSocketTimeout(30000)
					.setConnectTimeout(30000)
					.build();
			RequestConfig requestConfig = RequestConfig.copy(defaultRequestConfig).build();
			
			if(StringUtils.isNotBlank(ip)) {
				requestConfig = RequestConfig.copy(defaultRequestConfig)
						.setProxy(new HttpHost(ip, port))
						.build();
			}
			
			requestBuilder.setConfig(requestConfig);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
					.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE)
					.register("https", new SSLConnectionSocketFactory(sslcontext)).build();
			PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
					socketFactoryRegistry);
			HttpClients.custom().setConnectionManager(connManager);
			httpclient = HttpClients.custom().setConnectionManager(connManager).build();

			HttpUriRequest httpUriRequest = requestBuilder.build();
			httpResponse = httpclient.execute(httpUriRequest);
			statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				HttpEntity entity = httpResponse.getEntity();
				result = EntityUtils.toString(entity);
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
		}
		return result;
	}

	
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSLv3");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
					String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}
	
	public static void main(String[] args) {
		//HTTPClientsUtil.doGet("https://edu.sse.com.cn/edumanage/bespeak/queryBespeakTime.do?st=68003");
	}

}
