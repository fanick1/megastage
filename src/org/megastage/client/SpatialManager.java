package org.megastage.client;

import org.megastage.client.controls.ExplosionControl;
import com.artemis.Entity;
import com.cubes.Block;
import com.cubes.BlockTerrainControl;
import com.esotericsoftware.minlog.Log;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.LightNode;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Dome;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Torus;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ImageRaster;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.util.BufferUtils;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.concurrent.Callable;
import jmeplanet.FractalDataSource;
import jmeplanet.Planet;
import jmeplanet.PlanetAppState;
import jmeplanet.test.Utility;
import org.megastage.client.controls.AxisRotationControl;
import org.megastage.client.controls.DeleteControl;
import org.megastage.client.controls.EngineControl;
import org.megastage.client.controls.ForceFieldControl;
import org.megastage.client.controls.GyroscopeControl;
import org.megastage.client.controls.ImposterPositionControl;
import org.megastage.client.controls.LookAtControl;
import org.megastage.client.controls.PositionControl;
import org.megastage.client.controls.RandomSpinnerControl;
import org.megastage.client.controls.RotationControl;
import org.megastage.client.controls.ThermalLaserControl;
import org.megastage.components.client.ClientRaster;
import org.megastage.components.gfx.MonitorGeometry;
import org.megastage.components.gfx.PlanetGeometry;
import org.megastage.components.gfx.CharacterGeometry;
import org.megastage.components.gfx.EngineGeometry;
import org.megastage.components.gfx.ShipGeometry;
import org.megastage.components.gfx.SunGeometry;
import org.megastage.components.UsableFlag;
import org.megastage.components.gfx.VoidGeometry;
import org.megastage.components.Explosion;
import org.megastage.components.gfx.BatteryGeometry;
import org.megastage.components.gfx.FloppyDriveGeometry;
import org.megastage.components.gfx.ForceFieldGeometry;
import org.megastage.components.gfx.GyroscopeGeometry;
import org.megastage.components.gfx.ImposterGeometry;
import org.megastage.components.gfx.PPSGeometry;
import org.megastage.components.gfx.PowerPlantGeometry;
import org.megastage.components.gfx.RadarGeometry;
import org.megastage.components.gfx.ThermalLaserGeometry;
import org.megastage.util.Cube3dMap;
import org.megastage.util.Cube3dMap.BlockChange;
import org.megastage.util.ID;
import org.megastage.util.Mapper;

/**
 *
 * @author Orlof
 */
public class SpatialManager {

    private final SimpleApplication app;
    private final AssetManager assetManager;

    private HashMap<Integer, Node> nodes = new HashMap<>();
    private HashMap<Node, Entity> entities = new HashMap<>();
    
    public SpatialManager(SimpleApplication app) {
        super();
        this.app = app;
        assetManager = app.getAssetManager();
        
        ExplosionNode.initialize(assetManager);
    }

    public void deleteEntity(Entity entity) {
        final Node node = nodes.get(entity.id);

        if(node != null) {
            app.enqueue(new Callable() {
                @Override
                public Object call() throws Exception {
                    node.removeFromParent();
                    //TODO remove lights
                    return null;
                }
            });
        }
    }
    
    public Entity getUsableEntity(Node node, boolean onlyUsable) {
        Entity entity = entities.get(node);
        if(entity == null) {
            return null;
        }

        if(onlyUsable) {
            UsableFlag usable = Mapper.USABLE_FLAG.get(entity);
            return usable == null ? null: entity;
        }
        
        return entity;
    }
    
    public Node getNode(int id) {
        Node node = nodes.get(id);
 
        if(node == null) {
            Entity entity = ClientGlobals.artemis.world.getEntity(id);
            node = createNode(entity);
        }
        
        return node;
    }
    
    public Node getNode(Entity entity) {
        Node node = nodes.get(entity.id);
 
        if(node == null) {
            node = createNode(entity);
        }
        
        return node;
    }

    private Node createNode(Entity entity) {
        Node node = new Node(ID.get(entity));
        node.attachChild(new Node("offset"));
        nodes.put(entity.id, node);
        entities.put(node, entity);
        return node;
    }
    
    public void changeShip(final Entity shipEntity) {
        app.enqueue(new Callable() {
            @Override
            public Object call() throws Exception {
                leaveShip();
                enterShip(shipEntity);
                return null;
            }
        });
    }
    
