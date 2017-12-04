package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Constants;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class Bullet extends Participant implements AsteroidDestroyer
{
    private Shape outline;
    
    private Controller controller;
    
    /**
     * 
     * @param controller
     * @param ship
     */
    public Bullet (Controller controller, Ship ship)
    {
        
        // Create the asteroid
        this.controller = controller;
        setPosition(ship.getXNose(), ship.getYNose());
        setVelocity(Constants.BULLET_SPEED, ship.getRotation());
        createBullet();
    }
    
    private void createBullet() {
        Path2D.Double poly = new Path2D.Double();
        poly.moveTo(1, 1);
        poly.lineTo(1, -1);
        poly.lineTo(-1, -1);
        poly.lineTo(-1, 1);
        poly.closePath();
        
        outline = poly;
        
        new ParticipantCountdownTimer(this,Constants.BULLET_DURATION);
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof ShipDestroyer)
        {
            // Expire the asteroid
            Participant.expire(this);

            // Inform the controller
            controller.asteroidDestroyed();
        }
    }
    
    @Override
    public void countdownComplete(Object payload) {
        Participant.expire(this);
    }

}
