package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferFunds;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;

	public AccountsRepository getAccountsRepository() {
		return accountsRepository;
	}

	@Autowired
	public AccountsService(AccountsRepository accountsRepository) {
		this.accountsRepository = accountsRepository;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * This Service Layer Method calls Repository Layer to fulfill fundsTransferBetweenAccts functionality.
	 * 
	 * @param transferFunds
	 * @throws Exception 
	 */
	public void fundsTransferBetweenAccts(TransferFunds transferFunds) throws Exception {
		this.accountsRepository.fundsTransferBetweenAccts(transferFunds);
	}
}
