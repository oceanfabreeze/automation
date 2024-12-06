# automation
Linux automation for my homelab. 

## .github/workflows
Houses configuration for github actions. Currently Ansible Lint is the only checks I have run on this repository. It ensures ansible playbooks are written correctly before committing new code. 

## ansible
This houses inventory files for hosts, playbooks, and support files for scripts/playbooks.

## docker
Hosts docker-compose files for now. Not really used as i'm no longer contanerizing anything in the lab but keeping here for future. 

## .ansible-lint.yml
Config for the Ansible Linter workflow defined in Github Actions. Ignoring a few things that I don't care about in a lab setting. 
