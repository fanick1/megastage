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
 * This entity's position and rotation become relative to parent
 * @author Orlof
 */
public class BindTo extends BaseComponent {
    public int parent; 
    
    @Override
    public BaseComponent[] init(World world, Entity parent, Element element) throws Exception {
        this.parent = parent.id;
        
        return null;
    }

    @Override
    public Message replicate(Entity entity) {
        return always(entity);
    }
    
    @Override
    public Message synchronize(Entity entity) {
        return ifDirty(entity);
    }

    public void setParent(int eid) {
        this.dirty = true;
        this.parent = eid;
    }
    
    @Override
    public void receive(Connection pc, Entity entity) {
        Entity parentEntity = ClientGlobals.artemis.toClientEntity(parent);

        parent = parentEntity.id;

        if(ClientGlobals.playerEntity == entity) {
            ClientGlobals.spatialManager.changeShip(parentEntity);
        } else {
            ClientGlobals.spatialManager.bindTo(parentEntity, entity);
        }
    }

    @Override
    public String toString() {
        return "BindTo(serverID=" + parent + ")";
    }
}
