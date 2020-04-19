#!/bin/bash
# Step 0: set openssl path (work arround for Git Bash with Mingw)
if [ -f "/usr/bin/openssl" ]; then
	openssl=/usr/bin/openssl
else
	openssl=openssl
fi

echo "ASN1 qcStatements from ${1}:"
${openssl} asn1parse -in "${1}" | awk '/qcStatements/ { getline; print $0 }' | sed -n -e 's/^.*://p' | xxd -r -p | base64 | ${openssl} asn1parse -oid 'oid_data.txt' -in /dev/stdin
