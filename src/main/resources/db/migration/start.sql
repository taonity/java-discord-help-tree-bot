CREATE SEQUENCE IF NOT EXISTS guild_settings_seq;

CREATE TABLE IF NOT EXISTS guild_settings (
	id int4             NOT NULL,
    guild_id            varchar NOT NULL,
	log_channel_id      varchar NULL,
	help_channel_id     varchar NULL,
	gitea_user_id       int4 NULL,
	gitea_user_alphanumeric varchar NULL,

	CONSTRAINT guild_settings_pk PRIMARY KEY (id)
);

ALTER SEQUENCE guild_settings_seq OWNED BY guild_settings.id;