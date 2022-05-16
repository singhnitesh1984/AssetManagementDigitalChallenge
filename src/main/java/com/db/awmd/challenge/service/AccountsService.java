package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferFunds;
import com.db.awmd.challenge.exception.AccountDoesntExistException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.FundsTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.repository.AccountsRepositoryInMemory;

import lombok.Getter;

@Service
public class AccountsService {
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(AccountsService.class);
	
	// Declaring Constants
	public static final String ACCT_DOESNT_EXIST_STR = " doesn't exists for doing funds transfer.";
	public static final String FROM_ACCT_STR = "From Acct ";
	public static final String TO_ACCT_STR = "To Acct ";
	public static final String BALANCE_STR1 = "Account's {} Balance is {} and ";
	public static final String BALANCE_BEFORE_STR = "Account's {} Balance is {} before funds transfer.";
	public static final String BALANCE_AFTER_STR = "Account's {} Balance is {} after funds transfer.";
	public static final String INSUFFICIENT_BALANCE_STR = " doesn't have sufficient balance to do funds transfer.";
	public static final String AND_STR = " and ";
	public static final String TRANSFER_SUCCESSFUL_STR1 = "Funds From Acct ";
	public static final String TRANSFER_SUCCESSFUL_STR2 = " got transferred successfully To Acct ";
	public static final String TRANSFER_FAILURE_STR = "TRANSFER FAILED!! ";
	public static final String INITIATING_TRANSFER_STR = "Initiating fund transfer between accounts {} -> {}";
	public static final String CANT_TRANSFER_WITHIN_SAME_ACCT_STR = "Can't transfer funds within same account ";
	
	@Getter
	private final AccountsRepository accountsRepository;
	
	@Getter
	private final AccountsRepositoryInMemory accountsRepositoryInMemory;
	
	@Getter
	private final EmailNotificationService emailNotificationService;
	
	public AccountsRepository getAccountsRepository() {
		return accountsRepository;
	}

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, AccountsRepositoryInMemory accountsRepositoryInMemory, EmailNotificationService emailNotificationService) {
		this.accountsRepository = accountsRepository;
		this.accountsRepositoryInMemory = accountsRepositoryInMemory;
		this.emailNotificationService = emailNotificationService;
	}
	
	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/**
	 * In actual scenario where we will not be using AccountsRepositoryInMemory, 
	 * this Service Layer Method will call Repository Layer to fulfill fundsTransferBetweenAccts functionality.
	 * 
	 * @param transferFunds
	 * @throws Exception 
	 */
	public void fundsTransferBetweenAccts(TransferFunds transferFunds) throws Exception {
		
		// Repository call will go heare to fetch accounts information
		Map<String, Account> accounts = this.accountsRepositoryInMemory.accounts;
		
		// Validating if the From Acct and To Acct exists. If both, or any of the accounts doesn't exist, Funds Transfer can't be done, hence throw AccountDoesntExistException
		if (!accounts.containsKey(transferFunds.getFromAcctId()) &  !accounts.containsKey(transferFunds.getToAcctId())) {
			throw new AccountDoesntExistException(FROM_ACCT_STR + transferFunds.getFromAcctId() + AND_STR + TO_ACCT_STR + transferFunds.getToAcctId() + ACCT_DOESNT_EXIST_STR);
		} else if (!accounts.containsKey(transferFunds.getFromAcctId())) {
			throw new AccountDoesntExistException(FROM_ACCT_STR + transferFunds.getFromAcctId() + ACCT_DOESNT_EXIST_STR);
		} else if (!accounts.containsKey(transferFunds.getToAcctId())) {
			throw new AccountDoesntExistException(TO_ACCT_STR + transferFunds.getToAcctId() + ACCT_DOESNT_EXIST_STR);
		} if (transferFunds.getFromAcctId().equals(transferFunds.getToAcctId())) {
			throw new DuplicateAccountIdException(CANT_TRANSFER_WITHIN_SAME_ACCT_STR + transferFunds.getFromAcctId());
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
			
			StringBuilder transferMsg = new StringBuilder(TRANSFER_SUCCESSFUL_STR1.concat(transferFunds.getFromAcctId()).concat(TRANSFER_SUCCESSFUL_STR2).concat(transferFunds.getToAcctId()));
			
			// Notifying both accounts with funds transfer status in case of successful funds transfer.
			notifyAcctHoldersForSuccessfulFundsTransfer(transferFunds, transferMsg);
			
			log.info(BALANCE_STR1 + BALANCE_AFTER_STR, fromAcct.getAccountId(), fromAcct.getBalance(), toAcct.getAccountId(), toAcct.getBalance());
		}
	}
	
	/**
	 * This method contains logic of Notifying both accounts with funds transfer status.
	 * 
	 * @param transferFunds
	 * @param transferMsg
	 */
	private void notifyAcctHoldersForSuccessfulFundsTransfer(TransferFunds transferFunds, StringBuilder transferMsg) {
		
		// Notifying From Account Holder.
		emailNotificationService.notifyAboutTransfer(getAccount(transferFunds.getFromAcctId()), transferMsg.toString());
		
		// Notifying To Account Holder.
		emailNotificationService.notifyAboutTransfer(getAccount(transferFunds.getToAcctId()), transferMsg.toString());
	}
	
}
