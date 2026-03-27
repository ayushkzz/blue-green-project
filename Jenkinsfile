pipeline {
    agent any

    environment {
        IMAGE_NAME = "blue-green-app"
        SCRIPTS_DIR = "/var/lib/jenkins/blue-green-project/scripts"
        BLUE_PORT = "8081"
        GREEN_PORT = "8082"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                echo 'Building application with Maven...'
                // Added set -e to instantly fail the pipeline if the build fails
                sh 'set -e; mvn clean package -DskipTests=false -f ${WORKSPACE}/app/pom.xml'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh 'set -e; docker build -t ${IMAGE_NAME} -f ${WORKSPACE}/docker/Dockerfile ${WORKSPACE}'
            }
        }

        stage('Detect Environment') {
            steps {
                script {
                    echo 'Detecting which environment is currently active...'
                    def blueIsActive = sh(script: "curl -s http://localhost:${BLUE_PORT}/health | grep -q UP", returnStatus: true) == 0

                    if (blueIsActive) {
                        env.ACTIVE_ENV = "blue"
                        env.TARGET_ENV = "green"
                        env.TARGET_PORT = "${GREEN_PORT}"
                    } else {
                        env.ACTIVE_ENV = "green" 
                        env.TARGET_ENV = "blue"
                        env.TARGET_PORT = "${BLUE_PORT}"
                    }
                    echo "🟢 Active: ${env.ACTIVE_ENV} | 🚀 Deploying to: ${env.TARGET_ENV} on port ${env.TARGET_PORT}"
                }
            }
        }

        stage('Deploy to Target Environment') {
            steps {
                echo "Deploying to ${env.TARGET_ENV} environment..."
                sh """
                    set -e # Stops script execution immediately if a command fails
                    
                    docker stop ${env.TARGET_ENV}-container || true
                    docker rm ${env.TARGET_ENV}-container || true
                    
                    docker run -d --name ${env.TARGET_ENV}-container \
                        -p ${env.TARGET_PORT}:8080 \
                        -e DEPLOY_ENV=${env.TARGET_ENV} \
                        -e APP_VERSION="Build v${env.BUILD_NUMBER}" \
                        ${IMAGE_NAME}
                """
            }
        }

        stage('Smoke Test') {
            steps {
                echo "Running smoke test on ${env.TARGET_ENV} container..."
                sh """
                    set -e
                    sleep 10
                    curl -f http://localhost:${env.TARGET_PORT}/health
                """
            }
        }

        stage('Switch Traffic') {
            steps {
                echo "Switching Nginx traffic to ${env.TARGET_ENV}..."
                // Added set -e to catch any errors returning from your bash script
                sh "set -e; bash ${SCRIPTS_DIR}/switch-traffic.sh ${env.TARGET_PORT}"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful! ${env.TARGET_ENV} is now live."
            script {
                echo "Cleaning up old ${env.ACTIVE_ENV} container..."
                sh """
                    docker stop ${env.ACTIVE_ENV}-container || true
                    docker rm ${env.ACTIVE_ENV}-container || true
                """
            }
        }
        failure {
            script {
                // Safety check: If the build fails during Maven, TARGET_ENV doesn't exist yet!
                // This prevents the failure block itself from crashing.
                if (env.TARGET_ENV != null) {
                    echo "❌ Deployment failed! Nginx remains pointed at ${env.ACTIVE_ENV}."
                    sh """
                        docker stop ${env.TARGET_ENV}-container || true
                        docker rm ${env.TARGET_ENV}-container || true
                    """
                } else {
                    echo "❌ Build failed before deployment started."
                }
            }
        }
    }
}
