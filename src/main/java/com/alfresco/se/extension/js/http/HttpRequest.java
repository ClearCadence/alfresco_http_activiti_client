package com.alfresco.se.extension.js.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.alfresco.repo.processor.BaseProcessorExtension;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/****
 * 
 * @author RFernandes
 *
 */
public class HttpRequest extends BaseProcessorExtension {

	private static final String GET = "GET";
	private static final String POST = "POST";
	private static Log logger = LogFactory.getLog(HttpRequest.class);

	public String get(String urlString) throws IOException {
		return execute(urlString, null, null, GET, null, null);
	}

	public InputStream getStream(String urlString) throws IOException {
		return executeGetStream(urlString, null, null, GET, null, null);
	}

	public String get(String urlString, String name, String password) throws IOException {
		return execute(urlString, null, null, GET, name, password);
	}

	public InputStream getStream(String urlString, String name, String password) throws IOException {
		return executeGetStream(urlString, null, null, GET, name, password);
	}

	public String post(String urlString, String content, String contentType, String name, String password)
			throws IOException {
		return execute(urlString, content, contentType, POST, name, password);
	}

	public InputStream postStream(String urlString, String content, String contentType, String name, String password)
			throws IOException {
		return executeGetStream(urlString, content, contentType, POST, name, password);
	}

	public String execute(String urlString, String content, String contentType, String httpMethod, String name,
			String password) throws IOException {

		logger.info("::: INSIDE HttpRequest execute() ::: ");

		HttpURLConnection urlConnection = buildConnection(urlString, httpMethod);
		if (name != null && password != null) {
			setHttpBasicAuthentication(urlConnection, name, password);
		}

		if (POST.equals(httpMethod) && content != null && content.length() > 0) {
			logger.info("::: INSIDE HttpRequest execute() if POST loop ::: ");
			urlConnection.setDoOutput(true);
			writeRequestContent(urlConnection, content, contentType);
		} else
			urlConnection.setDoOutput(false);

		return sendRequest(urlConnection);
	}

	public InputStream executeGetStream(String urlString, String content, String contentType, String httpMethod,
			String name, String password) throws IOException {

		HttpURLConnection urlConnection = buildConnection(urlString, httpMethod);
		if (name != null && password != null) {
			setHttpBasicAuthentication(urlConnection, name, password);
		}

		if (POST.equals(httpMethod) && content != null && content.length() > 0) {
			urlConnection.setDoOutput(true);
			writeRequestContent(urlConnection, content, contentType);
		} else
			urlConnection.setDoOutput(false);

		return sendRequestGetStream(urlConnection);
	}

	private static void writeRequestContent(HttpURLConnection urlConnection, String content, String contentType)
			throws IOException {
		logger.info("::: INSIDE HttpRequest writeRequestContent() ::: ");
		logger.info("::: contentType ::: " + contentType);
		logger.info("::: content ::: " + content);

		if (contentType != null) {
			urlConnection.setRequestProperty("Content-Type", contentType);
		}
		// OutputStreamWriter out = null;

		try (OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream(), "ASCII")) {
			// out = new OutputStreamWriter(urlConnection.getOutputStream(),
			// "ASCII");
			out.write(content);
			out.flush();
		} catch (Exception e) {
			logger.info("::: OutputStreamWriter EXCEPTION ::: " + e.getMessage());
		} /*
			 * finally { if (out != null) try { out.close(); } catch (Exception
			 * e) { logger.info("::: OutputStreamWriter EXCEPTION ::: " +
			 * e.getMessage()); } }
			 */
	}

	private static void setHttpBasicAuthentication(HttpURLConnection urlConnection, String name, String password) {

		logger.info("::: INSIDE HttpRequest setHttpBasicAuthentication() ::: ");

		String authString = name + ":" + password;
		byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
		String authStringEnc = new String(authEncBytes);
		logger.info("::: authStringEnc ::: " + authStringEnc);
		urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

		logger.info("::: urlConnection.getRequestProperty ::: Authorization: "
				+ urlConnection.getRequestProperty("Authorization"));

		logger.info("::: urlConnection.toString() ::: " + urlConnection.toString());

	}

	private static String sendRequest(HttpURLConnection urlConnection) throws IOException {

		return readResponse(sendRequestGetStream(urlConnection));
	}

	private static InputStream sendRequestGetStream(HttpURLConnection urlConnection) throws IOException {
		int responseCode = urlConnection.getResponseCode();

		if (responseCode != 200) {
			throw new IOException("RESPONSE CODE:" + responseCode + "\n" + urlConnection.getResponseMessage());
		}

		return urlConnection.getInputStream();
	}

	private static HttpURLConnection buildConnection(String urlString, String httpMethod) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod(httpMethod);
		return urlConnection;
	}

	private static String readResponse(InputStream is) throws IOException {
		StringBuffer sb = new StringBuffer();

		int i;
		try {
			while ((i = is.read()) != -1) {
				sb.append((char) i);
			}

			return sb.toString();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) {
		try {
			HttpRequest req = new HttpRequest();
			System.out.println(req.get("http://192.168.99.223:3333/usersarray"));

			System.out.println("------");

			System.out.println(req.get("http://192.168.99.223:9090/activiti-app/api/enterprise/process-definitions",
					"rui", "rui"));

			System.out.println("------");

			System.out.println(req.post("http://192.168.99.223:3333/profile/1234", null, null, null, null));

			System.out.println("------");

			// System.out.println(req.post("http://192.168.99.223:9090/activiti-app/api/enterprise/tasks/query","rui","rui"));
			System.out.println(req.post("http://192.168.99.223:9090/activiti-app/api/enterprise/process-instances",
					"{\"name\":\"test\",\"processDefinitionId\":\"FOI-1:10:12091\",\"values\":{\"name\":\"xpto\"}}",
					"application/json", "rui", "rui"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
