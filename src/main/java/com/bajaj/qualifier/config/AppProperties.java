package com.bajaj.qualifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
	private String name;
	private String regNo;
	private String email;
	private String finalQuery;
	private String question1SqlPath = "sql/question1.sql";
	private String question2SqlPath = "sql/question2.sql";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFinalQuery() {
		return finalQuery;
	}

	public void setFinalQuery(String finalQuery) {
		this.finalQuery = finalQuery;
	}

	public String getQuestion1SqlPath() {
		return question1SqlPath;
	}

	public void setQuestion1SqlPath(String question1SqlPath) {
		this.question1SqlPath = question1SqlPath;
	}

	public String getQuestion2SqlPath() {
		return question2SqlPath;
	}

	public void setQuestion2SqlPath(String question2SqlPath) {
		this.question2SqlPath = question2SqlPath;
	}
}


