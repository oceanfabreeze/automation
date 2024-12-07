#authors= oceanfabreeze
#version= 1.0
#changelog = (12/07/2024) Initial Commit


import os
import subprocess
import getpass

def run_command(command, check=True):
    """Run a system command with error handling."""
    try:
        subprocess.run(command, shell=True, check=check, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except subprocess.CalledProcessError as e:
        print(f"Error executing command: {e}")
        raise

def create_user(username, password):
    """Create a user with the given username and password."""
    # Create the user with the specified username
    run_command(f"useradd {username}")

    # Set the password for the user
    run_command(f"echo '{username}:{password}' | chpasswd")

def add_user_to_wheel_group(username):
    """Add the user to the wheel group."""
    run_command(f"usermod -aG wheel {username}")

def modify_sudoers():
    """Modify the sudoers file to allow the wheel group to run commands without a password."""
    sudoers_path = "/etc/sudoers"
    
    # Ensure visudo is used to edit the sudoers file to avoid syntax errors
    sudoers_backup_path = "/etc/sudoers.bak"
    
    # Backup the sudoers file
    run_command(f"cp {sudoers_path} {sudoers_backup_path}")

    # Add the wheel group line for passwordless sudo access
    sudoers_content = ""
    with open(sudoers_path, "r") as f:
        sudoers_content = f.read()

    if "%wheel ALL=(ALL) NOPASSWD: ALL" not in sudoers_content:
        with open(sudoers_path, "a") as f:
            f.write("\n%wheel ALL=(ALL) NOPASSWD: ALL\n")
            print("Added '%wheel ALL=(ALL) NOPASSWD: ALL' to sudoers file")

def main():
    # Ensure the script is run as root or with sudo privileges
    if os.geteuid() != 0:
        print("This script must be run as root.")
        exit(1)

    # Get the password from the user running the script
    password = getpass.getpass("Enter the password for the ansible user: ")

    # Create the ansible user
    create_user("ansible", password)

    # Add the ansible user to the wheel group
    add_user_to_wheel_group("ansible")

    # Modify the sudoers file
    modify_sudoers()

    print("User 'ansible' created and configured successfully.")

if __name__ == "__main__":
    main()
