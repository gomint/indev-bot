package io.gomint.indev.config;

import io.gomint.config.YamlConfig;
import io.gomint.config.annotation.Comment;
import lombok.Getter;

@Getter
public class Config extends YamlConfig {

    @Comment("Discord bot token")
    private String discordToken;

    @Comment("Discord guild id")
    private String discordGuild;

    @Comment("Discord channel")
    private String discordChannel;

}
