package com.url_shortener;

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

	@Test
	void shouldReturnACashCardWhenDataIsSaved() {
		ResponseEntity<String> response = restTemplate.getForEntity("/api/url/url_count", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);


		UrlReq vr1 = new UrlReq("https://youtube.com");
		ResponseEntity<String> res1 = restTemplate.postForEntity("/api/url/encode", vr1, String.class);
		assertThat(res1.getStatusCode()).isEqualTo(HttpStatus.OK);

		UrlReq vr2 = new UrlReq("https://github.com/ayhem18/Towards_Data_Science/blob/main/Programming_Tools/Databases/Practice/sqlpad");
		ResponseEntity<String> res2 = restTemplate.postForEntity("/api/url/encode", vr2, String.class);
		assertThat(res2.getStatusCode()).isEqualTo(HttpStatus.OK);

		UrlReq ir1 = new UrlReq("https://github.com/ayhem 18");
		ResponseEntity<String> res3 = restTemplate.postForEntity("/api/url/encode", ir1, String.class);
		assertThat(res3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		UrlReq ir2 = new UrlReq("https://github.com/ayhem 18");
		ResponseEntity<String> res4 = restTemplate.postForEntity("/api/url/encode", ir2, String.class);
		assertThat(res4.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

	}

}
