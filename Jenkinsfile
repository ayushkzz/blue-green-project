pipeline {
    agent any

    environment {
        IMAGE_NAME = "blue-green-app"
        SCRIPTS_DIR = "/var/lib/jenkins/blue-green-project/scripts"
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
                sh 'bash ${SCRIPTS_DIR}/switch-to-green.sh'
            }
        }

    }

    post {
        success {
            echo '✅ Deployment successful! Green is now live.'
        }
        failure {
            echo '❌ Deployment failed! Rolling back to Blue...'
            sh 'bash ${SCRIPTS_DIR}/rollback-to-blue.sh'
        }
    }
}
