-- Create application user and schema
CREATE USER docservice IDENTIFIED BY "SecurePass123!";
GRANT CONNECT, RESOURCE TO docservice;
GRANT CREATE SESSION TO docservice;
GRANT UNLIMITED TABLESPACE TO docservice;

-- Create database
CREATE TABLESPACE docdb_data
    DATAFILE 'docdb_data.dbf'
    SIZE 100M
    AUTOEXTEND ON
    NEXT 10M
    MAXSIZE UNLIMITED;

ALTER USER docservice DEFAULT TABLESPACE docdb_data;

-- Grant necessary permissions
GRANT CREATE TABLE TO docservice;
GRANT CREATE SEQUENCE TO docservice;
GRANT CREATE PROCEDURE TO docservice;
GRANT CREATE TRIGGER TO docservice;
GRANT CREATE VIEW TO docservice;
GRANT CREATE SYNONYM TO docservice;

EXIT;
