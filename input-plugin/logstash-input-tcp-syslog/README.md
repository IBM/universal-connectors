# TCP input plug-in

## Meet TCP

- Tested versions: 8.8.2
- Developed by Elastic
- Supported Guardium versions:
  - Guardium Data Protection: 11.3 and above
  - Guardium Insights: 3.2 and above

This is a [Logstash](https://github.com/elastic/logstash) input plug-in for the universal connector that is featured in IBM Security Guardium. It enables Logstash to receive connections from TCP. The events are then sent to a corresponding filter plug-in, which transforms these audit logs into a [Guardium record](https://github.com/IBM/universal-connectors/blob/main/common/src/main/java/com/ibm/guardium/universalconnector/commons/structures/Record.java) instance, which is a standard structure made out of several parts. The information is then sent over to Guardium. Guardium records include an accessor (a person who tried to access the data), session, data, and exceptions. If there are no errors, the data contains details about the query "construct". The construct details the main action (verb) and collections (objects) involved.

To learn more about logstash input plugin, see logstash documentation

[logstash inputs tcp documentation](https://www.elastic.co/guide/en/logstash/current/plugins-inputs-tcp.html)

## Purpose

Specify a port, and this plug-in will listen to the port on the Logstash host for any new connection.

## Usage

### Parameters

| Parameter                  | Input Type | Required | Default      |
| -------------------------- | ---------- | -------- | ------------ |
| port                       | number     | Yes      |              |
| ssl_enabled                | boolean    | no       | False        |
| ssl_certificate            | string     | no       |              |
| ssl_key                    | string     | no       |              |
| ssl_key_passphrase         | string     | no       |              |
| ssl_client_authentication  | string     | no       | "optional"   |

#### `port`

The `port` setting allows specifying a port on which the Logstash host listens to the TCP connection.

#### `ssl_enabled`

Enable SSL.

#### `ssl_certificate`

Path to certificate in PEM format. This certificate will be presented to the connecting clients.

#### `ssl_key`

The path to the private key corresponding to the specified certificate (PEM format).

#### `ssl_key_passphrase`

The passphrase for the encrypted private key. Only required if the private key is encrypted.

#### `ssl_client_authentication`

Verify the identity of the other end of the SSL connection against the CA.

Valid options are:
- `none` - No SSL client authentication
- `optional` - SSL client authentication is optional
- `required` - SSL client authentication is required

#### Logstash default configuration parameters

Other standard Logstash parameters are available, such as:

- `add_field`
- `type`
- `tags`

#### Example without SSL

```txt
input {
  tcp {
    port => 514
  }
}
```

#### Example with SSL

**Important:** For backward compatibility information and SSL configuration requirements, see [Logstash 9 Upgrade Limitations](../../docs/LOGSTASH_9_UPGRADE_LIMITATIONS.md).

```txt
input {
  tcp {
    port => 6514
    ssl_enabled => false
    # ssl_certificate => "${SSL_DIR}/tls.crt"
    # ssl_key => "${SSL_DIR}/tls.key"
    # Uncomment the ssl_key_passphrase line for Guardium Data Protection v12.2.3 (or patch 5008) and above to use encrypted private keys
    # ssl_key_passphrase => "${ssl_key_passphrase}"
    # ssl_client_authentication => "none"
  }
}
```

**Note:** The `ssl_key_passphrase` parameter is only supported in Guardium Data Protection v12.2.3 (with patch 5008) and above. For older versions, keep this line commented out and use unencrypted private keys.

## Configuring rsyslog to send logs to Guardium

To learn more about rsyslog, see
[rsyslog documentation](https://www.rsyslog.com/doc)

### rsyslog configuration for sending over TCP

Replace the host token with your host and copy the snippet at the end of `/etc/rsyslog.conf`.

```txt
# Enable sending of logs over TCP to <HOST>
*.*  action(type="omfwd" target="<HOST>" port="514" protocol="tcp"
      )
```

### rsyslog configuration for sending over TCP with TLS

```txt
# Certificate Authority
$DefaultNetstreamDriverCAFile /path/to/tls/ca.pem

# Send logs over TCP with TLS
*.*  action(type="omfwd" target="<HOST>" port="6514" protocol="tcp"
      StreamDriver="gtls" # GnuTLS driver
      StreamDriverMode="1" # run driver in TLS-only mode
      )
```

Verify the configuration:

```bash
rsyslogd -N1
```

Verify successful response: `rsyslogd: End of config validation run. Bye.`

After making changes to the syslog configuration, restart the syslog daemon to apply the changes:
Run:

```bash
sudo service rsyslog restart
```
