package tisseo;

/*
 * Classe utilitaire pour le calcul de distance entre deux coordonnées GPS
 */
public class CalculPosition {

	/**
     * Distance entre 2 points GPS
     *  http://dotclear.placeoweb.com/post/Formule-de-calcul-entre-2-points-wgs84-pour-calculer-la-distance-qui-separe-ces-deux-points
     *
     * La distance mesurée le long d'un arc de grand cercle entre deux points dont on connaît les coordonnées {lat1,lon1} et {lat2,lon2} est donnée par :
     * d = acos(sin(lat1)*sin(lat2)+cos(lat1)*cos(lat2)*cos(lon1-lon2))
     * Le tout * 6366 pour l'avoir en km
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     
    public static double distanceVolOiseauEntre2PointsSansPrécision(double lon1, double lat1, double lon2, double lat2) {
    	System.out.println(" distance" + lat1+ " "+ lon1 + " "+ lat2 + " "+ lon2);
        return
        Math.acos(
            Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1-lon2)
        );
    }*/
    static final double _eQuatorialEarthRadius = 6378.1370D;
    static final double _d2r = (Math.PI / 180D);

    public static int HaversineInM(double long1, double lat1, double long2, double lat2) {
        return (int) (1000D * HaversineInKM(long1, lat1, long2, lat2));
    }

    public static double HaversineInKM(double long1, double lat1, double long2, double lat2) {
        double dlong = (long2 - long1) * _d2r;
        double dlat = (lat2 - lat1) * _d2r;
        double a = Math.pow(Math.sin(dlat / 2D), 2D) + Math.cos(lat1 * _d2r) * Math.cos(lat2 * _d2r)
                * Math.pow(Math.sin(dlong / 2D), 2D);
        double c = 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
        double d = _eQuatorialEarthRadius * c;

        return d/1000;
    }
}
