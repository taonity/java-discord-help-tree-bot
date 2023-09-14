REVOKE ALL ON schema public FROM public;

CREATE USER {{ app_user }} WITH PASSWORD '{{ app_password }}';
GRANT CONNECT ON DATABASE {{ db_name }} TO {{ app_user }};
GRANT USAGE ON SCHEMA public TO {{ app_user }};