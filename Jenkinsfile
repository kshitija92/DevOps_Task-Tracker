pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'DevOps',
                    url: 'https://github.com/kshitija92/DevOps_Task-Tracker.git'
            }
        }

        stage('Build Maven') {
            steps {
                bat 'mvn clean package'
            }
        }

        
stage('Identify JAR and Version') {
    steps {
        script {
            def jarFile = powershell(
                returnStdout: true,
                script: '''
                    $jar = Get-ChildItem target\\*.jar |
                        Where-Object { $_.Name -notlike "*.original" } |
                        Sort-Object LastWriteTime -Descending |
                        Select-Object -First 1

                    if (-not $jar) {
                        throw "JAR file not found"
                    }

                    Write-Output $jar.Name
                '''
            ).trim()

            env.JAR_FILE = jarFile
            env.IMAGE_TAG = jarFile.replace('.jar', '')
            env.ECR_REPOSITORY = 'task-tracker'

            echo "JAR file: ${env.JAR_FILE}"
            echo "Docker image tag: ${env.IMAGE_TAG}"
        }
    }
}

        stage('Build Docker Image') {
            steps {
                script {
                    bat "docker build --build-arg JAR_FILE=target/${env.JAR_FILE} -t ${env.ECR_REPOSITORY}:${env.IMAGE_TAG} ."
                }
            }
        }

        stage('AWS ECR Login') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-access-key-id', variable: 'AWS_ACCESS_KEY_ID'),
                    string(credentialsId: 'aws-secret-access-key', variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    powershell '''
                        $env:AWS_DEFAULT_REGION = "ap-south-1"

                        aws ecr get-login-password --region $env:AWS_DEFAULT_REGION |
                            docker login --username AWS --password-stdin 028231499843.dkr.ecr.ap-south-1.amazonaws.com
                    '''
                }
            }
        }

        stage('Tag Docker Image') {
            steps {
                script {
                    bat "docker tag ${env.ECR_REPOSITORY}:${env.IMAGE_TAG} 028231499843.dkr.ecr.ap-south-1.amazonaws.com/${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"
                }
            }
        }

        stage('Push to ECR') {
            steps {
                bat "docker push 028231499843.dkr.ecr.ap-south-1.amazonaws.com/${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"
            }
        }

    }

    post {
        success {
            echo 'Build, Docker image creation and ECR push completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check the Jenkins console output.'
        }
    }
}