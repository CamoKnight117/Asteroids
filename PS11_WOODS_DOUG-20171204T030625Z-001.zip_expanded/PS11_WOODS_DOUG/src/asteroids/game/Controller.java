package asteroids.game;

import static asteroids.game.Constants.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.*;
import asteroids.participants.Asteroid;
import asteroids.participants.Bullet;
import asteroids.participants.Ship;

/**
 * Controls a game of Asteroids.
 */
public class Controller implements KeyListener, ActionListener
{
    /** The state of all the Participants */
    private ParticipantState pstate;

    /** The ship (if one is active) or null (otherwise) */
    private Ship ship;
    
    private Bullet bullet;

    /** When this timer goes off, it is time to refresh the animation */
    private Timer refreshTimer;

    /**
     * The time at which a transition to a new stage of the game should be made. A transition is scheduled a few seconds
     * in the future to give the user time to see what has happened before doing something like going to a new level or
     * resetting the current level.
     */
    private long transitionTime;

    /** Number of lives left */
    private int lives;

    /** The game display */
    private Display display;

    /** Set of keys currently pressed */
    private final Set<Integer> pressed = new HashSet<>();

    /**
     * Constructs a controller to coordinate the game and screen
     */
    public Controller ()
    {
        // Initialize the ParticipantState
        pstate = new ParticipantState();

        // Set up the refresh timer.
        refreshTimer = new Timer(FRAME_INTERVAL, this);

        // Clear the transitionTime
        transitionTime = Long.MAX_VALUE;

        // Record the display object
        display = new Display(this);

        // Bring up the splash screen and start the refresh timer
        splashScreen();
        display.setVisible(true);
        refreshTimer.start();
    }

    /**
     * Returns the ship, or null if there isn't one
     */
    public Ship getShip ()
    {
        return ship;
    }

    /**
     * Configures the game screen to display the splash screen
     */
    private void splashScreen ()
    {
        // Clear the screen, reset the level, and display the legend
        clear();
        display.setLegend("Asteroids");

        // Place four asteroids near the corners of the screen.
        placeAsteroids();
    }

    /**
     * The game is over. Displays a message to that effect.
     */
    private void finalScreen ()
    {
        display.setLegend(GAME_OVER);
        display.removeKeyListener(this);
    }

    /**
     * Place a new ship in the center of the screen. Remove any existing ship first.
     */
    private void placeShip ()
    {
        // Place a new ship
        Participant.expire(ship);
        ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
        addParticipant(ship);
        display.setLegend("");
    }

    /**
     * Places an asteroid near one corner of the screen. Gives it a random velocity and rotation.
     */
    private void placeAsteroids ()
    {
        addParticipant(new Asteroid(RANDOM.nextInt(3), 2, EDGE_OFFSET, EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(3), 2, SIZE - EDGE_OFFSET, EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(3), 2, SIZE - EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(3), 2, EDGE_OFFSET, SIZE - EDGE_OFFSET, 3, this));
    }

    /**
     * Clears the screen so that nothing is displayed
     */
    private void clear ()
    {
        pstate.clear();
        display.setLegend("");
        ship = null;
    }

    /**
     * Sets things up and begins a new game.
     */
    private void initialScreen ()
    {
        // Clear the screen
        clear();

        // Plac asteroids
        placeAsteroids();

        // Place the ship
        placeShip();

        // Reset statistics
        lives = 1;

        // Start listening to events (but don't listen twice)
        display.removeKeyListener(this);
        display.addKeyListener(this);

        // Give focus to the game screen
        display.requestFocusInWindow();
    }

    /**
     * Adds a new Participant
     */
    public void addParticipant (Participant p)
    {
        pstate.addParticipant(p);
    }

    /**
     * The ship has been destroyed
     */
    public void shipDestroyed ()
    {
        // Null out the ship
        ship = null;

        // Display a legend
        display.setLegend("Ouch!");

        // Decrement lives
        lives--;

        // Since the ship was destroyed, schedule a transition
        scheduleTransition(END_DELAY);
    }

    /**
     * An asteroid has been destroyed
     */
    public void asteroidDestroyed ()
    {
        // If all the asteroids are gone, schedule a transition
        if (pstate.countAsteroids() == 0)
        {
            scheduleTransition(END_DELAY);
        }
    }

    /**
     * Schedules a transition m msecs in the future
     */
    private void scheduleTransition (int m)
    {
        transitionTime = System.currentTimeMillis() + m;
    }

    /**
     * This method will be invoked because of button presses and timer events.
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        // The start button has been pressed. Stop whatever we're doing
        // and bring up the initial screen
        if (e.getSource() instanceof JButton)
        {
            initialScreen();
        }

        // Time to refresh the screen and deal with keyboard input
        else if (e.getSource() == refreshTimer)
        {
            // It may be time to make a game transition
            performTransition();
            checkAllPressed();
            // Move the participants to their new locations
            pstate.moveParticipants();

            // Refresh screen
            display.refresh();
        }
    }

    /**
     * Returns an iterator over the active participants
     */
    public Iterator<Participant> getParticipants ()
    {
        return pstate.getParticipants();
    }

    /**
     * If the transition time has been reached, transition to a new state
     */
    private void performTransition ()
    {
        // Do something only if the time has been reached
        if (transitionTime <= System.currentTimeMillis())
        {
            // Clear the transition time
            transitionTime = Long.MAX_VALUE;

            // If there are no lives left, the game is over. Show the final
            // screen.
            if (lives <= 0)
            {
                finalScreen();
            }
        }
    }

    /**
     * If a key of interest is pressed, record that it is down.
     */
    @Override
    public void keyPressed (KeyEvent e)
    {
        if(e.getKeyCode() != KeyEvent.VK_SPACE)
        {
        pressed.add(e.getKeyCode());
        }
        else
        {
            if(!(pstate.countBullets() >= Constants.BULLET_LIMIT && ship != null)) {
                addParticipant(new Bullet(this, ship));
            }
        }
    }
    /**
     * These events are ignored.
     */
    @Override
    public void keyTyped (KeyEvent e)
    {
    }

    /**
     * These events are ignored.
     */
    @Override
    public void keyReleased (KeyEvent e)
    {
        pressed.remove(e.getKeyCode());
        /*
         * if (e.getKeyCode() == KeyEvent.VK_UP && ship != null) { pressed.remove('w'); } else if (e.getKeyCode() ==
         * KeyEvent.VK_RIGHT && ship != null) { pressed.remove('d'); } else if (e.getKeyCode() == KeyEvent.VK_LEFT &&
         * ship != null) { pressed.remove('a'); } else { pressed.remove(e.getKeyChar()); }
         */
    }

    private void checkAllPressed ()
    {
        for (int key : pressed)
        {
            if ((key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) && ship != null)
            {
                if(pressed.contains(KeyEvent.VK_D) && pressed.contains(KeyEvent.VK_RIGHT)) {
                ship.turnLeft();
                }
                ship.turnRight();
            }
            if ((key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) && ship != null)
            {
                ship.turnLeft();
            }
            if ((key == KeyEvent.VK_W || key == KeyEvent.VK_UP) && ship != null)
            {
                ship.accelerate();
            }
            if (key == KeyEvent.VK_SPACE && ship != null)
            {
                if(!(pstate.countBullets() >= Constants.BULLET_LIMIT)) {
                    addParticipant(new Bullet(this, ship));
                }
            }
        }
    }
}
