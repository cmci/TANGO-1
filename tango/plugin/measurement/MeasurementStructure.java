package tango.plugin.measurement;
import java.util.HashMap;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.SegmentedCellImages;
import tango.dataStructure.StructureQuantifications;
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
public interface MeasurementStructure extends Measurement {
    final static int Number=0;
    final static int ArrayO2O=1;
    final static int ArrayMisc=2;
    public int[] getStructures(); 
    public void getMeasure(InputCellImages rawImages, SegmentedCellImages segmentedImages, StructureQuantifications quantif);

}
