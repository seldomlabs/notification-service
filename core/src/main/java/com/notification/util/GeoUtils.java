package com.notification.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Used for distance calculations For tests, check

 * 
 * @author raunak
 */
public class GeoUtils {

	public static final byte FACE_BITS = 3;
	public static final byte NUM_FACES = 6;

	public static final byte MAX_LEVEL = 30;
	public static final byte POS_BITS = 2 * MAX_LEVEL + 1;
	public static final int MAX_SIZE = 1 << MAX_LEVEL;

	public static final long WRAP_OFFSET = (long) NUM_FACES << POS_BITS;
	public static final long MAX_UNSIGNED = -1L;

	private static final int LOOKUP_BITS = 4;
	private static final int SWAP_MASK = 0x01;
	private static final int INVERT_MASK = 0x02;

	private static final int[] LOOKUP_POS = new int[1 << (2 * LOOKUP_BITS + 2)];
	private static final int[] LOOKUP_IJ = new int[1 << (2 * LOOKUP_BITS + 2)];

	private static final int[] POS_TO_ORIENTATION = { SWAP_MASK, 0, 0, INVERT_MASK + SWAP_MASK };

	private static final int[][] POS_TO_IJ = {
			// 0 1 2 3
			{ 0, 1, 3, 2 }, // canonical order: (0,0), (0,1), (1,1), (1,0)
			{ 0, 2, 3, 1 }, // axes swapped: (0,0), (1,0), (1,1), (0,1)
			{ 3, 2, 0, 1 }, // bits inverted: (1,1), (1,0), (0,0), (0,1)
			{ 3, 1, 0, 2 }, // swapped & inverted: (1,1), (0,1), (0,0), (1,0)
	};

	public static final double EARTH_RADIUS_KM = 6371.0;

	static {
		initLookupCell(0, 0, 0, 0, 0, 0);
		initLookupCell(0, 0, 0, SWAP_MASK, 0, SWAP_MASK);
		initLookupCell(0, 0, 0, INVERT_MASK, 0, INVERT_MASK);
		initLookupCell(0, 0, 0, SWAP_MASK | INVERT_MASK, 0, SWAP_MASK | INVERT_MASK);
	}

	/**
	 * Uses Haversine Formula a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2) c =
	 * 2 ⋅ atan2( √a, √(1−a) )
	 * 
	 * @param lat1
	 *            in degrees
	 * @param lon1
	 *            in degrees
	 * @param lat2
	 *            in degrees
	 * @param lon2
	 *            in degrees
	 * @return distance in radians on unit sphere
	 */
	public static double getUnitDistance(double lat1, double lon1, double lat2, double lon2) {
		double φ1 = Math.toRadians(lat1);
		double φ2 = Math.toRadians(lat2);
		double dφ = Math.toRadians(lat2 - lat1);
		double dλ = Math.toRadians(lon2 - lon1);
		double sindφ2 = Math.sin(dφ / 2);
		double sindλ2 = Math.sin(dλ / 2);
		double a = sindφ2 * sindφ2 + Math.cos(φ1) * Math.cos(φ2) * sindλ2 * sindλ2;
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return c;
	}

	/**
	 * Returns distance which is radius of sphere * angular distance obtained
	 * from @see
	 * {@link GeoUtils#getUnitDistance(double, double, double, double)}
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param radius
	 * @return
	 */
	public static double getSphericalDistance(double lat1, double lon1, double lat2, double lon2, double radius) {
		return radius * getUnitDistance(lat1, lon1, lat2, lon2);
	}

