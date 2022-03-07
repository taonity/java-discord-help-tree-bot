package discord;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserManager {
    private GatewayDiscordClient gateway;
    private MessageChannel messageChannel;
    private List<SelectMenuManager> smManagers;
    private final Long appId;
    private final Long guildId;
    private TreeRoot treeRoot;
    private final List<Long> userWhiteList;
    private final Notificator notificator;

    private static final Logger log = LoggerFactory.getLogger(UserManager.class);


    public UserManager(GatewayDiscordClient gateway,
                       MessageChannel messageChannel,
                       Long appId,
                       Long guildId,
                       String helpTreePath,
                       List<Long> userWhiteList) {
        this.gateway = gateway;
        this.messageChannel = messageChannel;
        this.appId = appId;
        this.guildId = guildId;
        this.userWhiteList = userWhiteList;
        smManagers = new ArrayList<>();
        treeRoot = new TreeRoot(helpTreePath);
        notificator = new Notificator();
        setSelectMenuListener();
        setQuestionCommandListener();
        setMessageListener();
    }

    public List<SelectMenuManager> getSmManagers() {
        return smManagers;
    }

    public void setSmManagers(List<SelectMenuManager> smManagers) {
        this.smManagers = smManagers;
    }

    private void setQuestionCommandListener() {
        ApplicationCommandRequest questionCmdRequest = ApplicationCommandRequest.builder()
                .name("question")
                .description("Ask a question")
                .build();

        gateway.getRestClient().getApplicationService()
                .createGuildApplicationCommand(appId, guildId, questionCmdRequest)
                .subscribe();

        ApplicationCommandRequest updateCmdRequest = ApplicationCommandRequest.builder()
                .name("update")
                .description("Verify edited help_tree file and update it")
                .build();

        gateway.getRestClient().getApplicationService()
                .createGuildApplicationCommand(appId, guildId, updateCmdRequest)
                .subscribe();

        gateway.on(ApplicationCommandInteractionEvent.class, event -> {
            final var currentChannel = event.getInteraction().getChannel().block();
            if(currentChannel == null) {
                log.info("Error! currentChannel in ApplicationCommandInteractionEvent is null.");
                return Mono.empty();
            }
            if(!currentChannel.getId().equals(messageChannel.getId())) {
                return event.reply("This command works in "+messageChannel.getMention()+" only.")
                        .withEphemeral(true);
            }
            final var interactionAuthor = event.getInteraction().getMember();
            if(interactionAuthor.isEmpty()) {
                log.info("Error! interactionAuthor in ApplicationCommandInteractionEvent is empty.");
                return Mono.empty();
            }
            if(event.getCommandName().equals(updateCmdRequest.name())) {
                if(!userWhiteList.contains(interactionAuthor.get().getId().asLong())) {
                    return event.reply("You can not use this command.")
                            .withEphemeral(true);
                }
                var errorMessage = treeRoot.verifyFile(gateway);
                if(errorMessage == null) {
                    return event.reply("Success");
                } else {
                    return event.reply("```"+errorMessage+"```");
                }
            }
            if(event.getCommandName().equals(questionCmdRequest.name())) {
                Snowflake authorId = interactionAuthor.get().getId();
                getSmManagers().removeIf(SelectMenuManager::isDead);
                getSmManagers().removeIf(manager -> authorId.equals(manager.getUserId()));
                var selectMenuManager = new SelectMenuManager(authorId, treeRoot);
                getSmManagers().add(selectMenuManager);
                return event.reply("Выбери язык. Choose language.").withComponents(
                        ActionRow.of(selectMenuManager.getLanguageSelectMenu())
                );
            }
            return Mono.empty();
        }).subscribe();
    }


    private void setSelectMenuListener() {
        gateway.on(SelectMenuInteractionEvent.class, smEvent -> {
            final var interactionAuthor = smEvent.getInteraction().getMember();
            if(interactionAuthor.isEmpty()) {
                log.info("Error! interactionAuthor in SelectMenuInteractionEvent is empty.");
                return Mono.empty();
            }
            var activeMember = interactionAuthor.get();
            Snowflake authorId = activeMember.getId();
            System.out.println(getSmManagers().stream()
                    .map(SelectMenuManager::getUserId)
                    .collect(Collectors.toList()));
            SelectMenuManager smManager = getSmManagers().stream()
                    .filter(manager -> authorId.equals(manager.getUserId()))
                    .findAny()
                    .orElse(null);
            if(smManager == null) {
                return Mono.empty();
            }

            String value = smEvent
                    .getValues()
                    .toString().replace("[", "").replace("]", "");

            if(smEvent.getCustomId().equals(smManager.getLanguageSelectMenu().getCustomId())) {
                switch (value) {
                    case "English":
                        smManager.setLanguage(Language.EN);
                        break;
                    case "Русский":
                        smManager.setLanguage(Language.RU);
                        break;
                    default:
                        throw new IllegalArgumentException("Undefined language in SelectMenu");
                }
                SelectMenu selectMenu = smManager.createNextSelectMenu(null);
                messageChannel.createMessage(
                        LocalizedFields.get("lets", smManager.getLanguage())
                ).withComponents(ActionRow.of(selectMenu)).subscribe();
                smManager.updateLastUpdateTime();

                return smEvent.edit().withComponents(ActionRow.of(SelectMenuFactory.disableAndSetDefault(smManager.getLanguageSelectMenu(), value)));
            } else
            if(smEvent.getCustomId().equals(smManager
                    .getCurrentTreeSelectMenu()
                    .getCustomId())) {

                SelectMenu selectMenu = smManager.createNextSelectMenu(value);

                String selectedLabel = smManager.getCurrentTree()
                        .getCurrentNode()
                        .getLocalizedText()
                        .getTranslatedText(smManager.getLanguage());

                if(smManager.getCurrentTree().getCurrentNode().getChildText().get(0).getChildText() == null) {
                    Node answerNode = smManager.getCurrentTree().getCurrentNode().getChildText().get(0);
                    String translatedAnswer = answerNode.getLocalizedText()
                            .getTranslatedText(smManager.getLanguage());
                    messageChannel.createMessage(translatedAnswer).subscribe();
                    switch (answerNode.getNodeFunction()) {
                        case ASK_INPUT:
                            smManager.setUserStatus(UserStatus.WRITES_MESSAGE);
                            smManager.updateLastUpdateTime();
                            break;
                        case RETURN_TEXT:
                            getSmManagers().remove(smManager);
                            break;
                        default:
                            return Mono.empty();
                    }
                } else {
                    log.info("Test: clarification message. language: " + smManager.getLanguage().toString());
                    messageChannel.createMessage(
                            LocalizedFields.get("clar", smManager.getLanguage())
                    ).withComponents(ActionRow.of(selectMenu)).subscribe();
                    log.info("Test: clarification message. created");
                    smManager.updateLastUpdateTime();
                }

                return smEvent.edit().withComponents(
                        ActionRow.of(SelectMenuFactory.disableAndSetDefault(
                                smManager.getCurrentTreeSelectMenu(), selectedLabel))
                );
            } else {
                return Mono.empty();
            }
        }).subscribe();
    }

    private void setMessageListener() {
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            log.info("1");
            final var messageAuthor = event.getMessage().getAuthor();
            if(messageAuthor.isEmpty()) {
                log.info("Error! messageAuthor in MessageCreationEvent is empty.");
                return;
            }
            if(messageAuthor.get().isBot()) {
                return;
            }
            Guild guild = event.getGuild().block();
            log.info("2");
            if(guild != null) {
                log.info("3");
                final var currentMessage = event.getMessage().getChannel().block();
                if(currentMessage == null) {
                    log.info("Error! currentMessage in MessageCreateEvent is null.");
                    return;
                }
                log.info("4");
                if(currentMessage.getId().equals(messageChannel.getId())) {

                    Snowflake authorId = messageAuthor.get().getId();

                    SelectMenuManager smManager = getSmManagers().stream()
                            .filter(manager -> authorId.equals(manager.getUserId()))
                            .findAny()
                            .orElse(null);
                    if(smManager != null) {
                        if(smManager.getUserStatus() == UserStatus.WRITES_MESSAGE) {
                            String targetId = smManager.getCurrentTree().getCurrentNode().getChildText()
                                    .get(0).getTargetId();
                            var targetUser = gateway.getUserById(Snowflake.of(targetId)).block();
                            if(targetUser == null) {
                                log.info("Error! targetUser in MessageCreateEvent is null.");
                                return;
                            }
                            messageChannel.createMessage(MessageCreateSpec.builder()
                                    .messageReference(event.getMessage().getId())
                                    .content(targetUser.getMention())
                                    .build()
                            ).block();
                            getSmManagers().remove(smManager);
                            return;
                        }
                    }
                    log.info("5");
                    if(notificator.isTime() && event.getMessage().getContent().length() > 6) {
                        log.info("6");
                        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                                .color(Color.CYAN)
                                .title("Tip. Подсказка.")
                                .description(notificator.getNotificationText().toString())
                                .footer("This tip is triggered by random message every 1 hour", "")
                                .build();
                        log.info("7");
                        messageChannel.createMessage(embed).block();
                        log.info("8");
                    }
                }

            }

        });
    }
}
