pipeline {
    agent any
    
    parameters {
        string(name: 'TARGET_SERVER', defaultValue: 'station.taffyhome.internal', description: 'Target Linux server')
        string(name: 'TARGET_USER', defaultValue: 'ansible', description: 'SSH username')
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
                deleteDir()  // Clean workspace
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/oceanfabreeze/automation.git']]
                ])
            }
        }
        
        stage('Check for YAML Changes') {
            steps {
                script {
                    // Only check for changes if this is NOT the first build and previous build was successful
                    if (currentBuild.previousBuild != null && currentBuild.previousBuild.result == 'SUCCESS') {
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
                    } else {
                        echo "First build or previous build failed - deploying all YAML files"
                    }
                }
            }
        }
        
        stage('Deploy YAML Files') {
            steps {
                script {
                    // Install rsync if not present
                    sh '''
                        if ! command -v rsync &> /dev/null; then
                            echo "Installing rsync..."
                            apt-get update && apt-get install -y rsync
                        fi
                    '''
                    
                    withCredentials([sshUserPrivateKey(credentialsId: env.SSH_CREDENTIALS_ID, keyFileVariable: 'SSH_KEY')]) {
                        sh """
                            # Create target directory if it doesn't exist
                            ssh -i \$SSH_KEY -o StrictHostKeyChecking=no ${params.TARGET_USER}@${params.TARGET_SERVER} "sudo mkdir -p ${params.TARGET_PATH}"
                            
                            # Sync YAML files to target server (rsync only copies changed files)
                            rsync -avz --delete --rsync-path="sudo rsync" -e "ssh -i \$SSH_KEY -o StrictHostKeyChecking=no" \
                                --include='*.yml' \
                                --include='*.yaml' \
                                --include='*/' \
                                --exclude='logs/' \
                                --exclude='*' \
                                ${params.YAML_SOURCE_DIR}/ \
                                ${params.TARGET_USER}@${params.TARGET_SERVER}:${params.TARGET_PATH}/
                            
                            echo "Deployment complete - only changed files were transferred"
                        """
                    }
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: env.SSH_CREDENTIALS_ID, keyFileVariable: 'SSH_KEY')]) {
                        sh """
                            echo "Files deployed to ${params.TARGET_SERVER}:${params.TARGET_PATH}"
                            ssh -i \$SSH_KEY -o StrictHostKeyChecking=no ${params.TARGET_USER}@${params.TARGET_SERVER} "ls -lah ${params.TARGET_PATH}"
                        """
                    }
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