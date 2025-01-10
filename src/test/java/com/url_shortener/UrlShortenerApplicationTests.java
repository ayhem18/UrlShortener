package com.url_shortener;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.url_shortener.Urls.UrlReq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

//import com.url_shortener.Urls.UrlReq;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // not exactly sure about this
class UrlShortenerApplicationTests {
	// this class helps to call the rest apis we have
	@Autowired
	TestRestTemplate restTemplate;

//	@Test
//	void testUrlCountEndpoint() {
//		ResponseEntity<String> response = restTemplate.getForEntity("/api/url/url_count", String.class);
//		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//	}

//	@Test
//	void testPostEndpoint() {

//		String s1 = "https://youtube.com";
//		UrlReq vr1 = new UrlReq(s1);
//
//		// send a post request to the api/url/encode endpoint: first time should be a created status code
//		ResponseEntity<String> res1 = restTemplate.postForEntity("/api/url/encode", vr1, String.class);
//		assertThat(res1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//
//		res1 = restTemplate.postForEntity("/api/url/encode", vr1, String.class);
//		assertThat(res1.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//		// make sure the values are correct
//		ReadContext ctx = JsonPath.parse(res1.getBody());
//		String shortUrl = ctx.read("$.url_short", String.class);
//		assertThat(shortUrl).isEqualTo(String.valueOf(s1.hashCode()));
//
//		String s2 = "https://github.com/ayhem18/Towards_Data_Science/blob/main/Programming_Tools/Databases/Practice/sqlpad";
//		UrlReq vr2 = new UrlReq(s2);
//
//		ResponseEntity<String> res2 = restTemplate.postForEntity("/api/url/encode", vr2, String.class);
//		assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
//
//		res2 = restTemplate.postForEntity("/api/url/encode", vr2, String.class);
//		assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);
//
//		ctx = JsonPath.parse(res2.getBody());
//		shortUrl = ctx.read("$.url_short", String.class);
//		assertThat(shortUrl).isEqualTo(String.valueOf(s2.hashCode()));
//
//		UrlReq ir1 = new UrlReq("https://github.com/ayhem 18");
//		ResponseEntity<String> res3 = restTemplate.postForEntity("/api/url/encode", ir1, String.class);
//		assertThat(res3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//
//		UrlReq ir2 = new UrlReq("https://github.co/ayhem18");
//		ResponseEntity<String> res4 = restTemplate.postForEntity("/api/url/encode", ir2, String.class);
//		assertThat(res4.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//	}

}
