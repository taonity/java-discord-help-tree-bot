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
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private GatewayDiscordClient gateway;
    private MessageChannel messageChannel;
    private List<SelectMenuManager> smManagers;
    private final long appId;
    private final long guildId;
    private TreeRoot treeRoot;

    public UserManager(GatewayDiscordClient gateway, MessageChannel messageChannel, long appId, long guildId, String helpTreePath) {
        this.gateway = gateway;
        this.messageChannel = messageChannel;
        this.appId = appId;
        this.guildId = guildId;
        smManagers = new ArrayList<>();
        treeRoot = new TreeRoot(helpTreePath);
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
            if(event.getCommandName().equals(updateCmdRequest.name())) {
                var errorMessage = treeRoot.verifyFile(gateway);
                if(errorMessage == null) {
                    return event.reply("Success");
                } else {
                    return event.reply("```"+errorMessage+"```");
                }
            }
            if(event.getCommandName().equals(questionCmdRequest.name())) {
                if(!event.getInteraction().getChannel().block().getId().equals(messageChannel.getId())) {
                    return event.reply("This command works in "+messageChannel.getMention()+" only.");
                }
                Snowflake authorId = event.getInteraction().getMember().get().getId();
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
            var interation = smEvent.getInteraction();
            var optional = interation.getMember();
            var activeMember = smEvent.getInteraction().getMember().get();
            Snowflake authorId = activeMember.getId();
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
                    messageChannel.createMessage(
                            LocalizedFields.get("clar", smManager.getLanguage())
                    ).withComponents(ActionRow.of(selectMenu)).subscribe();
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
            Guild guild = event.getGuild().block();
            if(guild != null) {
                if(event.getMessage().getChannel().block().getId().equals(messageChannel.getId())) {
                    Snowflake authorId = event.getMessage().getAuthor().get().getId();
                    SelectMenuManager smManager = getSmManagers().stream()
                            .filter(manager -> authorId.equals(manager.getUserId()))
                            .findAny()
                            .orElse(null);
                    if(smManager != null) {
                        if(smManager.getUserStatus() == UserStatus.WRITES_MESSAGE) {
                            String targetId = smManager.getCurrentTree().getCurrentNode().getChildText()
                                    .get(0).getTargetId();
                            var targetUser = gateway.getUserById(Snowflake.of(targetId)).block();

                            messageChannel.createMessage(MessageCreateSpec.builder()
                                    .messageReference(event.getMessage().getId())
                                    .allowedMentions(AllowedMentions.builder().repliedUser(false).build())
                                    .content(targetUser.getMention())
                                    .build()
                            ).block();
                            getSmManagers().remove(smManager);
                        }
                    }

                }

            }

        });
    }
}
