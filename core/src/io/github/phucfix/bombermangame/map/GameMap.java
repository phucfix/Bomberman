package io.github.phucfix.bombermangame.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.phucfix.bombermangame.BombermanGame;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents the game map.
 * Holds all the objects and entities in the game.
 */
public class GameMap {
    
    // A static block is executed once when the class is referenced for the first time.
    static {
        // Initialize the Box2D physics engine.
        com.badlogic.gdx.physics.box2d.Box2D.init();
    }
    
    // Box2D physics simulation parameters (you can experiment with these if you want, but they work well as they are)
    /**
     * The time step for the physics simulation.
     * This is the amount of time that the physics simulation advances by in each frame.
     * It is set to 1/refreshRate, where refreshRate is the refresh rate of the monitor, e.g., 1/60 for 60 Hz.
     */
    private static final float TIME_STEP = 1f / Gdx.graphics.getDisplayMode().refreshRate;
    /** The number of velocity iterations for the physics simulation. */
    private static final int VELOCITY_ITERATIONS = 6;
    /** The number of position iterations for the physics simulation. */
    private static final int POSITION_ITERATIONS = 2;
    /**
     * The accumulated time since the last physics step.
     * We use this to keep the physics simulation at a constant rate even if the frame rate is variable.
     */
    private float physicsTime = 0;
    
    /** The game, in case the map needs to access it. */
    private final BombermanGame game;
    /** The Box2D world for physics simulation. */
    private final World world;

    public float mapWidth, mapHeight;

    // Game objects
    private final Player player;
    
    private final Chest chest;

    private final Wall[][] walls;

    private final BreakableWall[][] breakableWalls;
    
    private final Flowers[][] flowers;

    public GameMap(BombermanGame game) {
        this.game = game;
        this.world = new World(Vector2.Zero, true);
        // Create a player with initial position (1, 3)
        this.player = new Player(this.world, 1, 15);
        // Create a chest in the map
        this.chest = new Chest(world, 8, 15);
        // Create flowers in a 7x7 grid
        this.walls = new Wall[29][17];
        for (int i = 0; i < walls.length; i++) {
            for (int j = 0; j < walls[i].length; j++) {
                // Place wall only on the boundary cells (edges)
                if (i == 0 || i == 28 || j == 0 || j == 16 || (i % 2 == 0 && j % 2 == 0)) {
                    this.walls[i][j] = new Wall(this.world, i, j);
                }
            }
        }

        this.breakableWalls = new BreakableWall[29][17];
        for (int i = 1; i < breakableWalls.length; i++) {
            for (int j = 1; j < breakableWalls[i].length; j++) {
                // Place walls only on the boundary cells (edges)
                if ((i % 2 == 1 && j % 2 == 1)&& i > 8 && j > 8) {
                    this.breakableWalls[i][j] = new BreakableWall(this.world, i, j);
                }
            }
        }

        this.flowers = new Flowers[28][16];
        for (int i = 0; i < flowers.length; i++) {
            for (int j = 0; j < flowers[i].length; j++) {
                this.flowers[i][j] = new Flowers(i, j);
            }
        }

        this.mapWidth = flowers.length;
        this.mapHeight = flowers[0].length;
    }
    
    /**
     * Updates the game state. This is called once per frame.
     * Every dynamic object in the game should update its state here.
     * @param frameTime the time that has passed since the last update
     */
    public void tick(float frameTime) {
        this.player.tick(frameTime);
        doPhysicsStep(frameTime);
    }
    
    /**
     * Performs as many physics steps as necessary to catch up to the given frame time.
     * This will update the Box2D world by the given time step.
     * @param frameTime Time since last frame in seconds
     */
    private void doPhysicsStep(float frameTime) {
        this.physicsTime += frameTime;
        while (this.physicsTime >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            this.physicsTime -= TIME_STEP;
        }
    }
    
    /** Returns the player on the map. */
    public Player getPlayer() {
        return player;
    }
    
    /** Returns the chest on the map. */
    public Chest getChest() {
        return chest;
    }

    public List<Wall> getWalls() {
        // Turn two dimensions array to list without null element
        return Arrays.stream(walls).filter(Objects::nonNull).flatMap(Arrays::stream).toList();
    }

    public List<BreakableWall> getBreakableWalls() {
        return Arrays.stream(breakableWalls).filter(Objects::nonNull).flatMap(Arrays::stream).toList();
    }
    
    /** Returns the flowers on the map. */
    public List<Flowers> getFlowers() {
        return Arrays.stream(flowers).flatMap(Arrays::stream).toList();
    }
}
