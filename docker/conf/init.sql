-- Step 1: Create the testing database
CREATE DATABASE testing_db;

-- Step 2: Create a user and grant all privileges on the database
CREATE USER testing_user WITH PASSWORD 'testing_password';
GRANT ALL PRIVILEGES ON DATABASE testing_db TO testing_user;


-- Step 3: Perform database-specific operations
\c testing_db

-- Create a table in testing_db
CREATE TABLE sample_table (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    value TEXT
);

-- Grant ownership of the table to testing_user
ALTER TABLE sample_table OWNER TO testing_user;

-- Insert a sample record
INSERT INTO sample_table (name, value) VALUES ('SampleName', 'This is a sample value for testing.');

-- For Datadog DB monitoring
CREATE USER datadog WITH PASSWORD 'datadog_password';
ALTER ROLE datadog INHERIT;
GRANT ALL PRIVILEGES ON DATABASE testing_db TO datadog;
ALTER USER datadog SET SEARCH_PATH TO datadog,pg_catalog,public;

CREATE SCHEMA datadog;
GRANT USAGE ON SCHEMA datadog TO datadog;
GRANT USAGE ON SCHEMA public TO datadog;
GRANT pg_monitor TO datadog;
CREATE EXTENSION pg_stat_statements;

CREATE OR REPLACE FUNCTION datadog.explain_statement(
   l_query TEXT,
   OUT explain JSON
)
RETURNS SETOF JSON AS
$$
DECLARE
curs REFCURSOR;
plan JSON;

BEGIN
   OPEN curs FOR EXECUTE pg_catalog.concat('EXPLAIN (FORMAT JSON) ', l_query);
   FETCH curs INTO plan;
   CLOSE curs;
   RETURN QUERY SELECT plan;
END;
$$
LANGUAGE 'plpgsql'
RETURNS NULL ON NULL INPUT
SECURITY DEFINER;

\c postgres
GRANT ALL PRIVILEGES ON DATABASE postgres TO datadog;
ALTER USER datadog SET SEARCH_PATH TO datadog,pg_catalog,public;

CREATE SCHEMA datadog;
GRANT USAGE ON SCHEMA datadog TO datadog;
GRANT USAGE ON SCHEMA public TO datadog;
GRANT pg_monitor TO datadog;
CREATE EXTENSION pg_stat_statements;

CREATE OR REPLACE FUNCTION datadog.explain_statement(
   l_query TEXT,
   OUT explain JSON
)
RETURNS SETOF JSON AS
$$
DECLARE
curs REFCURSOR;
plan JSON;

BEGIN
   OPEN curs FOR EXECUTE pg_catalog.concat('EXPLAIN (FORMAT JSON) ', l_query);
   FETCH curs INTO plan;
   CLOSE curs;
   RETURN QUERY SELECT plan;
END;
$$
LANGUAGE 'plpgsql'
RETURNS NULL ON NULL INPUT
SECURITY DEFINER;