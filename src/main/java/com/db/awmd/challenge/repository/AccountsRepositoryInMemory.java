package com.db.awmd.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(AccountsRepositoryInMemory.class);
	
	public final Map<String, Account> accounts = new ConcurrentHashMap<>();
	
	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

}
