pipeline {
    agent any
    
    parameters {
        string(name: 'TARGET_SERVER', defaultValue: 'ansible@station.taffyhome.internal', description: 'Target Linux server')
        string(name: 'TARGET_PATH', defaultValue: '/etc/apps/homepage', description: 'Destination path on target server')
        string(name: 'YAML_SOURCE_DIR', defaultValue: 'jenkins/homepage', description: 'Source directory in repo containing YAML files')
    }
    
    environment {
        SSH_CREDENTIALS_ID = 'linux-server-ssh-key'  // Jenkins credential ID
    }
    
    triggers {
        // Poll every 5 minutes for changes - only triggers build if changes detected
        pollSCM('H/5 * * * *')
    }
    
    options {
        // Only keep last 10 builds to save space
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Prevent concurrent builds
        disableConcurrentBuilds()
        // Timeout after 10 minutes
        timeout(time: 10, unit: 'MINUTES')
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/oceanfabreeze/automation.git'
            }
        }
        
        stage('Check for YAML Changes') {
            steps {
                script {
                    // Check if any YAML files changed in this commit
                    def changedFiles = sh(
                        script: "git diff --name-only HEAD~1 HEAD | grep -E '${params.YAML_SOURCE_DIR}/.*\\.(yml|yaml)\$' || true",
                        returnStdout: true
                    ).trim()
                    
                    if (changedFiles == '') {
                        echo "No YAML files changed in ${params.YAML_SOURCE_DIR}, skipping deployment"
                        currentBuild.result = 'SUCCESS'
                        error('No YAML changes detected - stopping pipeline')
                    } else {
                        echo "YAML files changed:\n${changedFiles}"
                    }
                }
            }
        }
        
        stage('Deploy YAML Files') {
            steps {
                sshagent(credentials: [env.SSH_CREDENTIALS_ID]) {
                    sh """
                        # Create target directory if it doesn't exist
                        ssh -o StrictHostKeyChecking=no ${params.TARGET_SERVER} "mkdir -p ${params.TARGET_PATH}"
                        
                        # Sync YAML files to target server (rsync only copies changed files)
                        rsync -avz --delete \
                            --include='*.yml' \
                            --include='*.yaml' \
                            --include='*/' \
                            --exclude='*' \
                            ${params.YAML_SOURCE_DIR}/ \
                            ${params.TARGET_SERVER}:${params.TARGET_PATH}/
                        
                        echo "Deployment complete - only changed files were transferred"
                    """
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                sshagent(credentials: [env.SSH_CREDENTIALS_ID]) {
                    sh """
                        echo "Files deployed to ${params.TARGET_SERVER}:${params.TARGET_PATH}"
                        ssh -o StrictHostKeyChecking=no ${params.TARGET_SERVER} "ls -lah ${params.TARGET_PATH}"
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo "YAML files successfully deployed to ${params.TARGET_SERVER}:${params.TARGET_PATH}"
        }
        failure {
            echo "Deployment failed. Check logs for details."
        }
        aborted {
            echo "Pipeline aborted - no YAML changes detected"
        }
    }
}