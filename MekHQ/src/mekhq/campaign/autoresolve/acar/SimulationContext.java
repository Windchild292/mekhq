/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 *  This file is part of MekHQ.
 *
 *  MekHQ is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MekHQ is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.autoresolve.acar;


import megamek.common.*;
import megamek.common.actions.EntityAction;
import megamek.common.annotations.Nullable;
import megamek.common.enums.GamePhase;
import megamek.common.enums.SkillLevel;
import megamek.common.event.GameEvent;
import megamek.common.event.GameListener;
import megamek.common.force.Forces;
import megamek.logging.MMLogger;
import megamek.server.scriptedevent.TriggeredEvent;
import megamek.server.victory.VictoryHelper;
import megamek.server.victory.VictoryResult;
import mekhq.campaign.autoresolve.acar.action.Action;
import mekhq.campaign.autoresolve.acar.action.ActionHandler;
import mekhq.campaign.autoresolve.acar.report.PublicReportEntry;
import mekhq.campaign.autoresolve.component.AcTurn;
import mekhq.campaign.autoresolve.component.Formation;
import mekhq.campaign.autoresolve.component.FormationTurn;
import mekhq.campaign.autoresolve.converter.SetupForces;
import mekhq.campaign.autoresolve.event.AutoResolveConcludedEvent;
import mekhq.campaign.mission.AtBScenario;
import mekhq.campaign.mission.ScenarioObjective;
import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Luana Coppio
 */
public class SimulationContext implements IGame {

    private static final MMLogger logger = MMLogger.create(SimulationContext.class);

    private final SimulationOptions options;
    private final AtBScenario scenario;

    /**
    * Objectives that must be considered during the game
    */
    private static final int AWAITING_FIRST_TURN = -1;
    private final List<String> forceMustBePreserved = new ArrayList<>();
    private final List<Action> pendingActions = new ArrayList<>();

    /**
     * Game Phase and rules
     */
    private GamePhase phase = GamePhase.UNKNOWN;
    private GamePhase lastPhase = GamePhase.UNKNOWN;

    private final Map<Integer, SkillLevel> playerSkillLevels = new HashMap<>();
    private int lastEntityId;
    /**
     * Report and turnlist
     */
    private final List<AcTurn> turnList = new ArrayList<>();
    protected final ConcurrentHashMap<Integer, InGameObject> inGameObjects = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<>();
    protected final List<Team> teams = new ArrayList<>();

    protected Forces forces = new Forces(this);
    private final Map<Integer, List<Deployable>> deploymentTable = new HashMap<>();

    protected int currentRound = -1;

    protected int turnIndex = AWAITING_FIRST_TURN;


    /**
     * Tools for the game
     */
    private final List<ActionHandler> actionHandlers = new ArrayList<>();
    private MapSettings mapSettings;

    /**
     * Contains all units that have left the game by any means.
     */
    private final Vector<Entity> graveyard = new Vector<>();
    private final Map<String, Object> victoryContext = new HashMap<>();
    private final VictoryHelper victoryHelper = new VictoryHelper(this);
    private int victoryPlayerId = Player.PLAYER_NONE;
    private int victoryTeam = Player.TEAM_NONE;


    public SimulationContext(AtBScenario scenario, SimulationOptions gameOptions, SetupForces setupForces) {
        this.options = gameOptions;
        this.scenario = scenario;
        this.setupScenarioObjectives();
        setBoard(0, new Board());
        setupForces.createForcesOnGame(this);
    }

    private void setupScenarioObjectives() {
        forceMustBePreserved.clear();
        scenario.getScenarioObjectives().forEach(objective -> {
            if (objective.getObjectiveCriterion().equals(ScenarioObjective.ObjectiveCriterion.Preserve)) {
                forceMustBePreserved.addAll(objective.getAssociatedForceNames());
            }
        });
    }

    public AtBScenario getScenario() {
        return scenario;
    }

