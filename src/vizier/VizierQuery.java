package vizier;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for querying VizieR catalogue of astronomical catalogues
 *
 * Sample query
 * Default catalogue used is I/311/hip2 - Hipparcos, the New Reduction (van Leeuwen, 2007)
 * Default outputs are as follows
 *     DErad (deg)   (F12.8) Declination in ICRS, Ep=1991.25 [ucd=pos.eq.dec;meta.main]
 *     pmRA (mas/yr) (F8.2)  Proper motion in Right Ascension [ucd=pos.pm;pos.eq.ra]
 *     pmDE (mas/yr) (F8.2)  Proper motion in Declination [ucd=pos.pm;pos.eq.dec]
 *     Hpmag (mag)   (F7.4)  Hipparcos magnitude [ucd=phot.mag;em.opt.V]
 *     B-V (mag)     (F6.3)  Colour index [ucd=phot.color;em.opt.B;em.opt.V]
 *
 *     See the following URL for details about query
 *     http://vizier.u-strasbg.fr/vizier/doc/vizquery.htx
 */
public class VizierQuery {

    public static final String DEFAULT_CATALOGUE = "I/311/hip2";
    public static final String DEFAULT_OUT = "-out=DErad&-out=pmRA&-out=pmDE&-out=Hpmag&-out=B-V";
    public static final String ALL_OUT = "-out.all";



    String catalogue = DEFAULT_CATALOGUE;
    String outRequest = DEFAULT_OUT;

    List<String> filters = new ArrayList<>();

    /**
     * Read Vizier from URL
     * @param path The URL we are reading
     * @return a String with data
     */
    static String readVizier(String path){
       String ret = "";
        try {
            URL url = new URL(path);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            ret = sb.toString();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return ret;
    }


    /**
     * Add filter to query
     * @param filter filter to add
     */
    public void addFilter(String filter){
        filters.add(filter);
    }
    /**
     * Read From VizieR using the centre and radius. Catalogue and outputs will have already been set
     * @param centre the centre point we are reading. This can be an object name or Ra/Dec
     * @param radius the radius as a circle in degrees
     * @return the text for the query
     */
    public String readVizierCentre(String centre, double radius){
        try {
            centre = URLEncoder.encode(centre,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url_query = "http://vizier.u-strasbg.fr/viz-bin/asu-txt?-source=" + catalogue + "&-c=" + centre + ",rd=" + radius + ",eq=J2000&" + outRequest;

        for (String filter:
                filters) {
            url_query = url_query + "&" + filter;
        }

        return readVizier(url_query);
    }

    public static void main(String[] args) {
        VizierQuery query = new VizierQuery();
        //String str = readVizier(query.readVizierCentre("acrux", 3));
        //System.out.println(str);
        String str = readVizier(query.readVizierCentre("186.649563 -63.0", 10));
        System.out.println(str);

    }
}
