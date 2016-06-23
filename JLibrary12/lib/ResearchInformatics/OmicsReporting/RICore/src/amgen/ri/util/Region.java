package amgen.ri.util;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Description: Region is essentially an integer Pair with line segment class functions added.
 * These functions include UNION, SUBTRACTION, SUBTRACT UNION (XOR),  & INTERSECTION. These functions
 * use the binary CAG (Constructive Area Geometry) operations in the java2D library.</p>
 * @author Jeffrey A. McDowell
 * @version 1.0
 */

public class Region extends Pair implements Comparable {

    /**
     * Creates a Region using the String in the format "start,end"
     */
    public Region(String region) throws NumberFormatException {
        String[] values = region.split("[,;:]", 2);
        setStart(Integer.parseInt(values[0]));
        setEnd(Integer.parseInt(values[1]));
    }

    /**
     * Creates a Region with the given start and end
     * @param start
     * @param end
     */
    public Region(int start, int end) {
        super(start, end);
    }

    /**
     * Returns the start
     * @return
     */
    public int getStart() {
        return super.intName();
    }

    /**
     * Returns the end
     * @return
     */
    public int getEnd() {
        return super.intValue();
    }

    /**
     * Sets the start
     * @param start
     */
    public void setStart(int start) {
        super.setName(start);
    }

    /**
     * Sets the end
     * @param end
     */
    public void setEnd(int end) {
        super.setValue(end);
    }

    /**
     * Overrides the toString to return (start,end)
     * @return
     */
    public String toString() {
        return "(" + getStart() + "," + getEnd() + ")";
    }

    /**
     * Returns whether the given Region intersects this region
     */
    public boolean intersect(Region region) {
        Line2D line1 = new Line2D.Double(getStart(), 0, getEnd(), 0);
        Line2D line2 = new Line2D.Double(region.getStart(), 0, region.getEnd(), 0);
        return line1.intersectsLine(line2);
    }

    /**
     * Returns the Region of intersection of this Region and the given Regions.
     * @param regions Regions to be intersected with this Regions
     * @return the Region of intersection with this Region or null if no intersection is present.
     */
    public Region intersect(Region[] regions) {
        Rectangle2D fullRect = new Rectangle2D.Double(getStart(), 0, getEnd() - getStart(), 1);
        Area fullArea = new Area(fullRect); //Rectangular Area of the full Region
        Area subArea; //Accumulated Rectangular Area of all subtracted Regions
        if (regions.length == 0) {
            return null;
        }
        subArea = new Area(new Rectangle2D.Double(regions[0].getStart(), 0, regions[0].getEnd() - regions[0].getStart(), 1));
        for (int i = 1; i < regions.length; i++) {
            Rectangle2D subRect = new Rectangle2D.Double(regions[i].getStart(), 0, regions[i].getEnd() - regions[i].getStart(), 1);
            subArea.intersect(new Area(subRect));
        }
        fullArea.intersect(subArea);
        Region[] intersectRegions = getRegionsFromArea(fullArea);
        return (intersectRegions.length == 0 ? null : intersectRegions[0]);
    }

    /**
     * Returns the Region of intersection of this Region and the given Regions.
     * @param regions Regions to be intersected with this Regions
     * @return the Region of intersection with this Region or null if no intersection is present.
     */
    public Region[] union(Region[] regions) {
        Rectangle2D fullRect = new Rectangle2D.Double(getStart(), 0, getEnd() - getStart(), 1);
        Area fullArea = new Area(fullRect); //Rectangular Area of the full Region
        if (regions.length == 0) {
            return new Region[] {
                this};
        }
        for (int i = 0; i < regions.length; i++) {
            Rectangle2D subRect = new Rectangle2D.Double(regions[i].getStart(), 0, regions[i].getEnd() - regions[i].getStart(), 1);
            fullArea.add(new Area(subRect));
        }
        return getRegionsFromArea(fullArea);
    }

