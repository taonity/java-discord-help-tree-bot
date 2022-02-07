## java-discord-help-tree-bot
The bot that answers questions, configured like tree

## Features
- Works with multiple userrs in the same time
- Easy confirable questions and answers in YAML format
- Two type of answers for different scenarios 
- Online configuration file updating
- Basic verifying of configuration file before update for bot
- Easy to build, host and run
- Run only for one guild 

## Commands
- `/question` - start question asking session
- `/update` - verify if question tree configuration file is correct and if it is - update it for bot

## Build
Projecte wrote on Java 11.
Just open it with Intellij IDEA, download maven dependecies and build. 
For `jar` executable building run `mvn clean istall` in command prompt.

## Run
For Intellij IDEA modify run configurations, adding discord bot token and YAML configuration file path as arguments. For `jar` executable file run:
```
java -jar executable.jar <token> <path/to/configurations>
```
Make sure you have Java 11 JRE installed.

# Configuration file
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

## Example of usage
https://user-images.githubusercontent.com/42372666/152680156-ac49522a-1a7c-4c7d-882b-5bcff5bb71a6.mp4

