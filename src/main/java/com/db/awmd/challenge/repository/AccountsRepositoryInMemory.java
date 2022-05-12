package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferFunds;
import com.db.awmd.challenge.exception.AccountDoesntExistException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.FundsTransferException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(AccountsRepositoryInMemory.class);
	
	private final Map<String, Account> accounts = new ConcurrentHashMap<>();
	
	// Declaring Constants
	public static final String ACCT_DOESNT_EXIST_STR = " doesn't exists for doing funds transfer.";
	public static final String FROM_ACCT_STR = "From Acct ";
	public static final String TO_ACCT_STR = "To Acct ";
	public static final String BALANCE_STR1 = "Account's {} Balance is {} and ";
	public static final String BALANCE_BEFORE_STR = "Account's {} Balance is {} before funds transfer.";
	public static final String BALANCE_AFTER_STR = "Account's Balance is {} after funds transfer.";
	public static final String INSUFFICIENT_BALANCE_STR = " doesn't have sufficient balance to do funds transfer.";
	public static final String AND_STR = " and ";

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

	/**
	 * This Repository Layer Method is responsible for fulfilling fundsTransferBetweenAccts functionality.
	 * 
	 * @param transferFunds
	 */
	@Override
	public void fundsTransferBetweenAccts(TransferFunds transferFunds) {
		
		// Validating if the From Acct and To Acct exists. If both, or any of the accounts doesn't exist, Funds Transfer can't be done, hence throw AccountDoesntExistException
		if (!accounts.containsKey(transferFunds.getFromAcctId()) &  !accounts.containsKey(transferFunds.getToAcctId())) {
			throw new AccountDoesntExistException(FROM_ACCT_STR + transferFunds.getFromAcctId() + AND_STR + TO_ACCT_STR + transferFunds.getToAcctId() + ACCT_DOESNT_EXIST_STR);
		} else if (!accounts.containsKey(transferFunds.getFromAcctId())) {
			throw new AccountDoesntExistException(FROM_ACCT_STR + transferFunds.getFromAcctId() + ACCT_DOESNT_EXIST_STR);
		} else if (!accounts.containsKey(transferFunds.getToAcctId())) {
			throw new AccountDoesntExistException(TO_ACCT_STR + transferFunds.getToAcctId() + ACCT_DOESNT_EXIST_STR);
		} 
		// When From Acct and To Acct exists, continue with Funds Transfer.
		else {
			Account fromAcct = accounts.get(transferFunds.getFromAcctId());
			Account toAcct = accounts.get(transferFunds.getToAcctId());
			log.info(BALANCE_STR1 + BALANCE_BEFORE_STR, fromAcct.getAccountId(), fromAcct.getBalance(), toAcct.getAccountId(), toAcct.getBalance());
			
			BigDecimal transferAmt = transferFunds.getTransferAmt();
			
			// Transferring the Amount and updating each accounts old balance with the new balance.
			// No simultaneous threads should be executing the synchronized piece of code to avoid Race Condition (Balance Amt Data Inconsistency).
			synchronized(this) {
				// Check if From Acct has sufficient balance to do funds transfer.
				if (fromAcct.getBalance().compareTo(transferAmt) == 1) {
					fromAcct.setBalance(fromAcct.getBalance().subtract(transferAmt));
					toAcct.setBalance(toAcct.getBalance().add(transferAmt));
				}
				// Throw FundsTransferException From Acct does NOT have sufficient balance to do funds transfer.
				else {
					throw new FundsTransferException(transferFunds.getFromAcctId() + INSUFFICIENT_BALANCE_STR);
				}
			}
			
			log.info(BALANCE_STR1 + BALANCE_AFTER_STR, fromAcct.getAccountId(), fromAcct.getBalance(), toAcct.getAccountId(), toAcct.getBalance());
		}
		
	}

}
