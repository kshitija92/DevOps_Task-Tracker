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
                    url: "${env.GITHUB_REPO}"
            }
        }

        // 2. Maven build
        stage('Build') {
            steps {
                sh 'mvn -version'
                sh 'mvn clean package'
            }
        }

        // 3. Prepare build-specific artifact and Docker version
        stage('Prepare Artifact') {
            steps {
                script {
                    def artifactId = sh(
                        returnStdout: true,
                        script: 'mvn -q help:evaluate -Dexpression=project.artifactId -DforceStdout'
                    ).trim()

                    def version = sh(
                        returnStdout: true,
                        script: 'mvn -q help:evaluate -Dexpression=project.version -DforceStdout'
                    ).trim()

                    env.APP_ARTIFACT_ID = artifactId
                    env.APP_VERSION = version
                    env.IMAGE_TAG = "${version}-build-${env.BUILD_NUMBER}"
                    env.ARTIFACT_NAME = "${artifactId}-${version}-build-${env.BUILD_NUMBER}.jar"

                    sh '''
                        originalJar="target/${APP_ARTIFACT_ID}-${APP_VERSION}.jar"
                        newJar="target/${ARTIFACT_NAME}"

                        if [ ! -f "$originalJar" ]; then
                            echo "Original JAR not found: $originalJar"
                            exit 1
                        fi

                        cp "$originalJar" "$newJar"

                        if [ ! -f "$newJar" ]; then
                            echo "Versioned JAR creation failed"
                            exit 1
                        fi

                        echo "Created artifact: $newJar"
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

        // 5. Upload JAR to S3 using EC2 IAM role
        stage('Upload to S3') {
            steps {
                sh '''
                    artifactPath="target/${ARTIFACT_NAME}"
                    destination="s3://${S3_BUCKET}/task-tracker/${ARTIFACT_NAME}"

                    aws s3 cp "$artifactPath" "$destination" --region "$AWS_REGION"

                    echo "S3 upload successful"
                '''
            }
        }

        // 6. Prepare Docker JAR
        stage('Prepare Docker JAR') {
            steps {
                sh '''
                    cp "target/${ARTIFACT_NAME}" app.jar

                    if [ ! -f app.jar ]; then
                        echo "Docker JAR preparation failed"
                        exit 1
                    fi

                    echo "Docker JAR prepared successfully"
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

                sh '''
                    docker build -t "$LOCAL_IMAGE" .

                    echo "Docker image built: $LOCAL_IMAGE"
                '''
            }
        }

        // 8. Login to ECR using EC2 IAM role
        stage('Login to ECR') {
            steps {
                sh '''
                    registry="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

                    aws ecr get-login-password --region "$AWS_REGION" |
                    docker login "$registry" --username AWS --password-stdin

                    echo "ECR login successful"
                '''
            }
        }

        // 9. Tag Docker Image for ECR
        stage('Tag Docker Image') {
            steps {
                sh '''
                    docker tag "$LOCAL_IMAGE" "$ECR_IMAGE"

                    echo "ECR image tag: $ECR_IMAGE"
                '''
            }
        }

        // 10. Push Docker Image to ECR
        stage('Push Docker Image to ECR') {
            steps {
                sh '''
                    docker push "$ECR_IMAGE"

                    echo "Docker image pushed successfully"
                '''
            }
        }

        // 11. Create and push Git tag
        stage('Create Git Tag') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'github-tag-token',
                        usernameVariable: 'GIT_USERNAME',
                        passwordVariable: 'GIT_TOKEN'
                    )
                ]) {
                    sh '''
                        set -e

                        tag="v${IMAGE_TAG}"

                        git config user.name "Jenkins"
                        git config user.email "jenkins@localhost"

                        git tag -a "$tag" -m "Jenkins build ${BUILD_NUMBER} - ${APP_VERSION}"

                        encoded=$(printf '%s' "${GIT_USERNAME}:${GIT_TOKEN}" | base64 | tr -d '\\n')

                        git -c http.extraheader="AUTHORIZATION: basic ${encoded}" push origin "$tag"

                        echo "Git tag pushed: $tag"
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