    /**
     * Subtracts the union of the given Regions from this Region. Returned is an array of Regions representing this
     * difference. i.e. <This Region> - UNION(<Subtract Regions>)
     * @param subtractRegions Regions to be subtracted from this Region
     * @return an array of Regions difference in this Region which do not intersect any subtractRegion.
     */
    public Region[] subtractUnion(Region[] subtractRegions) {
        Rectangle2D fullRect = new Rectangle2D.Double(getStart(), 0, getEnd() - getStart(), 1);
        Area fullArea = new Area(fullRect); //Rectangular Area of the full Region
        Area subArea; //Accumulated Rectangular Area of all subtracted Regions
        if (subtractRegions.length == 0) {
            return new Region[] {
                this};
        }
        subArea = new Area(new Rectangle2D.Double(subtractRegions[0].getStart(), 0, subtractRegions[0].getEnd() - subtractRegions[0].getStart(), 1));
        for (int i = 1; i < subtractRegions.length; i++) {
            Rectangle2D subRect = new Rectangle2D.Double(subtractRegions[i].getStart(), 0, subtractRegions[i].getEnd() - subtractRegions[i].getStart(), 1);
            subArea.add(new Area(subRect));
        }
        fullArea.subtract(subArea);
        Region[] regions = getRegionsFromArea(fullArea);
        if (regions.length > 0 && regions[regions.length - 1].getEnd() == getEnd() + 1) {
            regions[regions.length - 1].setEnd(getEnd());
        }
        Arrays.sort(subtractRegions);
        List finalRegions = new ArrayList();
        for (int i = 0; i < regions.length; i++) {
            if (regions[i].getStart() + 1 == regions[i].getEnd()) {
                boolean isTransition = false;
                outer:for (int j = 0; j < subtractRegions.length; j++) {
                    Region subtractRegion = subtractRegions[j];
                    for (int k = j + 1; k < subtractRegions.length; k++) {
                        Region nextSubtractRegion = subtractRegions[k];
                        if (regions[i].getStart() == subtractRegion.getEnd() &&
                            regions[i].getEnd() == nextSubtractRegion.getStart()) {
                            isTransition = true;
                            break outer;
                        }
                    }
                }
                if (!isTransition) {
                    finalRegions.add(regions[i]);
                }
            } else {
                finalRegions.add(regions[i]);
            }
        }
        return (Region[]) finalRegions.toArray(new Region[0]);

    }

    public boolean contains(double p) {
        if (getStart() > getEnd()) {
            return (getEnd() <= p && p <= getStart());
        }
        return (getStart() <= p && p <= getEnd());
    }

    /**
     * Returns whether the given point, p, is in any of the given Regions
     * @param r
     * @param p
     * @return
     */
    public static boolean contains(Region[] r, double p) {
        for (int i = 0; i < r.length; i++) {
            if (r[i].contains(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to return Regions in the given Area. The Area has presumably been operated on by Area operations such as
     * add, subtract, ect
     * @param area
     * @return
     */
    private Region[] getRegionsFromArea(Area area) {
        PathIterator iterator = area.getPathIterator(null);
        Region currRegion = null;
        ArrayList regions = new ArrayList();
        int state = 0;
        while (!iterator.isDone()) {
            double[] coords = new double[6];
            int type = iterator.currentSegment(coords);
            switch (type) {
                case (PathIterator.SEG_MOVETO): //new segment

                    //System.out.print("SEG_MOVETO");
                    state = 1;
                    currRegion = new Region( -1, -1);
                    break;
                case (PathIterator.SEG_LINETO):

                    //System.out.print("SEG_LINETO");
                    if (state == 1) { //start coordinate
                        currRegion.setStart( (int) coords[0]); //set start
                        state = 2; //switch to end
                    } else if (state == 2) { //end coordinate
                        currRegion.setEnd( (int) coords[0]); //set end
                        state = 0; //switch to end
                    }
                    break;
                case (PathIterator.SEG_CLOSE): //segment complete

                    //System.out.print("SEG_CLOSE");
                    if (currRegion.getEnd() > -1 && currRegion.getStart() > -1) {
                        regions.add(currRegion);
                    } else {
                        System.err.println("WARNING: A region was not set properly");
                    }
                    break;
            }
            //System.out.println(" "+coords[0]+","+coords[1]+","+coords[2]+","+coords[3]+","+coords[4]+","+coords[5]);
            iterator.next();
        }
        return (Region[]) regions.toArray(new Region[0]);
    }

    public int compareTo(Object obj1) {
        int thisStart = getStart();
        int anotherStart = ( (Region) obj1).getStart();
        return (thisStart < anotherStart ? -1 : (thisStart == anotherStart ? 0 : 1));
    }

    public static void main(String[] a) {

        Region parent = new Region(1, 3545);
        Region e1 = new Region(1, 50);
        Region e2 = new Region(51, 128);
        Region e3 = new Region(129, 530);
        Region e4 = new Region(2193, 2255);
        Region e5 = new Region(2711, 3545);

        Region[] r = parent.subtractUnion(new Region[] {e1, e2, e3, e4, e5});
        for (int i = 0; i < r.length; i++) {
            System.out.println(r[i]);
        }

    }

    private static void print(Region r) {
        print(new Region[] {r});
    }

    private static void print(Region[] r) {
        for (int i = 0; i < r.length; i++) {
            System.out.println(r[i]);
        }
    }

}
