package io.github.phucfix.bombermangame.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import io.github.phucfix.bombermangame.texture.Animations;
import io.github.phucfix.bombermangame.texture.Drawable;

/**
 * A bomb is a static object with a hitbox, so the player cannot walk through it.
 */
/**
 * A bomb is a static object with a hitbox, so the player cannot walk through it.
 */
public class Bomb implements Drawable {

    // We would normally get the position from the hitbox, but since we don't need to move the bomb, we can store the position directly.
    private final float x;
    private final float y;
    private float bombTimer;
    private final Body hitbox;
    public static final float BOMB_EXPLOSION_TIME = 3 ;

    /// Static as it changes for all the bomb objects
    private static int activeBombs = 0;
    /// Initially only one bomb at a time
    private static int maxConcurrentBombs = 1;


    private static int currentBombRadius = 1;
    /// Initially only one bomb at a time
    private static final int MAX_BOMB_RADIUS = 8;

    private boolean bombActive ;



    /**
     * Create a bomb at the given position.
     * @param world The Box2D world to add the chest's hitbox to.
     * @param x The X position.
     * @param y The Y position.
     */
    public Bomb(World world, float x, float y) {
        this.x = x;
        this.y = y;
        this.hitbox = createHitbox(world, x, y);
        this.bombTimer = 0;
        this.bombActive = true;
        this.setSensor(true);
        // Since the hitbox never moves, and we never need to change it, we don't need to store a reference to it.
    }

    /**
     * Create a Box2D body for the bomb.
     * @param world The Box2D world to add the body to.
     */
    private Body createHitbox(World world,float x, float y) {
        // BodyDef is like a blueprint for the movement properties of the body.
        BodyDef bodyDef = new BodyDef();
        // Static bodies never move, but static bodies can collide with them.
        bodyDef.type = BodyDef.BodyType.StaticBody;
        // Set the initial position of the body.
        bodyDef.position.set(x,y);
        // Create the body in the world using the body definition.
        Body body = world.createBody(bodyDef);
        // Now we need to give the body a shape so the physics engine knows how to collide with it.
        // We'll use a polygon shape for the chest.
        PolygonShape box = new PolygonShape();
        // Make the polygon a square with a side length of 1 tile.
        box.setAsBox(0.4f, 0.4f);
        // Attach the shape to the body as a fixture.
        body.createFixture(box, 1.0f);
        // We're done with the shape, so we should dispose of it to free up memory.
        box.dispose();
        // Set the chest as the user data of the body so we can look up the chest from the body later.
        body.setUserData(this);
        return body;
    }

    public void tick(float frameTime) {
        this.bombTimer += frameTime;
    }

    @Override
    public TextureRegion getCurrentAppearance() {
        /// If the bomb has exploded, show the explosion animation.
        if (bombTimer >= BOMB_EXPLOSION_TIME) {
            destroy(); /// Deactivate the bomb's hitbox when the bomb explodes.
            /// Show the explosion animation
            // If the animation has finished, return null (indicating the segment is gone)
            TextureRegion explosionAnimation = Animations.EXPLOSION_CENTER.getKeyFrame(this.bombTimer - BOMB_EXPLOSION_TIME,false);
            if (Animations.EXPLOSION_CENTER.isAnimationFinished(this.bombTimer - BOMB_EXPLOSION_TIME)) {
                return null;
            }
            // Get the current frame based on elapsed time
            return explosionAnimation;
        }
        /// Shows the ticking animation, looping as long as the bomb is ticking
        return Animations.BOMB_TICKING.getKeyFrame(this.bombTimer, true);
    }

    public static int getCurrentBombRadius() {
        return currentBombRadius;
    }

    public static void incrementCurrentBombRadius(){
        if(currentBombRadius < MAX_BOMB_RADIUS){
            currentBombRadius++;
        }
    }

    public static void setCurrentBombRadius(int currentBombRadius) {
        Bomb.currentBombRadius = currentBombRadius;
    }

    /// Methods to monitor the active Bombs
    public static void incrementActiveBombs() {
        activeBombs++;
    }
    public static void decrementActiveBombs() {
        activeBombs--;
    }
    public static int getActiveBombs() {
        return activeBombs;
    }
    public static void setActiveBombs(int n) {
        activeBombs = n;
    }
    public static void setMaxConcurrentBombs(int n) {
        maxConcurrentBombs = n;
    }


    public static int getMaxConcurrentBombs() {
        return maxConcurrentBombs;
    }
    /// As the Player can plant at-most 8 concurrent bombs
    public static void incrementMaxConcurrentBombs() {
        if (maxConcurrentBombs < 8) {
            maxConcurrentBombs++;
        }
    }


    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void destroy(){
        hitbox.setActive(false);
        setBombActive(false);
    }

    /// Used to solidify the bomb as soon as the player is outside the bomb grid
    public void setSensor(boolean isSensor) {
        for (Fixture fixture : hitbox.getFixtureList()) {
            fixture.setSensor(isSensor);
        }
    }

    public float getBombTimer() {
        return bombTimer;
    }

    public boolean isBombActive() {
        return bombActive;
    }

    public void setBombActive(boolean bombActive) {
        this.bombActive = bombActive;
    }
}
