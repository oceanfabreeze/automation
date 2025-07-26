#!/bin/bash

# Gets cloudflare API data for a specific domain and subdomain.
# Author = oceanfabreeze
# Version = 0.1.0 Beta
# Changelog:
# 07/26/2025 (04:36PM EDT): Initial commit.

DOMAIN="taffyhome.com"  # Replace with your domain
RECORD_NAME="home.taffyhome.com"  # Replace with your subdomain (e.g., home.yourdomain.com)
API_TOKEN="xxxxx"  # Replace with your Cloudflare API Token

ZONE_ID=$(curl -s -X GET "https://api.cloudflare.com/client/v4/zones?name=$DOMAIN" \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" | jq -r '.result[0].id')

RECORD_ID=$(curl -s -X GET "https://api.cloudflare.com/client/v4/zones/$ZONE_ID/dns_records?name=$RECORD_NAME" \
  -H "Authorization: Bearer $API_TOKEN" \
  -H "Content-Type: application/json" | jq -r '.result[0].id')

echo "ZONE_ID=$ZONE_ID"
echo "RECORD_ID=$RECORD_ID"