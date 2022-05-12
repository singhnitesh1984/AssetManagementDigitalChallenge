package com.db.awmd.challenge.exception;

public class AccountDoesntExistException extends RuntimeException {

	public AccountDoesntExistException(String message) {
		super(message);
	}
}
