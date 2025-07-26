#!/bin/bash

# Updates a Cloudflare DNS record with the current public IP address.
# Author = oceanfabreeze
# Version = 0.1.0 Beta
# Changelog:
# 07/26/2025 (04:36PM EDT): Initial commit.

# Cloudflare API details
ZONE_ID="xxxxx"                 # Replace with your Cloudflare Zone ID
RECORD_ID="xxxxx"             # Replace with your DNS Record ID
API_TOKEN="xxxxx"  # Replace with your Cloudflare API Token
DNS_NAME="xxxxx"         # Replace with your subdomain (e.g., home.yourdomain.com)

# Get the current public IP address
CURRENT_IP=$(curl -s http://ipv4.icanhazip.com)

# Get the current DNS record content (the IP address associated with the subdomain)
OLD_IP=$(curl -s "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/$RECORD_ID" \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" | jq -r '.result.content')

# Compare the IPs. If different, update the DNS record.
if [ "$CURRENT_IP" != "$OLD_IP" ]; then
  echo "IP has changed. Updating DNS record."

  # Update the Cloudflare DNS record
  curl -X PUT "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records/$RECORD_ID" \
    -H "Authorization: Bearer $API_TOKEN" \
    -H "Content-Type: application/json" \
    --data '{"type":"A","name":"'$DNS_NAME'","content":"'$CURRENT_IP'","ttl":1,"proxied":true}'

  echo "DNS record updated to $CURRENT_IP."
else
  echo "IP address has not changed."
fi
