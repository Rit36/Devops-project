pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/Rit36/Devops-project.git'
        GIT_BRANCH = 'main'
        IMAGE_NAME = 'Ashutosh/smart-order'
        PROJECT_DIR = "/var/lib/jenkins/workspace/smart-order"
        SONARQUBE_TOKEN = credentials('sonarqube')
    }

    stages {
        stage('Clone GitHub Repo') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO}", credentialsId: 'git_credentials'
            }
        }

        stage('Fix Permissions After Checkout') {
            steps {
                sh """
                    sudo chmod -R 777 ${PROJECT_DIR} || true
                    sudo chown -R jenkins:jenkins ${PROJECT_DIR} || true
                """
            }
        }
        stage('Maven Build') {
            steps {
                dir("${PROJECT_DIR}") {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('SonarQube Analysis') {
    steps {
        script {
            def scannerHome = tool 'SonarQube Scanner'
            withSonarQubeEnv('SonarQube Server') {
                sh """
                    ${scannerHome}/bin/sonar-scanner \
                    -Dsonar.projectKey=smart-order-springboot \
                    -Dsonar.sources=. \
                    -Dsonar.java.binaries=target/classes \
                    -Dsonar.host.url=http://18.207.188.199:9000 \
                    -Dsonar.token=${SONARQUBE_TOKEN}
                """
            }
        }
    }
}
        stage('Docker Build') {
            steps {
                script {
                    def imageTag = "${env.BUILD_NUMBER}-${new Date().format('yyyyMMddHHmmss')}"
                    env.IMAGE_TAG = imageTag
                    sh """
                        docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                        docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                    """
                }
            }
        }

        stage('Scan with Trivy') {
            steps {
                sh """
                    trivy image --severity CRITICAL,HIGH ${IMAGE_NAME}:${IMAGE_TAG} > trivy-report.txt || true
                    cat trivy-report.txt
                """
                archiveArtifacts artifacts: 'trivy-report.txt', fingerprint: true
            }
        }

        stage('Push to DockerHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${IMAGE_NAME}:latest
                    """
                }
            }
        }

        stage('Commit and Push Updated Manifest to GitHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'git_credentials', usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASSWORD')]) {
                    sh """
                        cd ${PROJECT_DIR}
                        git config user.email "jenkins@finloge.com"
                        git config user.name "Jenkins CI"

                        sed -i 's|image: ${IMAGE_NAME}:.*|image: ${IMAGE_NAME}:${IMAGE_TAG}|' k8s/app-deployment.yaml

                        git add k8s/app-deployment.yaml
                        git diff --cached --quiet k8s/app-deployment.yaml || git commit -m "Update image tag to ${IMAGE_NAME}:${IMAGE_TAG}"
                        git push https://${GIT_USER}:${GIT_PASSWORD}@github.com/Soumyajit-Rout/smart-oder-springboot-java.git ${GIT_BRANCH}
                    """
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build and push successful: ${IMAGE_NAME}:${IMAGE_TAG}"
        }
        failure {
            echo "❌ Pipeline failed."
        }
    }
}
