package mcib3d.image3d;

import ij.*;
import ij.process.*;
import ij.gui.*;
import mcib3d.image3d.legacy.IntImage3D;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import mcib3d.geom.*;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 * @author Thomas Boudier
 */
public class ImageShort extends ImageInt {

    public short[][] pixels;

    public ImageShort(ImagePlus img) {
        super(img);
        buildPixels();
    }

    private void buildPixels() {
        pixels = new short[sizeZ][];
        if (img.getImageStack() != null) {
            for (int i = 0; i < sizeZ; i++) {
                //IJ.log("pixels="+img.getImageStack().getPixels(i + 1));
                pixels[i] = (short[]) img.getImageStack().getPixels(i + 1);
            }
        } else {
            ImageStack st = new ImageStack(sizeX, sizeY);
            st.addSlice(img.getProcessor());
            pixels[0] = (short[]) img.getProcessor().getPixels();
            this.img.setStack(null, st);
        }
    }

    public ImageShort(ImageStack img) {
        super(img);
        buildPixels();
    }

    public ImageShort(short[][] pixels, String title, int sizeX) {
        super(title, sizeX, pixels[0].length / sizeX, pixels.length, 0, 0, 0);
        this.pixels = pixels;
        ImageStack st = new ImageStack(sizeX, sizeY, sizeZ);
        for (int z = 0; z < sizeZ; z++) {
            st.setPixels(pixels[z], z + 1);
        }
        img = new ImagePlus(title, st);
    }

    public ImageShort(String title, int sizeX, int sizeY, int sizeZ) {
        super(title, sizeX, sizeY, sizeZ);
        img = NewImage.createShortImage(title, sizeX, sizeY, sizeZ, 1);
        pixels = new short[sizeZ][];
        for (int i = 0; i < sizeZ; i++) {
            pixels[i] = (short[]) img.getImageStack().getPixels(i + 1);
        }
    }

    public ImageShort(ImageHandler im, boolean scaling) {
        super(im.title, im.sizeX, im.sizeY, im.sizeZ, im.offsetX, im.offsetY, im.offsetZ);
        ImageStats st = getImageStats(null);
        if (im instanceof ImageByte) {
            ImageShort s = ((ImageByte) im).convertToShort(scaling);
            pixels = s.pixels;
            img = s.img;
            st.setMinAndMax(img.getProcessor().getMin(), img.getProcessor().getMax());
        } else if (im instanceof ImageFloat) {
            if (im.img != null) {
                ImageShort s = ((ImageFloat) im).convertToShort(scaling);
                pixels = s.pixels;
                img = s.img;
                st.setMinAndMax(img.getProcessor().getMin(), img.getProcessor().getMax());
            }
        } else {
            this.img = im.img;
            this.pixels = ((ImageShort) im).pixels;
        }
    }

    public static short[] getArray1DShort(ImagePlus img) {
        short[] res = new short[img.getNSlices() * img.getWidth() * img.getHeight()];
        int offZ = 0;
        int sizeXY = img.getWidth() * img.getHeight();
        for (int slice = 0; slice < img.getNSlices(); slice++) {
            System.arraycopy((short[]) img.getImageStack().getPixels(slice + 1), 0, res, offZ, sizeXY);
            offZ += sizeXY;
        }
        return res;
    }

    public Object getArray1D() {
        short[] res = new short[sizeXYZ];
        int offZ = 0;
        for (int slice = 0; slice < img.getNSlices(); slice++) {
            System.arraycopy((short[]) img.getImageStack().getPixels(slice + 1), 0, res, offZ, sizeXY);
            offZ += sizeXY;
        }
        return res;
    }

    public Object getArray1D(int z) {
        short[] res = new short[sizeXY];
        System.arraycopy((short[]) img.getImageStack().getPixels(z + 1), 0, res, 0, sizeXY);
        return res;
    }

