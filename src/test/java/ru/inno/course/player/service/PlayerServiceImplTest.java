package ru.inno.course.player.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.inno.course.player.data.DataProviderJSON;
import ru.inno.course.player.data.DataProviderXML;
import ru.inno.course.player.model.Player;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

public class PlayerServiceImplTest {

    private PlayerServiceImpl playerService;

    DataProviderJSON dataProviderJSON;

    DataProviderXML dataProviderXML;
    private final static Path FILEPATH = Path.of("./data.json");

    @BeforeEach
    public void setUp() throws IOException {
        if (Files.exists(FILEPATH)) {
            Files.write(FILEPATH, new byte[0]);
        }
        dataProviderJSON = new DataProviderJSON();
        dataProviderXML = new DataProviderXML();
        playerService = new PlayerServiceImpl();
    }

    // Позитивные тесты

    @Test
    public void testAddPlayerAndCheckPresenceInList() {
        Player player = getPlayer();

        playerService.createPlayer(player.getNick());

        Collection<Player> players = playerService.getPlayers();

        assertFalse(players.isEmpty());
        assertTrue(players.contains(player));
    }

    @Test
    public void testRemovePlayer_CheckAbsenceInList() {
        playerService.createPlayer(getPlayer().getNick());
        playerService.deletePlayer(1);

        Collection<Player> players = playerService.getPlayers();


        assertTrue(players.isEmpty());
    }

    @Test
    public void testAddPlayerWithOutJsonFile() throws IOException {
        File file = FILEPATH.toFile();
        if (!file.exists()) {
            file.createNewFile();
         }
        playerService.createPlayer("player");
        Collection<Player> players = playerService.getPlayers();

        assertFalse(players.isEmpty());
    }

    @Test
    public void testAddPlayerWithJsonFile() {
        File file = FILEPATH.toFile();
        if (file.exists()) {
            playerService.createPlayer("player");
        }

        Collection<Player> players = playerService.getPlayers();

        assertFalse(players.isEmpty());
    }

    @Test
    public void testAddPointsToExistingPlayer() {
        Player player = getPlayer();

        playerService.createPlayer(player.getNick());
        playerService.addPoints(player.getId(), 10);

        Player expectedPlayer = playerService.getPlayerById(player.getId());

        assertEquals(10, expectedPlayer.getPoints());
    }

    @Test
    public void testAddPointsOnTopOfExistingPoints() {
        Player player = getPlayer();

        playerService.createPlayer(player.getNick());
        playerService.addPoints(player.getId(), 10);
        playerService.addPoints(player.getId(), 10);

        Player expectedPlayer = playerService.getPlayerById(player.getId());

        assertEquals(20, expectedPlayer.getPoints());
    }

    @Test
    public void testGetPlayerById() {
        Player player = getPlayer();

        playerService.createPlayer(player.getNick());

        Player expectedPlayer = playerService.getPlayerById(player.getId());

        assertEquals(expectedPlayer, player);
    }

    @Test
    public void testSuccessfulGetJson() throws IOException {
        Player player = getPlayer();

        playerService.createPlayer(player.getNick());

        Collection<Player> players = dataProviderJSON.load();

        assertFalse(players.isEmpty());
    }

    @Test
    public void testSuccessfulSaveToJson() throws IOException {
        Player player = getPlayer();
        List<Player> players = new ArrayList<>();
        players.add(player);

        dataProviderJSON.save(players);

        Collection<Player> expectedPlayers = dataProviderJSON.load();

        assertFalse(expectedPlayers.isEmpty());

    }

    @Test
    public void testSuccessfulUniqueId() {
        playerService.createPlayer("1");
        playerService.createPlayer("2");
        playerService.createPlayer("3");
        playerService.createPlayer("4");
        playerService.createPlayer("5");

        playerService.getPlayerById(3);

        playerService.createPlayer("6");

        Player player = playerService.getPlayerById(6);

        assertNotNull(player);
    }

    @Test
    public void testCreatePlayerWithChars() {
        playerService.createPlayer("123456789123456");

        Player player = playerService.getPlayerById(1);

        assertNotNull(player);
    }

    // Негативные тесты

    @Test()
    public void testAddPlayerWithEmptyNickname() {
        playerService.createPlayer("1");
        playerService.createPlayer("2");
        playerService.createPlayer("3");
        playerService.createPlayer("4");
        playerService.createPlayer("5");

        assertThrows(NoSuchElementException.class, () -> {
            playerService.deletePlayer(10);
        }, "No such user: 10");

    }

    @Test
    public void testAddTwoPlayersDuplicates() {
        playerService.createPlayer("1");

        assertThrows(IllegalArgumentException.class, () -> {
            playerService.createPlayer("1");
        }, "Nickname is already in use: 1");
    }


    @Test
    public void testGetGhostPlayers() {
        playerService.createPlayer("1");
        assertThrows(NoSuchElementException.class, () -> {
            playerService.getPlayerById(2);
        }, "No such user: 2");
    }

    @Test
    public void testSavePlayerWithoutNick() {
        playerService.createPlayer(null);
        // сохраняется с пустым/null ником
    }

    @Test
    public void testAddMinusPoints() {
        Player player = getPlayer();

        playerService.createPlayer(player.getNick());
        playerService.addPoints(player.getId(), -10);

        Player expectedPlayer = playerService.getPlayerById(player.getId());

        assertEquals(-10, expectedPlayer.getPoints());

        // начисляются отрицательные очки
    }

    @Test
    public void testAddPointsToGhostPlayer() {
        Player player = getPlayer();

        playerService.createPlayer(player.getNick());
        assertThrows(NoSuchElementException.class, () -> {
            playerService.addPoints(2, 10);
        }, "No such user: 2");

    }

    private Player getPlayer() {
        return new Player(1, "player", 0, true);
    }
}

