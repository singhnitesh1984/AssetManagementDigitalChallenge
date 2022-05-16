package com.db.awmd.challenge.web;

import javax.validation.Valid;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferFunds;
import com.db.awmd.challenge.exception.AccountDoesntExistException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.FundsTransferException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(AccountsController.class);
	
	private final AccountsService accountsService;
	
	// Declaring Constants
	public static final String TRANSFER_FAILURE_STR = "TRANSFER FAILED!! ";
	public static final String INITIATING_TRANSFER_STR = "Initiating fund transfer between accounts {} -> {}";
	
	@Autowired
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account details for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	/**
	 * This controller API Method initiates fundsTransferBetweenAccts functionality.
	 * 
	 * @param transferFunds
	 * @return ResponseEntity<Object>
	 */
	@PostMapping(path = "/{fundsTransferBetweenAccts}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> fundsTransferBetweenAccts(@RequestBody @Valid TransferFunds transferFunds) throws Exception {
		
		log.info(INITIATING_TRANSFER_STR, transferFunds.getFromAcctId(), transferFunds.getToAcctId());
		try {
			// Initiating fundsTransferBetweenAccts
			this.accountsService.fundsTransferBetweenAccts(transferFunds);
		} catch (AccountDoesntExistException adeex) {
			log.info(TRANSFER_FAILURE_STR + adeex.getMessage());
			return new ResponseEntity<>(adeex.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (FundsTransferException ftex) {
			log.info(TRANSFER_FAILURE_STR + ftex.getMessage());
			return new ResponseEntity<>(ftex.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

}
