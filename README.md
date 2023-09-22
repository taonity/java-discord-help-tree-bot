## java-discord-help-tree-bot
A bot that answers questions using discord SelectMenu

### Features
- Can be invited to any guild
- Support two languages: English and Russian
- Easy configurable questions and answers in YAML format
- YAML file management using Gitea 
- Online question tree configuration file updating
- Error logging
- Supports backups
- Supports CI

### Commands
- `/question` - start question asking session using SelectMenu elements
- `/channelrole` - give a logging or helping role to the current channel
- `/config` - get credentials to access the question tree configuration file

### Docker compose project
The application can be deployed as a docker-compose project. It consists of the following services:
- `app` - the java application that runs the discord bot
- `gitea` - Gitea service that plays a UI role in providing access to the configuration file
- `db` - postgres database that holds guild settings
- `flyway` - a tool that runs migrations on the database
- `make-backup` - [offen/docker-volume-backup](https://github.com/offen/docker-volume-backup) tool to make backups
- `restore-backup` - [generaltao725/command-runner](https://github.com/taonity/command-runner) tool to run backup restoring scripts

### Prerequirements 
You can easily run this project on your local machine or server. All you need is a registered [discord application](https://discord.com/developers/applications) and some knowledge of Java, Docker, and Discord.
After you create the app you have to toggle on Server Member Intent and get an invite link with permission code 11005979776 so you can invite your bot in your guild. 

### Build
As a first part of the build process you have to build the app image:
```bash
mvn -P docker clean package -DskipTests
```
After that, you have to build the docker-compose project that will be placed in `target/docker/test` directory:
```bash
mvn -P automation clean package "-Ddiscord.token=<token>" -DskipTests=true
```
###Run
You can run the app both on Linux and Windows
#### Using IntelliJ IDEA (fast)
You can run all depending services in the docker compose project:
```bash
docker compose -f target/docker/test/docker-compose-test.yml up gitea db flyway -d
```
and the app apart, using IntelliJ. Don't forget to load env var JAVA_DISCORD_HELP_BOT_DISCORD_TOKEN with the token in Run/Debug Configurations.

#### All using docker compose (easy)
```bash
docker compose -f target/docker/test/docker-compose-test.yml up -d
```

### Test
The test is basic and checks if the application starts properly and backup works as expected. It uses testcontainers. To run a test run:
```bash
mvn -Dtest=discord.automation.runners.CucumberRunnerIT test
```

### Deploy
The app can be deployed manually or using [generaltao725/docker-webhook](https://github.com/taonity/docker-webhook) tool. 

### Backup
The backup can be scheduled using docker-volume-backup configurations. There are some manual functionality that are provided by scripts:
 - `target/docker/test/backup/make/scripts/make.sh` - make a backup manually
 - `target/docker/test/backup/restore/scripts/ls.sh` - list all backups
 - `target/docker/test/backup/restore/scripts/restore.sh <backup_name>` - restore a backup

### Question tree configuration file
After you join a first guild you will be able to access the question configuration file in Gtiea by admin account or as a user.
One node represents a question or answer. All leaves of the tree are answers. All other nodes are questions.
A question has the following format:
```
text:
  en: "question in english1"
  ru: "question in russian1"
childText:
  - text:
      en: "question in english2"
      ru: "question in russian2"
  - text:
      en: "question in english3"
      ru: "question in russian3"
   ...
```
An answer:
```
text:
  en: "answer in english1"
  ru: "answer in russian1"
func: "function"
targetId: "idOfdiscordUser"
```
An answer has two functions:
- `return_text` - just return the answer text
- `ask_question` - wait for the user message, then reply to it attaching mention with `idOfdiscordUser`.


### Example of usage
https://user-images.githubusercontent.com/42372666/152680156-ac49522a-1a7c-4c7d-882b-5bcff5bb71a6.mp4