    public static ImagePlus getImagePlus(short[] pixels, int sizeX, int sizeY, int sizeZ, boolean setMinAndMax) {
        if (pixels == null) {
            return null;
        }
        ImagePlus res = NewImage.createShortImage("", sizeX, sizeY, sizeZ, 1);
        int offZ = 0;
        int sizeXY = sizeX * sizeY;
        for (int z = 0; z < sizeZ; z++) {
            System.arraycopy(pixels, offZ, (short[]) res.getImageStack().getPixels(z + 1), 0, sizeXY);
            offZ += sizeXY;
        }
        if (setMinAndMax) {
            int max = 0;
            int min = 0;
            for (int i = 0; i < pixels.length; i++) {
                if ((pixels[i] & 0xffff) > max) {
                    max = pixels[i] & 0xffff;
                }
                if ((pixels[i] & 0xffff) < min) {
                    min = pixels[i] & 0xffff;
                }
            }
            res.getProcessor().setMinAndMax(min, max);
        }
        return res;
    }

    public static short[] convert(float[] input, boolean scaling) {
        short[] res = new short[input.length];
        if (!scaling) {
            for (int i = 0; i < input.length; i++) {
                res[i] = (short) (input[i] + 0.5f);
            }
        } else {
            float min = input[0];
            float max = input[0];
            for (float f : input) {
                if (f < min) {
                    min = f;
                }
                if (f > max) {
                    max = f;
                }
            }
            float coeff = 65535 / (max - min);
            for (int i = 0; i < input.length; i++) {
                res[i] = (short) ((input[i] - min) * coeff - 32767.5);
            }
        }
        return res;
    }

    public static short[] convert(byte[] input) {
        short[] res = new short[input.length];
        for (int i = 0; i < input.length; i++) {
            res[i] = (short) (input[i]);
        }
        return res;
    }

    public ImageByte convertToByte(boolean scaling) {
        if (scaling) {
            setMinAndMax(null);
        }
        ImageStats s = getImageStats(null);
        int currentSlice = img.getCurrentSlice();
        ImageProcessor ip;
        ImageStack stack2 = new ImageStack(sizeX, sizeY);
        String label;
        ImageStack stack1 = img.getImageStack();
        for (int i = 1; i <= sizeZ; i++) {
            label = stack1.getSliceLabel(i);
            ip = stack1.getProcessor(i);
            if (scaling) {
                ip.setMinAndMax(s.getMin(), s.getMax());
            }
            stack2.addSlice(label, ip.convertToByte(scaling));
        }
        ImagePlus imp2 = new ImagePlus(img.getTitle(), stack2);
        imp2.setCalibration(img.getCalibration()); //update calibration
        imp2.setSlice(currentSlice);
        imp2.getProcessor().setMinAndMax(0, 255);
        return (ImageByte) ImageHandler.wrap(imp2);
    }

    public ImageFloat convertToFloat(boolean scaling) {
        if (scaling) {
            setMinAndMax(null);
        }
        ImageStats s = getImageStats(null);
        int currentSlice = img.getCurrentSlice();
        ImageProcessor ip;
        ImageStack stack2 = new ImageStack(sizeX, sizeY);
        String label;
        ImageStack stack1 = img.getImageStack();
        for (int i = 1; i <= sizeZ; i++) {
            label = stack1.getSliceLabel(i);
            ip = stack1.getProcessor(i);
            if (scaling) {
                ip.setMinAndMax(s.getMin(), s.getMax());
            }
            stack2.addSlice(label, ip.convertToFloat());
        }
        ImagePlus imp2 = new ImagePlus(img.getTitle(), stack2);
        imp2.setCalibration(img.getCalibration()); //update calibration
        imp2.setSlice(currentSlice);
        imp2.getProcessor().setMinAndMax(0, 255);
        return (ImageFloat) ImageHandler.wrap(imp2);
    }

