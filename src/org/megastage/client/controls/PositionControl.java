/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.megastage.client.controls;

import com.artemis.Entity;
import com.esotericsoftware.minlog.Log;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import org.megastage.components.Position;
import org.megastage.components.server.ShipGeometry;
import org.megastage.util.ClientGlobals;
import org.megastage.util.Globals;

/**
 *
 * @author Teppo
 */
public class PositionControl extends AbstractControl {
    private final Entity entity;

    public PositionControl(Entity entity) {
        this.entity = entity;
        setEnabled(true);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(Log.TRACE) Log.trace("============== POSITION " + entity.toString() + "==============");
        if(Log.TRACE) Log.trace("Spatial is " + spatial.getName());
        if(Log.TRACE) Log.trace("Spatial is child of " + spatial.getParent().getName());
        Position position = entity.getComponent(Position.class);
        if(position != null) {
            if(ClientGlobals.fixedEntity == entity) {
                spatial.setLocalTranslation(0,0,0);
            } else {
                Vector3f vpos = position.getAsVector();
                spatial.setLocalTranslation(vpos);
            }
            
            if(Log.TRACE) Log.trace("Local" + spatial.getLocalTranslation().toString());
            if(Log.TRACE) Log.trace("World" + spatial.getWorldTranslation().toString());            
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {}

}