	/**
	 * Returns distance between 2 points on the surface of earth Uses Haversine
	 * Formula Source adapted from :
	 * http://www.movable-type.co.uk/scripts/latlong.html
	 * 
	 * @param lat1
	 *            in degrees
	 * @param lon1
	 *            in degrees
	 * @param lat2
	 *            in degrees
	 * @param lon2
	 *            in degrees
	 * @return distance between the points in kilometers
	 */
	public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
		return getUnitDistance(lat1, lon1, lat2, lon2) * EARTH_RADIUS_KM;
	}

	/**
	 * Checks if
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param r
	 * @return if point2 is within 'r' distance of point1
	 */
	public static boolean isWithinDist(double lat1, double lon1, double lat2, double lon2, double r) {
		return getDistance(lat1, lon1, lat2, lon2) <= r;
	}

	/**
	 * Used to check if any points are in whitelist or no points are in
	 * blacklist
	 * 
	 * @param lat
	 * @param lon
	 * @param points
	 *            - List of 3-length double arrays containing lat,lon,dist
	 * @param blacklist
	 * @return
	 */
	public static boolean checkMultiplePoints(double lat, double lon, ArrayList<Double[]> points, boolean blacklist) {
		if (points == null || points.isEmpty())
			return true;
		boolean res = points.stream().filter(p -> isWithinDist(lat, lon, p[0], p[1], p[2])).findFirst().isPresent();
		return blacklist ^ res;
	}

	/**
	 * Used to check if any points are in whitelist or no points are in
	 * blacklist
	 * 
	 * @param lat
	 * @param lon
	 * @param points
	 *            - List of 2-length double arrays containing lat,lon
	 * @param blacklist
	 * @return
	 */
	public static boolean checkMultiplePointsFromFixedRadius(double lat, double lon, double dist,
			ArrayList<Double[]> points, boolean blacklist) {
		if (points == null || points.isEmpty())
			return true;
		boolean res = points.stream().filter(p -> isWithinDist(lat, lon, p[0], p[1], dist)).findFirst().isPresent();
		return blacklist ^ res;
	}

	/**
	 * Builds ArrayList of lat,lon,dist (all Double) from raw string
	 * 
	 * @param s
	 * @return
	 */
	public static ArrayList<Double[]> buildPointList(String s) {
		if (StringUtils.isBlank(s))
			return null;
		ArrayList<Double[]> pList = new ArrayList<>();
		String[] arr = s.split(",");
		if (arr.length % 3 == 0) {
			for (int i = 0; i < arr.length; i += 3) {
				Double[] darr = new Double[3];
				darr[0] = Double.parseDouble(arr[i]);
				darr[1] = Double.parseDouble(arr[i + 1]);
				darr[2] = Double.parseDouble(arr[i + 2]);
				pList.add(darr);
			}
		}
		return pList;
	}

	// FIXME Implement method
	public static int generateGeoHash(double lat, double lon) {
		return 0;
	}

	public static long convertToS2CellId(double lat, double lon) {
		// 1. Convert to radians
		double phi = Math.toRadians(lat);
		double theta = Math.toRadians(lon);
		double cosphi = Math.cos(phi);
		// 2. Convert to x,y,z (point)
		double x = Math.cos(theta) * cosphi;
		double y = Math.sin(theta) * cosphi;
		double z = Math.sin(phi);
		// 3. Get face of cube
		int face = largestAbsComponent(x, y, z);
		// 4. Use face,x,y,z to ocnvert to u,v
		double u, v;
		switch (face) {
		case 0:
			u = y / x;
			v = z / x;
			break;
		case 1:
			u = -x / y;
			v = z / y;
			break;
		case 2:
			u = -x / z;
			v = -y / z;
			break;
		// Should not reach here
		case 3:
			u = z / x;
			v = y / x;
			break;
		case 4:
			u = z / y;
			v = -x / y;
			break;
		default:
			u = -y / z;
			v = -x / z;
		}
		// 5. u,v to s,t to i,j (ints)
		int i = fastIntRound(uvToST(u));
		int j = fastIntRound(uvToST(v));
		// 6. Finally, i,j to cell_id
		return fromFaceIJ(face, i, j);
	}

	public static String getS2CellToken(long cellId) {
		if (cellId == 0)
			return "X";
		String hex = Long.toHexString(cellId).toLowerCase(Locale.ENGLISH);
		StringBuilder sb = new StringBuilder(16);
		for (int i = hex.length(); i < 16; i++) {
			sb.append('0');
		}
		sb.append(hex);
		for (int len = 16; len > 0; len--) {
			if (sb.charAt(len - 1) != '0') {
				return sb.substring(0, len);
			}
		}
		throw new RuntimeException("Shouldn't make it here");
	}

	/**
	 * Returns index of largest absolute component, used in getting face of the
	 * cube 0 if abs(x) max, 1 for abs(y), 2 for abs(z)
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static int largestAbsComponent(double x, double y, double z) {
		double ax = Math.abs(x);
		double ay = Math.abs(y);
		double az = Math.abs(z);
		return (ax > ay) ? ((ax > az) ? 0 : 2) : ((ay > az) ? 1 : 2);
	}

	/**
	 * Transforms UV-face coordinates to ST Projection chosen is quadratic
	 * 
	 * @param u
	 * @return
	 */
	public static double uvToST(double u) {
		return (u >= 0) ? Math.sqrt(1 + 3 * u) - 1 : 1 - Math.sqrt(1 - 3 * u);
	}

	/**
	 * From :
	 * https://github.com/google/s2-geometry-library-java/blob/master/src/com/google/common/geometry/S2CellId.java
	 * Return the i- or j-index of the leaf cell containing the given s- or
	 * t-value.
	 */
	public static int fastIntRound(double s) {
		// Converting from float to integers via static_cast is very slow
		// on Intel processors because it requires changing the rounding mode.
		// Rounding to the nearest integer using FastIntRound() is much faster.

		final int m = MAX_SIZE / 2; // scaling multiplier
		return (int) Math.max(0, Math.min(2 * m - 1, Math.round(m * s + (m - 0.5))));
	}

	/**
	 * From :
	 * https://github.com/google/s2-geometry-library-java/blob/master/src/com/google/common/geometry/S2CellId.java
	 * 
	 * @param face
	 * @param i
	 * @param j
	 * @return
	 */
	public static long fromFaceIJ(int face, int i, int j) {
		// Optimization notes:
		// - Non-overlapping bit fields can be combined with either "+" or "|".
		// Generally "+" seems to produce better code, but not always.

		// gcc doesn't have very good code generation for 64-bit operations.
		// We optimize this by computing the result as two 32-bit integers
		// and combining them at the end. Declaring the result as an array
		// rather than local variables helps the compiler to do a better job
		// of register allocation as well. Note that the two 32-bits halves
		// get shifted one bit to the left when they are combined.
		long n[] = { 0, face << (POS_BITS - 33) };

		// Alternating faces have opposite Hilbert curve orientations; this
		// is necessary in order for all faces to have a right-handed
		// coordinate system.
		int bits = (face & SWAP_MASK);

		// Each iteration maps 4 bits of "i" and "j" into 8 bits of the Hilbert
		// curve position. The lookup table transforms a 10-bit key of the form
		// "iiiijjjjoo" to a 10-bit value of the form "ppppppppoo", where the
		// letters [ijpo] denote bits of "i", "j", Hilbert curve position, and
		// Hilbert curve orientation respectively.

		for (int k = 7; k >= 0; --k) {
			bits = getBits(n, i, j, k, bits);
		}

		return (((n[1] << 32) + n[0]) << 1) + 1;
	}

	private static int getBits(long[] n, int i, int j, int k, int bits) {
		final int mask = (1 << LOOKUP_BITS) - 1;
		bits += (((i >> (k * LOOKUP_BITS)) & mask) << (LOOKUP_BITS + 2));
		bits += (((j >> (k * LOOKUP_BITS)) & mask) << 2);
		bits = LOOKUP_POS[bits];
		n[k >> 2] |= ((((long) bits) >> 2) << ((k & 3) * 2 * LOOKUP_BITS));
		bits &= (SWAP_MASK | INVERT_MASK);
		return bits;
	}

	private static void initLookupCell(int level, int i, int j, int origOrientation, int pos, int orientation) {
		if (level == LOOKUP_BITS) {
			int ij = (i << LOOKUP_BITS) + j;
			LOOKUP_POS[(ij << 2) + origOrientation] = (pos << 2) + orientation;
			LOOKUP_IJ[(pos << 2) + origOrientation] = (ij << 2) + orientation;
		} else {
			level++;
			i <<= 1;
			j <<= 1;
			pos <<= 2;
			// Initialize each sub-cell recursively.
			for (int subPos = 0; subPos < 4; subPos++) {
				int ij = POS_TO_IJ[orientation][subPos];
				int orientationMask = POS_TO_ORIENTATION[subPos];
				initLookupCell(level, i + (ij >>> 1), j + (ij & 1), origOrientation, pos + subPos,
						orientation ^ orientationMask);
			}
		}
	}

	/**
	 * Returns x,y,z spherical coordinates in km assuming earth is a sphere (no
	 * it is not!)
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public double[] latLonToXyz(double lat, double lon) {
		double latRad = Math.toRadians(lat);
		double lonRad = Math.toRadians(lon);
		double x = EARTH_RADIUS_KM * Math.sin(latRad) * Math.cos(lonRad);
		double y = EARTH_RADIUS_KM * Math.sin(latRad) * Math.sin(lonRad);
		double z = EARTH_RADIUS_KM * Math.cos(latRad);
		return new double[] { x, y, z };
	}

	public double[] xyzToLatLon(double x, double y, double z) {
		return null;
	}

	public double[] meanLatLon(Iterable<Double[]> points) {
		return null;
	}

	public static String getValidatedLatlon(String latlon) {
		if (!StringUtils.isBlank(latlon)) {
			String[] latlonArray = latlon.split(",");
			if (latlonArray != null && latlonArray.length == 2 && latlonArray[0].matches("[0-9]+\\.?[0-9]*")
					&& latlonArray[1].matches("[0-9]+\\.?[0-9]*")) {
				return latlon;
			}
		}
		// Default fallback lat-lon (set as Connaught Place)
		return "28.6315,77.2167";
	}
	
	public static void test() {
		String ticketBody = "%s";
		
		List<String> ticketBodyList=Arrays.asList("abc\\$");
		
		for (String ticketBodyElement : ticketBodyList) {
		    ticketBody = ticketBody.replaceFirst("%s", ticketBodyElement);
		}
		
		System.out.println(ticketBody);
	}

	public static void main(String[] args) {
		test();
		//System.out.println(getDistance(28.4721314, 77.0717361 ,28.4723392, 77.0711917));
		// System.out.println(getDistance(28.47426, 77.177903, 28.777, 77.777));
		// System.out.println(getDistance(28.47426, 77.177903, 28.432, 77.107));
		// System.out.println(isWithinDist(28.473, 77.106, 28.432, 77.112,
		// 20.0));
		// System.out.println(isWithinDist(28.473, 77.106, 28.4725375,
		// 77.1045882, 0.15));
		// ArrayList<Double[]> pList = new ArrayList<>();
		// pList.add(new Double[] { 28.432, 77.112, 20.0 });
		// pList.add(new Double[] { 28.412, 77.212, 20.0 });
		// System.out.println(checkMultiplePoints(28.473, 77.106, pList,
		// false));
		// System.out.println(convertToS2CellId(28, 77));
		// System.out.println(getS2CellToken(convertToS2CellId(28, 77.5)));
		// System.out.println("28,77 gives: " + getValidatedLatlon("28,77"));
		// System.out.println(",77 gives: " + getValidatedLatlon(",77"));
		// System.out.println("28, gives: " + getValidatedLatlon("28,"));
		// System.out.println(", gives: " + getValidatedLatlon(","));
		// System.out.println("28.45,77 gives: " +
		// getValidatedLatlon("28.45,77"));
		// System.out.println("28,77.45 gives: " +
		// getValidatedLatlon("28,77.45"));
		// System.out.println("28aa,a77 gives: " +
		// getValidatedLatlon("28aa,a77"));
		// System.out.println(" gives: " +getValidatedLatlon(""));
		// System.out.println("null gives: " + getValidatedLatlon(null));

	}
}
