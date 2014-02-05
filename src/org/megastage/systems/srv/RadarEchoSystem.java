package org.megastage.systems.srv;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.EntitySystem;
import com.artemis.annotations.Mapper;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableBag;
import org.megastage.components.Position;
import org.megastage.components.Mass;
import org.megastage.components.RadarEcho;
import org.megastage.util.ServerGlobals;
import org.megastage.util.Time;
import org.megastage.util.Vector3d;

/**
 * Created with IntelliJ IDEA.
 * User: Orlof
 * Date: 8/19/13
 * Time: 12:09 PM
 */
public class RadarEchoSystem extends EntitySystem {
    private long interval;
    private long acc;

    @Mapper ComponentMapper<Position> POSITION;
    @Mapper ComponentMapper<Mass> MASS;
    @Mapper ComponentMapper<RadarEcho> RADAR_ECHO;

    public RadarEchoSystem(long interval) {
        super(Aspect.getAspectForAll(Mass.class, Position.class, RadarEcho.class));
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
    protected void processEntities(ImmutableBag<Entity> entities) {
        Bag<RadarData> next = new Bag<>(200);
        
        for(int i=0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            
            Position pos = POSITION.get(entity);
            Mass mass = MASS.get(entity);
            RadarEcho echo = RADAR_ECHO.get(entity);

            next.add(new RadarData(entity.getId(), pos, mass, echo));
        }
        
        ServerGlobals.radarEchoes = next;
    }

    public static class RadarData {
        public final int id;

        public final int echo;
        public final Vector3d coord;
        public final double mass;
        
        public RadarData(int id, Position position, Mass mass, RadarEcho echo) {
            this.id = id;
            this.echo = echo.type;
            this.coord = new Vector3d(position.x / 1000.0, position.y / 1000.0, position.z / 1000.0);
            this.mass = mass.mass;
        }

        public boolean match(char b) {
            return (char) (id & 0xffff) == b;
        }
    }
}
