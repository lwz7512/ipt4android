package com.pintu.http;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import com.pintu.PintuApp;
import com.pintu.util.DebugTimer;

import android.util.Log;


public class SimpleHttpClient implements HttpClientInterface{
	
    private static final String TAG = "SimpleHttpClient";
	
	private DefaultHttpClient mClient;

	public SimpleHttpClient() {
		mClient = new DefaultHttpClient();
	}
	
	private String getUser() {
		return PintuApp.userID;
	}
	
	//暴露唯一一个post方法
	public Response post(String url, ArrayList<BasicNameValuePair> params, File file, boolean authenticate) throws HttpException{
			//用户本人
			params.add(new BasicNameValuePair("user",getUser()));
			params.add(new BasicNameValuePair("owner",getUser()));
			params.add(new BasicNameValuePair("source","android"));
			return httpRequest(url, params, file, authenticate, HttpPost.METHOD_NAME);
	}
	//暴露唯一一个get方法
	public Response get(String url, boolean authenticated) throws HttpException{		
			return httpRequest(url, null, null, authenticated, HttpGet.METHOD_NAME);
	}	
	
    /**
     * Execute the DefaultHttpClient
     * 
     * @param url
     *            target
     * @param postParams
     * @param file
     *            can be NULL
     * @param authenticated
     *            need or not
     * @param httpMethod  
     *            HttpPost.METHOD_NAME
     *            HttpGet.METHOD_NAME
     *            HttpDelete.METHOD_NAME
     * @return Response from server
     * @throws HttpException 此异常包装了一系列底层异常 <br /><br />
     *          1. 底层异常, 可使用getCause()查看: <br />
     *              <li>URISyntaxException, 由`new URI` 引发的.</li>
     *              <li>IOException, 由`createMultipartEntity` 或 `UrlEncodedFormEntity` 引发的.</li>
     *              <li>IOException和ClientProtocolException, 由`HttpClient.execute` 引发的.</li><br />
     *         
     *          2. 当响应码不为200时报出的各种子类异常:
     *             <li>HttpRequestException, 通常发生在请求的错误,如请求错误了 网址导致404等, 抛出此异常,
     *             首先检查request log, 确认不是人为错误导致请求失败</li>
     *             <li>HttpAuthException, 通常发生在Auth失败, 检查用于验证登录的用户名/密码/KEY等</li>
     *             <li>HttpRefusedException, 通常发生在服务器接受到请求, 但拒绝请求, 可是多种原因, 具体原因
     *             服务器会返回拒绝理由, 调用HttpRefusedException#getError#getMessage查看</li>
     *             <li>HttpServerException, 通常发生在服务器发生错误时, 检查服务器端是否在正常提供服务</li>
     *             <li>HttpException, 其他未知错误.</li>
     */
    private Response httpRequest(String url, ArrayList<BasicNameValuePair> postParams,
            File file, boolean authenticated, String httpMethod) throws HttpException {
       
    	Log.d(TAG, "Sending " + httpMethod + " request to " + url);
        
        DebugTimer.betweenStart("HTTP");
        

        URI uri = createURI(url);

        HttpResponse response = null;
        Response res = null;
        HttpUriRequest method = null;

        // Create POST, GET or DELETE METHOD
        method = createMethod(httpMethod, uri, file, postParams);
        // Setup ConnectionParams, Request Headers
        setupHTTPConnectionParams(method);
        
        try {
        	// Execute Request
            response = mClient.execute(method);
            res = new Response(response);
        } catch (ClientProtocolException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new HttpException(e.getMessage(), e);
        } catch (IOException ioe) {
            throw new HttpException(ioe.getMessage(), ioe);
        }

        if (response != null) {
            int statusCode = response.getStatusLine().getStatusCode();
            // It will throw a weiboException while status code is not 200
            handleResponseStatusCode(statusCode, res);
        } else {
            Log.e(TAG, "response is null");
        }

        DebugTimer.betweenEnd("HTTP");

        return res;
    }
    