    public AutoResolveConcludedEvent getConclusionEvent() {

        var playerTeamWon = this.getVictoryTeam() == this.getLocalPlayer().getTeam();

        return new AutoResolveConcludedEvent(
            playerTeamWon,
            this.getGraveyard().stream().filter(Entity.class::isInstance).map(Entity.class::cast).toList(),
            this.getInGameObjects().stream().filter(Entity.class::isInstance).map(Entity.class::cast).toList(),
            this);
    }

    public void addUnit(InGameObject unit) {
        int id = unit.getId();
        if (inGameObjects.containsKey(id) || isOutOfGame(id) || (Entity.NONE == id)) {
            id = getNextEntityId();
            unit.setId(id);
        }
        inGameObjects.put(id, unit);
    }

    /** @return The TW Units (Entity) currently in the game. */
    public List<Entity> inGameTWEntities() {
        return filterToEntity(inGameObjects.values());
    }

    private List<Entity> filterToEntity(Collection<? extends BTObject> objects) {
        return objects.stream().filter(Entity.class::isInstance).map(o -> (Entity) o).toList();
    }

    public List<Deployable> deployableInGameObjects() {
        return getInGameObjects().stream()
            .filter(Deployable.class::isInstance)
            .map(Deployable.class::cast)
            .collect(Collectors.toList());
    }

    public int getNoOfEntities() {
        return inGameTWEntities().size();
    }

    public int getSelectedEntityCount(EntitySelector selector) {
        int retVal = 0;

        // If no selector was supplied, return the count of all game entities.
        if (null == selector) {
            retVal = getNoOfEntities();
        }

        // Otherwise, count the entities that meet the selection criteria.
        else {
            for (Entity entity : inGameTWEntities()) {
                if (selector.accept(entity)) {
                    retVal++;
                }
            }

        } // End use-selector

        // Return the number of selected entities.
        return retVal;
    }

    @Override
    public int getNextEntityId() {
        return inGameObjects.isEmpty() ? 0 : Collections.max(inGameObjects.keySet()) + 1;
    }

    /** @return The entity with the given id number, if any. */
    public Optional<Entity> getEntity(final int id) {
        InGameObject possibleEntity = inGameObjects.get(id);
        if (possibleEntity instanceof Entity) {
            return Optional.of((Entity) possibleEntity);
        }
        return Optional.empty();
    }

    public void addEntity(Entity entity) {
        int id = entity.getId();
        if (isIdUsed(id)) {
            id = getNextEntityId();
            entity.setId(id);
        }
        inGameObjects.put(id, entity);
        if (id > lastEntityId) {
            lastEntityId = id;
        }

        if (entity instanceof Mek mek) {
            mek.setAutoEject(true);
            mek.setCondEjectAmmo(!entity.hasCase() && !entity.hasCASEII());
            mek.setCondEjectEngine(true);
            mek.setCondEjectCTDest(true);
            mek.setCondEjectHeadshot(true);
        }

        entity.setInitialBV(entity.calculateBattleValue(false, false));
    }

    public boolean isOutOfGame(int id) {
        for (Entity entity : graveyard) {
            if (entity.getId() == id) {
                return true;
            }
        }

        return false;
    }


    private boolean isIdUsed(int id) {
        return inGameObjects.containsKey(id) || isOutOfGame(id);
    }

    @Override
    public List<AcTurn> getTurnsList() {
        return Collections.unmodifiableList(turnList);
    }


    @Override
    public SimulationOptions getOptions() {
        if (options != null) {
            return options;
        }
        return SimulationOptions.EMPTY;
    }

    @Override
    public GamePhase getPhase() {
        return phase;
    }

    public void addActionHandler(ActionHandler handler) {
        if (actionHandlers.contains(handler)) {
            logger.error("Tried to re-add action handler {}!", handler);
        } else {
            actionHandlers.add(handler);
        }
    }

    @Override
    public AcTurn getTurn() {
        if ((turnIndex < 0) || (turnIndex >= turnList.size())) {
            return null;
        }
        return turnList.get(turnIndex);
    }

