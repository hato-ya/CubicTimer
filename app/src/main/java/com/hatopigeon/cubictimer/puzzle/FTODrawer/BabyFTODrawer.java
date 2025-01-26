// Baby FTO Drawer is based on FTO Drawer

// FTO Drawer is from https://github.com/Sixstringcal/FTODrawer
// The code doesn't have any specific license, but permission has been granted for its use.
// https://github.com/Sixstringcal/FTODrawer/issues/3

package com.hatopigeon.cubictimer.puzzle.FTODrawer;

public class BabyFTODrawer {
    public static String getSVG(int[] state, String[] colors) {
        StringBuilder returnedString = new StringBuilder();
        returnedString.append(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"\n" +
                "     viewBox=\"0 0 46 24\" style=\"enable-background:new 0 0 46 24;\" xml:space=\"preserve\">\n");

        String[] pointsArray = {
                "2,2 12,2 7,7",
                "12,2 7,7 17,7",
                "12,2 22,2 17,7",
                "7,7 17,7 12,12",
                "2,2 7,7 2,12",
                "7,7 2,12 7,17",
                "7,7 12,12 7,17",
                "2,12 7,17 2,22",
                "12,12 7,17 17,17",
                "7,17 2,22 12,22",
                "7,17 17,17 12,22",
                "17,17 12,22 22,22",
                "17,7 12,12 17,17",
                "17,7 22,12 17,17",
                "22,2 17,7 22,12",
                "22,12 17,17 22,22",
                "24,2 29,7 24,12",
                "24,12 29,17 24,22",
                "29,7 24,12 29,17",
                "29,7 34,12 29,17",
                "24,2 34,2 29,7",
                "34,2 29,7 39,7",
                "34,2 44,2 39,7",
                "29,7 39,7 34,12",
                "44,2 39,7 44,12",
                "39,7 34,12 39,17",
                "39,7 44,12 39,17",
                "44,12 39,17 44,22",
                "39,17 34,22 44,22",
                "29,17 39,17 34,22",
                "29,17 24,22 34,22",
                "34,12 29,17 39,17"
        };

        for (int i = 0; i < pointsArray.length; i++) {
            returnedString.append("<polygon id=\"")
                    .append(i)
                    .append("\" points=\"")
                    .append(pointsArray[i])
                    .append("\"\n\tstyle=\"fill:")
                    .append(colors[state[i]])
                    .append(";stroke:black;stroke-width:.3\"/>\n");
        }

        returnedString.append("</svg>");

        return returnedString.toString();
    }
}