    /**
     * CreateURI from URL string
     * 
     * @param url
     * @return request URI
     * @throws HttpException
     *             Cause by URISyntaxException
     */
    private URI createURI(String url) throws HttpException {
        URI uri;

        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new HttpException("Invalid URL.");
        }

        return uri;
    }
    
    /**
     * Create request method, such as POST, GET, DELETE
     * 
     * @param httpMethod
     *            "GET","POST","DELETE"
     * @param uri
     *            请求的URI
     * @param file
     *            可为null
     * @param postParams
     *            POST参数
     * @return httpMethod Request implementations for the various HTTP methods
     *         like GET and POST.
     * @throws HttpException
     *             createMultipartEntity 或 UrlEncodedFormEntity引发的IOException
     */
    private HttpUriRequest createMethod(String httpMethod, URI uri, File file,
            ArrayList<BasicNameValuePair> postParams) throws HttpException {
        
        HttpUriRequest method;

        if (httpMethod.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
            // POST METHOD

            HttpPost post = new HttpPost(uri);
            // See this: http://groups.google.com/group/twitter-development-talk/browse_thread/thread/e178b1d3d63d8e3b
            post.getParams().setBooleanParameter("http.protocol.expect-continue", false);
            
            try {
                HttpEntity entity = null;
                if (null != file) {
                    entity = createMultipartEntity("photo", file, postParams);
                    post.setEntity(entity);
                } else if (null != postParams) {
                    entity = new UrlEncodedFormEntity(postParams, HTTP.UTF_8);
                }
                post.setEntity(entity);
            } catch (IOException ioe) {
                throw new HttpException(ioe.getMessage(), ioe);
            }

            method = post;
        } else if (httpMethod.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
            method = new HttpDelete(uri);
        } else {
            method = new HttpGet(uri);
        }
        
        return method;
    }

    /**
     * Setup HTTPConncetionParams
     * 
     * @param method
     */
    private void setupHTTPConnectionParams(HttpUriRequest method) {
        HttpConnectionParams.setConnectionTimeout(method.getParams(),
                CONNECTION_TIMEOUT_MS);
        HttpConnectionParams
                .setSoTimeout(method.getParams(), SOCKET_TIMEOUT_MS);
        mClient.setHttpRequestRetryHandler(requestRetryHandler);
        method.addHeader("Accept-Encoding", "gzip, deflate");
        method.addHeader("Accept-Charset", "UTF-8,*;q=0.5");
    }
    
    /**
     * Handle Status code
     * 
     * @param statusCode
     *            响应的状态码
     * @param res
     *            服务器响应
     * @throws HttpException
     *             当响应码不为200时都会报出此异常:<br />
     *             <li>HttpRequestException, 通常发生在请求的错误,如请求错误了 网址导致404等, 抛出此异常,
     *             首先检查request log, 确认不是人为错误导致请求失败</li>
     *             <li>HttpAuthException, 通常发生在Auth失败, 检查用于验证登录的用户名/密码/KEY等</li>
     *             <li>HttpRefusedException, 通常发生在服务器接受到请求, 但拒绝请求, 可是多种原因, 具体原因
     *             服务器会返回拒绝理由, 调用HttpRefusedException#getError#getMessage查看</li>
     *             <li>HttpServerException, 通常发生在服务器发生错误时, 检查服务器端是否在正常提供服务</li>
     *             <li>HttpException, 其他未知错误.</li>
     */
    private void handleResponseStatusCode(int statusCode, Response res)
            throws HttpException {
        String msg = getCause(statusCode) + "\n";
        

        switch (statusCode) {
        // It's OK, do nothing
        case OK:
            break;

        // Mine mistake, Check the Log
        case NOT_MODIFIED:
        case BAD_REQUEST:
        case NOT_FOUND:
        case NOT_ACCEPTABLE:
            throw new HttpException(msg + res.asString(), statusCode);

        // UserName/Password incorrect
        case NOT_AUTHORIZED:
            throw new HttpAuthException(msg + res.asString(), statusCode);

        // Server will return a error message, use
        // HttpRefusedException#getError() to see.
        case FORBIDDEN:
            throw new HttpRefusedException(msg, statusCode);

        // Something wrong with server
        case INTERNAL_SERVER_ERROR:
        case BAD_GATEWAY:
        case SERVICE_UNAVAILABLE:
            throw new HttpServerException(msg, statusCode);

        // Others
        default:
            throw new HttpException(msg + res.asString(), statusCode);
        }
    }
    
    /**
     * 创建可带一个File的MultipartEntity
     * 
     * @param filename
     *            文件名
     * @param file
     *            文件
     * @param postParams
     *            其他POST参数
     * @return 带文件和其他参数的Entity
     * @throws UnsupportedEncodingException
     */
    private MultipartEntity createMultipartEntity(String filename, File file,
            ArrayList<BasicNameValuePair> postParams)
            throws UnsupportedEncodingException {
        MultipartEntity entity = new MultipartEntity();
        // Don't try this. Server does not appear to support chunking.
        // entity.addPart("media", new InputStreamBody(imageStream, "media"));

        entity.addPart(filename, new FileBody(file));
        for (BasicNameValuePair param : postParams) {
            entity.addPart(param.getName(), new StringBody(param.getValue()));
        }
        return entity;
    }

    /**
     * 异常自动恢复处理, 使用HttpRequestRetryHandler接口实现请求的异常恢复
     */
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
        // 自定义的恢复策略
        public boolean retryRequest(IOException exception, int executionCount,
                HttpContext context) {
            // 设置恢复策略，在发生异常时候将自动重试N次
            if (executionCount >= RETRIED_TIME) {
                // Do not retry if over max retry count
                return false;
            }
            if (exception instanceof NoHttpResponseException) {
                // Retry if the server dropped connection on us
                return true;
            }
            if (exception instanceof SSLHandshakeException) {
                // Do not retry on SSL handshake exception
                return false;
            }
            HttpRequest request = (HttpRequest) context
                    .getAttribute(ExecutionContext.HTTP_REQUEST);
            boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
            if (!idempotent) {
                // Retry if the request is considered idempotent
                return true;
            }
            return false;
        }
    };
    
    /**
     * 解析HTTP错误码
     * 
     * @param statusCode
     * @return
     */
    private static String getCause(int statusCode) {
        String cause = null;
        switch (statusCode) {
        case NOT_MODIFIED:
            break;
        case BAD_REQUEST:
            cause = "The request was invalid.  An accompanying error message will explain why. This is the status code will be returned during rate limiting.";
            break;
        case NOT_AUTHORIZED:
            cause = "Authentication credentials were missing or incorrect.";
            break;
        case FORBIDDEN:
            cause = "The request is understood, but it has been refused.  An accompanying error message will explain why.";
            break;
        case NOT_FOUND:
            cause = "The URI requested is invalid or the resource requested, such as a user, does not exists.";
            break;
        case NOT_ACCEPTABLE:
            cause = "Returned by the Search API when an invalid format is specified in the request.";
            break;
        case INTERNAL_SERVER_ERROR:
            cause = "Something is broken.  Please post to the group so the Weibo team can investigate.";
            break;
        case BAD_GATEWAY:
            cause = "Weibo is down or being upgraded.";
            break;
        case SERVICE_UNAVAILABLE:
            cause = "Service Unavailable: The Weibo servers are up, but overloaded with requests. Try again later. The search and trend methods use this to indicate when you are being rate limited.";
            break;
        default:
            cause = "";
        }
        return statusCode + ":" + cause;
    }
    
    

} //end of class
