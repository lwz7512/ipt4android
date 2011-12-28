package com.ybcx.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.CharArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class Response {
    private final HttpResponse mResponse;
    private boolean mStreamConsumed = false;
    
    public Response(HttpResponse res) {
        mResponse = res;
    }
    
    
    //ADD BY lwz7512 @ 2011/08/12
    public HttpResponse getHttpResponse() {
		return mResponse;
	}



	/**
     * Convert Response to inputStream
     * 
     * @return InputStream or null
     * @throws ResponseException
     */
    public InputStream asStream() throws HttpException {
        try {
            final HttpEntity entity = mResponse.getEntity();
            if (entity != null) {
                return entity.getContent();
            }
        } catch (IllegalStateException e) {
            throw new HttpException(e.getMessage(), e);
        } catch (IOException e) {
            throw new HttpException(e.getMessage(), e);
        }
        return null;
    }
    
    
    /**
     * Convert Response to Context String
     * 
     * @return response context string or null
     * @throws ResponseException
     */
    public String asString() throws HttpException {
        try {
            return entityToString(mResponse.getEntity());
        } catch (IOException e) {
            throw new HttpException(e.getMessage(), e);
        }
    }
    
    /**
     * EntityUtils.toString(entity, "UTF-8");
     * 
     * @param entity
     * @return
     * @throws IOException
     * @throws ResponseException
     */
    private String entityToString(final HttpEntity entity) throws IOException, HttpException {
        if (null == entity) {
            throw new IllegalArgumentException("HTTP entity may not be null");
        }
        InputStream instream = entity.getContent();  
        //InputStream instream = asStream(entity);
        if (instream == null) {  
            return "";  
        }  
        if (entity.getContentLength() > Integer.MAX_VALUE) {  
            throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");  
        }  
        
        int i = (int) entity.getContentLength();
        if (i < 0) {
            i = 4096;
        }
        Log.i("LDS", i + " content length");
        
        Reader reader = new BufferedReader(new InputStreamReader(instream, "UTF-8"));
        CharArrayBuffer buffer = new CharArrayBuffer(i);
        try {
            char[] tmp = new char[1024];
            int l;
            while ((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
        } finally {
            reader.close();
        }
        
        return buffer.toString();
    }

    public JSONObject asJSONObject() throws HttpException {
        try {
            return new JSONObject(asString());
        } catch (JSONException jsone) {
            throw new HttpException(jsone.getMessage() + ":"
                    + asString(), jsone);
        }
    }

    public JSONArray asJSONArray() throws HttpException {
        try {
            return new JSONArray(asString());
        } catch (Exception jsone) {
            throw new HttpException(jsone.getMessage(), jsone);
        }
    }

    @SuppressWarnings("unused")
	private void setStreamConsumed(boolean mStreamConsumed) {
        this.mStreamConsumed = mStreamConsumed;
    }

    public boolean isStreamConsumed() {
        return mStreamConsumed;
    }

    
    private static Pattern escaped = Pattern.compile("&#([0-9]{3,5});");

    /**
     * Unescape UTF-8 escaped characters to string.
     * @author pengjianq...@gmail.com
     *
     * @param original The string to be unescaped.
     * @return The unescaped string
     */
    public static String unescape(String original) {
        Matcher mm = escaped.matcher(original);
        StringBuffer unescaped = new StringBuffer();
        while (mm.find()) {
            mm.appendReplacement(unescaped, Character.toString(
                    (char) Integer.parseInt(mm.group(1), 10)));
        }
        mm.appendTail(unescaped);
        return unescaped.toString();
    }

}