    public Optional<AcTurn> getCurrentTurn() {
        if ((turnIndex < 0) || (turnIndex >= turnList.size())) {
            return Optional.empty();
        }
        return Optional.of(turnList.get(turnIndex));
    }

    @Override
    public boolean hasMoreTurns() {
        return getTurnsList().size() > turnIndex + 1;
    }

    public void setTurns(List<AcTurn> turns) {
        this.turnList.clear();
        this.turnList.addAll(turns);
    }

    @Override
    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    @Override
    public void setLastPhase(GamePhase lastPhase) {
        this.lastPhase = this.phase;
    }

    @Override
    public void receivePhase(GamePhase phase) {
        setLastPhase(this.phase);
        setPhase(phase);
    }


    @Override
    public boolean isCurrentPhasePlayable() {
        return true;
    }


    @Override
    public void setPlayer(int id, Player player) {
        player.setGame(this);
        players.put(id, player);
        setupTeams();
    }

    @Override
    public void removePlayer(int id) {
        // not implemented
    }

    @Override
    public void setupTeams() {
        Vector<Team> initTeams = new Vector<>();

        // Now, go through all the teams, and add the appropriate player
        for (int t = Player.TEAM_NONE + 1; t < Player.TEAM_NAMES.length; t++) {
            Team newTeam = null;
            for (Player player : getPlayersList()) {
                if (player.getTeam() == t) {
                    if (newTeam == null) {
                        newTeam = new Team(t);
                    }
                    newTeam.addPlayer(player);
                }
            }

            if (newTeam != null) {
                initTeams.addElement(newTeam);
            }
        }

        for (Team newTeam : initTeams) {
            for (Team oldTeam : teams) {
                if (newTeam.equals(oldTeam)) {
                    newTeam.setInitiative(oldTeam.getInitiative());
                }
            }
        }

        // Carry over faction settings
        for (Team newTeam : initTeams) {
            for (Team oldTeam : teams) {
                if (newTeam.equals(oldTeam)) {
                    newTeam.setFaction(oldTeam.getFaction());
                }
            }
        }

        teams.clear();
        teams.addAll(initTeams);
    }

    @Override
    public void replaceUnits(List<InGameObject> units) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public List<InGameObject> getGraveyard() {
        List<InGameObject> destroyed = new ArrayList<>();
        for (Entity entity : this.graveyard) {
            if ((entity.getRemovalCondition() == IEntityRemovalConditions.REMOVE_SALVAGEABLE)
                || (entity.getRemovalCondition() == IEntityRemovalConditions.REMOVE_EJECTED)) {
                destroyed.add(entity);
            }
        }

        return destroyed;
    }

    @Override
    public int getLiveDeployedEntitiesOwnedBy(Player player) {
        var res = getActiveFormations(player).stream()
            .filter(Formation::isDeployed)
            .count();

        return (int) res;
    }

    @Override
    public ReportEntry getNewReport(int messageId) {
        return new PublicReportEntry(messageId);
    }

    @Override
    public List<TriggeredEvent> scriptedEvents() {
        return List.of();
    }

    @Override
    public void addScriptedEvent(TriggeredEvent event) {

    }

    @Override
    public void setVictoryPlayerId(int victoryPlayerId) {
        this.victoryPlayerId = victoryPlayerId;
    }

    @Override
    public void setVictoryTeam(int victoryTeam) {
        this.victoryTeam = victoryTeam;
    }

    @Override
    public void cancelVictory() {
        this.victoryPlayerId = Player.PLAYER_NONE;
        this.victoryTeam = Player.TEAM_NONE;
    }

    @Override
    public int getVictoryPlayerId() {
        return victoryPlayerId;
    }

    @Override
    public int getVictoryTeam() {
        return victoryTeam;
    }

    @Override
    public boolean gameTimerIsExpired() {
        return getRoundCount() >= 1000;
    }

    private int getRoundCount() {
        return currentRound;
    }

    @Override
    public int getLiveCommandersOwnedBy(Player player) {
        return 0;
    }

