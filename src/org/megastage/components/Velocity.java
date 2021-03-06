package org.megastage.components;

import com.artemis.Entity;
import com.artemis.World;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.megastage.components.srv.Acceleration;
import org.megastage.protocol.Message;
import org.megastage.util.Vector3d;

/**
 * MegaStage
 * User: Orlof
 * Date: 17.8.2013
 * Time: 20:58
 */
public class Velocity extends BaseComponent {
    public Vector3d vector;

    @Override
    public BaseComponent[] init(World world, Entity parent, Element element) throws DataConversionException {
        double x = getDoubleValue(element, "x", 0);
        double y = getDoubleValue(element, "y", 0);
        double z = getDoubleValue(element, "z", 0);
        
        vector = new Vector3d(x, y, z);
        
        return null;
    }

    @Override
    public Message replicate(Entity entity) {
        return always(entity);
    }

    public void add(Vector3d v) {
        vector = vector.add(v);
    }

    public Vector3d getPositionChange(float time) {
        double multiplier = 1000.0 * time;
        return vector.multiply(multiplier);
    }

    public void accelerate(Acceleration acceleration, float time) {
        vector = vector.add(acceleration.getVelocityChange(time));
    }

    public String toString() {
        return "Velocity(" + vector.toString() + ")";
    }
}
