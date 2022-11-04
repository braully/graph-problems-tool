package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author braully
 */
public class UtilParse {

    public static List<String> parseList(String str, String delimit) {
        String[] excludes = new String[]{str};
        if (str.contains(delimit)) {
            excludes = str.split(delimit);
        }
        return Arrays.asList(excludes);
    }

    public static List<Integer> parseAsIntList(String str, String delimit) {

        List<Integer> rlist = null;
        if (str != null && delimit != null) {
            rlist = new ArrayList<>();
            if (str.contains(delimit)) {
                String[] excludes = str.split(delimit);
                for (String s : excludes) {
                    rlist.add(Integer.parseInt(s.trim()));
                }
            }
        }
        return rlist;
    }
}
