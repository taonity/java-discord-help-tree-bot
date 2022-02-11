## java-discord-help-tree-bot
The bot that answers questions, configured like tree

## Features
- Works with multiple userrs in the same time
- Multilanguage for question and answers
- Easy confirable questions and answers in YAML format
- Two type of answers for different scenarios 
- Online configuration file updating
- Basic verifying of configuration file before update for bot
- Easy to build, host and run
- Run only for one guild 
- Extern settings configuration file

## Commands
- `/question` - start question asking session
- `/update` - verify if question tree configuration file is correct and if it is - update it for bot

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

