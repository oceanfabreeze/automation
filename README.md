# automation
Linux automation for my homelab. 

## .github/workflows
Houses configuration for github actions. Currently Ansible Lint is the only checks I have run on this repository. It ensures ansible playbooks are written correctly before committing new code. 

## ansible
This houses inventory files for hosts, playbooks, and support files for scripts/playbooks.

## docker
Hosts docker-compose files.

## standalone-scripts
Standalone Scripts written in Shell and Python to be run independantly from an automation solution.

## .ansible-lint.yml
Config for the Ansible Linter workflow defined in Github Actions. Ignoring a few things that I don't care about in a lab setting. 
