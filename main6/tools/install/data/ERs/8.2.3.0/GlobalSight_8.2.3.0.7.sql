# GBS-2554: Office 2010 hidden text extraction or not
ALTER TABLE `office2010_filter` ADD COLUMN `IS_HIDDENTEXT_TRANSLATE` CHAR(1) NOT NULL DEFAULT 'N' AFTER `IS_TOOLTIPS_TRANSLATE`;