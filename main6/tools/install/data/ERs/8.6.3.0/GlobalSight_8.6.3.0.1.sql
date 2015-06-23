#GBS-3945:Workflow Notifications & Listener URL
CREATE TABLE `workflow_state_posts` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `NAME` varchar(40) NOT NULL,
  `DESCRIPTION` varchar(4000) DEFAULT NULL,
  `LISTENER_URL` varchar(100) NOT NULL,
  `SECRET_KEY` varchar(100) DEFAULT NULL,
  `TIMEOUT_PERIOD` varchar(3) DEFAULT NULL,
  `RETRY_NUMBER` varchar(3) DEFAULT NULL,
  `NOTIFY_EMAIL` varchar(100) DEFAULT NULL,
  `COMPANY_ID` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8


ALTER TABLE COMPANY ADD COLUMN ENABLE_WORKFLOW_STATE_POSTS VARCHAR(1) DEFAULT 'N' AFTER ENABLE_DITA_CHECKS;

ALTER TABLE L10N_PROFILE ADD COLUMN WF_STATE_POST_ID BIGINT(20) DEFAULT '-1' AFTER JOB_EXCLUDE_TU_TYPES;
