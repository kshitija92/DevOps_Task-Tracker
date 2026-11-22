pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-south-1'
        AWS_ACCOUNT_ID = '028231499843'
        ECR_REPOSITORY = 'task-tracker'
        S3_BUCKET = 'task-tracker-jenkins-artifacts-2026'
        GITHUB_REPO = 'https://github.com/kshitija92/DevOps_Task-Tracker.git'
    }

    stages {

        // 1. Checkout source code
        stage('Checkout') {
            steps {
                git branch: 'development',
                    url: 'https://github.com/kshitija92/DevOps_Task-Tracker.git'
            }
        }

        // 2. Maven build
        stage('Build') {
            steps {
                bat 'mvn -version'
                bat 'mvn clean package'
            }
        }

        // 3. Generate build-specific artifact and image version
        stage('Prepare Artifact') {
            steps {
                script {
                    def artifactId = powershell(
                        returnStdout: true,
                        script: '''
                            (& mvn -q help:evaluate "-Dexpression=project.artifactId" "-DforceStdout").Trim()
                        '''
                    ).trim()

                    def version = powershell(
                        returnStdout: true,
                        script: '''
                            (& mvn -q help:evaluate "-Dexpression=project.version" "-DforceStdout").Trim()
                        '''
                    ).trim()

                    env.APP_ARTIFACT_ID = artifactId
                    env.APP_VERSION = version
                    env.IMAGE_TAG = "${version}-build-${env.BUILD_NUMBER}"
                    env.ARTIFACT_NAME = "${artifactId}-${version}-build-${env.BUILD_NUMBER}.jar"

                    powershell '''
                        $originalJar = "target/$env:APP_ARTIFACT_ID-$env:APP_VERSION.jar"
                        $newJar = "target/$env:ARTIFACT_NAME"

                        if (-not (Test-Path $originalJar)) {
                            throw "Original JAR not found: $originalJar"
                        }

                        Copy-Item $originalJar $newJar -Force

                        if (-not (Test-Path $newJar)) {
                            throw "Versioned JAR not created"
                        }

                        Write-Host "Created artifact: $newJar"
                    '''

                    echo "Application version: ${env.APP_VERSION}"
                    echo "Docker image tag: ${env.IMAGE_TAG}"
                    echo "Artifact name: ${env.ARTIFACT_NAME}"
                }
            }
        }

        // 4. Archive artifact
        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: "target/${env.ARTIFACT_NAME}",
                                 fingerprint: true
            }
        }

        // 5. Upload JAR to S3
        stage('Upload to S3') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-access-key-id',
                           variable: 'AWS_ACCESS_KEY_ID'),

                    string(credentialsId: 'aws-secret-access-key',
                           variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    powershell '''
                        $artifactPath = "target/$env:ARTIFACT_NAME"

                        $destination = "s3://$env:S3_BUCKET/task-tracker/$env:ARTIFACT_NAME"

                        aws s3 cp "$artifactPath" "$destination" --region $env:AWS_REGION

                        if ($LASTEXITCODE -ne 0) {
                            throw "S3 upload failed"
                        }

                        Write-Host "S3 upload successful"
                    '''
                }
            }
        }

 // 6. Prepare Docker JAR
stage('Prepare Docker JAR') {
    steps {
        powershell '''
            Copy-Item "target/$env:ARTIFACT_NAME" "app.jar" -Force

            if (-not (Test-Path "app.jar")) {
                throw "Docker JAR preparation failed"
            }

            Write-Host "Docker JAR prepared successfully"
        '''
    }
}
        // 7. Build Docker Image
