package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Vector2;

public class Physics implements ContactListener, StepListener {
    private World world;
    private BallPocketedListener ballPocketedListener;
    private ObjectsRestListener objectsRestListener;
    private BallsCollisionListener ballsCollisionListener;

    public Physics(){
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);

        this.world.addListener(this);
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void begin(Step step, World world) {

    }

    @Override
    public void updatePerformed(Step step, World world) {

    }

    @Override
    public void postSolve(Step step, World world) {

    }

    @Override
    public void end(Step step, World world) {
        int ballsMoving = 0;

        for (Ball ball : Ball.values()) {
            if (!ball.getBody().getLinearVelocity().isZero()){
                ballsMoving++;
            }
        }

        if (ballsMoving > 0){
            objectsRestListener.onEndAllObjectsRest();
        } else {
            objectsRestListener.onStartAllObjectsRest();
        }

    }

    @Override
    public void sensed(ContactPoint point) {

    }

    @Override
    public boolean begin(ContactPoint point) {
        Body body1 = point.getBody1();
        Body body2 = point.getBody2();

        if (body1.getUserData() instanceof Ball && body2.getUserData() instanceof Ball) {
            ballsCollisionListener.onBallsCollide(
                    (Ball) body1.getUserData(),
                    (Ball) body2.getUserData()
            );
        }
        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        if(point.isSensor()){
            Body body1 = point.getBody1();
            Body body2 = point.getBody2();
            if (body1.getUserData() instanceof Ball) {
                Vector2 ballPosition = body1.getTransform().getTranslation();
                Vector2 pocketPosition = body2.getTransform().getTranslation();

                // get pocket center
                Vector2 pocketCenter = point.getFixture2().getShape().getCenter();

                // get pocket position in world
                Vector2 pocketPositionInWorld = pocketPosition.add(pocketCenter);

                if (ballPosition.difference(pocketPositionInWorld).getMagnitude() <= Ball.Constants.RADIUS) {
                    ballPocketedListener.onBallPocketed((Ball) body1.getUserData());
                }
            } else if (body2.getUserData() instanceof Ball) {
                Vector2 ballPosition = body2.getTransform().getTranslation();
                Vector2 pocketPosition = body1.getTransform().getTranslation();

                // get pocket center
                Vector2 pocketCenter = point.getFixture1().getShape().getCenter();

                // get pocket position in world
                Vector2 pocketPositionInWorld = pocketPosition.add(pocketCenter);

                if (ballPosition.difference(pocketPositionInWorld).getMagnitude() <= Ball.Constants.RADIUS) {
                    ballPocketedListener.onBallPocketed((Ball) body2.getUserData());
                }
            }
        }
        return true;
    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }

    public void setBallPocketedListener(BallPocketedListener ballPocketedListener) {
        this.ballPocketedListener = ballPocketedListener;
    }

    public void setObjectsRestListener(ObjectsRestListener objectsRestListener) {
        this.objectsRestListener = objectsRestListener;
    }

    public void setBallsCollisionListener(BallsCollisionListener ballsCollisionListener) {
        this.ballsCollisionListener = ballsCollisionListener;
    }
}
