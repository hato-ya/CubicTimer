// This code is from https://github.com/Sixstringcal/FTODrawer
// The code doesn't have any specific license, but permission has been granted for its use.
// https://github.com/Sixstringcal/FTODrawer/issues/3

package com.hatopigeon.cubictimer.puzzle.FTODrawer;

/*
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
*/

public class FTOMain {
/*
    public static String[] colors = new String[]{
//            "white", "purple", "red", "green", "pink", "blue", "orange", "black"
            "#FFFFFF", "#8A1AFF", "#02D040", "#EC0000", "#999999", "#304FFE", "#FF8B24", "#FDD835"
    };
*/

/*
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(new File("colors.txt"));
            int i = 0;
            while(scanner.hasNextLine()){
                colors[i] = scanner.nextLine();
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        MainMenu mainMenu = new MainMenu();

    }
*/

    public static String getSVG(int[] state, String[] colors) {
        StringBuilder returnedString = new StringBuilder();
        returnedString.append(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"\n" +
                "     viewBox=\"17 7.6 79 39.5\" style=\"enable-background:new 17 7.6 79 39.5;\" xml:space=\"preserve\">\n");

        String[] pointsArray = {
                "19,9.6 30.8,9.6 24.9,15.5",
                "30.8,9.6 24.9,15.5 36.8,15.5",
                "30.8,9.6 36.8,15.5 42.7,9.6",
                "36.8,15.5 42.7,9.6 48.6,15.5",
                "42.7,9.6 48.6,15.5 54.5,9.6",
                "24.9,15.5 30.8,21.4 36.8,15.5",
                "30.8,21.4 36.8,15.5 42.7,21.4",
                "36.8,15.5 42.7,21.4 48.6,15.5",
                "30.8,21.4 36.8,27.3 42.7,21.4",
                "19,21.4 24.9,15.5 19,9.6",
                "24.9,27.3 19,21.4 24.9,15.5",
                "24.9,27.3 30.8,21.4 24.9,15.5",
                "30.8,33.3 24.9,27.3 30.8,21.4",
                "30.8,33.3 36.8,27.3 30.8,21.4",
                "19,33.3 24.9,27.3 19,21.4",
                "19,33.3 24.9,39.2 24.9,27.3",
                "24.9,39.2 30.8,33.3 24.9,27.3",
                "19,45.1 19,33.3 24.9,39.2",
                "42.7,33.3 36.8,27.3 30.8,33.3",
                "36.8,39.2 30.8,33.3 24.9,39.2",
                "42.7,33.3 36.8,39.2 30.8,33.3",
                "48.6,39.2 42.7,33.3 36.8,39.2",
                "30.8,45.1 24.9,39.2 19,45.1",
                "36.8,39.2 30.8,45.1 24.9,39.2",
                "42.7,45.1 36.8,39.2 30.8,45.1",
                "42.7,45.1 48.6,39.2 36.8,39.2",
                "54.5,45.1 42.7,45.1 48.6,39.2",
                "42.7,21.4 36.8,27.3 42.7,33.3",
                "42.7,21.4 48.6,27.3 42.7,33.3",
                "48.6,15.5 42.7,21.4 48.6,27.3",
                "54.5,21.4 48.6,15.5 48.6,27.3",
                "54.5,9.6 54.5,21.4 48.6,15.5",
                "48.6,27.3 42.7,33.3 48.6,39.2",
                "48.6,27.3 54.5,33.3 48.6,39.2",
                "54.5,21.4 48.6,27.3 54.5,33.3",
                "54.5,33.3 48.6,39.2 54.5,45.1",
                "58.5,21.4 64.4,15.5 58.5,9.6",
                "58.5,33.3 64.4,27.3 58.5,21.4",
                "64.4,27.3 58.5,21.4 64.4,15.5",
                "64.4,27.3 70.3,21.4 64.4,15.5",
                "58.5,45.1 58.5,33.3 64.4,39.2",
                "58.5,33.3 64.4,39.2 64.4,27.3",
                "64.4,39.2 70.3,33.3 64.4,27.3",
                "70.3,33.3 64.4,27.3 70.3,21.4",
                "70.3,33.3 76.3,27.3 70.3,21.4",
                "58.5,9.6 70.3,9.6 64.4,15.5",
                "70.3,9.6 64.4,15.5 76.3,15.5",
                "70.3,9.6 76.3,15.5 82.2,9.6",
                "76.3,15.5 82.2,9.6 88.1,15.5",
                "82.2,9.6 88.1,15.5 94,9.6",
                "64.4,15.5 70.3,21.4 76.3,15.5",
                "70.3,21.4 76.3,15.5 82.2,21.4",
                "76.3,15.5 82.2,21.4 88.1,15.5",
                "70.3,21.4 76.3,27.3 82.2,21.4",
                "94,9.6 94,21.4 88.1,15.5",
                "88.1,15.5 82.2,21.4 88.1,27.3",
                "94,21.4 88.1,15.5 88.1,27.3",
                "94,21.4 88.1,27.3 94,33.3",
                "82.2,21.4 76.3,27.3 82.2,33.3",
                "82.2,21.4 88.1,27.3 82.2,33.3",
                "88.1,27.3 82.2,33.3 88.1,39.2",
                "88.1,27.3 94,33.3 88.1,39.2",
                "94,33.3 88.1,39.2 94,45.1",
                "94,45.1 82.2,45.1 88.1,39.2",
                "82.2,45.1 88.1,39.2 76.3,39.2",
                "82.2,45.1 76.3,39.2 70.3,45.1",
                "76.3,39.2 70.3,45.1 64.4,39.2",
                "70.3,45.1 64.4,39.2 58.5,45.1",
                "88.1,39.2 82.2,33.3 76.3,39.2",
                "82.2,33.3 76.3,39.2 70.3,33.3",
                "76.3,39.2 70.3,33.3 64.4,39.2",
                "82.2,33.3 76.3,27.3 70.3,33.3",
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

/*
    public static String getSVG(int[] state) {
        String returnedString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"\n" +
                "\t viewBox=\"0 0 98 98\" style=\"enable-background:new 0 0 98 98;\" xml:space=\"preserve\">\n" +
                "<style type=\"text/css\">\n" +
                "\t.st0{fill:#FFFFFF;stroke:#808080;stroke-width:5;}\n" +
                "\t.st1{fill:#FFFFFF;stroke:#808080;stroke-width:0.1;}\n" +
                "\t.st2{fill:#FFFFFF;stroke:#808080;}\n" +
                "</style>\n" +
                "<polygon class=\"st0\" points=\"19.4,26.1 79.4,26.1 49.4,78\"/>\n" +
                "<polygon id=\"0\" points=\"19.4,26.1 39.4,26.1 29.4,43.4\"\n" +
                "\tstyle=\"fill:" + colors[state[0]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"1\" points=\"39.4,26.1 29.4,43.4 49.4,43.4\"\n" +
                "\tstyle=\"fill:" + colors[state[1]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"2\" points=\"39.4,26.1 49.4,43.4 59.4,26.1\"\n" +
                "\tstyle=\"fill:" + colors[state[2]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"3\" points=\"49.4,43.4 59.4,26.1 69.4,43.4\"\n" +
                "\tstyle=\"fill:" + colors[state[3]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"4\" points=\"59.4,26.1 69.4,43.4 79.4,26.1\"\n" +
                "\tstyle=\"fill:" + colors[state[4]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"5\" points=\"29.4,43.4 39.4,60.7 49.4,43.4\"\n" +
                "\tstyle=\"fill:" + colors[state[5]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"6\" points=\"39.4,60.7 49.4,43.4 59.4,60.7\"\n" +
                "\tstyle=\"fill:" + colors[state[6]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"7\" points=\"49.4,43.4 59.4,60.7 69.4,43.4\"\n" +
                "\tstyle=\"fill:" + colors[state[7]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"8\" points=\"39.4,60.7 49.4,78 59.4,60.7\"\n" +
                "\tstyle=\"fill:" + colors[state[8]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "\n" +
                "<polygon id=\"9\" points=\"20.7,48.4 16.4,50.9 6.4,33.6 10.7,31.1\"\n" +
                "\tstyle=\"fill:" + colors[state[9]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"11\" points=\"30.7,65.7 26.4,68.2 16.4,50.9 20.7,48.4\"\n" +
                "\tstyle=\"fill:" + colors[state[11]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"13\" points=\"40.7,83 36.4,85.5 26.4,68.2 30.7,65.7\"\n" +
                "\tstyle=\"fill:" + colors[state[13]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "\n" +
                "\n" +
                "<polygon points=\"49.4 85 46.9 89.3301 49.4 93.6602 51.9 89.3301\"\n" +
                "\tstyle=\"fill:white;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"18\" points=\"49.4 85 46.9 89.3301 51.9 89.3301\"\n" +
                "\tstyle=\"fill:" + colors[state[18]] + ";stroke:gray;stroke-width:.5\"/>\n" +
                "<polygon id=\"20\" points=\"46.9 89.3301 51.9 89.3301 49.4 93.6602\"\n" +
                "\tstyle=\"fill:" + colors[state[20]] + ";stroke:gray;stroke-width:.5\"/>\n" +
                "\n" +
                "<polygon id=\"27\" points=\"67.8,65.6 72.2,68.1 62.2,85.4 57.8,82.9\"\n" +
                "\tstyle=\"fill:" + colors[state[27]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"29\" points=\"77.8,48.3 82.2,50.8 72.2,68.1 67.8,65.6\"\n" +
                "\tstyle=\"fill:" + colors[state[29]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"31\" points=\"87.8,31 92.2,33.5 82.2,50.8 77.8,48.3\"\n" +
                "\tstyle=\"fill:" + colors[state[31]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "\n" +
                "<polygon points=\"85.4 22.6478 90.4 22.6478 92.9 18.3177 87.9 18.3177\"\n" +
                "\tstyle=\"fill:white;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"36\" points=\"85.4 22.6478 90.4 22.6478 87.9 18.3177\"\n" +
                "\tstyle=\"fill:" + colors[state[36]] + ";stroke:gray;stroke-width:.5\"/>\n" +
                "<polygon id=\"38\" points=\"90.4 22.6478 87.9 18.3177 92.9 18.3177\"\n" +
                "\tstyle=\"fill:" + colors[state[38]] + ";stroke:gray;stroke-width:.5\"/>\n" +
                "\n" +
                "<polygon id=\"45\" points=\"59.4,16.1 59.4,11.1 79.4,11.1 79.4,16.1\"\n" +
                "\tstyle=\"fill:" + colors[state[45]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"47\" points=\"39.4,16.1 39.4,11.1 59.4,11.1 59.4,16.1\"\n" +
                "\tstyle=\"fill:" + colors[state[47]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"49\" points=\"19.4,16.1 19.4,11.1 39.4,11.1 39.4,16.1\"\n" +
                "\tstyle=\"fill:" + colors[state[49]] + ";stroke:gray;stroke-width:1\"/>\n" +
                "\n" +
                "<polygon points=\"13.4 22.6478 10.9 18.3177 5.9 18.3177 8.4 22.6478\"\n" +
                "\tstyle=\"fill:white;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon id=\"54\" points=\"13.4 22.6478 10.9 18.3177 8.4 22.6478\"\n" +
                "\tstyle=\"fill:" + colors[state[54]] + ";stroke:gray;stroke-width:.5\"/>\n" +
                "<polygon id=\"56\" points=\"10.9 18.3177 8.4 22.6478 5.9 18.3177\"\n" +
                "\tstyle=\"fill:" + colors[state[56]] + ";stroke:gray;stroke-width:.5\"/>\n" +
                "\n" +
                "</svg>\n";
        return returnedString;
    }

    public static String drawScramble(int state[]){
        String returnedString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"\n" +
                "\t viewBox=\"0 0 98 98\" style=\"enable-background:new 0 0 98 98;\" xml:space=\"preserve\">\n" +
                "<style type=\"text/css\">\n" +
                "\t.st0{fill:#FFFFFF;stroke:#808080;stroke-width:5;}\n" +
                "\t.st1{fill:#FFFFFF;stroke:#808080;stroke-width:0.1;}\n" +
                "</style>\n" +
                "<polygon\n" +
                "\tid=\"outer_square1\"\n" +
                "\tpoints=\"19,9.6 19,45.1 54.5,45.1 54.5,9.6\"\n" +
                "\tstyle=\"fill:white;stroke:gray;stroke-width:2.5\"/>\n" +
                "<polygon\n" +
                "\tid=\"outer_square2\"\n" +
                "\tpoints=\"19,51.7 54.5,51.7 54.5,87.2 19,87.2\"\n" +
                "\tstyle=\"fill:white;stroke:gray;stroke-width:2.5\"/>\n" +
                "\n" +
                "<polygon id=\"0\" points=\"19,9.6 30.8,9.6 24.9,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[0]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"1\" points=\"30.8,9.6 24.9,15.5 36.8,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[1]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"2\" points=\"30.8,9.6 36.8,15.5 42.7,9.6 \"\n" +
                "\tstyle=\"fill:" + colors[state[2]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"3\" points=\"36.8,15.5 42.7,9.6 48.6,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[3]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"4\" points=\"42.7,9.6 48.6,15.5 54.5,9.6\"\n" +
                "\tstyle=\"fill:" + colors[state[4]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"5\" points=\"24.9,15.5 30.8,21.4 36.8,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[5]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"6\" points=\"30.8,21.4 36.8,15.5 42.7,21.4\"\n" +
                "\tstyle=\"fill:" + colors[state[6]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"7\" points=\"36.8,15.5 42.7,21.4 48.6,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[7]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"8\" points=\"30.8,21.4 36.8,27.3 42.7,21.4\"\n" +
                "\tstyle=\"fill:" + colors[state[8]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"9\" points=\"19,21.4 24.9,15.5 19,9.6\"\n" +
                "\tstyle=\"fill:" + colors[state[9]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"10\" points=\"24.9,27.3 19,21.4 24.9,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[10]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"11\" points=\"24.9,27.3 30.8,21.4 24.9,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[11]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"12\" points=\"30.8,33.3 24.9,27.3 30.8,21.4\"\n" +
                "\tstyle=\"fill:" + colors[state[12]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"13\" points=\"30.8,33.3 36.8,27.3 30.8,21.4\"\n" +
                "\tstyle=\"fill:" + colors[state[13]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"14\" points=\"19,33.3 24.9,27.3 19,21.4\"\n" +
                "\tstyle=\"fill:" + colors[state[14]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"15\" points=\"19,33.3 24.9,39.2 24.9,27.3\"\n" +
                "\tstyle=\"fill:" + colors[state[15]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"16\" points=\"24.9,39.2 30.8,33.3 24.9,27.3\"\n" +
                "\tstyle=\"fill:" + colors[state[16]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"17\" points=\"19,45.1 19,33.3 24.9,39.2\"\n" +
                "\tstyle=\"fill:" + colors[state[17]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"18\" points=\"42.7,33.3 36.8,27.3 30.8,33.3\"\n" +
                "\tstyle=\"fill:" + colors[state[18]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"19\" points=\"36.8,39.2 30.8,33.3 24.9,39.2\"\n" +
                "\tstyle=\"fill:" + colors[state[19]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"20\" points=\"42.7,33.3 36.8,39.2 30.8,33.3\"\n" +
                "\tstyle=\"fill:" + colors[state[20]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"21\" points=\"48.6,39.2 42.7,33.3 36.8,39.2\"\n" +
                "\tstyle=\"fill:" + colors[state[21]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"22\" points=\"30.8,45.1 24.9,39.2 19,45.1\"\n" +
                "\tstyle=\"fill:" + colors[state[22]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"23\" points=\"36.8,39.2 30.8,45.1 24.9,39.2\"\n" +
                "\tstyle=\"fill:" + colors[state[23]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"24\" points=\"42.7,45.1 36.8,39.2 30.8,45.1\"\n" +
                "\tstyle=\"fill:" + colors[state[24]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"25\" points=\"42.7,45.1 48.6,39.2 36.8,39.2\"\n" +
                "\tstyle=\"fill:" + colors[state[25]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"26\" points=\"54.5,45.1 42.7,45.1 48.6,39.2\"\n" +
                "\tstyle=\"fill:" + colors[state[26]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"27\" points=\"42.7,21.4 36.8,27.3 42.7,33.3\"\n" +
                "\tstyle=\"fill:" + colors[state[27]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"28\" points=\"42.7,21.4 48.6,27.3 42.7,33.3\"\n" +
                "\tstyle=\"fill:" + colors[state[28]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"29\" points=\"48.6,15.5 42.7,21.4 48.6,27.3\"\n" +
                "\tstyle=\"fill:" + colors[state[29]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"30\" points=\"54.5,21.4 48.6,15.5 48.6,27.3\"\n" +
                "\tstyle=\"fill:" + colors[state[30]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"31\" points=\"54.5,9.6 54.5,21.4 48.6,15.5\"\n" +
                "\tstyle=\"fill:" + colors[state[31]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"32\" points=\"48.6,27.3 42.7,33.3 48.6,39.2 \"\n" +
                "\tstyle=\"fill:" + colors[state[32]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"33\" points=\"48.6,27.3 54.5,33.3 48.6,39.2\"\n" +
                "\tstyle=\"fill:" + colors[state[33]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"34\" points=\"54.5,21.4 48.6,27.3 54.5,33.3\"\n" +
                "\tstyle=\"fill:" + colors[state[34]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"35\" points=\"54.5,33.3 48.6,39.2 54.5,45.1\"\n" +
                "\tstyle=\"fill:" + colors[state[35]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"36\" points=\"19,63.5 24.9,57.6 19,51.7\"\n" +
                "\tstyle=\"fill:" + colors[state[36]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"37\" points=\"19,75.4 24.9,69.4 19,63.5\"\n" +
                "\tstyle=\"fill:" + colors[state[37]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"38\" points=\"24.9,69.4 19,63.5 24.9,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[38]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"39\" points=\"24.9,69.4 30.8,63.5 24.9,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[39]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"40\" points=\"19,87.2 19,75.4 24.9,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[40]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"41\" points=\"19,75.4 24.9,81.3 24.9,69.4\"\n" +
                "\tstyle=\"fill:" + colors[state[41]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"42\" points=\"24.9,81.3 30.8,75.4 24.9,69.4\"\n" +
                "\tstyle=\"fill:" + colors[state[42]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"43\" points=\"30.8,75.4 24.9,69.4 30.8,63.5\"\n" +
                "\tstyle=\"fill:" + colors[state[43]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"44\" points=\"30.8,75.4 36.8,69.4 30.8,63.5\"\n" +
                "\tstyle=\"fill:" + colors[state[44]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"45\" points=\"19,51.7 30.8,51.7 24.9,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[45]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"46\" points=\"30.8,51.7 24.9,57.6 36.8,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[46]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"47\" points=\"30.8,51.7 36.8,57.6 42.7,51.7\"\n" +
                "\tstyle=\"fill:" + colors[state[47]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"48\" points=\"36.8,57.6 42.7,51.7 48.6,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[48]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"49\" points=\"42.7,51.7 48.6,57.6 54.5,51.7\"\n" +
                "\tstyle=\"fill:" + colors[state[49]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"50\" points=\"24.9,57.6 30.8,63.5 36.8,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[50]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"51\" points=\"30.8,63.5 36.8,57.6 42.7,63.5\"\n" +
                "\tstyle=\"fill:" + colors[state[51]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"52\" points=\"36.8,57.6 42.7,63.5 48.6,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[52]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"53\" points=\"30.8,63.5 36.8,69.4 42.7,63.5\"\n" +
                "\tstyle=\"fill:" + colors[state[53]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"54\" points=\"54.5,51.7 54.5,63.5 48.6,57.6\"\n" +
                "\tstyle=\"fill:" + colors[state[54]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"55\" points=\"48.6,57.6 42.7,63.5 48.6,69.4\"\n" +
                "\tstyle=\"fill:" + colors[state[55]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"56\" points=\"54.5,63.5 48.6,57.6 48.6,69.4\"\n" +
                "\tstyle=\"fill:" + colors[state[56]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"57\" points=\"54.5,63.5 48.6,69.4 54.5,75.4\"\n" +
                "\tstyle=\"fill:" + colors[state[57]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"58\" points=\"42.7,63.5 36.8,69.4 42.7,75.4\"\n" +
                "\tstyle=\"fill:" + colors[state[58]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"59\" points=\"42.7,63.5 48.6,69.4 42.7,75.4\"\n" +
                "\tstyle=\"fill:" + colors[state[59]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"60\" points=\"48.6,69.4 42.7,75.4 48.6,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[60]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"61\" points=\"48.6,69.4 54.5,75.4 48.6,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[61]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"62\" points=\"54.5,75.4 48.6,81.3 54.5,87.2\"\n" +
                "\tstyle=\"fill:" + colors[state[62]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"63\" points=\"54.5,87.2 42.7,87.2 48.6,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[63]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"64\" points=\"42.7,87.2 48.6,81.3 36.8,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[64]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"65\" points=\"42.7,87.2 36.8,81.3 30.8,87.2\"\n" +
                "\tstyle=\"fill:" + colors[state[65]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"66\" class=\"st1\" points=\"36.8,81.3 30.8,87.2 24.9,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[66]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"67\" class=\"st1\" points=\"30.8,87.2 24.9,81.3 19,87.2\"\n" +
                "\tstyle=\"fill:" + colors[state[67]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"68\" class=\"st1\" points=\"48.6,81.3 42.7,75.4 36.8,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[68]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"69\" points=\"42.7,75.4 36.8,81.3 30.8,75.4\"\n" +
                "\tstyle=\"fill:" + colors[state[69]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"70\" class=\"st1\" points=\"36.8,81.3 30.8,75.4 24.9,81.3\"\n" +
                "\tstyle=\"fill:" + colors[state[70]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "<polygon id=\"71\" class=\"st1\" points=\"42.7,75.4 36.8,69.4 30.8,75.4\"\n" +
                "\tstyle=\"fill:" + colors[state[71]] + ";stroke:gray;stroke-width:.1\"/>\n" +
                "\n" +
                "<polygon\n" +
                "\tid=\"u_face\"\n" +
                "\tpoints=\"19,9.6 54.5,9.6 36.8,27.3\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon\n" +
                "\tid=\"l_face\"\n" +
                "\tpoints=\"19,45.1 19,9.6 36.8,27.3\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon\n" +
                "\tid=\"r_face\"\n" +
                "\tpoints=\"54.5,9.6 54.5,45.1 36.8,27.3\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon\n" +
                "\tid=\"f_face\"\n" +
                "\tpoints=\"54.5,45.1 19,45.1 36.8,27.3\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon\n" +
                "\tid=\"b_face\"\n" +
                "\tpoints=\"19,51.7 54.5,51.7 36.8,69.4\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon\n" +
                "\tid=\"br_face\"\n" +
                "\tpoints=\"19,87.2 19,51.7 36.8,69.4\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon\n" +
                "\tid=\"bl_face\"\n" +
                "\tpoints=\"54.5,51.7 54.5,87.2 36.8,69.4\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "<polygon\n" +
                "\tid=\"d_face\"\n" +
                "\tpoints=\"54.5,87.2 19,87.2 36.8,69.4\"\n" +
                "\tstyle=\"fill:none;stroke:gray;stroke-width:1\"/>\n" +
                "</svg>\n";
        return returnedString;
    }
*/
}
