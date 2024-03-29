package com.oneoneone.game.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.oneoneone.game.BubbleMath;
import com.oneoneone.game.states.PlayState;

import java.util.Random;

/**
 * Created by David on 9/07/2016.
 */
public class Bubble {
    private static final int RANGE = 20;            //number of atoms
    private static final int BLUESTARTX = 1280;     //starting x coordinate for blue
    private static final int REDSTARTX = 0;         //starting x coordinate for red
    private static final int BUOYANCY = 1;          //velocity added each update to give effect of buoyancy
    private int atomicNumber;           //random "mass" of the bubble used to generate number in bubble and size
    private int sizeCurrent;            //current bubble size calculated in update()
    private float scaleFactor = 0;      //the scaling factor used to reach final size
    private int sizeFinal;              //final bubble size reached in update()
    private float dt;                   //poll time of current (or last) update
    private boolean isRed;              //is it red
    float x_touch_location;             //set as data members to calculate velocity after touch
    float y_touch_location;
    private Sprite sprite;          //sprite container for bubbles
    private Vector2 velocity;       //speed at which bubble moves
    private Vector2 position;       //position of bubble
    private Circle circleBound;     //collision detection representation of bubble

    /**
     * Bubble() creates an instance of a bubble sprite for the array.
     * The colour is selected using a random Boolean isRed; true creates a
     * blue bubble, false creates red.
     * A random scale factor is then created to be attached to the bubble.
     */
    public Bubble(boolean isRed) {
        this.isRed = isRed;
        Random rand = new Random();
        velocity = new Vector2(rand.nextInt(1000), rand.nextInt(1000));//set random velocity vector
        sprite = new Sprite(setTexture(isRed));
        atomicNumber = rand.nextInt(RANGE); //this generates the number that will be attached to the bubble and is used to define scale.
        sizeFinal = (int)Math.round((0.4+(0.6* atomicNumber)/RANGE) * sprite.getWidth());//sets scale between 0.4 (100 pix) and 1 (250 pix)
        circleBound = new Circle(position.x + (sizeCurrent / 2), position.y + sizeCurrent / 2, sizeCurrent / 2);
    }
    public Texture setTexture(boolean isRed){
        Texture texture;
        if (isRed) {
            texture = new Texture("red.png");
            position = new Vector2(REDSTARTX, 0);
        } else {
            texture = new Texture("blue.png");
            position = new Vector2(BLUESTARTX, BubbleMath.HEIGHT);
        }
        return texture;
    }

    public void update(float dt) { //dt = amount of time passed since last update
        this.dt = dt;
        if (isRed) {
            velocity.add(0, -BUOYANCY);
        } else {
            velocity.add(0, BUOYANCY);
        }
        velocity.scl(dt);
        if ((scaleFactor <= 1)) {
            scaleFactor += dt; //collect dt
            sizeCurrent = Math.round(sizeFinal * scaleFactor); //increase bubble scale
            circleBound.setRadius(sizeCurrent / 2);
        } else{
            position.add(velocity.x, velocity.y);
            circleBound.set(position, sizeCurrent / 2);
        }
        cornerCollision(); //detect collision after coordinates updated
        velocity.scl(1 / dt);
    }

    public Vector2 getPosition() {
        return position;
    }

    /* grabBubble() is called on touch and directs bubbles within 50 units
     * towards the touch location.
     */
    public void grabBubble(int pointer) {
        //the following retrieve the x and y coordinates of the current touch.
        //float previous_x_touch = x_touch_location;
        //float previous_y_touch = y_touch_location;
        x_touch_location = PlayState.X_SCALE_FACTOR * (Gdx.input.getX(pointer));
        y_touch_location = PlayState.Y_SCALE_FACTOR * (PlayState.SCREEN_HEIGHT - Gdx.input.getY(pointer));

        //the following creates a pair of variables to check the difference between
        //the bubble position and the touch location.
        float x_touch_difference = (position.x + sizeCurrent / 2) - x_touch_location;
        float y_touch_difference = (position.y + sizeCurrent / 2) - y_touch_location;

        //Check to see if the bubbles are in range of the touch and if so
        //direct them to the touch location.
        if (Math.hypot(x_touch_difference, y_touch_difference) < sizeCurrent / 2) {
            setThrowVelocity(x_touch_difference, y_touch_difference);
        }
    }
    public void setThrowVelocity(float x, float y){
        position.x = x_touch_location - (sizeCurrent / 2);//-x_touch_difference; //set bubble to position of touch
        position.y = y_touch_location - (sizeCurrent / 2);//-y_touch_difference; //offset texture.getWidth()/2 to centre bubble on touch (TODO needs better centre method for scaling)
        velocity.set((x_touch_location - x) / dt,(y_touch_location - y) / dt);
        if (velocity.x >= 15 / dt) {
            velocity.x = 15f / dt;
        }
        if (velocity.y >= 15 / dt) {
            velocity.y = 15f / dt;
        }
        if (velocity.x <= -15 / dt) {
            velocity.x = -15f / dt;
        }
        if (velocity.y <= -15 / dt) {
            velocity.y = -15f / dt;
        }

    }
    public Circle getCircleBound() {
        return circleBound;
    }

    /** Work in progress; resizes surviving bubble after collision.
     *  I want to implement a change in vector and velocity based
     *  on the change in mass (i.e. dE=dMc^2->v=sqrt(2E/m), etc.
     */
    public void setNewSize(int newMass){
        //scaleFactor = -atomicNumber/newMass;
        atomicNumber = newMass;
        sizeFinal = (int)Math.round(sprite.getWidth() * (0.4+(0.6* atomicNumber)/RANGE));
        sizeCurrent = sizeFinal;
    //        velocity.set(newV);
    }
    public void cornerCollision() {
        if ((position.y < 0)) {
            position.y = 0;
            velocity.y = -velocity.y;
        }
        if (position.y > (BubbleMath.HEIGHT - sizeCurrent)) {
            velocity.y = -velocity.y;
            position.y = BubbleMath.HEIGHT - sizeCurrent;
        }
        if (position.x < 0) {
            position.x = 0;
            velocity.x = -velocity.x;
        }
        if (position.x > (BubbleMath.WIDTH - sizeCurrent)) {
            position.x = BubbleMath.WIDTH - sizeCurrent;
            velocity.x = -velocity.x;
        }
    }

    public Sprite getSprite() {
        return sprite;
    }

    public int getSizeCurrent() {
        return sizeCurrent;
    }

    public int getAtomicNumber() {
        return atomicNumber;
    }
}


