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

import com.pintu.util.DebugTimer;

import android.util.Log;


public class SimpleHttpClient implements HttpClientInterface{
	
    private static final String TAG = "SimpleHttpClient";
	
	private DefaultHttpClient mClient;

	public SimpleHttpClient() {
		mClient = new DefaultHttpClient();
	}
	
	//��¶Ψһһ��post����
	public Response post(String url, ArrayList<BasicNameValuePair> params, File file, boolean authenticate){
		Response res = null;
		try {
			res = httpRequest(url, params, file, authenticate, HttpPost.METHOD_NAME);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	//��¶Ψһһ��get����
	public Response get(String url, ArrayList<BasicNameValuePair> params,boolean authenticated) {
		Response res = null;
		try {
			res = httpRequest(url, params, null, authenticated, HttpGet.METHOD_NAME);
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
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
     * @throws HttpException ���쳣��װ��һϵ�еײ��쳣 <br /><br />
     *          1. �ײ��쳣, ��ʹ��getCause()�鿴: <br />
     *              <li>URISyntaxException, ��`new URI` ������.</li>
     *              <li>IOException, ��`createMultipartEntity` �� `UrlEncodedFormEntity` ������.</li>
     *              <li>IOException��ClientProtocolException, ��`HttpClient.execute` ������.</li><br />
     *         
     *          2. ����Ӧ�벻Ϊ200ʱ�����ĸ��������쳣:
     *             <li>HttpRequestException, ͨ������������Ĵ���,����������� ��ַ����404��, �׳����쳣,
     *             ���ȼ��request log, ȷ�ϲ�����Ϊ����������ʧ��</li>
     *             <li>HttpAuthException, ͨ��������Authʧ��, ���������֤��¼���û���/����/KEY��</li>
     *             <li>HttpRefusedException, ͨ�������ڷ��������ܵ�����, ���ܾ�����, ���Ƕ���ԭ��, ����ԭ��
     *             �������᷵�ؾܾ�����, ����HttpRefusedException#getError#getMessage�鿴</li>
     *             <li>HttpServerException, ͨ�������ڷ�������������ʱ, �����������Ƿ��������ṩ����</li>
     *             <li>HttpException, ����δ֪����.</li>
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
     *            �����URI
     * @param file
     *            ��Ϊnull
     * @param postParams
     *            POST����
     * @return httpMethod Request implementations for the various HTTP methods
     *         like GET and POST.
     * @throws HttpException
     *             createMultipartEntity �� UrlEncodedFormEntity������IOException
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
     *            ��Ӧ��״̬��
     * @param res
     *            ��������Ӧ
     * @throws HttpException
     *             ����Ӧ�벻Ϊ200ʱ���ᱨ�����쳣:<br />
     *             <li>HttpRequestException, ͨ������������Ĵ���,����������� ��ַ����404��, �׳����쳣,
     *             ���ȼ��request log, ȷ�ϲ�����Ϊ����������ʧ��</li>
     *             <li>HttpAuthException, ͨ��������Authʧ��, ���������֤��¼���û���/����/KEY��</li>
     *             <li>HttpRefusedException, ͨ�������ڷ��������ܵ�����, ���ܾ�����, ���Ƕ���ԭ��, ����ԭ��
     *             �������᷵�ؾܾ�����, ����HttpRefusedException#getError#getMessage�鿴</li>
     *             <li>HttpServerException, ͨ�������ڷ�������������ʱ, �����������Ƿ��������ṩ����</li>
     *             <li>HttpException, ����δ֪����.</li>
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
     * �����ɴ�һ��File��MultipartEntity
     * 
     * @param filename
     *            �ļ���
     * @param file
     *            �ļ�
     * @param postParams
     *            ����POST����
     * @return ���ļ�������������Entity
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
     * �쳣�Զ��ָ�����, ʹ��HttpRequestRetryHandler�ӿ�ʵ��������쳣�ָ�
     */
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
        // �Զ���Ļָ�����
        public boolean retryRequest(IOException exception, int executionCount,
                HttpContext context) {
            // ���ûָ����ԣ��ڷ����쳣ʱ���Զ�����N��
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
     * ����HTTP������
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