    public void bindTo(final Entity parentEntity, final Entity childEntity) {
        final Node parent = getNode(parentEntity);
        final Node child = getNode(childEntity);

        app.enqueue(new Callable() {
            @Override
            public Object call() throws Exception {
                attach(parent, child, true);
                return null;
            }
        });
    }

    private Geometry createSphere(float radius, ColorRGBA color, boolean shaded) {
        Sphere mesh = new Sphere(
                ClientGlobals.gfxSettings.SPHERE_Z_SAMPLES,
                ClientGlobals.gfxSettings.SPHERE_RADIAL_SAMPLES, 
                radius);
        
        Geometry geom = new Geometry("gfx");
        geom.setMesh(mesh);

        if(shaded) {
            Material mat = material(color, true);
            mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/rock.jpg")); // with Lighting.j3md
            geom.setMaterial(mat);               // Use new material on this Geometry.
        } else {
            geom.setMaterial(material(color, false));
        }

        return geom;
    }

    private Material getMaterial(String name) {
        Material mat = material(ColorRGBA.Gray, true);
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/" + name));
        return mat;
    }
    
    public void attach(Node parent, Spatial child, boolean useOffset) {
        if(useOffset) {
            offset(parent).attachChild(child);
        } else {
            parent.attachChild(child);
        }
    }
    
    private Node offset(Node node) {
        return (Node) node.getChild(0);
    }
    
    public void setupSunLikeBody(final Entity entity, final SunGeometry data) {
        ColorRGBA colorRGBA = new ColorRGBA(data.red, data.green, data.blue, data.alpha);

        final Node node = getNode(entity);
        node.addControl(new PositionControl(entity, false));
        node.addControl(new RotationControl(entity));
        
        attach(node, createSphere(data.radius, colorRGBA, false), true);

        final PointLight light = new PointLight();
        light.setColor(colorRGBA);
        light.setRadius(data.lightRadius);

        LightNode lightNode = new LightNode(entity.toString() + " LightNode", light);
        attach(node, lightNode, true);

        app.enqueue(new Callable() {
            @Override
            public Object call() throws Exception {
                ClientGlobals.rootNode.addLight(light);
                ClientGlobals.sysMovNode.attachChild(node);
                return null;
            }
        });
    }

