// ===================================================================================================
//						   _  __	 _ _
//						  | |/ /__ _| | |_ _  _ _ _ __ _
//						  | ' </ _` | |  _| || | '_/ _` |
//						  |_|\_\__,_|_|\__|\_,_|_| \__,_|
//
// This file is part of the Kaltura Collaborative Media Suite which allows users
// to do with audio, video, and animation what Wiki platfroms allow them to do with
// text.
//
// Copyright (C) 2006-2011  Kaltura Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// @ignore
// ===================================================================================================
package com.kaltura.client;

import com.kaltura.client.enums.KalturaSessionType;
import com.kaltura.client.utils.EncryptionUtils;
import com.kaltura.client.utils.ParseUtils;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Contains non-generated client logic. Includes the doQueue method which is responsible for
 * making HTTP calls to the Kaltura server.
 * 
 * @author jpotts
 *
 */
@SuppressWarnings("serial")
abstract public class KalturaClientBaseOld implements Serializable {

	private static final String UTF8_CHARSET = "UTF-8";

    // KS v2 constants
    private static final int BLOCK_SIZE = 16;
    private static final String FIELD_EXPIRY = "_e";
    private static final String FIELD_USER = "_u";
	private static final String FIELD_TYPE = "_t";
	private static final int RANDOM_SIZE = 16;

	private static final int MAX_DEBUG_RESPONSE_STRING_LENGTH = 1024;
	protected ConnectionConfiguration connectionConfiguration;
   // protected List<KalturaServiceActionCall> callsQueue;
    protected List<Class<?>> requestReturnType;
   // protected KalturaParams multiRequestParamsMap;

	/** will be added to the request body to configure version and etc **/
	protected /*Map<String, Object>*/KalturaParams clientConfiguration = new KalturaParams();// new HashMap<String, Object>(); // generated by php script from input file
	/** will be added to the request body and will include session related data (ks, partnerId etc) */
	protected /*Map<String, Object>*/KalturaParams requestConfiguration = new KalturaParams();// new HashMap<String, Object>(); // generated by php script from input file

	private static IKalturaLogger logger = KalturaLogger.getLogger(KalturaClientBaseOld.class);

    //private Header[] responseHeaders = null;

    //private boolean acceptGzipEncoding = true;

   // protected static final String HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding";

	//protected static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";

////	protected static final String ENCODING_GZIP = "gzip";

	//private ActionsQueue mActionsQueue;

