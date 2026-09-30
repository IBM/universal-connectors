# Add Custom Certificate/CA Chains

This document describes how to import custom SSL/TLS certificates for a Kafka cluster on Guardium Central Manager
using the `store certificate kafka` CLI command.

## Overview

The `store certificate kafka` command imports custom SSL/TLS certificates for a Kafka cluster with host validation
enabled. It prompts for the private key, the End-Entity certificate, and the trusted certificate chain (intermediate
and root CA certificates). The intermediate certificate is optional.

## Prerequisites

- Must be run on **Central Manager**
- Certificates must be in **PEM format**
- The End-Entity certificate must contain at least one **Subject Alternative Name (SAN)**

## Required Inputs

| # | Input | Description |
|---|-------|-------------|
| 1 | **Private Key** | Private key in PEM format |
| 2 | **End-Entity Certificate** | Server certificate in PEM format with SAN extension |
| 3 | **Trusted Certificate** | Certificate chain (intermediate & root CA) in PEM format; intermediate is optional |

---

## Step-by-Step Procedure

### Step 1: Log into CM Backend

Log in to the Guardium Central Manager (CM) backend.

### Step 2: Log into CLI Mode

Enter CLI mode on the Central Manager.

### Step 3: Run the `store certificate kafka` Command

Run the following command and provide the required inputs when prompted:

```
store certificate kafka
```

#### Sample Interaction

```
> store certificate kafka

WARNING: Kafka certificate already exists. Do you want to replace it? [y/N]
y

Continuing with Kafka certificate replacement...

Host validation is enabled on the kafka cluster. Host details are required.

Please paste your private key below in PEM encoded format. A private key in PEM
encoded format should include the '-----BEGIN PRIVATE KEY-----' and '-----END
PRIVATE KEY-----' tags, as follows:

	-----BEGIN PRIVATE KEY-----
	(Private Key)
	-----END PRIVATE KEY-----

Once done pasting your private key, press ENTER followed by CTRL-D to continue.

-----BEGIN PRIVATE KEY-----
.
.
-----END PRIVATE KEY-----

Please paste your End-Entity certificate below in PEM encoded format. A certificate in
PEM encoded format should include the '-----BEGIN CERTIFICATE-----' and '-----END
CERTIFICATE-----' tags. The Certificate Authority (CA) Root and Intermediate
certificate(s) (if applicable) will also need to be pasted at this time for
validation purposes. Please ensure that all certificates are in PEM format and
include the aforementioned tags. When pasting multiple certificates, please make
sure that each certificate is pasted on a new line in the following order:

	-----BEGIN CERTIFICATE-----
	(End-Entity certificate)
	-----END CERTIFICATE-----
	-----BEGIN CERTIFICATE-----
	(Intermediate certificate(s) - if applicable)
	-----END CERTIFICATE-----
	-----BEGIN CERTIFICATE-----
	(Root certificate)
	-----END CERTIFICATE-----

Once done pasting your certificate(s), press ENTER followed by CTRL-D to continue.

-----BEGIN CERTIFICATE-----
.
.
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
.
.
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
.
.
-----END CERTIFICATE-----

Certificate and Key validation was successful!


SUCCESS: Certificate imported successfully - Keystore has been updated.
```

### Step 4: Verify the JKS Has Been Updated

Confirm that the Java KeyStore (JKS) file has been updated at the following path on the Central Manager:

```
/opt/IBM/Guardium/etc/kafkacluster/
```

### Step 5: Run Portal User Sync

Log in to the CM GUI and trigger a Portal User Sync to propagate the certificate changes:

1. Navigate to **Manage** → **Central Management** → **Portal User Sync**
2. Click **Run Once Now**

### Step 6: Wait for Propagation

Wait approximately **45 minutes** for the process to fully propagate across the environment.

---

## Notes

- If no existing Kafka certificate is present, the replacement prompt (`WARNING: Kafka certificate already exists…`)
  will not appear and the command proceeds directly to the key input.
- When pasting multi-certificate chains, ensure each certificate block starts on a new line with no blank lines
  between blocks.
- Press **ENTER** followed by **CTRL-D** to signal end-of-input after pasting each PEM block.

---

## Additional Resources

- [Universal Connector Configuration Guide](../KAFKA_GDP.md)
- [Guardium Data Protection Documentation](https://www.ibm.com/docs/en/gdp)

---

**Last Updated**: 2025-10-23
