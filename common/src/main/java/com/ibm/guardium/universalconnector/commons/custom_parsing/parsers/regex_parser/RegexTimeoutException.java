/*
 * Licensed Materials - Property of IBM
 * 5725I71-CC011829
 * (C) Copyright IBM Corp. 2021. All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.ibm.guardium.universalconnector.commons.custom_parsing.parsers.regex_parser;

public class RegexTimeoutException extends RuntimeException {
    RegexTimeoutException(String m) {
        super(m);
    }
}