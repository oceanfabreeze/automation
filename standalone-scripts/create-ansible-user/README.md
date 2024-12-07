# create_ansible_user.py

### How-to run the script:

`sudo python3 create_ansible_user.py` 

This will prompt for the password of the  `ansible`  user, create the user, add them to the  `wheel`  group, and modify the sudoers file.


### Functions Explained:

1.  **run_command**: A helper function to run shell commands and handle errors.
2.  **create_user**: Creates the user  `ansible`  and sets the provided password.
3.  **add_user_to_wheel_group**: Adds the  `ansible`  user to the  `wheel`  group, which is used for sudo access.
4.  **modify_sudoers**: Ensures that the sudoers file has the line  `%wheel ALL=(ALL) NOPASSWD: ALL`, which allows members of the wheel group to run sudo commands without a password prompt.
5.  **main**: The main function that runs the above logic after checking if the script is run as root (or with sudo).

### Notes:

-   This script assumes that  `/etc/sudoers`  is configured in a standard way (i.e., the  `%wheel`  group is allowed to use  `sudo`).
-   The script creates a backup of  `/etc/sudoers`  as  `/etc/sudoers.bak`  before making any changes to protect against errors that might corrupt the sudoers file.