/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.megastage.components.gfx;

import com.artemis.Entity;
import com.artemis.World;
import com.esotericsoftware.kryonet.Connection;
import org.jdom2.Element;
import org.megastage.components.BaseComponent;
import org.megastage.client.ClientGlobals;
import org.megastage.protocol.Message;


    
/**
 *
 * @author Orlof
 */
public class SunGeometry extends BaseComponent {
    public float radius;
    public float red, green, blue, alpha;
    public float lightRadius;

    @Override
    public BaseComponent[] init(World world, Entity parent, Element element) throws Exception {
        radius = (float) (getFloatValue(element, "radius", 10.0f));
        lightRadius = (float) (getFloatValue(element, "light_radius", 2000000.0f));
        red = getFloatValue(element, "red", 1.0f); 
        green = getFloatValue(element, "green", 1.0f); 
        blue = getFloatValue(element, "blue", 1.0f); 
        alpha = getFloatValue(element, "alpha", 1.0f); 
        
        return null;
    }

    @Override
    public Message replicate(Entity entity) {
        return always(entity);
    }
    
    @Override
    public void receive(Connection pc, Entity entity) {
        ClientGlobals.spatialManager.setupSunLikeBody(entity, this);
    }
    
    @Override
    public void delete(Connection pc, Entity entity) {
        ClientGlobals.spatialManager.deleteEntity(entity);
        entity.deleteFromWorld();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SunGeometry(");
        sb.append("radius=").append(radius);
        sb.append(", light_radius=").append(lightRadius);
        sb.append(", red=").append(red);
        sb.append(", green=").append(green);
        sb.append(", blue=").append(blue);
        sb.append(", alpha=").append(alpha);
        sb.append(")");
        return sb.toString();
    }
}
