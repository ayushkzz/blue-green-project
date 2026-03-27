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
                sh 'mvn clean package -DskipTests=false -f ${WORKSPACE}/app/pom.xml'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh 'docker build -t ${IMAGE_NAME} -f ${WORKSPACE}/docker/Dockerfile ${WORKSPACE}'
            }
        }

        stage('Detect Environment') {
            steps {
                script {
                    echo 'Detecting active environment...'
                    def blueActive = sh(script: "curl -s http://localhost:${BLUE_PORT}/health | grep -q UP", returnStatus: true) == 0

                    if (blueActive) {
                        env.ACTIVE_ENV = "blue"
                        env.TARGET_ENV = "green"
                        env.TARGET_PORT = "${GREEN_PORT}"
                    } else {
                        env.ACTIVE_ENV = "green"
                        env.TARGET_ENV = "blue"
                        env.TARGET_PORT = "${BLUE_PORT}"
                    }
                    echo "Active: ${env.ACTIVE_ENV} → Deploying to: ${env.TARGET_ENV} on port ${env.TARGET_PORT}"
                }
            }
        }

        stage('Deploy') {
            steps {
                sh """
                    docker stop ${env.TARGET_ENV}-container || true
                    docker rm ${env.TARGET_ENV}-container || true
                    docker run -d --name ${env.TARGET_ENV}-container \
                        -p ${env.TARGET_PORT}:8080 \
                        -e DEPLOY_ENV=${env.TARGET_ENV} \
                        ${IMAGE_NAME}
                """
            }
        }

        stage('Smoke Test') {
            steps {
                sh """
                    sleep 10
                    curl -f http://localhost:${env.TARGET_PORT}/health || exit 1
                """
            }
        }

        stage('Switch Traffic') {
            steps {
                sh "bash ${SCRIPTS_DIR}/switch-traffic.sh ${env.TARGET_PORT}"
            }
        }
    }

    post {
        success {
            echo "✅ Deployment successful! ${env.TARGET_ENV} is now live."
            sh """
                docker stop ${env.ACTIVE_ENV}-container || true
                docker rm ${env.ACTIVE_ENV}-container || true
            """
        }
        failure {
            script {
                if (env.TARGET_ENV != null) {
                    echo "❌ Deployment failed! Rolling back..."
                    sh """
                        docker stop ${env.TARGET_ENV}-container || true
                        docker rm ${env.TARGET_ENV}-container || true
                    """
                } else {
                    echo "❌ Build failed before deployment."
                }
            }
        }
    }
}