    @Override
    public void erase() {
        for (int xy = 0; xy < sizeXY; xy++) {
            pixels[0][xy] = 0;
        }
        for (int z = 1; z < sizeZ; z++) {
            System.arraycopy(pixels[0], 0, pixels[z], 0, sizeXY);
        }
    }

    @Override
    public void fill(double value) {
        for (int xy = 0; xy < sizeXY; xy++) {
            pixels[0][xy] = (short) value;
        }
        for (int z = 1; z < sizeZ; z++) {
            System.arraycopy(pixels[0], 0, pixels[z], 0, sizeXY);
        }
    }

    @Override
    public ImageShort duplicate() {
        ImageShort res = new ImageShort(img.duplicate());
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        if (title != null) {
            res.title = title;
        }
        return res;
    }

    public void copy(ImageShort destination) {
        for (int z = 0; z < sizeZ; z++) {
            System.arraycopy(pixels[z], 0, destination.pixels[z], 0, sizeXY);
        }
    }

    @Override
    public float getPixel(int coord) {
        return (float) (pixels[coord / sizeXY][coord % sizeXY] & 0xffff);
    }

    @Override
    public float getPixel(Point3D v) {
        return (float) (pixels[v.getRoundZ()][v.getRoundX() + v.getRoundY() * sizeX] & 0xffff);
    }

    @Override
    public int getPixelInt(Point3D v) {
        return pixels[v.getRoundZ()][v.getRoundX() + v.getRoundY() * sizeX] & 0xffff;
    }

    @Override
    public float getPixel(int x, int y, int z) {
        return (float) (pixels[z][x + y * sizeX] & 0xffff);
    }

    @Override
    public float getPixel(int xy, int z) {
        return (float) (pixels[z][xy] & 0xffff);
    }

    @Override
    public int getPixelInt(int x, int y, int z) {
        return pixels[z][x + y * sizeX] & 0xffff;

    }

    @Override
    public int getPixelInt(int xy, int z) {
        return pixels[z][xy] & 0xffff;
    }

    @Override
    public int getPixelInt(int coord) {
        return pixels[coord / sizeXY][coord % sizeXY] & 0xffff;
    }

    @Override
    public void setPixel(int coord, float value) {
        pixels[coord / sizeXY][coord % sizeXY] = (short) value;
    }

    @Override
    public void setPixel(Point3D point, float value) {
        pixels[point.getRoundZ()][point.getRoundX() + point.getRoundY() * sizeX] = (short) value;
    }

    @Override
    public void setPixel(int x, int y, int z, float value) {
        pixels[z][x + y * sizeX] = (short) value;
    }

    @Override
    public void setPixel(int xy, int z, float value) {
        pixels[z][xy] = (short) value;
    }

    @Override
    public void setPixel(int x, int y, int z, int value) {
        pixels[z][x + y * sizeX] = (short) value;
    }

    @Override
    public void setPixel(int xy, int z, int value) {
        pixels[z][xy] = (short) value;
    }

    public void setPixel(mcib3d.geom.Voxel3D v, short value) {
        pixels[(int) v.getZ()][(int) v.getX() + ((int) v.getY()) * sizeX] = value;
    }

