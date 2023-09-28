package gg_bot;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class Main {
    public static void main(String[] args) {
        String token = System.getenv("DISCORD_BOT_TOKEN");

        DiscordClient client = DiscordClient.create(token);
        client.withGateway((GatewayDiscordClient gateway) -> {

            Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
                            Mono.fromRunnable(() -> {
                                final User self = event.getSelf();
                                System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
                            }))
                    .then();

            Mono<Void> handleMessage = gateway.on(MessageCreateEvent.class, event -> {
                Message message = event.getMessage();
              
                if (!message.getAuthor().map(user -> user.getId().equals(gateway.getSelfId())).orElse(true)) {

                    return message.getChannel()
                            .flatMap(channel -> channel.createMessage("GG!"))
                            .then();
                }
                return Mono.empty();
            }).then();

            return Mono.when(printOnLogin, handleMessage);
        }).block();
    }
}
