# GBS-4239 : Wholly Internal text segments always are written into storage TM

ALTER TABLE tm_profile ADD IS_WHOLLY_INTERNAL_TEXT_TO_PROJ_TM CHAR(1) NOT NULL DEFAULT 'N' CHECK (IS_WHOLLY_INTERNAL_TEXT_TO_PROJ_TM IN('Y', 'N')) AFTER IS_LOC_SEG_SAVED_TO_PROJ_TM;

COMMIT;