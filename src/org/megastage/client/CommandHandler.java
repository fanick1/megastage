package org.megastage.client;

import com.artemis.Entity;
import com.cubes.BlockTerrainControl;
import com.cubes.Vector3Int;
import com.esotericsoftware.minlog.Log;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.*;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.concurrent.Callable;
import org.megastage.components.Rotation;
import org.megastage.components.gfx.ShipGeometry;
import org.megastage.protocol.CharacterMode;
import org.megastage.util.ID;
import org.megastage.util.Mapper;

public class CommandHandler implements AnalogListener, ActionListener {

    private static String[] dcpuMappings = new String[]{
        "WALK_LookLeft",
        "WALK_LookRight",
        "WALK_LookUp",
        "WALK_LookDown",
        "DCPU_Exit",
    };

    private static String[] menuMappings = new String[]{
        "MENU_Exit",
    };

    private static String[] walkMappings = new String[]{
        "WALK_LookLeft",
        "WALK_LookRight",
        "WALK_LookUp",
        "WALK_LookDown",
        "WALK_MoveForward",
        "WALK_MoveBackward",
        "WALK_MoveLeft",
        "WALK_MoveRight",
        "SHIP_MoveForward",
        "SHIP_MoveBackward",
        "SHIP_MoveUp",
        "SHIP_MoveDown",
        "SHIP_MoveLeft",
        "SHIP_MoveRight",
        "SHIP_PitchUp",
        "SHIP_PitchDown",
        "SHIP_RollCW",
        "SHIP_RollCCW",
        "SHIP_YawLeft",
        "SHIP_YawRight",
        "WALK_InvertY",
        "ITEM_Pick",
        "ITEM_RightPick",
        "GAME_Exit",
    };

    private void pickItem(boolean right) {
        CollisionResults results = new CollisionResults();
        Ray ray = new Ray(ClientGlobals.cam.getLocation(), ClientGlobals.cam.getDirection());
        ClientGlobals.rootNode.collideWith(ray, results);
        for(int i=0; i < results.size(); i++) {
            CollisionResult closest = results.getCollision(i);
            if(!closest.getGeometry().getName().equals("forceshield")) {
                Node target = closest.getGeometry().getParent();

                Entity entity = null;
                while(true) {
                    if(target == ClientGlobals.rootNode) {
                        return;
                    }

                    entity = ClientGlobals.spatialManager.getUsableEntity(target, true);
                    //Log.info("Pick entity: " + ID.get(entity));
                    if(entity != null) break;
                    target = target.getParent();
                }

                ShipGeometry geom = Mapper.SHIP_GEOMETRY.get(entity);
                if(geom != null) {
                    if(entity == ClientGlobals.shipEntity) {
                        Node offset = (Node) target.getChild("offset");
                        Vector3Int loc = CubesManager.getCurrentPointedBlockLocation(offset, !right);
                        if(loc != null) {
                            BlockTerrainControl ctrl = offset.getControl(BlockTerrainControl.class);
                            if(!right) {
                                ClientGlobals.userCommand.build(loc);
                                //ctrl.setBlock(loc, CubesManager.Combi.class);
                            } else {
                                ClientGlobals.userCommand.unbuild(loc);
                                //ctrl.removeBlock(loc);
                            }
                        }
                    } else {
                        // TELEPORT
                        ClientGlobals.userCommand.teleport(entity);
                        Log.info("TELEPORT");
                    }
                } else {
                    ClientGlobals.userCommand.pickItem(entity);
                }
                return;
            }
        }
    }
    
    private void unpickItem() {
        ClientGlobals.userCommand.unpickItem();
    }

    public int mode = CharacterMode.NONE;
    private InputManager inputManager;
    private DCPURawInputListener dcpuListener = new DCPURawInputListener();

    public void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;

