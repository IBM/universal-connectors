package com.ibm.guardium.databricks.sql_parsing.exception;

public class InvalidStatementException extends Exception {
  public InvalidStatementException(String statement) {
    super("Statement [" + statement + "] is not supported.");
  }
}