    @Override
    public Optional<Player> playerForPlayername(String playerName) {
        // not implemented
        return Optional.empty();
    }

    @Override
    public Optional<Integer> idForPlayername(String playerName) {
        // not implemented
        return Optional.empty();
    }

    public List<ActionHandler> getActionHandlers() {
        return actionHandlers;
    }

    public Optional<AcTurn> changeToNextTurn() {
        turnIndex++;
        return getCurrentTurn();
    }

    public boolean hasEligibleFormation(FormationTurn turn) {
        return (turn != null) && getActiveFormations().stream().anyMatch(f -> turn.isValidEntity(f, this));
    }

    /**
     * Returns the formation of the given ID, if one can be found.
     *
     * @param formationID the ID to look for
     * @return The formation or an empty Optional
     */
    public Optional<Formation> getFormation(int formationID) {
        Optional<InGameObject> unit = getInGameObject(formationID);
        if (unit.isPresent() && unit.get() instanceof Formation formation) {
            return Optional.of(formation);
        } else {
            return Optional.empty();
        }
    }

    public GamePhase getLastPhase() {
        return lastPhase;
    }

    public void setVictoryContext(Map<String, Object> ctx) {
        victoryContext.clear();
        victoryContext.putAll(ctx);
    }

    // check current turn, phase, formation
    private boolean isEligibleForAction(Formation formation) {
        return (getTurn() instanceof FormationTurn)
            && getTurn().isValidEntity(formation, this);
    }

    /**
     * Returns the list of formations that are in the game's InGameObject list, i.e.
     * that aren't destroyed
     * or otherwise removed from play.
     *
     * @return The currently active formations
     */
    public List<Formation> getActiveFormations() {
        return getInGameObjects().stream()
            .filter(u -> u instanceof Formation)
            .map(u -> (Formation) u)
            .toList();
    }

    public List<Formation> getActiveFormations(Player player) {
        return getActiveFormations().stream()
            .filter(f -> f.getOwnerId() == player.getId())
            .toList();
    }

    public VictoryResult getVictoryResult() {
        return victoryHelper.checkForVictory(this, victoryContext);
    }

    public void addUnitToGraveyard(Entity entity) {
        var entityInGame = getEntity(entity.getId());
        if (entityInGame.isPresent()) {
            removeEntity(entity);
            graveyard.add(entity);
        }
    }

    public void setPlayerSkillLevel(int playerId, SkillLevel averageSkillLevel) {
        playerSkillLevels.put(playerId, averageSkillLevel);
    }

    public Player getLocalPlayer() {
        return getPlayer(0);
    }

    @Override
    public Forces getForces() {
        return forces;
    }

    @Override
    public Player getPlayer(int id) {
        var player = players.get(id);
        if (player == null) {
            throw new IllegalArgumentException("No player with ID " + id + " found.");
        }
        return player;
    }

    @Override
    public void addPlayer(int id, Player player) {
        players.put(id, player);
        player.setGame(this);
        setupTeams();
    }

    @Override
    public List<Player> getPlayersList() {
        return new ArrayList<>(players.values());
    }

    @Override
    public int getNoOfPlayers() {
        return players.size();
    }

    @Override
    public List<Team> getTeams() {
        return new ArrayList<>(teams);
    }

    @Override
    public int getNoOfTeams() {
        return teams.size();
    }

    @Override
    public List<InGameObject> getInGameObjects() {
        return new ArrayList<>(inGameObjects.values());
    }

    public void removeFormation(Formation formation) {
        inGameObjects.remove(formation.getId());
    }

    public void removeEntity(Entity entity) {
        inGameObjects.remove(entity.getId());
    }

    @Override
    public void addGameListener(GameListener listener) {}

    @Override
    public void removeGameListener(GameListener listener) {}

    @Override
    public boolean isForceVictory() {
        return false;
    }

    @Override
    public void fireGameEvent(GameEvent event) {}

    @Override
    public void receiveBoard(int boardId, Board board) {}

    @Override
    public void receiveBoards(Map<Integer, Board> boards) {}

