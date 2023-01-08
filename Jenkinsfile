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
        string(
            name: "Branch_Name",
            defaultValue: 'master',
            description: '')

        string(
            name: "Image_Name",
            defaultValue: 'java-discord-help-bot',
            description: '')

        string(
            name: "Image_Tag",
            defaultValue: 'latest',
            description: 'Image tag')

        booleanParam(
           name: "PushImage",
           defaultValue: true)
    }

    stages {

        stage('Build JAR file') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage("Build docker images") {
            steps {
                script {
                    echo "Build docker images"
                        def buildArgs = """\
                            -f Dockerfile \
                            ."""
                        def image = docker.build(
                            "${params.Image_Name}:${params.Image_Tag}",
                            buildArgs)
                }
            }
        }

        stage("Push to Dockerhub") {
            when {
                equals expected: "true",
                actual: "${params.PushImage}"
            }
            steps {
                script {
                    echo "Pushing the image to docker hub"
                    def localImage = "${params.Image_Name}:${params.Image_Tag}"
                    def repositoryName = "generaltao725/${localImage}"

                    sh "docker tag ${localImage} ${repositoryName} "

                    docker.withRegistry("", "DockerHubCredentials") {
                        def image = docker.image("${repositoryName}");
                        try {
                            image.push()
                        } catch(Exception ex) {
                            println(ex);
                            image.push()
                        }
                    }

                    sh "docker rmi -f ${localImage} "
                    sh "docker rmi -f ${repositoryName} "
                }
            }
        }
    }
}