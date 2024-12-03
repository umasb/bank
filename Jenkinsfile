pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven '3.6.3'
    }

    environment {
        SONARQUBE_SERVER = 'sonar'  // Jenkins SonarQube server name
        SCANNER_HOME = tool 'sonar-scanner'
        TRIVY_IMAGE = 'aquasec/trivy:latest'  // Trivy Docker image
        KUBE_CONFIG_PATH = credentials('k8-secret')
        NAMESPACE = 'bank'
        IMAGE_NAME = 'payment-gateway'  // Image name
        IMAGE_TAG = 'latest'  // Tag
    }

    stages {
        stage('Clone Repository') {
            steps {
                echo 'Cloning the repository...'
                script {
                    try {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: '*/main']],
                            doGenerateSubmoduleConfigurations: false,
                            extensions: [],
                            userRemoteConfigs: [[
                                url: 'https://github.com/umasb/bank.git',
                                credentialsId: 'git-token'
                            ]]
                        ])

                        // Debug: List files after cloning
                        sh 'ls -l'
                    } catch (Exception e) {
                        echo "Error while cloning the repository: ${e.getMessage()}"
                        error("Stopping pipeline due to repository clone failure")
                    }
                }
            }
        }

        stage('Print Java File Path') {
            steps {
                script {
                    // Specify the file name you're looking for
                    def javaFileName = 'src/main/java/com/example/BankApplication.java'
                    
                    // Check if the file exists and print the path
                    if (fileExists(javaFileName)) {
                        echo "The Java file is located at: ${pwd()}/${javaFileName}"
                    } else {
                        echo "The specified Java file does not exist."
                    }
                }
            }
        }

        stage('Build the source code') {
            steps {
                sh 'mvn clean install'
                echo "Build is done"
            }
        }

        stage('Publish artifact to Nexus') {
            steps {
                withMaven(globalMavenSettingsConfig: 'global-settings', jdk: 'jdk17', maven: 'maven3', mavenSettingsConfig: 'custom-settings') {
                    sh 'mvn deploy'
                    echo "Deployment is done"
                }
            }
        }

        stage('Code Analysis with SonarQube') {
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv(SONARQUBE_SERVER) {
                    script {
                        try {
                            // Debug: Print Java version and Maven version
                            sh 'java -version'
                            sh 'mvn -v'

                            // Run SonarQube analysis
                            sh '''$SCANNER_HOME/bin/sonar-scanner -Dsonar.projectName=payment-gateway \
                                   -Dsonar.projectKey=payment-gateway -Dsonar.java.binaries=.'''

                            // Debug: Check if the sonar analysis results are available
                            sh 'ls -l target/sonar-report'
                        } catch (Exception e) {
                            echo "Error during SonarQube analysis: ${e.getMessage()}"
                            error("Stopping pipeline due to SonarQube analysis failure")
                        }
                    }
                }
            }
        }

        stage('Quality gate') {
            steps {
                script {
                    waitForQualityGate abortPipeline: false, credentialsId: 'sonar-token'
                }
            }
        }

        stage('Push Image to DockerHub Registry') {
            steps {
                echo 'Pushing Docker image to DockerHub Container Registry...'
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials-id', usernameVariable: 'DKR_USER', passwordVariable: 'DKR_PASS')]) {
                    script {
                        try {
                            // Docker login to DockerHub registry
                            sh """
                                echo ${DKR_PASS} | docker login -u ${DKR_USER} --password-stdin
                            """

                            // Debug: List Docker images before tagging
                            sh 'docker images'

                            // Tag and push image to DockerHub registry
                            sh """
                                docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DKR_USER}/${IMAGE_NAME}:${IMAGE_TAG}
                                docker push ${DKR_USER}/${IMAGE_NAME}:${IMAGE_TAG}
                            """

                            // Debug: List images after push to verify
                            sh 'docker images'
                        } catch (Exception e) {
                            echo "Error pushing Docker image to DockerHub: ${e.getMessage()}"
                            error("Stopping pipeline due to DockerHub push failure")
                        }
                    }
                }
            }
        }

        stage('Scan Image with Trivy') {
            steps {
                echo 'Scanning Docker image with Trivy...'
                script {
                    try {
                        // Debug: Pull the Trivy image to ensure it's available
                        sh 'docker pull ${TRIVY_IMAGE}'

                        // Run Trivy vulnerability scan
                        sh """
                            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock ${TRIVY_IMAGE} image --severity HIGH,CRITICAL ${IMAGE_NAME}:${IMAGE_TAG}
                        """
                        
                        // Debug: Show any available Trivy reports/logs
                        sh 'cat /var/log/trivy.log || echo "No Trivy log file found"'
                    } catch (Exception e) {
                        echo "Error during Trivy scan: ${e.getMessage()}"
                        error("Stopping pipeline due to Trivy scan failure")
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying application to Kubernetes...'
                script {
                    try {
                        // Switch to correct Kubernetes project
                        sh """
                            export KUBECONFIG=${KUBE_CONFIG_PATH}
                        """

                        // Apply the deployment configuration
                        sh 'kubectl apply -f deployment-service.yaml'

                        // Debug: Get the list of deployments and pods
                        sh 'kubectl get deployments -n bank'
                        sh 'kubectl get pods -n bank'
                        sh 'kubectl get svc -n bank'

                        // Debug: Check logs of the deployed pods (use the first pod in the list)
                        sh 'kubectl logs $(kubectl get pods --selector=app=payment-gateway -o jsonpath={.items[0].metadata.name})'
                    } catch (Exception e) {
                        echo "Error deploying to kubectl: ${e.getMessage()}"
                        error("Stopping pipeline due to kubectl deployment failure")
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                def jobName = env.JOB_NAME
                def buildNumber = env.BUILD_NUMBER
                def pipelineStatus = currentBuild.result ?: 'UNKNOWN'
                def bannerColor = pipelineStatus.toUpperCase() == 'SUCCESS' ? 'green' : 'red'

                def body = """
                    <html>
                    <body>
                    <div style="border: 4px solid ${bannerColor}; padding: 10px;">
                    <h2>${jobName} - Build ${buildNumber}</h2>
                    <div style="background-color: ${bannerColor}; padding: 10px;">
                    <h3 style="color: white;">Pipeline Status: ${pipelineStatus.toUpperCase()}</h3>
                    </div>
                    <p>Check the <a href="${BUILD_URL}">console output</a>.</p>
                    </div>
                    </body>
                    </html>
                """

                emailext(
                    subject: "${jobName} - Build ${buildNumber} - ${pipelineStatus.toUpperCase()}",
                    body: body,
                    to: 'umabalasubramanian72@gmail.com',
                    from: 'jenkins@example.com',
                    replyTo: 'jenkins@example.com',
                    mimeType: 'text/html',
                    attachmentsPattern: 'trivy.log'
                )
            }
        }
    }
}