    public void setupPlanetLikeBody(final Entity entity, PlanetGeometry data) {
        // Add planet
        final Node node = getNode(entity);

        final PositionControl positionControl = new PositionControl(entity, false);
        final RotationControl rotationControl = new RotationControl(entity);
        
        if(ClientGlobals.gfxSettings.ENABLE_PLANETS) {
            final Planet planet = createPlanet(data);

            app.enqueue(new Callable() { @Override public Object call() throws Exception {
                attach(node, planet, true);

                PlanetAppState appState = app.getStateManager().getState(PlanetAppState.class);
                if(appState != null) appState.addPlanet(planet);

                node.addControl(positionControl);
                node.addControl(rotationControl);

                if(node.getParent() == null) {
                    ClientGlobals.sysMovNode.attachChild(node);
                }
                return null;
            }});
        } else {
            ColorRGBA color = null;
            try {
                color = (ColorRGBA) ColorRGBA.class.getDeclaredField(data.color).get(null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            final Geometry geom = createSphere(data.radius, color, true);
            app.enqueue(new Callable() { @Override public Object call() throws Exception {
                attach(node, geom, true);

                node.addControl(positionControl);
                node.addControl(rotationControl);

                if(node.getParent() == null) {
                    ClientGlobals.sysMovNode.attachChild(node);
                }
                return null;
            }});
        }
    }
    
    public void setupVoidNode(Entity entity, VoidGeometry data) {
        getNode(entity);
    }
    
    public void updateShip(Entity entity, ShipGeometry data) {
        Cube3dMap theMap = Mapper.SHIP_GEOMETRY.get(entity).map;
        
        final Node node = getNode(entity);
        BlockTerrainControl ctrl = offset(node).getControl(BlockTerrainControl.class);

        int xsize = Math.max(data.map.xsize, theMap.xsize);
        int ysize = Math.max(data.map.ysize, theMap.ysize);
        int zsize = Math.max(data.map.zsize, theMap.zsize);
        
        for(int x = 0; x <= xsize; x++) {
            for(int y = 0; y <= ysize; y++) {
                for(int z = 0; z <= zsize; z++) {
                    char c = data.map.get(x, y, z);
                    if(c != theMap.get(x, y, z)) {
                        theMap.set(x, y, z, c, BlockChange.BUILD);
                        if(c == 0) {
                            ctrl.removeBlock(x, y, z);
                        } else {
                            Class<? extends Block> block = CubesManager.getBlock(c);
                            ctrl.setBlock(x, y, z, block);
                        }
                    }
                }
            }
        }
    }
        
    public void updateShipBlock(Entity entity, final BlockChange data) {
        Cube3dMap theMap = Mapper.SHIP_GEOMETRY.get(entity).map;
        theMap.set(data.x, data.y, data.z, data.type, data.event);
        
        final Node node = getNode(entity);
        BlockTerrainControl ctrl = offset(node).getControl(BlockTerrainControl.class);

        if(data.type == 0) {
            ctrl.removeBlock(data.x, data.y, data.z);
            if(data.event == BlockChange.BREAK) {
                
                final ParticleEmitter pe = (ParticleEmitter) node.getChild("BlockSparks");
                
                //==========================================================
                app.enqueue(new Callable() {@Override public Object call() throws Exception {
                //==========================================================
                SoundManager.get(SoundManager.EXPLOSION_3).playInstance();

                pe.killAllParticles();
                //pe.removeControl(DeleteControl.class);
                pe.setLocalTranslation(data.x, data.y, data.z);

                pe.emitAllParticles();
                //pe.addControl(new DeleteControl(3000));
                //==========================================================
                return null;}});
                //==========================================================
            }
        } else {
            Class<? extends Block> block = CubesManager.getBlock(data.type);
            ctrl.setBlock(data.x, data.y, data.z, block);
        }
    }
        
    public void setupShip(Entity entity, ShipGeometry data) {
        BlockTerrainControl blockControl = CubesManager.getControl(data.map);
        for(int x = 0; x <= data.map.xsize; x++) {
            for(int y = 0; y <= data.map.ysize; y++) {
                for(int z = 0; z <= data.map.zsize; z++) {
                    char c = data.map.get(x, y, z);
                    Class<? extends Block> block = CubesManager.getBlock(c);
                    if(block != null) {
                        blockControl.setBlock(x, y, z, block);
                    }
                }
            }
        }
        
        final Node node = getNode(entity);
        node.addControl(new PositionControl(entity, true));
        node.addControl(new RotationControl(entity));

        if(ClientGlobals.gfxSettings.ENABLE_SHIP_SHADOWS) {
            node.setShadowMode(ShadowMode.CastAndReceive);
        }

        offset(node).addControl(blockControl);
        //shipNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);

        setOffsetTranslation(node, data.map.getCenter().negateLocal());

        ParticleEmitter pe = ExplosionNode.blockSparks(assetManager);
        attach(node, pe, true);

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            if(node.getParent() == null) {
                ClientGlobals.sysMovNode.attachChild(node);
            }
            return null;
        }});
    }

    public void setupEngine(Entity entity, EngineGeometry data) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new  PositionControl(entity, false);
        final RotationControl rotationControl = new  RotationControl(entity);

        final Node burn = (Node) assetManager.loadModel("Scenes/testScene.j3o"); 
        ParticleEmitter emitter = (ParticleEmitter) burn.getChild("Emitter");
        emitter.addControl(new EngineControl(entity));
        emitter.setEnabled(true);

        final Geometry geom = new Geometry("", new Cylinder(16, 16, 0.5f, 1, true));
        geom.setMaterial(material(ColorRGBA.Gray, true));
        
        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, burn, true);
            attach(node, geom, true);
            return null;
        }});
    }
    
    public void setupMonitor(Entity entity, MonitorGeometry data) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new PositionControl(entity, false);
        final RotationControl rotationControl = new RotationControl(entity);

        BufferedImage img = new BufferedImage(128, 96, BufferedImage.TYPE_INT_ARGB);
        Image img2 = new AWTLoader().load(img, false);
        ImageRaster raster = ImageRaster.create(img2);
        
        Texture2D tex = new Texture2D(img2);
        tex.setMagFilter(Texture.MagFilter.Nearest);
        tex.setMinFilter(Texture.MinFilter.Trilinear);

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", tex);

        final Geometry panel = new Geometry(entity.toString(), new Quad(data.width-0.2f, data.height-0.2f, true));
        panel.setMaterial(mat);
        panel.setLocalTranslation(-0.5f+0.1f, -0.5f+0.1f, 0f);

        ClientRaster rasterComponent = ClientGlobals.artemis.getComponent(entity, ClientRaster.class);
        rasterComponent.raster = raster;

        final Geometry box = new Geometry(entity.toString(), new Box(data.width/2.0f, data.height/2.0f, 0.1f));
        box.setMaterial(material(ColorRGBA.Gray, true));
        box.setLocalTranslation(-0.5f + data.width/2.0f, -0.5f + data.height/2.0f, -0.3f);

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, panel, true);
            attach(node, box, true);
            return null;
        }});
    }

    public void setupCharacter(Entity entity, CharacterGeometry data) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new PositionControl(entity, true);
        final AxisRotationControl bodyRotationControl = new AxisRotationControl(entity, false, true, false);
        final AxisRotationControl headRotationControl = new AxisRotationControl(entity, true, false, false);

        Material mat = material(new ColorRGBA(data.red, data.green, data.blue, data.alpha), true);

        final Geometry body = new Geometry("body", new Box(0.25f, 0.5f, 0.25f));
        body.setMaterial(mat);

        final Geometry head = new Geometry("head", new Box(0.25f, 0.25f, 0.25f));
        head.setMaterial(mat);
        head.setLocalTranslation(0, 1.0f, 0);
        
        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            attach(node, body, true);
            attach(node, head, true);

            node.addControl(positionControl);
            node.addControl(bodyRotationControl);
            head.addControl(headRotationControl);

            return null;
        }});
    }

    private Material material(ColorRGBA color, boolean lighting) {
        ColorRGBA c = new ColorRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() * .3f);
        if(lighting) {
            Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md"); 
            mat.setBoolean("UseMaterialColors",true);
            mat.setColor("Ambient", c);
            mat.setColor("Diffuse", c);
            return mat;
        } else {
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setColor("Color", color);
            return mat;           
        }
    }
    
    private Planet createPlanet(PlanetGeometry data) {
        if(data.generator.equalsIgnoreCase("Earth")) {
            FractalDataSource planetDataSource = new FractalDataSource(4);
            planetDataSource.setHeightScale(data.radius / 100f);
            return Utility.createEarthLikePlanet(assetManager, data.radius, null, planetDataSource);
        } else if(data.generator.equalsIgnoreCase("Moon")) {
            FractalDataSource planetDataSource = new FractalDataSource(4);
            planetDataSource.setHeightScale(data.radius / 20f);
            return Utility.createMoonLikePlanet(assetManager, data.radius, planetDataSource);
        } else if(data.generator.equalsIgnoreCase("Water")) {
            return Utility.createWaterPlanet(assetManager, data.radius, null);
        } 

        throw new RuntimeException("Unknown planet generator: " + data.generator);
    }

    private void leaveShip() {
        Entity ship = ClientGlobals.shipEntity;
        
        if(ship != null) {
            Log.info(ID.get(ship));

            Node shipNode = getNode(ship);
            ClientGlobals.sysMovNode.attachChild(shipNode);

            ClientGlobals.shipEntity = null;
            Mapper.ROTATION.get(ship).dirty = true;
            Mapper.POSITION.get(ship).dirty = true;
            
            ClientGlobals.fixedNode.attachChild(ClientGlobals.playerNode);
        }
    }

    private void enterShip(Entity shipEntity) {
        Log.info(ID.get(shipEntity));

        ClientGlobals.shipEntity = shipEntity;

        Node shipNode = getNode(shipEntity);
        ClientGlobals.fixedNode.attachChild(shipNode);
        attach(shipNode, ClientGlobals.playerNode, true);
        ClientGlobals.playerNode.setLocalTranslation(0, 0, 0);
    }

    public void setupPlayer(final Entity entity) {
        final AxisRotationControl bodyRotationControl = new AxisRotationControl(entity, false, true, false);
        final AxisRotationControl headRotationControl = new AxisRotationControl(entity, true, false, false);

        app.enqueue(new Callable() { @Override public Object call() throws Exception {
            ClientGlobals.playerNode.addControl(new PositionControl(entity, true));
            ClientGlobals.playerNode.addControl(bodyRotationControl);
            ClientGlobals.playerNode.getChild(0).addControl(headRotationControl);
            return null;
        }});
    }

    public void setupExplosion(final Entity entity, final Explosion explosion) {
        final Node node = getNode(entity);
        final ExplosionNode explosionNode = new ExplosionNode("ExplosionFX");

        explosionNode.addControl((Control) new ExplosionControl(explosion, explosionNode));

        app.enqueue(new Callable() { @Override public Object call() throws Exception {
            attach(node, explosionNode, false);
            return null;
        }});
    }

    
    public void imposter(Entity entity, boolean gfxVisible) {
        Node node = getNode(entity);

        for(Spatial s: node.getChildren()) {
            if(s.getName().equals("imposter")) {
                boolean imposterVisible = !gfxVisible;
                boolean imposterDraw = draw(s);
                if(!imposterVisible && imposterDraw) {
                    s.setCullHint(Spatial.CullHint.Always);
                    s.getParent().getControl(ImposterPositionControl.class).setEnabled(false);
                    s.getParent().getControl(PositionControl.class).setEnabled(true);
                } else if(imposterVisible && !imposterDraw) {
                    s.setCullHint(Spatial.CullHint.Inherit);
                    s.getParent().getControl(ImposterPositionControl.class).setEnabled(true);
                    s.getParent().getControl(PositionControl.class).setEnabled(false);
                }
            } else {
                boolean gfxDraw = draw(s);
                if(gfxVisible && !gfxDraw) {
                    s.setCullHint(Spatial.CullHint.Inherit);
                } else if(!gfxVisible && gfxDraw) {
                    s.setCullHint(Spatial.CullHint.Always);
                }
            }
        }
    }
    
    private boolean draw(Spatial s) {
        return s.getCullHint() != Spatial.CullHint.Always;
    }
    
    private Geometry createImposter(float size, ColorRGBA color) {
        Mesh q = new Mesh();

        Vector3f [] vertices = new Vector3f[] { new Vector3f(0,0,0) };
        q.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));

        q.setMode(Mesh.Mode.Points);
        q.setPointSize(size);
        q.updateBound();
        q.setStatic();

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        
        Geometry geom = new Geometry("imposter", q);
        geom.setMaterial(mat);

        return geom;
    }

    public void setupImposter(final Entity entity, final ImposterGeometry data) {
        ColorRGBA colorRGBA = new ColorRGBA(data.red, data.green, data.blue, data.alpha);

        final Geometry imposter = createImposter(data.radius, colorRGBA);
        final Node node = getNode(entity);
        
        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(new ImposterPositionControl(entity));
            node.attachChild(imposter);
            return null;
        }});
    }

    public void setupPPS(Entity entity, PPSGeometry aThis) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new PositionControl(entity, false);
        final RotationControl rotationControl = new RotationControl(entity);

        final Geometry base = new Geometry("base", new Box(0.5f, 0.05f, 0.5f));
        base.setMaterial(getMaterial("rock09.jpg"));
        base.setLocalTranslation(0,-0.45f,0);
        
        final Geometry spinner = new Geometry("spinner", new Torus(12, 12, 0.05f, 0.2f));
        spinner.setMaterial(material(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f), true));
        
        spinner.addControl(new RandomSpinnerControl());

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, base, true);
            attach(node, spinner, true);
            return null;
        }});
    }

    public void setupFloppyDrive(Entity entity, FloppyDriveGeometry comp) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new PositionControl(entity, false);
        final RotationControl rotationControl = new RotationControl(entity);

        final Geometry base = new Geometry("dcpu", new Box(0.5f, 0.5f, 0.5f));
        base.setMaterial(material(ColorRGBA.Black, true));
        
        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, base, true);
            return null;
        }});
    }

    public void setupRadar(Entity entity, RadarGeometry aThis) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new PositionControl(entity, false);
        final RotationControl rotationControl = new RotationControl(entity);

        final Geometry base = new Geometry("base", new Box(0.5f, 0.05f, 0.5f));
        base.setMaterial(getMaterial("rock09.jpg"));
        base.setLocalTranslation(0,-0.45f,0);

        final Node spinnerRotator = new Node("Spinner Align");
        
        Node spinner = new Node("spinner");
        spinnerRotator.attachChild(spinner);
        Quaternion t = new Quaternion().fromAngles((float) (-Math.PI / 2.0), 0, 0);
        spinner.setLocalRotation(t);
        
        Geometry inside = new Geometry("inside", new Dome(Vector3f.ZERO, 12, 12, 0.38f, true));
        inside.setMaterial(material(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f), false));
        spinner.attachChild(inside);

        Geometry outside = new Geometry("outside", new Dome(Vector3f.ZERO, 12, 12, 0.39f, false));
        outside.setMaterial(material(new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f), true));
        spinner.attachChild(outside);
        
        spinnerRotator.addControl(new LookAtControl(entity));

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, base, true);
            attach(node, spinnerRotator, true);
            return null;
        }});
    }

    public void setupGyroscope(Entity entity, GyroscopeGeometry aThis) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new PositionControl(entity, false);

        final Geometry base = new Geometry("base", new Box(0.5f, 0.05f, 0.5f));
        //base.setMaterial(material(new ColorRGBA(0.7f, 0.7f, 0.7f, 0.5f), true));
        base.setMaterial(getMaterial("rock09.jpg"));
        base.setLocalTranslation(0, -0.45f, 0);

        final Geometry wheel = new Geometry("wheel", new Cylinder(5, 5, 0.35f, 0.35f, 0.45f, true, false));
        wheel.setMaterial(getMaterial("rock09.jpg"));

        wheel.addControl(new GyroscopeControl(entity));
        wheel.addControl(new RotationControl(entity));

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);

            attach(node, base, true);
            attach(node, wheel, true);
            return null;
        }});
    }

    private void setOffsetTranslation(Node node, Vector3f value) {
        offset(node).setLocalTranslation(value);
    }

    public void setupThermalLaser(Entity entity, ThermalLaserGeometry data) {
        final Node node = getNode(entity);

        final PositionControl positionControl = new  PositionControl(entity, false);
        final RotationControl rotationControl = new  RotationControl(entity);
        final Cylinder cyl = new Cylinder(6, 6, 0.2f, 0.2f, 100, true, false);

        final Geometry beam = new Geometry("beam", cyl);
        beam.setQueueBucket(Bucket.Transparent); // Remenber to set the queue bucket to transparent for the spatial
        beam.setLocalTranslation(0, 0, -100/2f);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        ColorRGBA yellow = new ColorRGBA(1f, 1f, 0f, 0.8f);
        mat.setColor("Color", yellow);
        mat.setColor("GlowColor", ColorRGBA.Yellow);
        beam.setMaterial(mat);
        // TRANSPARENT?
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        // ---
        beam.addControl(new ThermalLaserControl(entity, cyl));

        final Geometry weapon = new Geometry("weapon", new Cylinder(16, 16, 0.5f, 0.3f, data.length, true, false));
        weapon.setLocalTranslation(0, 0, -data.length/2f + 0.5f);
        weapon.setMaterial(material(ColorRGBA.Gray, true));
        
        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, beam, true);
            attach(node, weapon, true);
            return null;
        }});
    }

    public void setupForceField(Entity entity, ForceFieldGeometry aThis) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new  PositionControl(entity, false);
        //final RotationControl rotationControl = new  RotationControl(entity);

        final Geometry base = new Geometry("base", new Box(0.5f, 0.05f, 0.5f));
        base.setMaterial(getMaterial("rock09.jpg"));
        base.setLocalTranslation(0, -0.45f, 0);

