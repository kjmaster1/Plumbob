package com.kjmaster.plumbob;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/** This class is largely based on a MinecraftByExample tutorial.
 *  The tutorial can be found here https://goo.gl/4NT5zE
 */

public class PlumbobRender {

    private static final ResourceLocation gemTexture =
            new ResourceLocation("plumbob:textures/entity/gem.png");
    private int renderTicks = 0;
    static Color gemColour;
    private final long INVALID_TIME = 0;
    private long lastTime = INVALID_TIME; // used for animation
    private double lastAngularPosition; // used for animation

    private ArrayList<Color> colourChoices = new ArrayList<>();

    @SubscribeEvent
    public void render(RenderPlayerEvent.Post event)
    {
        EntityPlayer player = event.getEntityPlayer();

        if (player.getDistanceSq(Minecraft.getMinecraft().player) < 64 * 64)
        {
            renderPlumbob(event.getEntityPlayer(), event.getX(), event.getY(), event.getZ());
        }
    }

    private void renderPlumbob(EntityPlayer player, double x, double y, double z)
    {
        renderTicks++;
        if (renderTicks >= PlumbobConfig.ticks && PlumbobConfig.healthBased && !PlumbobConfig.changeColour)
        {
            float health = player.getHealth();
            if (!PlumbobConfig.doubleHealth)
            {
                if (health <= 6)
                {
                    gemColour = Color.RED;
                } else if (health <= 13)
                {
                    gemColour = Color.ORANGE;
                } else {
                    gemColour = Color.GREEN;
                }
            } else {
                if (health <= 6)
                {
                    gemColour = new Color(153, 0, 0);
                } else if (health <= 13)
                {
                    gemColour = new Color(255, 0, 0);
                } else if (health <= 20)
                {
                    gemColour = new Color(255, 102, 0);
                } else if (health <= 27)
                {
                    gemColour = new Color(255, 153, 51);
                } else if (health <= 34)
                {
                    gemColour = new Color(102, 255, 51);
                } else {
                    gemColour = new Color(0, 153, 51);
                }
            }
        }
        if (renderTicks >= PlumbobConfig.ticks && PlumbobConfig.changeColour && !PlumbobConfig.healthBased)
        {
            Random random = Minecraft.getMinecraft().world.rand;
            for (int i = 0; i < PlumbobConfig.colours.length; i++)
            {
                Color colour = new Color(PlumbobConfig.colours[i]);
                colourChoices.add(colour);
            }
            gemColour = colourChoices.get(random.nextInt(colourChoices.size()));
            colourChoices.clear();
            renderTicks = 0;
        }
        final double plumbobCentreOffsetX = 0.0;
        double plumbobCentreOffsetY = PlumbobConfig.yOffset;

        if (player.getUniqueID().equals(Minecraft.getMinecraft().player.getUniqueID()) || Minecraft.getMinecraft().gameSettings.hideGUI)
        {
         plumbobCentreOffsetY -= 0.4;
        }

        final double plumbobCentreOffsetZ = 0.0;
        Vec3d playerEye = new Vec3d(0.0, 0.0, 0.0);
        Vec3d plumbobCentre = new Vec3d(x + plumbobCentreOffsetX, y + plumbobCentreOffsetY, z + plumbobCentreOffsetZ);
        double playerDistance = playerEye.distanceTo(plumbobCentre);

        final double DISTANCE_FOR_MIN_SPIN = PlumbobConfig.DISTANCE_FOR_MIN_SPIN;
        final double DISTANCE_FOR_MAX_SPIN = PlumbobConfig.DISTANCE_FOR_MAX_SPIN;
        final double DISTANCE_FOR_MIN_LEVITATE = PlumbobConfig.DISTANCE_FOR_MIN_LEVITATE;
        final double DISTANCE_FOR_MAX_LEVITATE = PlumbobConfig.DISTANCE_FOR_MAX_LEVITATE;

        final double MIN_LEVITATE_HEIGHT = PlumbobConfig.MIN_LEVITATE_HEIGHT;
        final double MAX_LEVITATE_HEIGHT = PlumbobConfig.MAX_LEVITATE_HEIGHT;
        double gemCentreOffsetX = plumbobCentreOffsetX;
        double gemCentreOffsetY = plumbobCentreOffsetY + interpolate(playerDistance, DISTANCE_FOR_MIN_LEVITATE,
                DISTANCE_FOR_MAX_LEVITATE, MIN_LEVITATE_HEIGHT, MAX_LEVITATE_HEIGHT);
        double gemCentreOffsetZ = plumbobCentreOffsetZ;

        final double MIN_REV_PER_SEC = 0.0;
        final double MAX_REV_PER_SEC = 0.5;
        double revsPerSecond = interpolate(playerDistance, DISTANCE_FOR_MIN_SPIN, DISTANCE_FOR_MAX_SPIN,
                MIN_REV_PER_SEC, MAX_REV_PER_SEC);
        double angularPositionInDegrees = getNextAngularPosition(revsPerSecond);

        try {
            // save the transformation matrix and the rendering attributes, so that we can restore them after rendering.  This
            //   prevents us disrupting any vanilla TESR that render after ours.
            //  using try..finally is not essential but helps make it more robust in case of exceptions
            // For further information on rendering using the Tessellator, see http://greyminecraftcoder.blogspot.co.at/2014/12/the-tessellator-and-worldrenderer-18.html
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();

            // First we need to set up the translation so that we render our gem with the bottom point at 0,0,0
            // when the renderTileEntityAt method is called, the tessellator is set up so that drawing a dot at [0,0,0] corresponds to the player's eyes
            // This means that, in order to draw a dot at the TileEntity [x,y,z], we need to translate the reference frame by the difference between the
            // two points, i.e. by the [relativeX, relativeY, relativeZ] passed to the method.  If you then draw a cube from [0,0,0] to [1,1,1], it will
            // render exactly over the top of the TileEntity's block.
            // In this example, the zero point of our model needs to be in the middle of the block, not at the [x,y,z] of the block, so we need to
            // add an extra offset as well, i.e. [gemCentreOffsetX, gemCentreOffsetY, gemCentreOffsetZ]
            GlStateManager.translate(x + gemCentreOffsetX, y + gemCentreOffsetY, z + gemCentreOffsetZ);

            GlStateManager.rotate((float) angularPositionInDegrees, 0, 1, 0); // rotate around the vertical axis

            final double GEM_HEIGHT = 0.5; // desired render height of the gem
            final double MODEL_HEIGHT = 1.0; // actual height of the gem in the vertexTable
            final double SCALE_FACTOR = GEM_HEIGHT / MODEL_HEIGHT;
            GlStateManager.scale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
            textureManager.bindTexture(gemTexture);

            // set the key rendering flags appropriately...
            GL11.glDisable(GL11.GL_LIGHTING); // turn off "item" lighting (face brightness depends on which direction it is facing)
            GL11.glDisable(GL11.GL_BLEND); // turn off "alpha" transparency blending
            GL11.glDepthMask(true); // gem is hidden behind other objects

            // set the rendering colour as the gem base colour
            Color fullBrightnessColour = getGemColour();
            float red = (float) (fullBrightnessColour.getRed() / 255.0);
            float green = (float) (fullBrightnessColour.getGreen() / 255.0);
            float blue = (float) (fullBrightnessColour.getBlue() / 255.0);
            GlStateManager.color(red, green, blue); // change the rendering colour

            bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
            addGemVertices(bufferBuilder);
            tessellator.draw();

        } finally {
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    /** linearly interpolate for y between [x1, y1] to [x2, y2] using x
     *  y = y1 + (y2 - y1) * (x - x1) / (x2 - x1)
     *  For example:  if [x1, y1] is [0, 100], and [x2,y2] is [1, 200], then as x increases from 0 to 1, this function
     *    will increase from 100 to 200
     * @param x  the x value to linearly interpolate on
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @return linearly interpolated value.  If x is outside the range, clip it to the nearest end
     */
    private static double interpolate(double x, double x1, double x2, double y1, double y2)
    {
        if (x1 > x2) {
            double temp = x1; x1 = x2; x2 = temp;
            temp = y1; y1 = y2; y2 = temp;
        }

        if (x <= x1) return y1;
        if (x >= x2) return y2;
        double xFraction = (x - x1) / (x2 - x1);
        return y1 + xFraction * (y2 - y1);
    }

    private void addGemVertices(BufferBuilder bufferBuilder)
    {
        final double[][] vertexTable = {
                {0.000,1.000,0.000,0.000,0.118},          //1
                {-0.354,0.500,-0.354,0.000,0.354},
                {-0.354,0.500,0.354,0.236,0.236},
                {-0.354,0.500,0.354,0.236,0.236},         //2
                {-0.354,0.500,-0.354,0.000,0.354},
                {0.000,0.000,0.000,0.236,0.471},
                {-0.354,0.500,0.354,0.236,0.236},         //3
                {0.000,0.000,0.000,0.236,0.471},
                {0.354,0.500,0.354,0.471,0.354},
                {-0.354,0.500,0.354,0.236,0.236},         //4
                {0.354,0.500,0.354,0.471,0.354},
                {0.000,1.000,0.000,0.471,0.118},
                {0.000,1.000,0.000,0.471,0.118},          //5
                {0.354,0.500,0.354,0.471,0.354},
                {0.354,0.500,-0.354,0.707,0.236},
                {0.354,0.500,-0.354,0.707,0.236},         //6
                {0.354,0.500,0.354,0.471,0.354},
                {0.000,0.000,0.000,0.707,0.471},
                {0.354,0.500,-0.354,0.707,0.236},         //7
                {0.000,0.000,0.000,0.707,0.471},
                {-0.354,0.500,-0.354,0.943,0.354},
                {0.000,1.000,0.000,0.943,0.118},          //8
                {0.354,0.500,-0.354,0.707,0.236},
                {-0.354,0.500,-0.354,0.943,0.354}
        };

        for (double [] vertex : vertexTable) {
            bufferBuilder.pos(vertex[0], vertex[1], vertex[2])
                    .tex(vertex[3], vertex[4])
                    .endVertex();
        }
    }

    private double getNextAngularPosition(double revsPerSecond) {

        // we calculate the next position as the angular speed multiplied by the elapsed time since the last position.
        // Elapsed time is calculated using the system clock, which means the animations continue to
        //  run while the game is paused.
        // Alternatively, the elapsed time can be calculated as
        //  time_in_seconds = (number_of_ticks_elapsed + partialTick) / 20.0;
        //  where your tileEntity's update() method increments number_of_ticks_elapsed, and partialTick is passed by vanilla
        //   to your TESR renderTileEntityAt() method.
        long timeNow = System.nanoTime();
        if (lastTime == INVALID_TIME) { // automatically initialise to 0 if not set yet
            lastTime = timeNow;
            lastAngularPosition = 0.0;
        }
        final double DEGREES_PER_REV = 360.0;
        final double NANOSECONDS_PER_SECOND = 1e9;
        double nextAngularPosition = lastAngularPosition + (timeNow - lastTime) * revsPerSecond * DEGREES_PER_REV / NANOSECONDS_PER_SECOND;
        nextAngularPosition = nextAngularPosition % DEGREES_PER_REV;
        lastAngularPosition = nextAngularPosition;
        lastTime = timeNow;
        return nextAngularPosition;
    }

    // get the colour of the gem. Returns INVALID_COLOUR if not set yet.
    private static Color getGemColour() {
        return gemColour;
    }
}
