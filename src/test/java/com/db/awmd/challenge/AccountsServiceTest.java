package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferFunds;
import com.db.awmd.challenge.exception.AccountDoesntExistException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.FundsTransferException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  
  	@Test
	public void fundsTransferBetweenAccts() throws Exception {

		Account fromAcct = new Account("123123123", new BigDecimal(1000));
		Account toAcct = new Account("234234234", new BigDecimal(800));

		this.accountsService.createAccount(fromAcct);
		this.accountsService.createAccount(toAcct);

		TransferFunds transferFunds = new TransferFunds("123123123", "234234234", new BigDecimal(200));

		this.accountsService.fundsTransferBetweenAccts(transferFunds);
		assertThat(this.accountsService.getAccount(transferFunds.getFromAcctId()).equals(fromAcct.getAccountId()));
		assertThat(this.accountsService.getAccount(transferFunds.getToAcctId()).equals(toAcct.getAccountId()));
			
	}
  	
  	@Test
	public void fundsTransferBetweenAccts_AccountDoesntExistException() throws Exception {

		Account fromAcct = new Account("345345345", new BigDecimal(1000));
		Account toAcct = new Account("456456456", new BigDecimal(800));

		this.accountsService.createAccount(fromAcct);
		this.accountsService.createAccount(toAcct);

		TransferFunds transferFunds = new TransferFunds("345", "456456456", new BigDecimal(200));

		try {
			this.accountsService.fundsTransferBetweenAccts(transferFunds);
		} catch (AccountDoesntExistException ex) {
			assertThat(ex instanceof AccountDoesntExistException);
		}

	}
  	
  	@Test
	public void fundsTransferBetweenAccts_FundsTransferException() throws Exception {

		Account fromAcct = new Account("567567567", new BigDecimal(100));
		Account toAcct = new Account("678678678", new BigDecimal(100));

		this.accountsService.createAccount(fromAcct);
		this.accountsService.createAccount(toAcct);

		TransferFunds transferFunds = new TransferFunds("567567567", "678678678", new BigDecimal(200));

		try {
			this.accountsService.fundsTransferBetweenAccts(transferFunds);
		} catch (FundsTransferException ex) {
			assertThat(ex instanceof FundsTransferException);
		}

	}
  	
}
