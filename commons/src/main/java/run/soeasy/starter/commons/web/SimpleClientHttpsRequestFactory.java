package run.soeasy.starter.commons.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleClientHttpsRequestFactory extends SimpleClientHttpRequestFactory {
	private SSLSocketFactory sslSocketFactory;

	@Override
	protected HttpURLConnection openConnection(URL url, Proxy proxy) throws IOException {
		HttpURLConnection httpURLConnection = super.openConnection(url, proxy);
		if (httpURLConnection instanceof HttpsURLConnection) {
			afterProperties((HttpsURLConnection) httpURLConnection);
		}
		return httpURLConnection;
	}

	protected void afterProperties(HttpsURLConnection httpsURLConnection) {
		if (sslSocketFactory != null) {
			httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
		}
	}
}
