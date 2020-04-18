# Step 1: Create dummy CA
# Create the key
#openssl ecparam -genkey -name prime256v1 -out "out/dummy-ca.key"
#openssl ecparam -genkey -name prime256v1 -out "out/dummy-ca.key"
openssl genrsa -out "out/dummy-ca.key" 4096
# Create a CSR
#openssl req -new -nodes -sha512 \
#		-key "out/dummy-ca.key" -subj "${CAsubj}" -out "put/dummy-ca.csr"
# We issue self-signed cert for the CA
# without any key constraints or extended usages (,=all permitted):
#        -sigopt rsa_padding_mode:pss -sigopt rsa_pss_saltlen:32 \
openssl req -new -x509 -sha512 -set_serial 1 -days 30 \
        -subj "/C=DE/L=Berlin/O=No-Trust CA/CN=No-Trust Root CA 42" \
        -key "out/dummy-ca.key" -out "out/dummy-ca.crt"

# Step 2: Create dummy certs
# 2a: e-Seal
# Serial Seal: 420042 
#openssl ecparam -genkey -name prime256v1 -out "out/e-seal.key"
openssl genrsa -out "out/e-seal.key" 4096
#PSS Parameters:        -sigopt rsa_padding_mode:pss -sigopt rsa_pss_saltlen:32 \
openssl req -new -nodes -sha512 \
        -subj "/C=DE/L=Berlin/O=Homer Simpson Vocational College/CN=Homer Simpson Vocational College/organizationIdentifier=DE:BSN-08B04" \
        -config "qc_extensions.cnf" \
        -extensions for_seal \
		-key "out/e-seal.key" -out "out/e-seal.csr"
openssl x509 -req -sha512 -set_serial 420042 -days 15 \
		-CAkey "out/dummy-ca.key" -CA "out/dummy-ca.crt" \
		-in "out/e-seal.csr" -out "out/e-seal.crt"

#-extfile "extensions.cnf" -extensions "for_a_node" \        
# 2b: e-Sign
# Serial Sign: 230023 
openssl genrsa -out "out/e-sign.key" 4096
openssl req -new -nodes -sha512 \
        -sigopt rsa_padding_mode:pss -sigopt rsa_pss_saltlen:32 \
        -subj "/C=DE/L=Berlin/O=Homer Simpson Vocational College/CN=Seymour Skinner" \
        -config "qc_extensions.cnf" \
        -extensions for_sign \
		-key "out/e-sign.key" -out "out/e-sign.csr"
openssl x509 -req -sha512 -set_serial 230023 -days 15 \
		-CAkey "out/dummy-ca.key" -CA "out/dummy-ca.crt" \
		-in "out/e-sign.csr" -out "out/e-sign.crt"


# Step 3: Package pkcs12
openssl pkcs12 -export -passout "pass:sign" -in "out/e-sign.crt" -inkey "out/e-sign.key" -out "out/e-sign.p12" -chain -CAfile "out/dummy-ca.crt"
openssl pkcs12 -export -passout "pass:seal" -in "out/e-seal.crt" -inkey "out/e-seal.key" -out "out/e-seal.p12" -chain -CAfile "out/dummy-ca.crt"

# Step 3: Copy pkcs12 to test suite
cp out/*.p12 ../europass-clst/src/test/java/de/mserve/europass/clst/