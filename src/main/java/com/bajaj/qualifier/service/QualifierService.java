package com.bajaj.qualifier.service;

import com.bajaj.qualifier.config.AppProperties;
import com.bajaj.qualifier.model.GenerateWebhookRequest;
import com.bajaj.qualifier.model.GenerateWebhookResponse;
import com.bajaj.qualifier.model.SubmissionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QualifierService {
	private static final Logger log = LoggerFactory.getLogger(QualifierService.class);
	private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
	private static final String DEFAULT_SUBMIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

	private final RestTemplate restTemplate;
	private final AppProperties props;

	public QualifierService(RestTemplate restTemplate, AppProperties props) {
		this.restTemplate = restTemplate;
		this.props = props;
	}

	public void runFlow() {
		GenerateWebhookResponse webhookResponse = generateWebhook();
		if (webhookResponse == null) {
			log.error("Webhook generation failed; aborting.");
			return;
		}
		String submitUrl = Optional.ofNullable(webhookResponse.getWebhook()).filter(s -> !s.isBlank()).orElse(DEFAULT_SUBMIT_URL);
		String accessToken = webhookResponse.getAccessToken();
		String finalQuery = resolveFinalQuery(props.getRegNo());
		submitSolution(submitUrl, accessToken, finalQuery);
	}

	private GenerateWebhookResponse generateWebhook() {
		GenerateWebhookRequest body = new GenerateWebhookRequest(props.getName(), props.getRegNo(), props.getEmail());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(body, headers);
		try {
			ResponseEntity<GenerateWebhookResponse> response = restTemplate.exchange(
				GENERATE_WEBHOOK_URL, HttpMethod.POST, entity, GenerateWebhookResponse.class
			);
			log.info("Generated webhook. Status: {}", response.getStatusCode());
			return response.getBody();
		} catch (HttpStatusCodeException ex) {
			log.error("Failed to generate webhook. Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
			return null;
		} catch (Exception ex) {
			log.error("Unexpected error generating webhook", ex);
			return null;
		}
	}

	private String resolveFinalQuery(String regNo) {
		if (props.getFinalQuery() != null && !props.getFinalQuery().isBlank()) {
			log.info("Using finalQuery from application properties.");
			return props.getFinalQuery();
		}
		boolean isOdd = isLastTwoDigitsOdd(regNo);
		String resourcePath = isOdd ? props.getQuestion1SqlPath() : props.getQuestion2SqlPath();
		try {
			ClassPathResource resource = new ClassPathResource(resourcePath);
			String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8).trim();
			log.info("Loaded SQL from resource '{}' ({} chars).", resourcePath, sql.length());
			return sql;
		} catch (IOException e) {
			log.error("Could not load SQL from '{}'. Provide app.finalQuery or add resource file.", resourcePath, e);
			return "";
		}
	}

	private boolean isLastTwoDigitsOdd(String regNo) {
		if (regNo == null) return true;
		Pattern p = Pattern.compile("(\\d{1,2})\\D*$|.*?(\\d{2})$");
		Matcher m = p.matcher(regNo);
		String twoDigits = null;
		if (m.matches()) {
			twoDigits = m.group(1) != null ? m.group(1) : m.group(2);
		}
		if (twoDigits == null) {
			Matcher all = Pattern.compile(".*?(\\d+)$").matcher(regNo);
			if (all.matches()) {
				String last = all.group(1);
				if (last.length() >= 2) {
					twoDigits = last.substring(last.length() - 2);
				} else {
					twoDigits = last;
				}
			}
		}
		if (twoDigits == null) return true;
		int val = Integer.parseInt(twoDigits);
		return (val % 2) == 1;
	}

	private void submitSolution(String submitUrl, String accessToken, String finalQuery) {
		if (finalQuery == null || finalQuery.isBlank()) {
			log.error("finalQuery is empty. Aborting submission.");
			return;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (accessToken != null && !accessToken.isBlank()) {
			headers.set(HttpHeaders.AUTHORIZATION, accessToken);
		} else {
			log.warn("accessToken is empty; Authorization header will not be set.");
		}
		SubmissionRequest body = new SubmissionRequest(finalQuery);
		HttpEntity<SubmissionRequest> entity = new HttpEntity<>(body, headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(submitUrl, HttpMethod.POST, entity, String.class);
			log.info("Submission completed. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
		} catch (HttpStatusCodeException ex) {
			log.error("Submission failed. Status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
		} catch (Exception ex) {
			log.error("Unexpected error during submission", ex);
		}
	}
}


