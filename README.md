## java-discord-help-tree-bot
A bot that answers questions, configured like a tree

## Features
- Can be invited to any guild
- Support two languages: English and Russian
- Easy configurable questions and answers in YAML format
- YAML file management using Gitea 
- Online question tree configuration file updating
- Error logging
- Supports backups
- Supports CI

## Commands
- `/question` - start question asking session using SelectMenu elements
- `/channelrole` - give a logging or helping role to the current channel
- `/config` - get credentials to access question tree configuration file

## Docker compose project
The application can be deployed as a docker-compose project. It consists of following services:
- `app` - the java application that runs the discord bot
- `gitea` - Gitea service that plays a UI role to provide access to configuration file
- `db` - postgres database that holds guild settings
- `flyway` - a tool that runs migrations on the database
- `make-backup` - [offen/docker-volume-backup](https://github.com/offen/docker-volume-backup) tool to make backups
- `restore-backup` - [generaltao725/command-runner](https://github.com/taonity/command-runner) tool to run backup restoring scripts

## Build
Projecte wrote on Java 11.
Just open it with Intellij IDEA, download maven dependecies and build. 
For `jar` executable building run `mvn clean install` in command prompt.

## Run
For Intellij IDEA modify run configurations, adding setting configuration file as argument. For `jar` executable file run:
```
java -jar executable.jar <token> <path/to/setting_configurations>
```
Make sure you have Java 11 JRE installed.

## Question tree configuration file
The question tree configuration file can be found in repo by path `/src/main/resources/help_tree.yaml`.
One node represents question or answer. All leaves of the tree are answers. All other nodes are questions.
A question has the folowing format:
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
- `ask_question` - wait for user message, then reply on it attaching mention with `idOfdiscordUser`.

## Setting configuration file
This file has following structure:
```
guildId: <guildId>                                # The guild id where bot wil be used
channelId: <channelId>                            # The channel id where bot will be used
token: <bot_token>                                # Token of the bot
treePath: <path/to/tree_cinfiguration_file.yaml>  # Path to tree config file 
userWhiteList:                                    # White list of user ids for /update command usage
  - <userId1>
  - <userId2>
  ...
```

## Example of usage
https://user-images.githubusercontent.com/42372666/152680156-ac49522a-1a7c-4c7d-882b-5bcff5bb71a6.mp4

