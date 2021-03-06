package tango.dataStructure;

import com.mongodb.BasicDBObject;
import ij.IJ;
import ij.measure.Calibration;
import java.util.ArrayList;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.exceptionPrinter;
import tango.gui.Core;
import tango.gui.parameterPanel.VirtualStructurePanel;
import tango.gui.util.ColocFactory;
import tango.parameter.BooleanParameter;
import tango.parameter.Parameter;
import tango.parameter.StructureParameter;
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
 */

public abstract class VirtualStructure extends Structure {
    public final static String[] methodsCurrent = new String[]{"Constant Object Number", "Cluster"}; //"Post-Processing Chain"
    public final static String[] methodsTesting = new String[]{"Constant Object Number", "Cluster", "Object Colocalization"}; //"Post-Processing Chain", 
    public static String[] methods = (Core.TESTING) ? methodsTesting:methodsCurrent;
    public VirtualStructure(String title, int idx, Cell cell) {
        super(title, idx, cell);
    }
    
    @Override
    public abstract void process();
    
    protected static VirtualStructurePanel getPanel(Cell cell, int idx) {
        BasicDBObject settings = cell.xp.getChannelSettings(idx);
        VirtualStructurePanel vsp = new VirtualStructurePanel();
        vsp.setData(settings);
        vsp.initPanel();
        return vsp;
    }
    
    public static VirtualStructure createStructure(String title, int idx, Cell cell) {
        VirtualStructurePanel vsp = getPanel(cell, idx);
        if ("Constant Object Number".equals(vsp.type.getSelectedItem())) {
            return new VirtualStructureObjectNumber(title, idx, cell);
        } else if ("Object Colocalization".equals(vsp.type.getSelectedItem())) { // coloc
            return new VirtualStructureColoc(title, idx, cell);
        } else if ("Cluster".equals(vsp.type.getSelectedItem())) {
            return new VirtualStructureCluster(title, idx, cell);
        } //else if ("Post-Processing Chain".equals(vsp.type.getSelectedItem())) {
        else return null;
    }
    
}