    @Override
    protected synchronized void getMinAndMax(ImageInt mask) {
        ImageStats s = getImageStats(mask);
        if (s.minAndMaxSet()) {
            return;
        }
        int max = 0;
        int min = Integer.MAX_VALUE;
        if (mask == null) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xffff) > max) {
                        max = pixels[z][xy] & 0xffff;
                    }
                    if ((pixels[z][xy] & 0xffff) < min) {
                        min = pixels[z][xy] & 0xffff;
                    }
                }
            }
        } else {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if (mask.getPixel(xy, z) != 0) {
                        if ((pixels[z][xy] & 0xffff) > max) {
                            max = pixels[z][xy] & 0xffff;
                        }
                        if ((pixels[z][xy] & 0xffff) < min) {
                            min = pixels[z][xy] & 0xffff;
                        }
                    }
                }
            }
        }
        s.setMinAndMax(min, max);
    }

    @Override
    protected int[] getHisto(ImageInt mask) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        getMinAndMax(mask);
        ImageStats s = getImageStats(mask);
        double min = s.getMin();
        double coeff = 256f / (s.getMax() - s.getMin());
        int[] histo = new int[256];
        int idx;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0) {
                    idx = (int) (((pixels[z][xy] & 0xFFFF) - min) * coeff);
                    if (idx >= 256) {
                        histo[255]++;
                    } else {
                        histo[idx]++;
                    }
                }
            }
        }
        s.setHisto256(histo, 1d / coeff);
        return histo;
    }

    @Override
    protected int[] getHisto(ImageInt mask, int nBins, double min, double max) {
        if (mask == null) {
            mask = new BlankMask(this);
        }
        double scale = (double) nBins / (max - min);
        int[] hist = new int[nBins];
        int index;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) != 0) {
                    index = (int) (((pixels[z][xy] & 0xFFFF) - min) * scale);
                    if (index >= nBins) {
                        hist[nBins - 1]++;
                    } else {
                        hist[index]++;
                    }
                }
            }
        }
        return hist;
    }

    @Override
    public IntImage3D getImage3D() {
        return new IntImage3D(img.getImageStack());
    }
    
    @Override
    public ImageByte thresholdRangeInclusive(float min, float max){
        ImageByte res = new ImageByte(this.title + "thld", sizeX, sizeY, sizeZ);
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        
         for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if (((pixels[z][xy] & 0xFFFF) >=min)&&(((pixels[z][xy] & 0xFFFF) <=max))) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        return res;
    }
    
    @Override
    public ImageByte thresholdRangeExclusive(float min, float max){
        ImageByte res = new ImageByte(this.title + "thld", sizeX, sizeY, sizeZ);
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        
         for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if (((pixels[z][xy] & 0xFFFF) >min)&&(((pixels[z][xy] & 0xFFFF) <max))) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        return res;
    }
    
    

    @Override
    public ImageByte threshold(float thld, boolean keepUnderThld, boolean strict) {
        ImageByte res = new ImageByte(this.title + "thld", sizeX, sizeY, sizeZ);
        res.offsetX = offsetX;
        res.offsetY = offsetY;
        res.offsetZ = offsetZ;
        if (!keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) >= thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (!keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) > thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) <= thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        } else if (keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) < thld) {
                        res.pixels[z][xy] = (byte) 255;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void thresholdCut(float thld, boolean keepUnderThld, boolean strict) { //modifies the image
        if (!keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) < thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (!keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) <= thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (keepUnderThld && !strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) > thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        } else if (keepUnderThld && strict) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    if ((pixels[z][xy] & 0xFFFF) >= thld) {
                        pixels[z][xy] = 0;
                    }
                }
            }
        }
    }

    @Override
    public ImageShort crop3D(String title, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageShort res = new ImageShort(title, sX, sY, sZ);
        res.offsetX = x_min + offsetX;
        res.offsetY = y_min + offsetY;
        res.offsetZ = z_min + offsetZ;
        res.setScale(this);
        int oZ = -z_min;
        int oY_i = 0;
        int oX = 0;
        if (x_min <= -1) {
            x_min = 0;
        }
        if (x_max >= sizeX) {
            x_max = sizeX - 1;
        }
        if (y_min <= -1) {
            oY_i = -sX * y_min;
            y_min = 0;
        }
        if (y_max >= sizeY) {
            y_max = sizeY - 1;
        }
        if (z_min <= -1) {
            z_min = 0;
        }
        if (z_max >= sizeZ) {
            z_max = sizeZ - 1;
        }
        int sXo = x_max - x_min + 1;
        for (int z = z_min; z <= z_max; z++) {
            int offY = y_min * sizeX;
            int oY = oY_i;
            for (int y = y_min; y <= y_max; y++) {
                System.arraycopy(pixels[z], offY + x_min, res.pixels[z + oZ], oY + oX, sXo);
                oY += sX;
                offY += sizeX;
            }
        }
        return res;
    }

    @Override
    public ImageShort[] crop3D(TreeMap<Integer, int[]> bounds) {
        ImageShort[] ihs = new ImageShort[bounds.size()];
        ArrayList<Integer> keys = new ArrayList<Integer>(bounds.keySet());
        for (int idx = 0; idx < ihs.length; idx++) {
            int label = keys.get(idx);
            int[] bds = bounds.get(label);
            ihs[idx] = this.crop3D(title + ":" + label, bds[0], bds[1], bds[2], bds[3], bds[4], bds[5]);
        }
        return ihs;
    }

    @Override
    public ImageShort crop3DMask(String title, ImageInt mask, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageShort res = new ImageShort(title, sX, sY, sZ);
        res.offsetX = x_min;
        res.offsetY = y_min;
        res.offsetZ = z_min;
        res.setScale(this);
        int oZ = -z_min;
        int oY_i = 0;
        int oX = -x_min;
        if (x_min <= -1) {
            x_min = 0;
        }
        if (x_max >= sizeX) {
            x_max = sizeX - 1;
        }
        if (y_min <= -1) {
            oY_i = -sX * y_min;
            y_min = 0;
        }
        if (y_max >= sizeY) {
            y_max = sizeY - 1;
        }
        if (z_min <= -1) {
            z_min = 0;
        }
        if (z_max >= sizeZ) {
            z_max = sizeZ - 1;
        }
        if (mask instanceof ImageShort) {
            ImageShort m = (ImageShort) mask;
            for (int z = z_min; z <= z_max; z++) {
                int offY = y_min * sizeX;
                int oY = oY_i;
                for (int y = y_min; y <= y_max; y++) {
                    for (int x = x_min; x <= x_max; x++) {
                        if ((m.pixels[z][offY + x] & 0xffff) == label) {
                            res.pixels[z + oZ][oY + x + oX] = pixels[z][offY + x];
                        }
                    }
                    oY += sX;
                    offY += sizeX;
                }
            }
        } else if (mask instanceof ImageByte) {
            ImageByte m = (ImageByte) mask;
            for (int z = z_min; z <= z_max; z++) {
                int offY = y_min * sizeX;
                int oY = oY_i;
                for (int y = y_min; y <= y_max; y++) {
                    for (int x = x_min; x <= x_max; x++) {
                        if ((m.pixels[z][offY + x] & 0xff) == label) {
                            res.pixels[z + oZ][oY + x + oX] = pixels[z][offY + x];
                        }
                    }
                    oY += sX;
                    offY += sizeX;
                }
            }
        }
        return res;
    }

    @Override
    public ImageByte crop3DBinary(String title, int label, int x_min_, int x_max_, int y_min_, int y_max_, int z_min_, int z_max_) {
        //IJ.log("crop:"+x_min_+";"+x_max_+y_min_+";"+y_max_+z_min_+";"+z_max_);
        int x_min = x_min_;
        int z_min = z_min_;
        int y_min = y_min_;
        int x_max = x_max_;
        int y_max = y_max_;
        int z_max = z_max_;
        int sX = x_max - x_min + 1;
        int sY = y_max - y_min + 1;
        int sZ = z_max - z_min + 1;
        ImageByte res = new ImageByte(title, sX, sY, sZ);
        res.offsetX = x_min;
        res.offsetY = y_min;
        res.offsetZ = z_min;
        res.setScale(this);
        int oZ = -z_min;
        int oY_i = 0;
        int oX = -x_min;
        if (x_min <= -1) {
            x_min = 0;
        }
        if (x_max >= sizeX) {
            x_max = sizeX - 1;
        }
        if (y_min <= -1) {
            oY_i = -sX * y_min;
            y_min = 0;
        }
        if (y_max >= sizeY) {
            y_max = sizeY - 1;
        }
        if (z_min <= -1) {
            z_min = 0;
        }
        if (z_max >= sizeZ) {
            z_max = sizeZ - 1;
        }
        for (int z = z_min; z <= z_max; z++) {
            int offY = y_min * sizeX;
            int oY = oY_i;
            for (int y = y_min; y <= y_max; y++) {
                for (int x = x_min; x <= x_max; x++) {
                    if ((pixels[z][offY + x] & 0xffff) == label) {
                        res.pixels[z + oZ][oY + x + oX] = (byte) 255;
                    }
                }
                oY += sX;
                offY += sizeX;
            }
        }
        return res;
    }

    public void appendMasks(Iterable<ImageInt> images, int startLabel) { // 1 or several labels per image
        if (images == null) {
            return;
        }
        Iterator<ImageInt> iter = images.iterator();
        short currentLabel = (short) startLabel;
        int inLabel;
        Short outLabel;
        while (iter.hasNext()) {
            ImageInt image = iter.next();
            if (image == null) {
                continue;
            }
            //ij.IJ.log("merge. image:"+image.getTitle()+ " offsetX:"+image.offsetX);
            HashMap<Integer, Short> labelCorespondance = new HashMap<Integer, Short>(3);
            for (int z = 0; z < image.sizeZ; z++) {
                for (int y = 0; y < image.sizeY; y++) {
                    for (int x = 0; x < image.sizeX; x++) {
                        inLabel = image.getPixelInt(x, y, z);
                        if (inLabel != 0) {
                            outLabel = labelCorespondance.get(inLabel);
                            if (outLabel == null) {
                                outLabel = ++currentLabel;
                                labelCorespondance.put(inLabel, outLabel);
                            }
                            int xx = x + image.offsetX;
                            int yy = y + image.offsetY;
                            int zz = z + image.offsetZ;
                            if (zz >= 0 && zz < sizeZ && xx >= 0 && xx < sizeX && yy >= 0 && yy < sizeY) {
                                pixels[zz][xx + yy * sizeX] = outLabel;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shiftIndexes(TreeMap<Integer, int[]> bounds) {
        boolean change = false;
        int newLabel = 1;
        ArrayList<Integer> keySet = new ArrayList<Integer>(bounds.keySet());
        for (int i = 0; i < keySet.size(); i++) {
            int label = keySet.get(i);
            if (label > newLabel) {
                int[] bds = bounds.get(label);
                for (int z = bds[4]; z <= bds[5]; z++) {
                    for (int y = bds[2]; y <= bds[3]; y++) {
                        for (int x = bds[0]; x <= bds[1]; x++) {
                            int xy = x + y * sizeX;
                            if ((pixels[z][xy] & 0XFFFF) == label) {
                                pixels[z][xy] = (short) newLabel;
                            }
                        }
                    }
                }
                change = true;
                bounds.remove(label);
                bounds.put(newLabel, bds);
            }
            newLabel++;
        }
        return change;
    }

    @Override
    public ImageHandler resize(int dX, int dY, int dZ) {
        int newX = Math.max(1, sizeX + 2 * dX);
        int newY = Math.max(1, sizeY + 2 * dY);
        boolean bck = Prefs.get("resizer.zero", true);
        Prefs.set("resizer.zero", true);
        ij.plugin.CanvasResizer cr = new ij.plugin.CanvasResizer();
        ImageStack res = cr.expandStack(img.getStack(), newX, newY, dX, dY);
        if (!bck) {
            //Prefs.set("resizer.zero", false);
        }
        if (dZ > 0) {
            for (int i = 0; i < dZ; i++) {
                res.addSlice("", new ShortProcessor(newX, newY), 0);
                res.addSlice("", new ShortProcessor(newX, newY));
            }
        } else {
            for (int i = 0; i < -dZ; i++) {
                if (res.getSize() <= 2) {
                    break;
                }
                res.deleteLastSlice();
                res.deleteSlice(1);
            }
        }
        return new ImageShort(new ImagePlus(title + "::resized", res));
    }

    @Override
    public ImageShort resample(int newX, int newY, int newZ, int method) {
        if (method == -1) {
            method = ij.process.ImageProcessor.BICUBIC;
        }
        if ((newX == sizeX && newY == sizeY && newZ == sizeZ) || (newX == 0 && newY == 0 && newZ == 0)) {
            return new ImageShort(img.duplicate());
        }
        ImagePlus ip;
        if (newX != 0 && newY != 0 && newX != sizeX && newY != sizeY) {
            StackProcessor sp = new StackProcessor(img.getImageStack(), img.getProcessor());
            ip = new ImagePlus(title + "::resampled", sp.resize(newX, newY, true));
        } else {
            ip = img;
        }
        if (newZ != 0 && newZ != sizeZ) {
            ij.plugin.Resizer r = new ij.plugin.Resizer();
            ip = r.zScale(ip, newZ, method);
        }
        return new ImageShort(ip);
    }

    @Override
    public ImageShort resample(int newZ, int method) {
        if (method == -1) {
            method = ij.process.ImageProcessor.BICUBIC;
        }
        ij.plugin.Resizer r = new ij.plugin.Resizer();
        return new ImageShort(r.zScale(img, newZ, method));
    }

    @Override
    protected ImageFloat normalize_(ImageInt mask, double saturation) {
        getMinAndMax(mask);
        ImageStats s = getImageStats(mask);
        double max_ = s.getMax();
        if (saturation > 0 && saturation < 1) {
            max_ = this.getPercentile(saturation, mask);
        }
        if (max_ <= s.getMin()) {
            max_ = s.getMin() + 1;
        }
        double scale = 1 / (max_ - s.getMin());
        double offset = -s.getMin() * scale;
        ImageFloat res = new ImageFloat(title + "::normalized", sizeX, sizeY, sizeZ);
        if (saturation > 0 && saturation < 1) {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    res.pixels[z][xy] = (float) ((pixels[z][xy] >= max_) ? 1 : (pixels[z][xy] & 0XFFFF) * scale + offset);
                }
            }
        } else {
            for (int z = 0; z < sizeZ; z++) {
                for (int xy = 0; xy < sizeXY; xy++) {
                    res.pixels[z][xy] = (float) ((pixels[z][xy] & 0XFFFF) * scale + offset);
                }
            }
        }
        return res;
    }

    @Override
    public ImageFloat normalize(double min, double max) {
        double scale = 1 / (max - min);
        double offset = -min * scale;
        ImageFloat res = new ImageFloat(title + "::normalized", sizeX, sizeY, sizeZ);
        double pix;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                pix = pixels[z][xy] & 0xffff;
                if (pix >= max) {
                    res.pixels[z][xy] = 1;
                } else if (pix <= min) {
                    res.pixels[z][xy] = 0;
                } else {
                    res.pixels[z][xy] = (float) (pix * scale + offset);
                }
            }
        }
        return res;
    }

    // remove parameter mask ??
    // see invertMask ??
    @Override
    public void invert(ImageInt mask) {
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                pixels[z][xy] = (short) (65535 - pixels[z][xy]);
            }
        }
    }

    @Override
    public void draw(Object3D o, float value) {
        draw(o, (int) (value + 0.5));
    }

    @Override
    public void draw(Object3D o, int value) {
        Object3DVoxels ov;
        if (!(o instanceof Object3DVoxels)) {
            ov = o.getObject3DVoxels();
        } else {
            ov = (Object3DVoxels) o;
        }
        if (value > 65535) {
            value = 255;
        }
        if (value < 0) {
            value = 0;
        }
        short val = (short) value;
        for (Voxel3D v : ov.getVoxels()) {
            if (contains(v.getX(), v.getY(), v.getZ())) {
                pixels[v.getRoundZ()][v.getRoundX() + v.getRoundY() * sizeX] = val;
            }
        }
    }

    @Override
    protected void flushPixels() {
        if (pixels != null) {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = null;
            }
            pixels = null;
        }
    }

    @Override
    public boolean isOpened() {
        return !(pixels == null || img == null || img.getProcessor() == null);
    }

    @Override
    public float getPixelInterpolated(Point3D P) {
        return getPixel((float) P.getX(), (float) P.getY(), (float) P.getZ());
    }

    @Override
    public int getPixelIntInterpolated(Point3D P) {
        return (int) getPixel((float) P.getX(), (float) P.getY(), (float) P.getZ());
    }

    @Override
    public ImageHandler deleteSlices(int zmin, int zmax) {
        int z0 = Math.min(zmin, zmax);
        int z1 = Math.max(zmin, zmax);
        int diff = z1 - z0 + 1;
        int newSz = sizeZ - diff;

        ImageShort res = new ImageShort("deleted slices", sizeX, sizeY, newSz);
        // copy before zmin
        for (int z = 0; z < z0; z++) {
            System.arraycopy(pixels[z], 0, res.pixels[z], 0, sizeXY);
        }
        // copy after zmax
        for (int z = z1 + 1; z < sizeZ; z++) {
            System.arraycopy(pixels[z], 0, res.pixels[z - diff], 0, sizeXY);
        }

        return res;
    }

    @Override
    public void trimSlices(int zmin, int zmax) {
        int z0 = Math.max(1, Math.min(zmin, zmax));
        int z1 = Math.min(sizeZ, Math.max(zmin, zmax));
        int newSize = z1 - z0 + 1;
        short[][] newPixels = new short[newSize][];
        for (int i = 0; i < newSize; i++) {
            newPixels[i] = pixels[i + z0 - 1];
        }
        if (this.img != null) {
            ImageStack stack = img.getImageStack();
            for (int i = 1; i < z0; i++) {
                stack.deleteSlice(1);
            }
            for (int i = z1 + 1; i <= sizeZ; i++) {
                stack.deleteLastSlice();
            }
        }
        this.sizeZ = newSize;
        this.sizeXYZ = sizeXY * sizeZ;
        this.offsetZ += z0 - 1;
        this.stats = new HashMap<ImageHandler, ImageStats>(2);
    }

    @Override
    public void intersectMask(ImageInt mask) {
        if (mask==null) return;
        for (int z = 0; z < sizeZ; z++) {
            for (int xy = 0; xy < sizeXY; xy++) {
                if (mask.getPixel(xy, z) == 0) {
                    pixels[z][xy] = 0;
                }
            }
        }
    }

    @Override
    public double getSizeInMb() {
        return (double) (2 * sizeX * sizeY * sizeZ) / (1024 * 1024);
    }

    @Override
    public int getType() {
        return ImagePlus.GRAY16;
    }
    
    @Override 
    public ImageByte toMask() {
        ImageByte res = new ImageByte("mask", this.sizeX, this.sizeY, this.sizeZ);
        res.setScale(this);
        res.setOffset(this);
        for (int z = 0; z<sizeZ; z++) {
            for (int xy = 0; xy<sizeXY; xy++) {
                if (pixels[z][xy]!=0) res.pixels[z][xy]=(byte)255;
            }
        }
        return res;
    }
    
    @Override 
    public int countMaskVolume() {
        int count = 0;
        for (int z = 0; z<sizeZ; z++) {
            for (int xy = 0; xy<sizeXY; xy++) {
                if (pixels[z][xy]!=0) count++;
            }
        }
        return count;
    }
}