//        final Geometry dome = new Geometry("dome", new Sphere(12, 12, 0.45f));
//
//        dome.setLocalTranslation(0, 0.05f, 0);
//        dome.setMaterial(material(ColorRGBA.Blue, true));
//
        final Node dome = new Node("chamber");

        Geometry cylinder = new Geometry("item", new Sphere(12, 12, 0.45f));
        dome.attachChild(cylinder);
        dome.setLocalTranslation(0, 0.05f, 0);
        //dome.setLocalRotation(new Quaternion().fromAngles((float) (Math.PI / 2.0), 0, 0));
        //chamber.setLocalTranslation(0, -0.4f, 0);
        dome.setMaterial(material(ColorRGBA.Black, true));
        
        //Spatial cylinder = assetManager.loadModel("Models/jme_lightblow.mesh.xml");

        //cylinder.setMaterial(new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")); 

        electrify(dome, "Materials/Electricity/electricity3_line2.j3m");

        // Create spatial to be the shield
        final Sphere sphere = new Sphere(30, 30, 15);
        final Geometry shield = new Geometry("forceshield", sphere);
        shield.setQueueBucket(Bucket.Transparent); // Remenber to set the queue bucket to transparent for the spatial
 
        // Create ForceShieldControl
        Material material = new Material(assetManager, "ShaderBlow/MatDefs/ForceShield/ForceShield.j3md");
        material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        material.setFloat("MaxDistance", 1);

        ForceFieldControl forceFieldControl = new ForceFieldControl(entity, shield, material, sphere);
        shield.addControl(forceFieldControl); // Add the control to the spatial
        forceFieldControl.setEffectSize(10f); // Set the effect size
        forceFieldControl.setColor(new ColorRGBA(0, 0, 1, 1)); // Set effect color
        forceFieldControl.setVisibility(0.1f); // Set shield visibility.
 
        // Set a texture to the shield
        forceFieldControl.setTexture(this.assetManager.loadTexture("Textures/fs_texture.png"));

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);

            attach(node, base, true);
            attach(node, dome, true);
            attach(node, shield, true);
            return null;
        }});
    }

    public void setupPowerPlant(Entity entity, PowerPlantGeometry aThis) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new  PositionControl(entity, false);
        final RotationControl rotationControl = new  RotationControl(entity);

        final Geometry base = new Geometry("base", new Box(0.5f, 0.05f, 0.5f));
        base.setMaterial(getMaterial("rock09.jpg"));
        //base.setLocalTranslation(0, -0.45f, 0);
        base.setLocalTranslation(0, -0.45f, 0);

        final Node chamber = new Node("chamber");

        Geometry cylinder = new Geometry("Reactor core", new Cylinder(16, 16, 0.45f, 0.9f, true));
        chamber.attachChild(cylinder);
        chamber.setLocalTranslation(0, 0.1f, 0);
        chamber.setLocalRotation(new Quaternion().fromAngles((float) (-Math.PI / 2.0), 0, 0));
        //chamber.setLocalTranslation(0, -0.4f, 0);
        chamber.setMaterial(material(ColorRGBA.White, true));
        
        //Spatial cylinder = assetManager.loadModel("Models/jme_lightblow.mesh.xml");

        //cylinder.setMaterial(new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")); 

        electrify(chamber, "Materials/Electricity/electricity1_2.j3m");

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, base, true);
            attach(node, chamber, true);
            return null;
        }});
    }
    
    void electrify(Node man, String material) {
        Material mat = assetManager.loadMaterial(material);
        
        for (Spatial child : ((Node)man).getChildren()){
            if (child instanceof Geometry){
                Geometry electricity = new Geometry("electrified_" + child.getName());
                electricity.setQueueBucket(Bucket.Transparent);
                electricity.setMesh(((Geometry)child).getMesh());
                electricity.setMaterial(mat);
                ((Node)man).attachChild(electricity);
            }
        }
    }

    public void setupBattery(Entity entity, BatteryGeometry aThis) {
        final Node node = getNode(entity);
        final PositionControl positionControl = new  PositionControl(entity, false);
        final RotationControl rotationControl = new  RotationControl(entity);

        final Geometry base = new Geometry("base", new Box(0.5f, 0.05f, 0.5f));
        base.setMaterial(getMaterial("rock09.jpg"));
        //base.setLocalTranslation(0, -0.45f, 0);
        base.setLocalTranslation(0, -0.45f, 0);

        final Node chamber = new Node("chamber");

        Geometry cylinder = new Geometry("Battery core", new Cylinder(6, 6, 0.45f, 0.9f, true));
        chamber.attachChild(cylinder);
        chamber.setLocalTranslation(0, 0.1f, 0);
        chamber.setLocalRotation(new Quaternion().fromAngles((float) (-Math.PI / 2.0), 0, 0));
        //chamber.setLocalTranslation(0, -0.4f, 0);
        chamber.setMaterial(material(ColorRGBA.Yellow, true));
        
        //Spatial cylinder = assetManager.loadModel("Models/jme_lightblow.mesh.xml");

        //cylinder.setMaterial(new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")); 

        electrify(chamber, "Materials/Electricity/electricity3_line1.j3m");

        app.enqueue(new Callable() {@Override public Object call() throws Exception {
            node.addControl(positionControl);
            node.addControl(rotationControl);

            attach(node, base, true);
            attach(node, chamber, true);
            return null;
        }});
    }
    
}