stage('Build Docker Image') {
    steps {
        script {
            env.LOCAL_IMAGE = "${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"
            env.ECR_IMAGE = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com/${env.ECR_REPOSITORY}:${env.IMAGE_TAG}"
        }

        powershell '''
            docker build -t "$env:LOCAL_IMAGE" .

            if ($LASTEXITCODE -ne 0) {
                throw "Docker build failed"
            }
        '''
    }
}

        // 7. Login and push image to ECR
        stage('Push Docker Image to ECR') {
            steps {
                withCredentials([
                    string(credentialsId: 'aws-access-key-id',
                           variable: 'AWS_ACCESS_KEY_ID'),

                    string(credentialsId: 'aws-secret-access-key',
                           variable: 'AWS_SECRET_ACCESS_KEY')
                ]) {
                    powershell '''
                        $registry = "$env:AWS_ACCOUNT_ID.dkr.ecr.$env:AWS_REGION.amazonaws.com"

                        aws ecr get-login-password --region $env:AWS_REGION |
                            docker login --username AWS --password-stdin $registry

                        if ($LASTEXITCODE -ne 0) {
                            throw "ECR login failed"
                        }

                        docker tag "$env:LOCAL_IMAGE" "$env:ECR_IMAGE"

                        if ($LASTEXITCODE -ne 0) {
                            throw "Docker tag failed"
                        }

                        docker push "$env:ECR_IMAGE"

                        if ($LASTEXITCODE -ne 0) {
                            throw "Docker push to ECR failed"
                        }

                        Write-Host "Image pushed successfully:"
                        Write-Host "$env:ECR_IMAGE"
                    '''
                }
            }
        }

        // 8. Automatically create and push Git tag
        stage('Create Git Tag') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'github-tag-token',
                        usernameVariable: 'GIT_USERNAME',
                        passwordVariable: 'GIT_TOKEN'
                    )
                ]) {
                    powershell '''
                        $tag = "v$env:IMAGE_TAG"

                        git config user.name "Jenkins"
                        git config user.email "jenkins@localhost"

                        git tag -a $tag -m "Jenkins build $env:BUILD_NUMBER - $env:APP_VERSION"

                        if ($LASTEXITCODE -ne 0) {
                            throw "Git tag creation failed"
                        }

                        $encoded = [Convert]::ToBase64String(
                            [Text.Encoding]::UTF8.GetBytes(
                                "$env:GIT_USERNAME`:$env:GIT_TOKEN"
                            )
                        )

                        $header = "AUTHORIZATION: basic $encoded"

                        git -c http.extraheader="$header" push origin $tag

                        if ($LASTEXITCODE -ne 0) {
                            throw "Git tag push failed"
                        }

                        Write-Host "Git tag pushed: $tag"
                    '''
                }
            }
        }
    }

    post {

        success {
            emailext(
                subject: "SUCCESS: Task Tracker Build ${BUILD_NUMBER}",
                body: """
                    <h2>Task Tracker Pipeline Successful</h2>
                    <p>Build Number: ${BUILD_NUMBER}</p>
                    <p>Application Version: ${APP_VERSION}</p>
                    <p>Artifact: ${ARTIFACT_NAME}</p>
                    <p>Docker Image: ${ECR_IMAGE}</p>
                    <p>Git Tag: v${IMAGE_TAG}</p>
                    <p>S3: s3://${S3_BUCKET}/task-tracker/${ARTIFACT_NAME}</p>
                    <p>Jenkins: ${BUILD_URL}</p>
                """,
                mimeType: 'text/html',
                to: 'kshitijagaikwad92@gmail.com'
            )
        }

        failure {
            emailext(
                subject: "FAILURE: Task Tracker Build ${BUILD_NUMBER}",
                body: """
                    <h2>Task Tracker Pipeline Failed</h2>
                    <p>Build Number: ${BUILD_NUMBER}</p>
                    <p>Check the Jenkins console output.</p>
                    <p>Jenkins: ${BUILD_URL}</p>
                """,
                mimeType: 'text/html',
                to: 'kshitijagaikwad92@gmail.com'
            )
        }

        always {
            echo 'Jenkins pipeline execution finished.'
        }
    }
}
