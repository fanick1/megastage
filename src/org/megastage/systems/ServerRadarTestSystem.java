package org.megastage.systems;

import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntitySystem;
import com.badlogic.gdx.utils.Array;
import org.megastage.components.dcpu.VirtualRadar;
import org.megastage.util.Mapper;
import org.megastage.util.Time;

public class ServerRadarTestSystem extends EntitySystem {
    private long interval;
    private long acc;
    
    public ServerRadarTestSystem(long interval) {
        super(Aspect.getAspectForAll(VirtualRadar.class));
        this.interval = interval;
    }

    @Override
    protected boolean checkProcessing() {
        if(Time.value >= acc) {
                acc = Time.value + interval;
                return true;
        }
        return false;
    }

    @Override
    protected void processEntities(Array<Entity> entities) {
    }	
}
