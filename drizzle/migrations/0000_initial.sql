CREATE TABLE `announcements` (
	`id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
	`author` text,
	`title` text NOT NULL,
	`content` text,
	`created_at` text NOT NULL,
	`archived_at` text,
	`level` integer DEFAULT 0 NOT NULL
);
