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

        stage('Run automation tests') {
            script {

                withCredentials([string(credentialsId: 'discordAutomationTestToken', variable: 'TOKEN')]) {
                    sh "clean install \"-Ddiscord.token=${$TOKEN}\" -DskipTests=true"
                }

                // Read the content of a file
                def fileContent = readFile "${WORKSPACE}/target/docker/.env"

                // Split the content into lines
                def lines = fileContent.readLines()

                // Build the string with properties
                def mvnCommand = lines.collect { line ->
                    "\"-D${line.split('=')[0]}=${line.split('=')[1]}\""
                }.join(' ')

                // Append additional properties
                mvnCommand += " \"-Dtest=discord.automation.runners.CucumberRunnerIT\""

                sh "mvn ${mvnCommand} test"
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