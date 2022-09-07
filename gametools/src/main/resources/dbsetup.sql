create table if not exists gt_teams
(
    teamId              varchar(20)  not null,
    teamName            varchar(100) not null,
    teamPrefix          varchar(16)  not null,
    teamFormatCode      varchar(10)  not null,
    teamScoreboardColor varchar(2)   not null,
    teamColor           int          not null,
    teamIsSpectator     boolean      not null,
    primary key (teamId),
    constraint gt_teams_teamId_uindex
        unique (teamId)
);

create table if not exists gt_player_teams
(
    playerId varchar(36) not null,
    teamId   varchar(20) not null,
    primary key (playerId, teamId),
    constraint gt_player_teams_playerId_uindex
        unique (playerId),
    foreign key (teamId)
        references gt_teams (teamId)
);

create table if not exists mm_minigame_state
(
    id    varchar(40) not null
        primary key,
    value varchar(60) null
);

create table if not exists mm_score
(
    playerId   varchar(40) not null,
    minigame   varchar(20) not null,
    round      int         not null,
    reason     varchar(100) null,
    points     double      null,
    multiplier double      null
);

