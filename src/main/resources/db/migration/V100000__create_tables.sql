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

CREATE TABLE IF NOT EXISTS app_settings (
    id int4             NOT NULL,
	gitea_token     varchar     NOT NULL,

	CONSTRAINT app_settings_pk PRIMARY KEY (id)
);