package org.megastage.util;

import com.artemis.ComponentMapper;
import com.artemis.World;
import org.megastage.components.Explosion;
import org.megastage.components.Mass;
import org.megastage.components.Mode;
import org.megastage.components.Orbit;
import org.megastage.components.Position;
import org.megastage.components.PrevPosition;
import org.megastage.components.Rotation;
import org.megastage.components.SpawnPoint;
import org.megastage.components.UsableFlag;
import org.megastage.components.client.ClientVideoMemory;
import org.megastage.components.dcpu.DCPU;
import org.megastage.components.dcpu.VirtualEngine;
import org.megastage.components.dcpu.VirtualGyroscope;
import org.megastage.components.dcpu.VirtualMonitor;
import org.megastage.components.dcpu.VirtualRadar;
import org.megastage.components.gfx.BindTo;
import org.megastage.components.gfx.ShipGeometry;
import org.megastage.components.srv.Acceleration;
import org.megastage.components.srv.Identifier;
import org.megastage.components.srv.Velocity;
import org.megastage.components.transfer.EngineData;
import org.megastage.components.transfer.GyroscopeData;
import org.megastage.components.transfer.RadarTargetData;

public class Mapper {
    public static ComponentMapper<Acceleration> ACCELERATION;
    public static ComponentMapper<Position> POSITION;
    public static ComponentMapper<PrevPosition> PREV_POSITION;
    public static ComponentMapper<Rotation> ROTATION;
    public static ComponentMapper<UsableFlag> USABLE_FLAG;
    public static ComponentMapper<EngineData> ENGINE_DATA;
    public static ComponentMapper<GyroscopeData> GYROSCOPE_DATA;
    public static ComponentMapper<RadarTargetData> RADAR_TARGET_DATA;
    public static ComponentMapper<Explosion> EXPLOSION;
    public static ComponentMapper<Mass> MASS;
    public static ComponentMapper<DCPU> DCPU;
    public static ComponentMapper<Velocity> VELOCITY;
    public static ComponentMapper<Orbit> ORBIT;
    public static ComponentMapper<VirtualEngine> VIRTUAL_ENGINE;
    public static ComponentMapper<VirtualGyroscope> VIRTUAL_GYROSCOPE;
    public static ComponentMapper<VirtualRadar> VIRTUAL_RADAR;
    public static ComponentMapper<VirtualMonitor> VIRTUAL_MONITOR;
    public static ComponentMapper<BindTo> BIND_TO;
    public static ComponentMapper<Identifier> IDENTIFIER;
    public static ComponentMapper<SpawnPoint> SPAWN_POINT;
    public static ComponentMapper<ShipGeometry> SHIP_GEOMETRY;
    public static ComponentMapper<Mode> MODE;

    public static void init(World world) {
        ACCELERATION = world.getMapper(Acceleration.class);
        POSITION = world.getMapper(Position.class);
        PREV_POSITION = world.getMapper(PrevPosition.class);
        ROTATION = world.getMapper(Rotation.class);
        VELOCITY = world.getMapper(Velocity.class);
        USABLE_FLAG = world.getMapper(UsableFlag.class);
        ENGINE_DATA = world.getMapper(EngineData.class);
        GYROSCOPE_DATA = world.getMapper(GyroscopeData.class);
        RADAR_TARGET_DATA = world.getMapper(RadarTargetData.class);
        EXPLOSION = world.getMapper(Explosion.class);
        MASS = world.getMapper(Mass.class);
        DCPU = world.getMapper(DCPU.class);
        ORBIT = world.getMapper(Orbit.class);
        VIRTUAL_ENGINE = world.getMapper(VirtualEngine.class);
        VIRTUAL_GYROSCOPE = world.getMapper(VirtualGyroscope.class);
        VIRTUAL_RADAR = world.getMapper(VirtualRadar.class);
        VIRTUAL_MONITOR = world.getMapper(VirtualMonitor.class);
        BIND_TO = world.getMapper(BindTo.class);
        IDENTIFIER = world.getMapper(Identifier.class);
        SPAWN_POINT = world.getMapper(SpawnPoint.class);
        SHIP_GEOMETRY = world.getMapper(ShipGeometry.class);
        MODE = world.getMapper(Mode.class);
    }
}