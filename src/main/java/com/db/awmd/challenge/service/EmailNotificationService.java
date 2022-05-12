package com.db.awmd.challenge.service;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.db.awmd.challenge.domain.Account;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailNotificationService implements NotificationService {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

	@Override
	public void notifyAboutTransfer(Account account, String transferDescription) {
		// THIS METHOD SHOULD NOT BE CHANGED - ASSUME YOUR COLLEAGUE WILL IMPLEMENT IT

		log.info("Sending notification to owner of {}: {}", account.getAccountId(), transferDescription);
	}

}
