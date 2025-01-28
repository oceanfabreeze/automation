#!/bin/bash
# -------------------------------------------------------------------------
#author=Thomas A. Fabrizio (oceanfabreeze)
#version=1.0
#Changelog= (12/07/2024 12:03PM EST) Initial Commit
# ----------------------------------------------------------------------------------
#Check if media filesystems are mounted and if not, mount them and restart docker
# ----------------------------------------------------------------------------------

# Check if /home/movies is mounted
if ! mount | grep -q '/home/movies'; then
    echo "/home/movies is not mounted. Running mount -a..."
    mount -a
    docker_restart_needed=true
fi

# Check if /home/tv is mounted
if ! mount | grep -q '/home/tv'; then
    echo "/home/tv is not mounted. Running mount -a..."
    mount -a
    docker_restart_needed=true
fi

# Restart Docker service if needed
if [ "$docker_restart_needed" = true ]; then
    echo "Filesystems were not mounted. Restarting Docker service..."
    systemctl restart docker
else
    echo "All filesystems are already mounted. No need to restart Docker."
fi

echo "Script execution completed."
