package com.API;

/**
 * 
 */

/**
 * @author kiran koli
 *
 * 
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for handling notifications. Author: Kiran Koli
 */
@RestController
public class Controller {

	@Value("${apiUrl}")
	private String apiUrl;

	private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

	@Autowired
	private CloseableHttpClient httpClient;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@GetMapping("/home")
	@Async("taskExecutor")
	public CompletableFuture<ResponseEntity<String>> handleRequest() {
		
		return CompletableFuture.supplyAsync(() -> {
			try {
				String jsonInputString = "{\"name\": \"Apple MacBook Pro 16\",\"data\": {\"year\": 2019,\"price\": 1849.99,\"CPU model\": \"Intel Core i9\",\"Hard disk size\": \"1 TB\"}}";
				String responseCode = sendWithRetry(jsonInputString, 3);
				return ResponseEntity.ok(responseCode);
			} catch (IllegalArgumentException e) {
				LOGGER.error("Invalid input: {}", e.getMessage());
				return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
			} catch (Exception e) {
				LOGGER.error("An error occurred: {}", e.getMessage(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
			}
		}, taskExecutor);
	}

	private String sendWithRetry(String jsonInputString, int maxRetries) {

		try {
			return send(jsonInputString);
		} catch (Exception e) {
			LOGGER.error(e.toString());

		}

		return "Error: Unable to send Data.";
	}

	private String send(String jsonInputString) throws Exception {
		LOGGER.debug("Calling send with request parameter: {}", jsonInputString);
		
		HttpPost httpPost = new HttpPost(apiUrl);
		httpPost.addHeader("Content-Type", "application/json");
		httpPost.addHeader("Accept", "application/json");
		httpPost.setEntity(new StringEntity(jsonInputString));

		try (CloseableHttpResponse response = httpClient.execute(httpPost);
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {

			int responseCode = response.getStatusLine().getStatusCode();
			StringBuilder responseBody = new StringBuilder();
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				responseBody.append(inputLine);
			}

			String responseOutput = String.format("Response code: %d, Response body: %s", responseCode, responseBody);
			LOGGER.debug("Response: {}", responseOutput);
			return responseOutput;
		}
	}

	
}
