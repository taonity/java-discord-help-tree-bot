UPDATE pg_database SET datallowconn = 'false' WHERE datname = 'discord_help_bot';

SELECT pg_terminate_backend(pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = 'discord_help_bot';

DROP DATABASE discord_help_bot;

UPDATE pg_database SET datallowconn = 'true' WHERE datname = 'discord_help_bot';



