import hudson.FilePath

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

//        stage('Build JAR & image') {
//            steps {
//                sh 'mvn -B -P docker clean package -DskipTests'
//            }
//        }

//        stage('Run automation tests') {
//            steps {
//                script {

//                    withCredentials([string(credentialsId: 'javaDiscordHelpBotAutomationTestToken', variable: 'TOKEN')]) {
//                        sh "mvn -P automation clean install \"-Ddiscord.token=$TOKEN\" -DskipTests=true"
//                    }

//                    // Read the content of a file
//                    def fileContent = readFile "${WORKSPACE}/target/docker/.env"
//
//                    // Split the content into lines
//                    def lines = fileContent.readLines()
//
//                    // Build the string with properties
//                    def mvnCommand = lines.collect { line ->
//                        "\"-D${line.split('=')[0]}=${line.split('=')[1]}\""
//                    }.join(' ')
//
//                    // Append additional properties
//                    mvnCommand += " \"-Dtest=discord.automation.runners.CucumberRunnerIT\""
//
//                    withCredentials([usernamePassword(
//                            credentialsId: 'generalTaoDockerHub',
//                            usernameVariable: 'USERNAME',
//                            passwordVariable: 'PASSWORD')]) {
//                        mvnCommand += " \"-Dregistry.username=$USERNAME\" \"-Dregistry.password=$PASSWORD\""
//                    }
//
//                    docker.withRegistry("", "generalTaoDockerHub") {
//                        sh "mvn ${mvnCommand} test"
//                    }
//                }
//            }
//        }

        stage("Show compose logs") {
            steps {
                script {
                    // Define the log folder path
                    def logFolderPath = "${WORKSPACE}/at-compose-logs"

                    // Get a list of log files in the folder
                    def logFiles = findLogFiles(logFolderPath)

                    // Sort log files by name (which includes date) in ascending order
                    logFiles.sort { a, b -> a.name <=> b.name }

                    if (!logFiles.empty) {
                        // Get the most recent log file
                        def latestLogFile = logFiles.last()

                        // Read and log content of the file line by line
                        latestLogFile.withReader { reader ->
                            reader.eachLine { line ->
                                echo line
                            }
                        }
                    } else {
                        echo "No log files found in ${logFolderPath}."
                    }
                }
            }
        }

//        stage("Push to Dockerhub") {
//            when {
//                equals expected: "true",
//                actual: "${params.push_image}"
//            }
//            steps {
//                script {
//                    echo "Pushing the image to docker hub"
//                    def repositoryName = "generaltao725/${params.image_name}"
//
//                    docker.withRegistry("", "generalTaoDockerHub") {
//                        def image = docker.image(repositoryName);
//                        try {
//                            image.push("latest")
//                            image.push("0.0.0")
//                        } catch(Exception ex) {
//                            println(ex);
//                            image.push("latest")
//                            image.push("0.0.0")
//                        }
//                    }
//
//                    sh "docker rmi -f ${repositoryName} "
//                }
//            }
//        }
    }
}

def findLogFiles(folderPath) {
    def files = []
    def folder = new FilePath(new File(folderPath))
    folder.list().each { file ->
        if (file.name.endsWith('.log')) {
            files.add(file)
        }
    }
    return files
}