package io.github.phucfix.bombermangame;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import io.github.phucfix.bombermangame.audio.MusicTrack;
import io.github.phucfix.bombermangame.map.GameMap;
import io.github.phucfix.bombermangame.screen.GameScreen;
import io.github.phucfix.bombermangame.screen.MenuScreen;
import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import io.github.phucfix.bombermangame.screen.PauseScreen;
import io.github.phucfix.bombermangame.screen.TutorialScreen;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

/**
 * The BomberQuestGame class represents the core of the Bomber Quest game.
 * It manages the screens and global resources like SpriteBatch and Skin.
 */
public class BombermanGame extends Game {

    /**
     * Sprite Batch for rendering game elements.
     * This eats a lot of memory, so we only want one of these.
     */
    private SpriteBatch spriteBatch;

    /** The game's UI skin. This is used to style the game's UI elements. */
    private Skin skin;
    
    /**
     * The file chooser for loading map files from the user's computer.
     * This will give you access to a {@link com.badlogic.gdx.files.FileHandle} object,
     * which you can use to read the contents of the map file as a String, and then parse it into a {@link GameMap}.
     */
    private final NativeFileChooser fileChooser;

    //The hash map store coord of the respective object
    private HashMap<String, String> coordinatesAndObjects = new HashMap();
    private boolean userChoosenMap;

    /**
     * The map. This is where all the game objects are stored.
     * This is owned by {@link BombermanGame} and not by {@link GameScreen}
     * because the map should not be destroyed if we temporarily switch to another screen.
     */
    private GameMap map;

    /**
     * Constructor for BomberQuestGame.
     *
     * @param fileChooser The file chooser for the game, typically used in desktop environment.
     */
    public BombermanGame(NativeFileChooser fileChooser) {
        this.fileChooser = fileChooser;
    }

    /**
     * Called when the game is created. Initializes the SpriteBatch and Skin.
     * During the class constructor, libGDX is not fully initialized yet.
     * Therefore this method serves as a second constructor for the game,
     * and we can use libGDX resources here.
     */
    @Override
    public void create() {
        // Create sprite batch for rendering
        this.spriteBatch = new SpriteBatch();

        // Load UI Skin
        this.skin = new Skin(Gdx.files.internal("skin/craftacular/craftacular-ui.json"));

        // Create new map, or can load map from file instead
        this.map = new GameMap(this);

        // Play some background music
        //MusicTrack.BACKGROUND.play();

        // Navigate to the menu screen
        goToMenu();
    }

    /**
     * Switches to the menu screen.
     */
    public void goToMenu() {
        MusicTrack.PLAYER_MOVE.stop();
        MusicTrack.BACKGROUND2.stop();
        MusicTrack.BACKGROUND.play();
        this.setScreen(new MenuScreen(this)); // Set the current screen to MenuScreen
    }

    /**
     * Switches to the game screen.
     */
    public void goToGame() {
        MusicTrack.BACKGROUND.stop();
        MusicTrack.BACKGROUND2.play();
        this.setScreen(new GameScreen(this)); // Set the current screen to GameScreen
    }

    /**
     * Go to pause screen
     */
    public void goToPauseScreen() {
        MusicTrack.BACKGROUND2.stop();
        MusicTrack.BACKGROUND.play();
        this.setScreen(new PauseScreen(this));
    }

    public void goToTutorial() {
        this.setScreen(new TutorialScreen(this));
    }

    /**
     * Goes to map selected by user
     */
    public void goToSelectedMap() {
        MusicTrack.BACKGROUND.stop();
        MusicTrack.BACKGROUND2.play();
        this.setScreen(new GameScreen(this));
    }

    /** Returns the skin for UI elements. */
    public Skin getSkin() {
        return skin;
    }

    /** Returns the main SpriteBatch for rendering. */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    /** Returns the current map, if there is one. */
    public GameMap getMap() {
        return map;
    }
    
    /**
     * Switches to the given screen and disposes of the previous screen.
     * @param screen the new screen
     */
    @Override
    public void setScreen(Screen screen) {
        Screen previousScreen = super.screen;
        super.setScreen(screen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    public NativeFileChooser getFileChooser() {
        return fileChooser;
    }

    public HashMap<String, String> getCoordinatesAndObjects() {
        return coordinatesAndObjects;
    }

    public void setCoordinatesAndObjects(HashMap<String, String> coordinatesAndObjects) {
        this.coordinatesAndObjects = coordinatesAndObjects;
    }

    public boolean isUserChoosenMap() {
        return userChoosenMap;
    }

    public void setUserChoosenMap(boolean userChoosenMap) {
        this.userChoosenMap = userChoosenMap;
    }

    public void loadFileChooser() {
        // First params of chooseFile method, important to open dir and lead to the correct dir
        NativeFileChooserConfiguration configuration = new NativeFileChooserConfiguration();
        // Title
        configuration.title = "Please choose file map";
        // Directory, most important, without this the user won't show up
        configuration.directory = Gdx.files.getFileHandle("maps/", Files.FileType.Internal);
        // Optional: Intent
        // configuration.intent = NativeFileChooserIntent.OPEN;

        // Filter by suffixes
        configuration.nameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".properties");
            }
        };

        // Params to decide what happens like choosing a file and cancel the process
        NativeFileChooserCallback fileChooserCallback = new NativeFileChooserCallback() {
            @Override
            public void onFileChosen(FileHandle file) {
                setUserChoosenMap(true);

                // Read the properties files
                String EntireText = file.readString();
                // Split map properties
                String[] linesOfText = EntireText.split("\n");

                ///This method will take that array, and will split it again on the basis of "=",
                ///So the end result should bring us to the selected map in the game.
                doYourMagic(linesOfText);
            }

            @Override
            public void onCancellation() {
                System.out.println("Cancel");
                setUserChoosenMap(false);
            }

            @Override
            public void onError(Exception e) {
                System.out.println("Error " + e);
                setUserChoosenMap(false);
            }
        };

        ///Then we put the arguments in this method, so that everything comes together in the end.
        fileChooser.chooseFile(configuration, fileChooserCallback);
    }

    // Converting array String into hashmap and then invoking the Game Map constructor
    // The Constructor will parse hashmap to GameMap, creating object in that map
    public void doYourMagic(String[] linesOfText) {
        for (String line : linesOfText) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] keyValue = line.split("=");
            coordinatesAndObjects.put(keyValue[0].trim(), keyValue[1].trim());
        }

        this.map = new GameMap(this, coordinatesAndObjects);
        goToSelectedMap();
    }

    /** Cleans up resources when the game is disposed. */
    @Override
    public void dispose() {
        getScreen().hide(); // Hide the current screen
        getScreen().dispose(); // Dispose the current screen
        spriteBatch.dispose(); // Dispose the spriteBatch
        skin.dispose(); // Dispose the skin
    }
}
