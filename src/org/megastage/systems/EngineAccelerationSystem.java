package org.megastage.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.annotations.Mapper;
import com.artemis.systems.EntityProcessingSystem;
import org.megastage.components.Acceleration;
import org.megastage.components.Mass;
import org.megastage.components.Rotation;
import org.megastage.components.dcpu.Engine;
import org.megastage.util.Quaternion;
import org.megastage.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: contko3
 * Date: 8/19/13
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineAccelerationSystem extends EntityProcessingSystem {
    @Mapper ComponentMapper<Engine> ENGINE;
    @Mapper ComponentMapper<Rotation> ROTATION;
    @Mapper ComponentMapper<Mass> MASS;
    @Mapper ComponentMapper<Acceleration> ACCELERATION;

    public EngineAccelerationSystem() {
        super(Aspect.getAspectForAll(Engine.class));
    }

    @Override
    protected void process(Entity entity) {
        Engine engine = ENGINE.get(entity);

        if(engine.isActive()) {
            double shipMass = MASS.get(engine.ship).mass;
            Vector acc = engine.getAcceleration(shipMass);

            // rotate acceleration into global coordinate system
            Quaternion shipRot = ROTATION.get(engine.ship).getQuaternion();

            // convert speed change from local to global -space
            Vector globalAcc = acc.multiply(shipRot);

            ACCELERATION.get(engine.ship).add(globalAcc);
        }
    }
}
