package com.bajaj.qualifier.startup;

import com.bajaj.qualifier.service.QualifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);
	private final QualifierService qualifierService;

	public StartupRunner(QualifierService qualifierService) {
		this.qualifierService = qualifierService;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Starting qualifier flow on application startup...");
		qualifierService.runFlow();
	}
}


