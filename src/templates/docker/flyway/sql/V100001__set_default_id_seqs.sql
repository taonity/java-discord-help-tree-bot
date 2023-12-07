ALTER TABLE guild_settings ALTER COLUMN id SET DEFAULT nextval('guild_settings_seq');
ALTER SEQUENCE guild_settings_seq OWNED BY guild_settings.id;

CREATE SEQUENCE IF NOT EXISTS app_settings_seq;
ALTER TABLE app_settings ALTER COLUMN id SET DEFAULT nextval('app_settings_seq');
ALTER SEQUENCE app_settings_seq OWNED BY app_settings.id;