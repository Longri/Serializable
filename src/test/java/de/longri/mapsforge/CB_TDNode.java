/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2015 lincomatic
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.longri.mapsforge;

import java.util.Arrays;

public class CB_TDNode {

    private static final byte ZOOM_HOUSENUMBER = (byte) 18;

    // private static final byte ZOOM_NAME = (byte) 16;


    private final short elevation;
    private final String houseNumber;

    private final long id;
    private final int latitude;
    private final byte layer;
    private final int longitude;
    private final String name;

    private short[] tags;

    /**
     * @param id          the OSM id
     * @param latitude    the latitude
     * @param longitude   the longitude
     * @param elevation   the elevation if existent
     * @param layer       the layer if existent
     * @param houseNumber the house number if existent
     * @param name        the name if existent
     */
    public CB_TDNode(long id, int latitude, int longitude, short elevation, byte layer, String houseNumber, String name) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.houseNumber = houseNumber;
        this.layer = layer;
        this.name = name;
    }

    /**
     * @param id          the OSM id
     * @param latitude    the latitude
     * @param longitude   the longitude
     * @param elevation   the elevation if existent
     * @param layer       the layer if existent
     * @param houseNumber the house number if existent
     * @param name        the name if existent
     * @param tags        the
     */
    public CB_TDNode(long id, int latitude, int longitude, short elevation, byte layer, String houseNumber, String name,
                     short[] tags) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.houseNumber = houseNumber;
        this.layer = layer;
        this.name = name;
        this.tags = tags;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CB_TDNode other = (CB_TDNode) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    /**
     * @return the elevation
     */
    public short getElevation() {
        return this.elevation;
    }

    /**
     * @return the houseNumber
     */
    public String getHouseNumber() {
        return this.houseNumber;
    }

    /**
     * @return the id
     */
    public long getId() {
        return this.id;
    }

    /**
     * @return the latitude
     */
    public int getLatitude() {
        return this.latitude;
    }

    /**
     * @return the layer
     */
    public byte getLayer() {
        return this.layer;
    }

    /**
     * @return the longitude
     */
    public int getLongitude() {
        return this.longitude;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the tags
     */
    public short[] getTags() {
        return this.tags;
    }

//    /**
//     * @return the zoom level on which the node appears first
//     */
//    public byte getZoomAppear() {
//        if (this.tags == null || this.tags.length == 0) {
//            if (this.houseNumber != null) {
//                return ZOOM_HOUSENUMBER;
//            }
//            return Byte.MAX_VALUE;
//        }
//        return CB_OSMTagMapping.getInstance().getZoomAppearPOI(this.tags);
//    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.id ^ (this.id >>> 32));
        return result;
    }

    /**
     * @return true if the node represents a POI
     */
    public boolean isPOI() {
        return this.houseNumber != null || this.elevation != 0 || this.tags.length > 0;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(short[] tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "CB_TDNode [id=" + this.id + ", latitude=" + this.latitude + ", longitude=" + this.longitude + ", name="
                + this.name + ", tags=" + Arrays.toString(this.tags) + "]";
    }
}
