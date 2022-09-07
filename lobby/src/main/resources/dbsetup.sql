create table if not exists mm_minigames
(
    minigameSpinnerId int         not null,
    minigameId        varchar(50) not null,
    minigameName      varchar(50) not null,
    minigameIcon      varchar(50) not null,
    minigameSelected  boolean     not null
);
