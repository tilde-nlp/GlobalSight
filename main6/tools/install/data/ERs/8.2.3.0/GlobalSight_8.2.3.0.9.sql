# For GBS-2408
ALTER TABLE `office2010_filter` ADD COLUMN `INTERNAL_TEXT_CHARACTER_STYLES` VARCHAR(4000) NOT NULL AFTER `UNEXTRACTABLE_WORD_CHARACTER_STYLES`;