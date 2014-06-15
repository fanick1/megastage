package org.megastage.components.gfx;

import org.jdom2.Element;
import org.megastage.ecs.BaseComponent;
import org.megastage.client.ClientGlobals;
import org.megastage.ecs.ReplicatedComponent;
import org.megastage.ecs.World;

public class BindTo extends ReplicatedComponent {
    public int parent; 
    
    @Override
    public BaseComponent[] init(World world, int parentEid, Element element) throws Exception {
        this.parent = parentEid;
        
        return null;
    }

    public void setParent(int eid) {
        this.dirty = true;
        this.parent = eid;
    }
    
    @Override
    public void receive(int eid) {
        if(ClientGlobals.playerEntity == eid) {
            ClientGlobals.spatialManager.changeShip(parent);
        } else {
            ClientGlobals.spatialManager.bindTo(parent, eid);
        }
    }
}
