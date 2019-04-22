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
 *     <br>RArad (deg)   (F12.8) Right Ascension in ICRS, Ep=1991.25 [ucd=pos.eq.ra;meta.main]
 *     <br>DErad (deg)   (F12.8) Declination in ICRS, Ep=1991.25 [ucd=pos.eq.dec;meta.main]
 *     <br>pmRA (mas/yr) (F8.2)  Proper motion in Right Ascension [ucd=pos.pm;pos.eq.ra]
 *     <br>pmDE (mas/yr) (F8.2)  Proper motion in Declination [ucd=pos.pm;pos.eq.dec]
 *     <br>Hpmag (mag)   (F7.4)  Hipparcos magnitude [ucd=phot.mag;em.opt.V]
 *     <br>B-V (mag)     (F6.3)  Colour index [ucd=phot.color;em.opt.B;em.opt.V]
 *<br>
 *     <br>
 *     See <a href="http://vizier.u-strasbg.fr/vizier/doc/vizquery.htx">http://vizier.u-strasbg.fr/vizier/doc/vizquery.htx</a>
 *     for details about query
 */
public class VizierQuery {

    public static final String DEFAULT_CATALOGUE = "I/311/hip2";
    public static final String DEFAULT_OUT = "-out=RArad&-out=DErad&-out=pmRA&-out=pmDE&-out=Hpmag&-out=B-V";
    public static final String ALL_OUT = "-out.all";



    String catalogue = DEFAULT_CATALOGUE;
    String outRequest = DEFAULT_OUT;

    List<String> filters = new ArrayList<>();

    /**
     * Return the VizieR string from the last read
     * @return VizieR text from last read
     */
    public String getLastVizierDataRead() {
        return lastVizierDataRead;
    }

    String lastVizierDataRead = "";

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
            System.out.println("Unable to connect to VizieR");
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

        lastVizierDataRead = readVizier(url_query);
        return lastVizierDataRead;
    }

    public static void main(String[] args) {
        VizierQuery query = new VizierQuery();
        //String str = readVizier(query.readVizierCentre("acrux", 3));
        //System.out.println(str);
        String str = readVizier(query.readVizierCentre("186.649563 -63.0", 10));
        System.out.println(str);

    }

    /**
     * Add a filter to Vizier
     * @param filterName the name of the parameter we are filtering. EG Hpmag
     * @param operand must be &lt; or &gt;
     * @param filter_param the value we are adding to filter
     */
    public void addFilter(String filterName, String operand, float filter_param) {
        addFilter(filterName + "=" + operand + filterName);
    }

    /**
     * erase this filter parameter from our VizieR queries
     * @param filterName name of filter
     */
    public void eraseFilter(String filterName) {
        // go in backwards order so we can just remove

        for (int i = filters.size() -1; i >= 0; i--){
            String filter =  filters.get(i);
            if (filter.startsWith(filterName)){
                filters.remove(i);
            }
        }
    }

    /**
     * Erase all filters to Vizier
     */
    public void clearFilters() {
        filters.clear();
    }
}
