#!/bin/bash

# Check if the user provided the correct number of arguments
if [ $# -ne 2 ]; then
    echo "Usage: $0 /path/to/directory server.dns"
    exit 1
fi

# Set the directory where the certificates will be stored
CERT_DIR=$1

# Set the server DNS
SERVER_DNS=$2

# Create the CA directory
mkdir -p $CERT_DIR/CA

# Change to the CA directory
cd $CERT_DIR/CA

# Create the CA private key and certificate
openssl req -x509 -sha256 -days 356 -nodes -newkey rsa:2048 -subj "/CN=syslog.lan/C=IL" -keyout syslogCA.key -out syslogCA.crt

# Create the demo directory
mkdir -p $CERT_DIR/cert

# Change to the demo directory
cd $CERT_DIR/cert

# Create a private key for syslog
openssl genrsa -out syslog.key 2048

# Convert the private key to PKCS#8 format
openssl pkcs8 -in syslog.key -topk8 -out syslog-pkcs8.key -nocrypt

# Create a certificate request configuration file
cat > syslog-csr.conf <<EOF
[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn
[ dn ]
C = IL
CN = syslog.lan
[ req_ext ]
subjectAltName = @alt_names
[ alt_names ]
DNS.1 = $SERVER_DNS
EOF

# Create a certificate signing request for syslog
openssl req -new -key syslog.key -out syslog.csr -config syslog-csr.conf

# Create a certificate configuration file
cat > syslog-cert.conf <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names
[alt_names]
DNS.1 = $SERVER_DNS
EOF

# Create a certificate for syslog, signed by the CA
openssl x509 -req -in syslog.csr -CA ../CA/syslogCA.crt -CAkey ../CA/syslogCA.key -CAcreateserial -out syslog.crt -days 365 -sha256 -extfile syslog-cert.conf

rm syslog.csr
rm syslog-cert.conf
rm syslog-csr.conf

cd ../CA
rm syslogCA.srl

echo "done"
