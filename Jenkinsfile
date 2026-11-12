pipeline {
    agent {
        label 'eventpro-windows-java17'
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
                    set "JAVA_HOME=C:\\Users\\Youcode\\.jdks\\temurin-17.0.20.1"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"

                    java -version
                    .\\mvnw.cmd -version
                '''
            }
        }
    }
}