    @Override
    public void setBoard(int boardId, Board board) {}

    @Override
    public Map<Integer, Board> getBoards() {
        return Map.of();
    }

    @Override
    public int getCurrentRound() {
        return currentRound;
    }

    @Override
    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    /**
     * Empties the list of pending EntityActions completely.
     *
     * @see #getActionsVector()
     */
    public void clearActions() {
        pendingActions.clear();
    }

    /**
     * Removes all pending EntityActions by the InGameObject (Entity, unit) of the
     * given ID from the list
     * of pending actions.
     */
    public void removeActionsFor(int id) {
        pendingActions.removeIf(action -> action.getEntityId() == id);
    }

    /**
     * Remove the given EntityAction from the list of pending actions.
     */
    public void removeAction(Action action) {
        pendingActions.remove(action);
    }

    /**
     * Returns the pending EntityActions. Do not use to modify the actions; Arlith
     * said: I will be
     * angry. &gt;:[
     */
    public List<EntityAction> getActionsVector() {
        return Collections.unmodifiableList(pendingActions);
    }

    /**
     * Adds the specified action to the list of pending EntityActions for this phase
     * and fires a GameNewActionEvent.
     */
    public void addAction(Action action) {
        pendingActions.add(action);
    }

    /**
     * Clears and re-calculates the deployment table, i.e. assembles all
     * units/objects in the game
     * that are undeployed (that includes returning units or reinforcements)
     * together with the game
     * round that they are supposed to deploy on. This method can be called at any
     * time in the game
     * and will assemble deployment according to the present game state.
     */
    public void setupDeployment() {
        deploymentTable.clear();
        for (Deployable unit : deployableInGameObjects()) {
            if (!unit.isDeployed()) {
                deploymentTable.computeIfAbsent(unit.getDeployRound(), k -> new ArrayList<>()).add(unit);
            }
        }
    }

    public int lastDeploymentRound() {
        return deploymentTable.isEmpty() ? -1 : Collections.max(deploymentTable.keySet());
    }

    public boolean isDeploymentComplete() {
        return lastDeploymentRound() < currentRound;
    }

    /**
     * Check to see if we should deploy this round
     */
    public boolean shouldDeployThisRound() {
        return shouldDeployForRound(currentRound);
    }

    public boolean shouldDeployForRound(int round) {
        return deploymentTable.containsKey(round);
    }

    /**
     * Clear this round from this list of entities to deploy
     */
    public void clearDeploymentThisRound() {
        deploymentTable.remove(currentRound);
    }

    /**
     * Resets the turn index to {@link #AWAITING_FIRST_TURN}
     */
    public void resetTurnIndex() {
        turnIndex = AWAITING_FIRST_TURN;
    }

    @Override
    public int getTurnIndex() {
        return turnIndex;
    }

    @Override
    public synchronized void setForces(Forces fs) {
        forces = fs;
        forces.setGame(this);
    }

    @Override
    public void incrementCurrentRound() {
        currentRound++;
    }

    /**
     * Sets the turn index to the given value.
     *
     * @param turnIndex the new turn index
     */
    protected void setTurnIndex(int turnIndex) {
        this.turnIndex = turnIndex;
    }

    public boolean hasBoardLocation(@Nullable BoardLocation boardLocation) {
        return hasBoardLocation(boardLocation.coords(), boardLocation.boardId());
    }

    public boolean hasBoardLocation(Coords coords, int boardId) {
        return hasBoard(boardId) && getBoard(boardId).contains(coords);
    }

    public boolean hasBoard(@Nullable BoardLocation boardLocation) {
        return (boardLocation != null) && hasBoard(boardLocation.boardId());
    }

    public boolean hasBoard(int boardId) {
        return true;
    }


    /**
     * Resets this game, i.e. prepares it for a return to the lobby.
     */
    public void reset() {
        clearActions();
        inGameObjects.clear();
        turnIndex = AWAITING_FIRST_TURN;
        currentRound = -1;
        forces = new Forces(this);
    }
}
