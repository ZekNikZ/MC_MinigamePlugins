package io.zkz.mc.minigameplugins.testplugin.service;

import io.zkz.mc.minigameplugins.gametools.teams.TeamService;

public class TestService extends TestPluginService {
    private static final TestService INSTANCE = new TestService();

    public static TestService getInstance() {
        return INSTANCE;
    }

    @Override
    protected void setup() {

    }

    @Override
    public void onEnable() {
        TeamService.getInstance().setupDefaultTeams();
    }

    @Override
    public void onDisable() {

    }
}