	public void setClientConfigProperty(String key, String value){
		try {
			this.clientConfiguration.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return String
	 */
	public String getClientConfigProperty(String key){
		if(this.clientConfiguration.containsKey(key)){
			try {
				return (String) this.clientConfiguration.get(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return null;
	}


	/** moved-to {@link ConnectionConfiguration}
	 * Set whether to accept GZIP encoding, that is, whether to
	 * send the HTTP "Accept-Encoding" header with "gzip" as value.
	 * <p>Default is "true". Turn this flag off if you do not want
	 * GZIP response compression even if enabled on the HTTP server.
	 *//*
	public void setAcceptGzipEncoding(boolean acceptGzipEncoding) {
		this.acceptGzipEncoding = acceptGzipEncoding;
	}
    *//**
	 * Return whether to accept GZIP encoding, that is, whether to
	 * send the HTTP "Accept-Encoding" header with "gzip" as value.
	 *//*
	public boolean isAcceptGzipEncoding() {
		return acceptGzipEncoding;
	}
   */
    /*
	 * Determine whether the given response is a GZIP response.
	 * <p>Default implementation checks whether the HTTP "Content-Encoding"
	 * header contains "gzip" (in any casing).
	 * @param postMethod the PostMethod to check
	 */
	/*protected boolean isGzipResponse(HttpPost postMethod) {
		Header encodingHeader = postMethod.getFirstHeader(HTTP_HEADER_CONTENT_ENCODING); //getResponseHeader(HTTP_HEADER_CONTENT_ENCODING);
		if (encodingHeader == null || encodingHeader.getValue() == null) {
			return false;
		}
		return (encodingHeader.getValue().toLowerCase().indexOf(ENCODING_GZIP) != -1);
	}*/

    /*
	 * Extract the response body from the given executed remote invocation
	 * request.
	 * <p>The default implementation simply fetches the PostMethod's response
	 * body stream. If the response is recognized as GZIP response, the
	 * InputStream will get wrapped in a GZIPInputStream.
	 * @param postMethod the HttpPost to read the response body from
	 * @return an InputStream for the response body
	 * @throws IOException if thrown by I/O methods
	 * @see #isGzipResponse
	 * @see java.util.zip.GZIPInputStream
	 * @see org.apache.http.client.methods.HttpPost#getEntity().getContent()
	 * @see org.apache.http.client.methods.HttpPost#getFirstHeader(String)
	 */
	/*protected InputStream getResponseBody(HttpPost postMethod)
			throws IOException {

		if (isGzipResponse(postMethod)) {
			return new GZIPInputStream(postMethod.getEntity().getContent());// getResponseBodyAsStream());
		}
		else {
			return postMethod.getEntity().getContent();//getResponseBodyAsStream();
		}
	}*/

    /*public Header[] getResponseHeaders()
    {
        return responseHeaders;
    }*/

    public KalturaClientBaseOld() {
    }

    public KalturaClientBaseOld(ConnectionConfiguration config) {
    	// sets the default configuration values and add/overwrite with new properties from config
		this.connectionConfiguration = new ConnectionConfiguration(config);
	}

    /*public KalturaClientBase(ConnectionConfiguration config) {
        this.connectionConfiguration = config;
        this.callsQueue = new ArrayList<KalturaServiceActionCall>();
        this.multiRequestParamsMap = new KalturaParams();
    }*/

	/*public boolean isMultiRequest() {
		return (requestReturnType != null);
	}
*/
	public void setConnectionConfiguration(ConnectionConfiguration connectionConfiguration) {
		this.connectionConfiguration = connectionConfiguration;
	}

	public ConnectionConfiguration getConnectionConfiguration() {
		return this.connectionConfiguration;
	}



	/*public void queueServiceCall(String service, String action, KalturaParams kparams, OnCompletion onCompletion) throws KalturaAPIException {
		this.queueServiceCall(service, action, kparams, new KalturaFiles(), null, onCompletion);
	}

	public void queueServiceCall(String service, String action, KalturaParams kparams, Class<?> expectedClass, OnCompletion onCompletion) throws KalturaAPIException {
		this.queueServiceCall(service, action, kparams, new KalturaFiles(), expectedClass, onCompletion);
	}

	public void queueServiceCall(String service, String action, KalturaParams kparams, KalturaFiles kfiles, OnCompletion onCompletion) throws KalturaAPIException {
		this.queueServiceCall(service, action, kparams, kfiles, null, onCompletion);
	}

	public void queueServiceCall(String service, String action, KalturaParams kparams, KalturaFiles kfiles, Class<?> expectedClass, OnCompletion onCompletion) throws KalturaAPIException {
		Object value;
		for(Entry<String, Object> itr : this.requestConfiguration.entrySet()) {
			value = itr.getValue();
			if(value instanceof KalturaObjectBase){
				kparams.add(itr.getKey(), (KalturaObjectBase)value);
			}
			else{
				kparams.add(itr.getKey(), String.valueOf(value));
			}
		}

		KalturaServiceActionCall call = new KalturaServiceActionCall(service, action, kparams, kfiles, onCompletion);
		if(requestReturnType != null)
			requestReturnType.add(expectedClass);
		//this.callsQueue.add(call);

		mActionsQueue.addAction(call);
		//APIOkRequestsExecutor.getInstance().
	}*/

	/*!! user get APIOkRequestsExecutor from loader class (injector class)
	*!! and requests are queued/executed by it.
	*!! KalturaClientBase object should be passed on the requests activation time, queue/execute*/

	//!! RequestAlement should be created for handling files requests - this method will be
	//!! execution method.
	/* post request with params on url
	public String serve() throws KalturaAPIException {

		KalturaParams kParams = new KalturaParams();
		String url = extractParamsFromCallQueue(kParams, new KalturaFiles());
		String kParamsString = kParams.toQueryString();
		url += "?" + kParamsString;

		return url;
	}*/

	//abstract protected void resetRequest();

	/*implemented by ActionsQueue implementor
	public Element doQueue() throws KalturaAPIException {

		if (this.callsQueue.isEmpty()) return null;

		if (logger.isEnabled())
			logger.debug("service url: [" + this.connectionConfiguration.getEndpoint() + "]");

		KalturaParams kparams = new KalturaParams();
		KalturaFiles kfiles = new KalturaFiles();

		String url = extractParamsFromCallQueue(kparams, kfiles);

		if (logger.isEnabled())
		{
			logger.debug("JSON: [" + kparams + "]");
		}

		HttpPost method;
		try {
			method = createPostMethod(kparams, kfiles, url);
		} catch (UnsupportedEncodingException e) {
			resetRequest();
			throw new KalturaAPIException("Unsupported encoding: " + e.getMessage());
		}

		HttpClient client = createHttpClient();
		setRetryPolicy(client);
		String responseString = null;
		try {
			responseString = executeMethod(client, method);
		} finally {
			resetRequest();
		}

		Element responseXml = XmlUtils.parseXml(responseString);
		Element resultXml = this.validateXmlResult(responseXml);
		this.throwExceptionOnAPIError(resultXml);

		return resultXml;
	}*/

    /*protected String readRemoteInvocationResult(InputStream is)
    	throws IOException {

        try {
    	  return doReadRemoteInvocationResult(is);
        }
        finally {
    	  is.close();
        }
    }*/

    /*protected String doReadRemoteInvocationResult(InputStream is)
    	throws IOException {

        byte[] buf = new byte[1024];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len;
        while ( (len = is.read(buf)) > 0)
        {
        	out.write(buf,0,len);
        }
        return new String(out.toByteArray(), UTF8_CHARSET);
    }*/

    /* implemented by ActionsQueue implementor
    protected String executeMethod(HttpClient client, HttpPost method) throws KalturaAPIException {
		String responseString = "";
		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (logger.isEnabled())
			{
				Header[] headers = method.getRequestHeaders();
				for(Header header : headers)
					logger.debug("Header [" + header.getName() + " value [" + header.getValue() + "]");
			}

			if (logger.isEnabled() && statusCode != HttpStatus.SC_OK) {
				logger.error("Method failed: " + method.getStatusLine ( ));
				throw new KalturaAPIException("Unexpected Http return code: " + statusCode);
			}

			// Read the response body
            InputStream responseBodyIS = null;
            if (isGzipResponse(method)) {
                responseBodyIS = new GZIPInputStream(method.getResponseBodyAsStream());
                if (logger.isEnabled()) logger.debug("Using gzip compression to handle response for: "+method.getName()+" "+method.getPath()+"?"+method.getQueryString());
            } else {
                responseBodyIS = method.getResponseBodyAsStream();
                if (logger.isEnabled()) logger.debug("No gzip compression for this response");
            }
            String responseBody = readRemoteInvocationResult(responseBodyIS);
            responseHeaders = method.getResponseHeaders();

            // print server debug info
            String serverName = null;
            String serverSession = null;
            for(Header header : responseHeaders)
            {
            	if (header.getName().compareTo("X-Me") == 0)
                    serverName = header.getValue();
            	else if (header.getName().compareTo("X-Kaltura-Session") == 0)
                    serverSession = header.getValue();
			}
			if (serverName != null || serverSession != null)
				logger.debug("Server: [" + serverName + "], Session: [" + serverSession + "]");

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary data
			responseString = new String (responseBody.getBytes(UTF8_CHARSET), UTF8_CHARSET); // Unicon: this MUST be set to UTF-8 charset -AZ
			if (logger.isEnabled())
			{
				if(responseString.length() < MAX_DEBUG_RESPONSE_STRING_LENGTH) {
					logger.debug(responseString);
				} else {
					logger.debug("Received long response. (length : " + responseString.length() + ")");
				}
			}

			return responseString;

		} catch ( HttpException e ) {
			if (logger.isEnabled())
				logger.error( "Fatal protocol violation: " + e.getMessage ( ) ,e);
			throw new KalturaAPIException("Protocol exception occured while executing request");
		} catch ( SocketTimeoutException e) {
			if (logger.isEnabled())
				logger.error( "Fatal transport error: " + e.getMessage ( ), e);
			throw new KalturaAPIException("Request was timed out");
		} catch ( ConnectTimeoutException e) {
			if (logger.isEnabled())
				logger.error( "Fatal transport error: " + e.getMessage ( ), e);
			throw new KalturaAPIException("Connection to server was timed out");
		} catch ( IOException e ) {
			if (logger.isEnabled())
				logger.error( "Fatal transport error: " + e.getMessage ( ), e);
			throw new KalturaAPIException("I/O exception occured while reading request response");
		}  finally {
			// Release the connection.
			method.releaseConnection ( );
		}
	}*/

	/* implemented by ActionsQueue implementor
	private HttpPost createPostMethod(KalturaParams kparams, KalturaFiles kfiles, String url) throws UnsupportedEncodingException {
		HttpPost method = new HttpPost(url);
        method.setHeader("Accept","text/xml,application/xml,*//*");
        method.setHeader("Accept-Charset","utf-8,ISO-8859-1;q=0.7,*;q=0.5");

        if (!kfiles.isEmpty()) {
            method = this.getPostMultiPartWithFiles(method, kparams, kfiles);
        } else {
            method = this.addParams(method, kparams);
        }

        if (isAcceptGzipEncoding()) {
			method.addHeader(HTTP_HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
		}

		// Provide custom retry handler is necessary
		//method.getParams().setParameter(HttpParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler (3, false));

		return method;
	}*/

/*	implemented by ActionsQueue implementor
	private void setRetryPolicy(HttpClient client){

		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {

			public boolean retryRequest(
					IOException exception,
					int executionCount,
					HttpContext context) {
				if (executionCount >= 5) {
					// Do not retry if over max retry count
					return false;
				}
				if (exception instanceof InterruptedIOException) {
					// Timeout
					return false;
				}
				if (exception instanceof UnknownHostException) {
					// Unknown host
					return false;
				}
				if (exception instanceof ConnectException) {
					// Connection refused
					return false;
				}
				if (exception instanceof SSLException) {
					// SSL handshake exception
					return false;
				}
				HttpRequest request = (HttpRequest) context.getAttribute(
						ExecutionContext.HTTP_REQUEST);
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					// Retry if the request is considered idempotent
					return true;
				}
				return false;
			}

		};

		client.setHttpRequestRetryHandler(myRetryHandler);

	}*/

	/*implemented by ActionsQueue implementor
	protected HttpClient createHttpClient() {
		HttpClient client = new DefaultHttpClient();

		// added by Unicon to handle proxy hosts
		String proxyHost = System.getProperty( "http.proxyHost" );
		if ( proxyHost != null ) {
			int proxyPort = -1;
			String proxyPortStr = System.getProperty( "http.proxyPort" );
			if (proxyPortStr != null) {
				try {
					proxyPort = Integer.parseInt( proxyPortStr );
				} catch (NumberFormatException e) {
					if (logger.isEnabled())
						logger.warn("Invalid number for system property http.proxyPort ("+proxyPortStr+"), using default port instead");
				}
			}
			ProxyHost proxy = new ProxyHost( proxyHost, proxyPort );
			client.getHostConfiguration().setProxyHost( proxy );
		}
		// added by Unicon to force encoding to UTF-8
		client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, UTF8_CHARSET);
		client.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET, UTF8_CHARSET);
		client.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET, UTF8_CHARSET);

		HttpConnectionManagerParams connParams = client.getHttpConnectionManager().getParams();
		if(this.connectionConfiguration.getConnectTimeout() != 0) {
			connParams.setSoTimeout(this.connectionConfiguration.getConnectTimeout());
			connParams.setConnectionTimeout(this.connectionConfiguration.getConnectTimeout());
		}
		client.getHttpConnectionManager().setParams(connParams);
		return client;
	}*/

	/*
	 * We need to make sure that we shut down the connection.
	 * The possible connection manager types are taken from here:
	 * http://hc.apache.org/httpclient-legacy/apidocs/org/apache/commons/httpclient/HttpConnectionManager.html
	 *
	 * The issue details is described here:
	 * http://fuyun.org/2009/09/connection-close-in-httpclient/
	 *
	 * @param client The client we wish to close
	 */
	/*protected void closeHttpClient(HttpClient client) {
		HttpConnectionManager mgr = client.getHttpConnectionManager();

		if (mgr instanceof SimpleHttpConnectionManager) {
		    ((SimpleHttpConnectionManager)mgr).shutdown();
		}

		if(mgr instanceof MultiThreadedHttpConnectionManager) {
			((MultiThreadedHttpConnectionManager)mgr).shutdown();
		}
	}*/

	/*private String extractParamsFromCallQueue(KalturaParams kparams, KalturaFiles kfiles) throws KalturaAPIException {

		String url = this.connectionConfiguration.getEndpoint() + "/api_v3";

		// append the basic params
		kparams.add("format", this.connectionConfiguration.getServiceFormat());
		kparams.add("ignoreNull", true);

		Object value;
		for(Entry<String, Object> itr : this.clientConfiguration.entrySet()) {
			value = itr.getValue();
			if(value instanceof KalturaObjectBase){
				kparams.add(itr.getKey(), (KalturaObjectBase)value);
			}
			else{
				kparams.add(itr.getKey(), String.valueOf(value));
			}
		}

		if (requestReturnType != null) {
			url += "/service/multirequest";
			int i = 1;
			for (KalturaServiceActionCall call : this.callsQueue) {
				KalturaParams callParams = call.getParamsForMultiRequest(i);
				kparams.add(callParams);
				KalturaFiles callFiles = call.getFilesForMultiRequest(i);
				kfiles.add(callFiles);
				i++;
			}

			// map params
			for (Object key : this.multiRequestParamsMap.keySet()) {
				String requestParam = (String) key;
				KalturaParams resultParam = this.multiRequestParamsMap.getParams(requestParam);

				if (kparams.containsKey(requestParam)) {
					kparams.add(requestParam, resultParam);
				}
			}

			// Clean
			this.multiRequestParamsMap.clear();

		} else {
			KalturaServiceActionCall call = this.callsQueue.get(0);
			url += "/service/" + call.getService() + "/action/" + call.getAction();
			kparams.add(call.getParams());
			kfiles.add(call.getFiles());
		}

		// cleanup
		this.callsQueue.clear();

		kparams.add("kalsig", this.signature(kparams));
		return url;
	}
*/
	/*public void startMultiRequest() {
		requestReturnType = new ArrayList<Class<?>>();
	}*/

	/*
	public Element getElementByXPath(Element element, String xPath) throws KalturaAPIException
	{
		try
		{
			return XmlUtils.getElementByXPath(element, xPath);
		}
		catch (XPathExpressionException xee)
		{
			throw new KalturaAPIException("XPath expression exception evaluating result");
		}
	}*/

	/*public KalturaMultiResponse doMultiRequest() throws KalturaAPIException
	{
		Element multiRequestResult = doQueue();

		KalturaMultiResponse multiResponse = new KalturaMultiResponse();

		for(int i = 0; i < multiRequestResult.getChildNodes().getLength(); i++)
		{
			Element arrayNode = (Element)multiRequestResult.getChildNodes().item(i);

			try
			{
				KalturaAPIException exception = getExceptionOnAPIError(arrayNode);
				if (exception != null)
				{
					multiResponse.add(exception);
				}
				else if (getElementByXPath(arrayNode, "objectType") != null)
				{
			   		multiResponse.add(KalturaObjectFactory.create(arrayNode, requestReturnType.get(i)));
				}
				else if (getElementByXPath(arrayNode, "item/objectType") != null)
				{
			   		multiResponse.add(ParseUtils.parseArray(requestReturnType.get(i), arrayNode));
				}
				else
				{
					multiResponse.add(arrayNode.getTextContent());
				}
			}
			catch (KalturaAPIException e)
			{
				multiResponse.add(e);
			}
	   }

		// Cleanup
		this.requestReturnType = null;
		return multiResponse;
	}*/


	/*public void mapMultiRequestParam(int resultNumber, int requestNumber, String requestParamName) throws KalturaAPIException {
		this.mapMultiRequestParam(resultNumber, null, requestNumber, requestParamName);
	}
*/
	/*public void mapMultiRequestParam(int resultNumber, String resultParamName, int requestNumber, String requestParamName) throws KalturaAPIException {
		String resultParam = "{" + resultNumber + ":result";
		if (resultParamName != null && resultParamName != ""){
			resultParam += resultParamName;
		}
		resultParam += "}";

		String requestNumberString = Integer.toString(requestNumber);
		KalturaParams params = new KalturaParams();
		params.add(requestParamName, resultParam);
		this.multiRequestParamsMap.add(requestNumberString, params);
	}*/

	/*public static String signature(KalturaParams kparams) {
		return new String(Hex.encodeHex(DigestUtils.md5(kparams.toString())));
	}*/

	/* moved-to XmlUtils
	private Element validateXmlResult(Element resultXml) throws KalturaAPIException {

		Element resultElement = null;
   		resultElement = getElementByXPath(resultXml, "/xml/result");

		if (resultElement != null) {
			return resultElement;
		} else {
			throw new KalturaAPIException("Invalid result");
		}
	}*/

	/* moved-to XmlUtils
	private KalturaAPIException getExceptionOnAPIError(Element result) throws KalturaAPIException {
		Element errorElement = getElementByXPath(result, "error");
		if (errorElement == null)
		{
			return null;
		}
		
		Element messageElement = getElementByXPath(errorElement, "message");
		Element codeElement = getElementByXPath(errorElement, "code");
		if (messageElement == null || codeElement == null)
		{
			return null;
		}
		
		return new KalturaAPIException(messageElement.getTextContent(),codeElement.getTextContent());
	}
*/
	/*moved-to XmlUtils
	private void throwExceptionOnAPIError(Element result) throws KalturaAPIException {
		KalturaAPIException exception = getExceptionOnAPIError(result);
		if (exception != null)
		{
			throw exception;
		}
	}
*/
	/*private HttpPost getPostMultiPartWithFiles(HttpPost method, KalturaParams kparams, KalturaFiles kfiles) {
 
		String boundary = "---------------------------" + System.currentTimeMillis();
		List <Part> parts = new ArrayList<Part>();
		parts.add(new StringPart (HttpMethodParams.MULTIPART_BOUNDARY, boundary));
 
		parts.add(new StringPart ("json", kparams.toString()));	   

		for (String key : kfiles.keySet()) {
			final KalturaFile kFile = kfiles.get(key);
			parts.add(new StringPart(key, "filename=" + kFile.getName()));
			if (kFile.getFile() != null) {
				// use the file
				File file = kFile.getFile();
				try {
					parts.add(new FilePart(key, file));
				} catch (FileNotFoundException e) {
					// TODO this sort of leaves the submission in a weird
					// state... -AZ
					if (logger.isEnabled())
						logger.error("Exception while iterating over kfiles", e);
				}
			} else {
				// use the input stream
				PartSource fisPS = new PartSource() {
					public long getLength() {
						return kFile.getSize();
					}

					public String getFileName() {
						return kFile.getName();
					}

					public InputStream createInputStream() throws IOException {
						return kFile.getInputStream();
					}
				};
				parts.add(new FilePart(key, fisPS));
			}
		}
	 
		Part allParts[] = new Part[parts.size()];
		allParts = parts.toArray(allParts);
	 
		method.setRequestEntity(new MultipartRequestEntity(allParts, method.getParams()));
 
		return method;
	}*/
		
	/*private HttpPost addParams(HttpPost method, KalturaParams kparams) throws UnsupportedEncodingException {
		String content = kparams.toString();
		String contentType = "application/json";
		StringRequestEntity requestEntity = new StringRequestEntity(content, contentType , null);
		
		method.setRequestEntity(requestEntity);
		return method;
	}*/
	
	public String generateSession(String adminSecretForSigning, String userId, KalturaSessionType type, int partnerId) throws Exception
	{
		return this.generateSession(adminSecretForSigning, userId, type, partnerId, 86400);
	}
	
	public String generateSession(String adminSecretForSigning, String userId, KalturaSessionType type, int partnerId, int expiry) throws Exception
	{
		return this.generateSession(adminSecretForSigning, userId, type, partnerId, expiry, "");
	}

	public String generateSession(String adminSecretForSigning, String userId, KalturaSessionType type, int partnerId, int expiry, String privileges) throws Exception
	{
		try
		{
			// initialize required values
			int rand = (int)(Math.random() * 32000);
			expiry += (int)(System.currentTimeMillis() / 1000);
			
			// build info string
			StringBuilder sbInfo = new StringBuilder();
			sbInfo.append(partnerId).append(";"); // index 0 - partner ID
			sbInfo.append(partnerId).append(";"); // index 1 - partner pattern - using partner ID
			sbInfo.append(expiry).append(";"); // index 2 - expiration timestamp
			sbInfo.append(type.getValue()).append(";"); // index 3 - session type
			sbInfo.append(rand).append(";"); // index 4 - random number
			sbInfo.append(userId).append(";"); // index 5 - user ID
			sbInfo.append(privileges); // index 6 - privileges
			
			byte[] infoSignature = signInfoWithSHA1(adminSecretForSigning + (sbInfo.toString()));
			
			// convert signature to hex:
			String signature = ParseUtils.convertToHex(infoSignature);
			
			// build final string to base64 encode
			StringBuilder sbToEncode = new StringBuilder();
			sbToEncode.append(signature.toString()).append("|").append(sbInfo.toString());
			
			// encode the signature and info with base64
			String hashedString = new String(Base64.encodeBase64(sbToEncode.toString().getBytes()));
			
			// remove line breaks in the session string
			String ks = hashedString.replace("\n", "");
			ks = hashedString.replace("\r", "");
			
			// return the generated session key (KS)
			return ks;
		} catch (NoSuchAlgorithmException ex)
		{
			throw new Exception(ex);
		}
	}

	public String generateSessionV2(String adminSecretForSigning, String userId, KalturaSessionType type, int partnerId, int expiry, String privileges) throws Exception
	{
		try {
		// build fields array
		KalturaParams fields = new KalturaParams();
		String[] privilegesArr = privileges.split(",");
		for (String curPriv : privilegesArr) {
			String privilege = curPriv.trim();
			if(privilege.length() == 0)
				continue;
			if(privilege.equals("*"))
				privilege = "all:*";
			
			String[] splittedPriv = privilege.split(":");
			if(splittedPriv.length>1) {
				fields.add(splittedPriv[0], URLEncoder.encode(splittedPriv[1], UTF8_CHARSET));
			} else {
				fields.add(splittedPriv[0], "");
			}
		}
		
		Integer expiryInt = (int)(System.currentTimeMillis() / 1000) + expiry;
		String expStr = expiryInt.toString();
		fields.add(FIELD_EXPIRY,  expStr);
		fields.add(FIELD_TYPE, Integer.toString(type.getValue()));
		fields.add(FIELD_USER, userId);
		
		// build fields string
		byte[] randomBytes = createRandomByteArray(RANDOM_SIZE);
		byte[] fieldsByteArray = fields.toQueryString().getBytes();
		int totalLength = randomBytes.length + fieldsByteArray.length;
		byte[] fieldsAndRandomBytes = new byte[totalLength];
		System.arraycopy(randomBytes, 0, fieldsAndRandomBytes, 0, randomBytes.length);
		System.arraycopy(fieldsByteArray, 0, fieldsAndRandomBytes, randomBytes.length, fieldsByteArray.length);

		byte[] infoSignature = EncryptionUtils.encryptSHA1(fieldsAndRandomBytes);
		byte[] input = new byte[infoSignature.length + fieldsAndRandomBytes.length];
		System.arraycopy(infoSignature, 0, input, 0, infoSignature.length);
		System.arraycopy(fieldsAndRandomBytes,0,input,infoSignature.length, fieldsAndRandomBytes.length);
		
		// encrypt and encode
		byte[] encryptedFields = EncryptionUtils.encryptAES(adminSecretForSigning, input);
		String prefix = "v2|" + partnerId + "|";
		
		byte[] output = new byte[encryptedFields.length + prefix.length()];
		System.arraycopy(prefix.getBytes(), 0, output, 0, prefix.length());
		System.arraycopy(encryptedFields,0,output,prefix.length(), encryptedFields.length);
		
		String encodedKs = new String(Base64.encodeBase64(output));
		encodedKs = encodedKs.replaceAll("\\+", "-");
		encodedKs = encodedKs.replaceAll("/", "_");
		encodedKs = encodedKs.replace("\n", "");
		encodedKs = encodedKs.replace("\r", "");
		
		return encodedKs;
		} catch (GeneralSecurityException ex) {
			logger.error("Failed to generate v2 session.");
			throw new Exception(ex);
		} 
	}
	
	/*moved-to com.kaltura.client.utils.EncryptionUtils
	private byte[] signInfoWithSHA1(String text) throws GeneralSecurityException {
		return signInfoWithSHA1(text.getBytes());
	}*/
	
	/*moved-to com.kaltura.client.utils.EncryptionUtils
	private byte[] signInfoWithSHA1(byte[] data) throws GeneralSecurityException {
		MessageDigest algorithm = MessageDigest.getInstance("SHA1");
		algorithm.reset();
		algorithm.update(data);
		byte infoSignature[] = algorithm.digest();
		return infoSignature;
	}*/
	
	/*moved-to com.kaltura.client.utils.EncryptionUtils
	private byte[] aesEncrypt(String secretForSigning, byte[] text) throws GeneralSecurityException, UnsupportedEncodingException {
		// Key
		byte[] hashedKey = signInfoWithSHA1(secretForSigning);
		byte[] keyBytes = new byte[BLOCK_SIZE];
		System.arraycopy(hashedKey,0,keyBytes,0,BLOCK_SIZE);
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		
		// IV
		byte[] ivBytes = new byte[BLOCK_SIZE];
		IvParameterSpec iv = new IvParameterSpec(ivBytes);
		
		// Text
		int textSize = ((text.length + BLOCK_SIZE - 1) / BLOCK_SIZE) * BLOCK_SIZE;
		byte[] textAsBytes = new byte[textSize];
		Arrays.fill(textAsBytes, (byte)0);
		System.arraycopy(text, 0, textAsBytes, 0, text.length);
		
		// Encrypt
		Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
	    cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return cipher.doFinal(textAsBytes);
	}*/
	
	
	private byte[] createRandomByteArray(int size)	{
		byte[] b = new byte[size];
		new Random().nextBytes(b);
		return b;
	}

	// !! moved-to ParseUtils
	/*private String convertToHex(byte[] data) {
		*//*check this out:
		* final StringBuilder builder = new StringBuilder();
			for(byte b : in) {
				builder.append(String.format("%02x", b));
			}
			return builder.toString();*//*

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while(two_halfs++ < 1);
		}
		return buf.toString();
	} */
	
}
