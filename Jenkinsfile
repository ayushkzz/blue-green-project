pipeline {
    agent any

    environment {
        IMAGE_NAME = "blue-green-app"
        APP_DIR = "/home/ayushkzz/blue-green-project"
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
                sh 'cd ${APP_DIR}/app && mvn clean package -DskipTests=false'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh 'cd ${APP_DIR} && docker build -t ${IMAGE_NAME} -f docker/Dockerfile .'
            }
        }

        stage('Deploy to Green') {
            steps {
                echo 'Deploying to Green environment...'
                sh '''
                    docker stop green-container || true
                    docker rm green-container || true
                    docker run -d --name green-container -p 8082:8080 ${IMAGE_NAME}
                '''
            }
        }

        stage('Smoke Test') {
            steps {
                echo 'Running smoke test on Green...'
                sh '''
                    sleep 10
                    curl -f http://localhost:8082/health || exit 1
                '''
            }
        }

        stage('Switch Traffic to Green') {
            steps {
                echo 'Switching Nginx traffic to Green...'
                sh '${APP_DIR}/scripts/switch-to-green.sh'
            }
        }

    }

    post {
        success {
            echo '✅ Deployment successful! Green is now live.'
        }
        failure {
            echo '❌ Deployment failed! Rolling back to Blue...'
            sh '${APP_DIR}/scripts/rollback-to-blue.sh'
        }
    }
}
