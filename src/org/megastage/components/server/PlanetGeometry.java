/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.megastage.components.server;

import com.artemis.Entity;
import com.artemis.World;
import com.esotericsoftware.kryonet.Connection;
import org.jdom2.Element;
import org.megastage.components.EntityComponent;
import org.megastage.systems.ClientNetworkSystem;


    
/**
 *
 * @author Teppo
 */
public class PlanetGeometry extends EntityComponent {
    public int center;
    public float radius;
    public String generator;
    public String color;

    @Override
    public void init(World world, Entity parent, Element element) throws Exception {
        center = parent.getId();

        radius = getFloatValue(element, "radius", 10.0f);
        generator = getStringValue(element, "generator", "Earth");
        color = getStringValue(element, "color", "red");
    }

    @Override
    public void receive(ClientNetworkSystem system, Connection pc, Entity entity) {
        // center = system.cems.get(center).getId();
        system.csms.setupPlanetLikeBody(entity, this);
    }
    
    public String toString() {
        return "PlanetGeometry(center=" + center + ", generator='" + generator + "', radius=" + radius + ")";
    }
}