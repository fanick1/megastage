package org.megastage.components.gfx;

import com.artemis.Entity;
import com.esotericsoftware.kryonet.Connection;
import org.megastage.client.ClientGlobals;
import org.megastage.components.BaseComponent;
import org.megastage.protocol.Message;
    
/**
 *
 * @author Orlof
 */
public class FloppyDriveGeometry extends BaseComponent {
    @Override
    public void receive(Connection pc, Entity entity) {
        ClientGlobals.spatialManager.setupFloppyDrive(entity, this);
    }
    
    @Override
    public Message replicate(Entity entity) {
        return always(entity);
    }
}
