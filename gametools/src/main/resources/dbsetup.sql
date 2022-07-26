create table if not exists gt_teams
(
    teamId         varchar(20)  not null,
    teamName       varchar(100) not null,
    teamPrefix     varchar(16)  not null,
    teamFormatCode char         not null,
    teamColor      int          not null,
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
        references gt_teams(teamId)
);



