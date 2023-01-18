pipeline {
    agent any

    options {
        skipStagesAfterUnstable()
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timestamps()
    }

    tools {
        maven 'maven-3.8.6'
    }

    parameters {
        string(name: "image_name", defaultValue: 'java-discord-help-bot')
        string(name: "image_tag", defaultValue: 'latest')
        booleanParam(name: "push_image", defaultValue: true)
    }

    stages {

        stage('Build JAR & image') {
            steps {
                sh 'mvn -B -P docker clean package -DskipTests'
            }
        }

        stage("Push to Dockerhub") {
            when {
                equals expected: "true",
                actual: "${params.push_image}"
            }
            steps {
                script {
                    echo "Pushing the image to docker hub"
                    def repositoryName = "generaltao725/${params.image_name}"

                    docker.withRegistry("", "DockerHubCredentials") {
                        def image = docker.image(repositoryName);
                        try {
                            image.push("latest")
                            image.push("0.0.0")
                        } catch(Exception ex) {
                            println(ex);
                            image.push("latest")
                            image.push("0.0.0")
                        }
                    }

                    sh "docker rmi -f ${repositoryName} "
                }
            }
        }
    }
}