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
openssl req -x509 -sha256 -days 356 -nodes -newkey rsa:2048 -subj "/CN=uc-ca/C=US" -keyout ucCA.key -out ucCA.crt

# Create the demo directory
mkdir -p $CERT_DIR/cert

# Change to the demo directory
cd $CERT_DIR/cert

# Create a private key for Filebeat
openssl genrsa -out uc-key.pem 2048

# Convert the private key to PKCS#8 format
openssl pkcs8 -in uc-key.pem -topk8 -out uc-key-pkcs8.pem -nocrypt

# Create a certificate request configuration file
cat > uc-csr.conf <<EOF
[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C = US
CN = uc-filebeat

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = $SERVER_DNS
EOF

# Create a certificate signing request for Filebeat
openssl req -new -key uc-key.pem -out uc-csr.pem -config uc-csr.conf

# Create a certificate configuration file
cat > uc-cert.conf <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = $SERVER_DNS
EOF

# Create a certificate for Universal Connector, signed by the CA
openssl x509 -req -in uc-csr.pem -CA ../CA/ucCA.crt -CAkey ../CA/ucCA.key -CAcreateserial -out uc-cert.pem -days 365 -sha256 -extfile uc-cert.conf

rm uc-csr.pem
rm uc-cert.conf
rm uc-csr.conf

cd ../CA
rm ucCA.srl

echo "done"
