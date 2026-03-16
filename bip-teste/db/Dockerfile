FROM postgres:16-alpine

COPY schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY seed.sql   /docker-entrypoint-initdb.d/02-seed.sql

ENV POSTGRES_USER=user
ENV POSTGRES_PASSWORD=password
ENV POSTGRES_DB=beneficios_db
