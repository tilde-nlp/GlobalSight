# GBS-3824:Translation Edit Report Attached to email notification

ALTER TABLE PROJECT ADD COLUMN AUTO_ACCEPT_TRANS CHAR(1) DEFAULT '0';
ALTER TABLE PROJECT ADD COLUMN AUTO_SEND_TRANS CHAR(1) DEFAULT '0';
