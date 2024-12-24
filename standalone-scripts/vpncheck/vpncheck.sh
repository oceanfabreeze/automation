#!/bin/bash
# -------------------------------------------------------------------------
#author=Thomas A. Fabrizio (TAFFY)
#version=1.4
#Changelog= Changing command ran as vpn service is not restarting
# ----------------------------------------------------------------------------------
# Script to check if openvpn is running. If not, start it. 
# ----------------------------------------------------------------------------------

DATE=$(date)

if pgrep openvpn;
then
    printf "\n($DATE) VPN IS RUNNING!" >> /root/vpn.log
else
   systemctl start openvpn-client@vpn && printf "\n($DATE) VPN IS NOT RUNNING. STARTING." >> /root/vpn.log
fi