package com.ibm.guardium.trino.sql_parsing.exception;

public class InvalidStatementException extends Exception {
  public InvalidStatementException(String statement) {
    super("Statement [" + statement + "] is not supported.");
  }
}
