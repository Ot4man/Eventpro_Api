pipeline {
    agent {
        label 'eventpro-windows-java17'
    }

    environment {
        JAVA_HOME = 'C:\\Users\\Youcode\\.jdks\\temurin-17.0.20.1'
    }

    stages {

        stage('Checkout Verification') {
            steps {
                echo 'EventPro repository connected successfully!'
                echo 'Running on Windows Jenkins Agent.'
            }
        }

        stage('Verify Java and Maven') {
            steps {
                bat '''
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    java -version
                    .\\mvnw.cmd -version
                '''
            }
        }

        stage('Build & Test with JaCoCo') {
            steps {
                bat '''
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    .\\mvnw.cmd clean verify
                '''
            }
        }

        stage('Archive JaCoCo Reports') {
            steps {
                archiveArtifacts artifacts: 'target/site/jacoco/**',
                                 allowEmptyArchive: false
            }
        }
        stage('Publish JaCoCo Report') {
            steps {
                publishHTML(target: [
                    reportDir: 'target/site/jacoco',
                    reportFiles: 'index.html',
                    reportName: 'JaCoCo Coverage Report',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ])
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}