
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

        stage('Build') {
            steps {
                bat '''
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    .\\mvnw.cmd clean compile
                '''
            }
        }

        stage('Tests') {
            steps {
                bat '''
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    .\\mvnw.cmd test
                '''
            }
        }
    }
}
