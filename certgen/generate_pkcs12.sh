#!/bin/bash
# Step 0: set openssl path (work arround for Git Bash with Mingw)
if [ -f "/usr/bin/openssl" ]; then
	openssl=/usr/bin/openssl
else
	openssl=openssl
fi
# Step 1: Create dummy CA
# Create the key
#openssl ecparam -genkey -name prime256v1 -out "out/dummy-ca.key"
#openssl ecparam -genkey -name prime256v1 -out "out/dummy-ca.key"
if [ ! -f "out/dummy-ca.key" ]; then
    ${openssl} genrsa -out "out/dummy-ca.key" 4096
fi
# Create a CSR
#openssl req -new -nodes -sha512 \
#		-key "out/dummy-ca.key" -subj "${CAsubj}" -out "put/dummy-ca.csr"
# We issue self-signed cert for the CA
# without any key constraints or extended usages (,=all permitted):
#        -sigopt rsa_padding_mode:pss -sigopt rsa_pss_saltlen:32 \

${openssl} req -new -x509 -sha512 -set_serial 1 -days 30 \
        -subj "/C=DE/L=Berlin/O=No-Trust CA/CN=No-Trust Root CA 42" \
        -key "out/dummy-ca.key" -out "out/dummy-ca.crt"

# Step 2: Create dummy certs
# 2a: e-Seal
# Serial Seal: 420042
#openssl ecparam -genkey -name prime256v1 -out "out/e-seal.key"
if [ ! -f "out/e-seal.key" ]; then
    openssl genrsa -out "out/e-seal.key" 4096
fi
#PSS Parameters:        -sigopt rsa_padding_mode:pss -sigopt rsa_pss_saltlen:32 \
${openssl} req -new -nodes -sha512 \
        -subj "/C=DE/L=Berlin/O=Homer Simpson Vocational College/CN=Homer Simpson Vocational College/organizationIdentifier=DE:BSN-08B04" \
        -config "qc_extensions.cnf" \
        -extensions for_seal \
		-key "out/e-seal.key" -out "out/e-seal.csr"
openssl x509 -req -sha512 -set_serial 420042 -days 15 \
        -extfile "qc_extensions.cnf" \
        -extensions for_seal \
		-CAkey "out/dummy-ca.key" -CA "out/dummy-ca.crt" \
		-in "out/e-seal.csr" -out "out/e-seal.crt"

#-extfile "extensions.cnf" -extensions "for_a_node" \        
# 2b: e-Sign
# Serial Sign: 230023 
if [ ! -f "out/e-sign.key" ]; then
    ${openssl} genrsa -out "out/e-sign.key" 4096
fi    
${openssl} req -new -nodes -sha512 \
        -sigopt rsa_padding_mode:pss -sigopt rsa_pss_saltlen:32 \
        -subj "/C=DE/L=Berlin/O=Homer Simpson Vocational College/CN=Seymour Skinner" \
        -config "qc_extensions.cnf" \
        -extensions for_sign \
		-key "out/e-sign.key" -out "out/e-sign.csr"
${openssl} x509 -req -sha512 -set_serial 230023 -days 15 \
        -extfile "qc_extensions.cnf" \
        -extensions for_sign \
		-CAkey "out/dummy-ca.key" -CA "out/dummy-ca.crt" \
		-in "out/e-sign.csr" -out "out/e-sign.crt"


# Step 3: Package pkcs12
${openssl} pkcs12 -export -passout "pass:sign" -in "out/e-sign.crt" -inkey "out/e-sign.key" -out "out/e-sign.p12" -chain -CAfile "out/dummy-ca.crt"
${openssl} pkcs12 -export -passout "pass:seal" -in "out/e-seal.crt" -inkey "out/e-seal.key" -out "out/e-seal.p12" -chain -CAfile "out/dummy-ca.crt"

# Step 4: Copy pkcs12 to test suite
cp out/*.p12 ../europass-clst/src/test/java/de/mserve/europass/clst/

# Step 5: Check content
echo "ASN1 qcStatements from e-seal.crt:"
${openssl} asn1parse -in "out/e-seal.crt" | awk '/qcStatements/ { getline; print $0 }' | sed -n -e 's/^.*://p' | xxd -r -p | base64 | ${openssl} asn1parse -oid 'oid_data.txt' -in /dev/stdin
echo "ASN1 qcStatements from e-sign.crt:"
${openssl} asn1parse -in "out/e-sign.crt" | awk '/qcStatements/ { getline; print $0 }' | sed -n -e 's/^.*://p' | xxd -r -p | base64 | ${openssl} asn1parse -oid 'oid_data.txt' -in /dev/stdin
