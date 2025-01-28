# media-filesystem-check-xxxx.sh

### How-to run the script:

Add the script to run after boot using the @reboot flag in cron.

### Script Explained:

This script checks if the correct media filesystems are mounted at system boot. If not, it attempts to mount them, and then restarts the docker service. 