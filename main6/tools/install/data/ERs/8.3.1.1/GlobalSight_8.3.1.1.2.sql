-- GBS-2959: DOCX endnotes not exposed for translation in GlobalSight
ALTER TABLE office2010_filter ADD COLUMN IS_FOOTENDNOTE_TRANSLATE CHAR(1) NOT NULL DEFAULT 'N'  AFTER IS_HEADER_TRANSLATE;