        //inputManager.setCursorVisible(false);
        //initWalkMode();
    }

    public void unregisterInput() {

        if (inputManager == null) {
            return;
        }

        inputManager.clearMappings();
        inputManager.clearRawInputListeners();

        inputManager.setCursorVisible(false);
    }

    public void changeMode(int newMode) {
        if(newMode == mode) return;
        
        Log.info("Mode change " + mode + " -> " + newMode);
        
        switch(mode) {
            case CharacterMode.WALK:
                exitWalkMode();
                break;
            case CharacterMode.DCPU:
                exitDCPUMode();
                break;
            case CharacterMode.MENU:
                exitMenuMode();
                break;
        }

        switch(newMode) {
            case CharacterMode.WALK:
                initWalkMode();
                break;
            case CharacterMode.DCPU:
                initDCPUMode();
                break;
            case CharacterMode.MENU:
                initMenuMode();
                break;
        }
    }
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (ClientGlobals.playerEntity == null) {
            return;
        }

        switch (name) {
            case "WALK_LookLeft":
                lookLeft(value);
                break;
            case "WALK_LookRight":
                lookLeft(-value);
                break;
            case "WALK_LookUp":
                lookUp(value * (invertY ? -1 : 1));
                break;
            case "WALK_LookDown":
                lookUp(-value * (invertY ? -1 : 1));
                break;
            case "WALK_MoveForward":
                move(value, false);
                break;
            case "WALK_MoveBackward":
                move(-value, false);
                break;
            case "WALK_MoveLeft":
                move(value, true);
                break;
            case "WALK_MoveRight":
                move(-value, true);
                break;
            case "SHIP_MoveForward":
                moveShip(0, 0, -value);
                break;
            case "SHIP_MoveBackward":
                moveShip(0, 0, value);
                break;
            case "SHIP_MoveUp":
                moveShip(0, value, 0);
                break;
            case "SHIP_MoveDown":
                moveShip(0, -value, 0);
                break;
            case "SHIP_MoveLeft":
                moveShip(-value, 0, 0);
                break;
            case "SHIP_MoveRight":
                moveShip(value, 0, 0);
                break;
            case "SHIP_PitchUp":
                pitch(value);
                break;
            case "SHIP_PitchDown":
                pitch(-value);
                break;
            case "SHIP_RollCW":
                roll(-value);
                break;
            case "SHIP_RollCCW":
                roll(value);
                break;
            case "SHIP_YawLeft":
                yaw(value);
                break;
            case "SHIP_YawRight":
                yaw(-value);
                break;
        }
    }

    @Override
    public void onAction(String name, boolean value, float tpf) {
        switch (name) {
            case "WALK_InvertY":
                // Toggle on the up.
                if (!value) {
                    invertY = !invertY;
                }
                break;
            case "ITEM_Pick":
                // Toggle on the up.
                if (!value) {
                    //initDCPUMode();
                    pickItem(false);
                }
                break;
            case "ITEM_RightPick":
                // Toggle on the up.
                if (!value) {
                    //initDCPUMode();
                    pickItem(true);
                }
                break;
            case "DCPU_Exit":
                // Toggle on the up.
                if (!value) {
                    unpickItem();
                }
                break;
            case "MENU_Exit":
                // Toggle on the up.
                if (!value) {
                    unpickItem();
                }
                break;
            case "GAME_Exit":
                // Toggle on the up.
                if (!value) {
                    ClientGlobals.app.stop();
                }
                break;
        }
    }
    protected float rotationSpeed = 1f;
    protected float walkSpeed = 3f;
    protected float shipSpeed = 100f;
    protected boolean invertY = true;

    public void setWalkSpeed(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    protected void lookUp(float value) {
        Rotation rot = Mapper.ROTATION.get(ClientGlobals.playerEntity);
        if (rot == null) {
            return;
        }

        Quaternion q = rot.getQuaternion3f();

        float[] eulerAngles = q.toAngles(null);
        eulerAngles[0] = FastMath.clamp(eulerAngles[0] + value, -0.9f * FastMath.HALF_PI, 0.9f * FastMath.HALF_PI);
        eulerAngles[2] = 0f;

        q.fromAngles(eulerAngles).normalizeLocal();

        rot.set(q);

        ClientGlobals.userCommand.look(rot);
    }

    protected void lookLeft(float value) {
        Rotation rot = Mapper.ROTATION.get(ClientGlobals.playerEntity);
        if (rot == null) {
            return;
        }

        Quaternion q = rot.getQuaternion3f();
        float[] eulerAngles = q.toAngles(null);
        eulerAngles[1] = (eulerAngles[1] + value) % FastMath.TWO_PI;
        eulerAngles[2] = 0f;

        q.fromAngles(eulerAngles).normalizeLocal();
        rot.set(q);

        ClientGlobals.userCommand.look(rot);
    }

    protected void move(float value, boolean sideways) {
        Rotation playerRotation = Mapper.ROTATION.get(ClientGlobals.playerEntity);
        if (playerRotation == null) {
            return;
        }

        Quaternion playerQuaternion = new Quaternion(
                (float) playerRotation.x, (float) playerRotation.y,
                (float) playerRotation.z, (float) playerRotation.w);

        float[] eulerAngles = playerQuaternion.toAngles(null);
        eulerAngles[0] = 0f;
        if (sideways) {
            eulerAngles[1] += FastMath.HALF_PI;
        }
        eulerAngles[2] = 0f;
        playerQuaternion.fromAngles(eulerAngles).normalizeLocal();

        Vector3f playerMovement = new Vector3f(0, 0, value * walkSpeed);
        playerQuaternion.multLocal(playerMovement);

        ClientGlobals.userCommand.move(playerMovement.x, playerMovement.y, playerMovement.z);
    }

    private void moveShip(float x, float y, float z) {
        ClientGlobals.userCommand.shipMove(x, y, z);
    }

    private void pitch(float value) {
        ClientGlobals.userCommand.shipPitch(value);
    }

    private void roll(float value) {
        ClientGlobals.userCommand.shipRoll(value);
    }

    private void yaw(float value) {
        ClientGlobals.userCommand.shipYaw(value);
    }

    public void initWalkMode() {
        Log.info("");

        mode = CharacterMode.WALK;

        inputManager.addMapping("WALK_LookLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("WALK_LookRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("WALK_LookUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("WALK_LookDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));

        inputManager.addMapping("WALK_MoveForward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("WALK_MoveBackward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("WALK_MoveLeft", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("WALK_MoveRight", new KeyTrigger(KeyInput.KEY_D));

        inputManager.addMapping("SHIP_MoveForward", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("SHIP_MoveBackward", new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping("SHIP_MoveUp", new KeyTrigger(KeyInput.KEY_PGUP));
        inputManager.addMapping("SHIP_MoveDown", new KeyTrigger(KeyInput.KEY_PGDN));
        inputManager.addMapping("SHIP_MoveLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("SHIP_MoveRight", new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addMapping("SHIP_PitchUp", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("SHIP_PitchDown", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("SHIP_RollCW", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("SHIP_RollCCW", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("SHIP_YawLeft", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("SHIP_YawRight", new KeyTrigger(KeyInput.KEY_O));

        inputManager.addMapping("ITEM_Pick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("ITEM_RightPick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("GAME_Exit", new KeyTrigger((KeyInput.KEY_ESCAPE)));

        inputManager.addListener(this, walkMappings);

        ClientGlobals.app.enqueue(new Callable() { @Override public Object call() throws Exception {
            ClientGlobals.crosshair.setCullHint(Spatial.CullHint.Inherit);
            return null;
        }});
    }

    private void exitWalkMode() {
        Log.info("");

        mode = CharacterMode.NONE;
        inputManager.clearMappings();

        ClientGlobals.app.enqueue(new Callable() { @Override public Object call() throws Exception {
            ClientGlobals.crosshair.setCullHint(Spatial.CullHint.Always);
            return null;
        }});
    }

    public void initDCPUMode() {
        Log.info("");

        mode = CharacterMode.DCPU;

        inputManager.addMapping("WALK_LookLeft", new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping("WALK_LookRight", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        inputManager.addMapping("WALK_LookUp", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
        inputManager.addMapping("WALK_LookDown", new MouseAxisTrigger(MouseInput.AXIS_Y, true));

        inputManager.addMapping("DCPU_Exit", new MouseButtonTrigger(MouseInput.BUTTON_LEFT), new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addRawInputListener(dcpuListener);
        inputManager.addListener(this, dcpuMappings);
    }

    private void exitDCPUMode() {
        Log.info("");

        mode = CharacterMode.NONE;

        inputManager.clearMappings();
        inputManager.removeRawInputListener(dcpuListener);
        //inputManager.clearRawInputListeners();
    }

    public void initMenuMode() {
        Log.info("");
        
        mode = CharacterMode.MENU;
        inputManager.addMapping("MENU_Exit", new KeyTrigger((KeyInput.KEY_ESCAPE)));
        inputManager.addListener(this, menuMappings);
        
        ClientGlobals.app.getStateManager().attach(ClientGlobals.dcpuMenuState);
        ClientGlobals.dcpuMenuState.setEnabled(true);
    }

    private void exitMenuMode() {
        Log.info("");

        mode = CharacterMode.NONE;
        inputManager.clearMappings();
        ClientGlobals.dcpuMenuState.setEnabled(false);
        // ClientGlobals.app.getStateManager().detach(ClientGlobals.dcpuMenuState);
    }
}
