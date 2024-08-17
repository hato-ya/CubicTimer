// This code is from https://github.com/Sixstringcal/FTODrawer
// The code doesn't have any specific license, but permission has been granted for its use.
// https://github.com/Sixstringcal/FTODrawer/issues/3

package com.hatopigeon.cubictimer.puzzle.FTODrawer;

public class FTO {

    /**
     * The state of the puzzle.
     * Interpretation:
     * Face Order (indexed starting at 0): U, L, F, R, BR, B, BL, D
     * Face Position: indexed 0-8 starting top left piece as if looking at face going left to right, top to bottom
     * Piece Number (or state[x]) = (Face Index * 9) + Face Position
     */
    private int[] state = new int[72];


    /**
     * Creates the FTO
     */
    public FTO() {

        //Creates a solved FTO.
        for (int i = 0; i < 9; i++) {
            state[i] = 0;
        }
        for (int i = 9; i < 18; i++) {
            state[i] = 1;
        }
        for (int i = 18; i < 27; i++) {
            state[i] = 2;
        }
        for (int i = 27; i < 36; i++) {
            state[i] = 3;
        }
        for (int i = 36; i < 45; i++) {
            state[i] = 4;
        }
        for (int i = 45; i < 54; i++) {
            state[i] = 5;
        }
        for (int i = 54; i < 63; i++) {
            state[i] = 6;
        }
        for (int i = 63; i < 72; i++) {
            state[i] = 7;
        }
    }

    public int[] getState() {
        return state;
    }

    /**
     * Applies a set of moves to the FTO
     *
     * @param scramble
     */
    public void doMoves(String scramble) {
        String[] moves = scramble.split(" ");
        for (int i = 0; i < moves.length; i++) {
            switch (moves[i].trim()) {

                /**
                 * R-based moves and rotations
                 */
                case "R":
                    doR(1);
                    break;
                case "R'":
                    doR(2);
                    break;
                case "BLs'":
                case "Rs":
                    doRs(1);
                    break;
                case "BLs":
                case "Rs'":
                    doRs(2);
                case "Rw":
                    doR(1);
                    doRs(1);
                    break;
                case "Rw'":
                    doR(2);
                    doRs(2);
                    break;
                case "BLo'":
                case "Ro":
                    doR(1);
                    doRs(1);
                    doBL(2);
                    break;
                case "BLo":
                case "Ro'":
                    doR(2);
                    doRs(2);
                    doBL(1);
                    break;

                /**
                 * U-based moves and rotations
                 */
                case "U":
                    doU(1);
                    break;
                case "U'":
                    doU(2);
                    break;
                case "Ds'":
                case "E'":
                case "Us":
                    doUs(1);
                    break;
                case "Ds":
                case "E":
                case "Us'":
                    doUs(2);
                    break;
                case "Uw":
                    doU(1);
                    doUs(1);
                    break;
                case "Uw'":
                    doU(2);
                    doUs(2);
                    break;
                case "Do'":
                case "Uo":
                    doU(1);
                    doUs(1);
                    doD(2);
                    break;
                case "Do":
                case "Uo'":
                    doU(2);
                    doUs(2);
                    doD(1);
                    break;

                /**
                 * L-based moves and rotations
                 */
                case "L":
                    doL(1);
                    break;
                case "L'":
                    doL(2);
                    break;
                case "BRs'":
                case "Ls":
                    doLs(1);
                    break;
                case "BRs":
                case "Ls'":
                    doLs(2);
                    break;
                case "Lw":
                    doL(1);
                    doLs(1);
                    break;
                case "Lw'":
                    doL(1);
                    doLs(2);
                    break;
                case "BRo'":
                case "Lo":
                    doL(1);
                    doLs(1);
                    doBR(2);
                    break;
                case "BRo":
                case "Lo'":
                    doL(2);
                    doLs(2);
                    doBR(1);
                    break;

                /**
                 * F-based moves and rotations
                 */
                case "F":
                    doF(1);
                    break;
                case "F'":
                    doF(2);
                    break;
                case "Bs'":
                case "S":
                case "Fs":
                    doFs(1);
                    break;
                case "Bs":
                case "S'":
                case "Fs'":
                    doFs(2);
                    break;
                case "Fw":
                    doF(1);
                    doFs(1);
                    break;
                case "Fw'":
                    doF(1);
                    doFs(1);
                    break;
                case "Bo'":
                case "Fo":
                    doF(1);
                    doFs(1);
                    doB(2);
                    break;
                case "Bo":
                case "Fo'":
                    doF(2);
                    doFs(2);
                    doB(1);
                    break;

                /**
                 * D-based moves and rotations
                 */
                case "D":
                    doD(1);
                    break;
                case "D'":
                    doD(2);
                    break;
                case "Dw":
                    doD(1);
                    doUs(2);
                    break;
                case "Dw'":
                    doD(2);
                    doUs(1);
                    break;

                /**
                 * B-based moves and rotations
                 */
                case "B":
                    doB(1);
                    break;
                case "B'":
                    doB(2);
                    break;
                case "Bw":
                    doB(1);
                    doFs(2);
                    break;
                case "Bw'":
                    doB(2);
                    doFs(1);
                    break;

                /**
                 * BR-based moves and rotations
                 */
                case "BR":
                    doBR(1);
                    break;
                case "BR'":
                    doBR(2);
                    break;
                case "BRw":
                    doBR(1);
                    doLs(2);
                    break;
                case "BRw'":
                    doBR(2);
                    doLs(1);
                    break;

                /**
                 * BL-based moves and rotations
                 */
                case "BL":
                    doBL(1);
                    break;
                case "BL'":
                    doBL(2);
                    break;
                case "BLw":
                    doBL(1);
                    doRs(2);
                    break;
                case "BLw'":
                    doBL(2);
                    doRs(1);
                    break;

                default:
                    //throw new IllegalArgumentException("The move " + moves[i] + " is not supported.");
            }
        }
    }

    /**
     * Applies a R move "times" times
     *
     * @param times
     */
    public void doR(int times) {
        int temp;
        for (int i = 0; i < times; i++) {

            //Top Corners
            temp = state[4];
            state[4] = state[18];
            state[18] = state[40];
            state[40] = temp;

            //Back Corners
            temp = state[45];
            state[45] = state[13];
            state[13] = state[67];
            state[67] = temp;

            //Right Corners
            temp = state[31];
            state[31] = state[27];
            state[27] = state[35];
            state[35] = temp;

            //Bottom Corners
            temp = state[36];
            state[36] = state[8];
            state[8] = state[26];
            state[26] = temp;

            //U Edges
            temp = state[7];
            state[7] = state[21];
            state[21] = state[37];
            state[37] = temp;

            //R Edges
            temp = state[29];
            state[29] = state[32];
            state[32] = state[34];
            state[34] = temp;

            //Back Triangles
            temp = state[3];
            state[3] = state[20];
            state[20] = state[41];
            state[41] = temp;

            //Front Triangles
            temp = state[6];
            state[6] = state[25];
            state[25] = state[38];
            state[38] = temp;

            //Right Triangles
            temp = state[30];
            state[30] = state[28];
            state[28] = state[33];
            state[33] = temp;
        }
    }

    /**
     * Applies an Rs move "times" times
     *
     * @param times
     */
    public void doRs(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //UB Edges
            temp = state[2];
            state[2] = state[19];
            state[19] = state[42];
            state[42] = temp;

            //BU Edges
            temp = state[47];
            state[47] = state[16];
            state[16] = state[70];
            state[70] = temp;

            //UL Edges
            temp = state[5];
            state[5] = state[24];
            state[24] = state[39];
            state[39] = temp;

            //LU Edges
            temp = state[11];
            state[11] = state[65];
            state[65] = state[50];
            state[50] = temp;

            //U Triangles
            temp = state[1];
            state[1] = state[23];
            state[23] = state[43];
            state[43] = temp;

            //L Triangles
            temp = state[12];
            state[12] = state[66];
            state[66] = state[46];
            state[46] = temp;
        }
    }

    /**
     * Applies a BL move "times" times
     *
     * @param times
     */
    public void doBL(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Top Corners
            temp = state[0];
            state[0] = state[44];
            state[44] = state[22];
            state[22] = temp;

            //Back Corners
            temp = state[49];
            state[49] = state[71];
            state[71] = state[17];
            state[17] = temp;

            //Bottom Corners
            temp = state[54];
            state[54] = state[58];
            state[58] = state[62];
            state[62] = temp;

            //Front Corners
            temp = state[9];
            state[9] = state[53];
            state[53] = state[63];
            state[63] = temp;

            //Front Edges
            temp = state[14];
            state[14] = state[52];
            state[52] = state[68];
            state[68] = temp;

            //Left Edges
            temp = state[55];
            state[55] = state[60];
            state[60] = state[57];
            state[57] = temp;

            //Left Triangles
            temp = state[56];
            state[56] = state[59];
            state[59] = state[61];
            state[61] = temp;

            //Top Triangles
            temp = state[10];
            state[10] = state[51];
            state[51] = state[64];
            state[64] = temp;

            //Bottom Triangles
            temp = state[15];
            state[15] = state[48];
            state[48] = state[69];
            state[69] = temp;
        }
    }

    /**
     * Applies a U move "times" times
     *
     * @param times
     */
    public void doU(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Top Corners
            temp = state[0];
            state[0] = state[8];
            state[8] = state[4];
            state[4] = temp;

            //Back Corners
            temp = state[49];
            state[49] = state[13];
            state[13] = state[31];
            state[31] = temp;

            //Bottom Corners
            temp = state[54];
            state[54] = state[18];
            state[18] = state[36];
            state[36] = temp;

            //Frontish Corners
            temp = state[9];
            state[9] = state[27];
            state[27] = state[45];
            state[45] = temp;

            //U Edges
            temp = state[2];
            state[2] = state[5];
            state[5] = state[7];
            state[7] = temp;

            //Outer Edges
            temp = state[47];
            state[47] = state[11];
            state[11] = state[29];
            state[29] = temp;

            //U Triangles
            temp = state[1];
            state[1] = state[6];
            state[6] = state[3];
            state[3] = temp;

            //Right Triangles
            temp = state[12];
            state[12] = state[30];
            state[30] = state[48];
            state[48] = temp;

            //Left Triangles
            temp = state[10];
            state[10] = state[28];
            state[28] = state[46];
            state[46] = temp;
        }
    }

    /**
     * Applies a Us move "times" times
     *
     * @param times
     */
    public void doUs(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Front Triangles
            temp = state[20];
            state[20] = state[56];
            state[56] = state[38];
            state[38] = temp;

            //Left Triangles
            temp = state[15];
            state[15] = state[51];
            state[51] = state[33];
            state[33] = temp;

            //FR Edges
            temp = state[21];
            state[21] = state[57];
            state[57] = state[39];
            state[39] = temp;

            //RF Edges
            temp = state[41];
            state[41] = state[14];
            state[14] = state[50];
            state[50] = temp;

            //FL Edges
            temp = state[19];
            state[19] = state[55];
            state[55] = state[37];
            state[37] = temp;

            //LF Edges
            temp = state[16];
            state[16] = state[52];
            state[52] = state[43];
            state[43] = temp;
        }
    }

    /**
     * Applies a D move "times" times
     *
     * @param times
     */
    public void doD(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Bottom Corners
            temp = state[63];
            state[63] = state[71];
            state[71] = state[67];
            state[67] = temp;

            //Up Corners
            temp = state[17];
            state[17] = state[53];
            state[53] = state[35];
            state[35] = temp;

            //Right Corners
            temp = state[22];
            state[22] = state[58];
            state[58] = state[40];
            state[40] = temp;

            //Left Corners
            temp = state[62];
            state[62] = state[44];
            state[44] = state[26];
            state[26] = temp;

            //Bottom Edges
            temp = state[65];
            state[65] = state[68];
            state[68] = state[70];
            state[70] = temp;

            //Outer Edges
            temp = state[24];
            state[24] = state[60];
            state[60] = state[42];
            state[42] = temp;

            //Bottom Triangles
            temp = state[64];
            state[64] = state[69];
            state[69] = state[66];
            state[66] = temp;

            //Right Triangles
            temp = state[25];
            state[25] = state[61];
            state[61] = state[43];
            state[43] = temp;

            //Left Triangles
            temp = state[23];
            state[23] = state[59];
            state[59] = state[41];
            state[41] = temp;
        }
    }

    /**
     * Applies an L move "times" times
     *
     * @param times
     */
    public void doL(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //U Corners
            temp = state[0];
            state[0] = state[62];
            state[62] = state[18];
            state[18] = temp;

            //Bottom Corners
            temp = state[54];
            state[54] = state[22];
            state[22] = state[8];
            state[8] = temp;

            //Back Corners
            temp = state[49];
            state[49] = state[63];
            state[63] = state[27];
            state[27] = temp;

            //Frontish Corners
            temp = state[9];
            state[9] = state[17];
            state[17] = state[13];
            state[13] = temp;

            //Left Edges
            temp = state[11];
            state[11] = state[14];
            state[14] = state[16];
            state[16] = temp;

            //Right Edges
            temp = state[5];
            state[5] = state[57];
            state[57] = state[19];
            state[19] = temp;

            //Left Triangles
            temp = state[10];
            state[10] = state[15];
            state[15] = state[12];
            state[12] = temp;

            //Top Back Left Triangles
            temp = state[1];
            state[1] = state[61];
            state[61] = state[20];
            state[20] = temp;

            //Top Front Triangles
            temp = state[6];
            state[6] = state[56];
            state[56] = state[23];
            state[23] = temp;
        }
    }

    /**
     * Applies an Ls move "times" times
     *
     * @param times
     */
    public void doLs(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Top Triangles
            temp = state[3];
            state[3] = state[59];
            state[59] = state[25];
            state[25] = temp;

            //Right Triangles
            temp = state[28];
            state[28] = state[48];
            state[48] = state[64];
            state[64] = temp;

            //UB Edges
            temp = state[2];
            state[2] = state[60];
            state[60] = state[21];
            state[21] = temp;

            //BU Edges
            temp = state[47];
            state[47] = state[68];
            state[68] = state[32];
            state[32] = temp;

            //UR Edges
            temp = state[7];
            state[7] = state[55];
            state[55] = state[24];
            state[24] = temp;

            //RU Edges
            temp = state[29];
            state[20] = state[52];
            state[52] = state[65];
            state[65] = temp;
        }
    }

    /**
     * Applies BR "times" times
     *
     * @param times
     */
    public void doBR(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Top Corners
            temp = state[4];
            state[4] = state[26];
            state[26] = state[58];
            state[58] = temp;

            //Bottom Corners
            temp = state[36];
            state[36] = state[40];
            state[40] = state[44];
            state[44] = temp;

            //Back Corners
            temp = state[45];
            state[45] = state[35];
            state[35] = state[71];
            state[71] = temp;

            //Rightish Corners
            temp = state[31];
            state[31] = state[67];
            state[67] = state[53];
            state[53] = temp;

            //Outer Edges
            temp = state[34];
            state[34] = state[70];
            state[70] = state[50];
            state[50] = temp;

            //Inner Edges
            temp = state[37];
            state[37] = state[42];
            state[42] = state[39];
            state[39] = temp;

            //Inner Triangles
            temp = state[38];
            state[38] = state[41];
            state[41] = state[43];
            state[43] = temp;

            //Top Outer Triangles
            temp = state[30];
            state[30] = state[66];
            state[66] = state[51];
            state[51] = temp;

            //Bottom Outer Triangles
            temp = state[33];
            state[33] = state[69];
            state[69] = state[46];
            state[46] = temp;
        }
    }

    /**
     * Applies F "times" times
     *
     * @param times
     */
    public void doF(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Top Corners
            temp = state[8];
            state[8] = state[62];
            state[62] = state[40];
            state[40] = temp;

            //Bottom Corners
            temp = state[18];
            state[18] = state[22];
            state[22] = state[26];
            state[26] = temp;

            //Right Corners
            temp = state[27];
            state[27] = state[17];
            state[17] = state[67];
            state[67] = temp;

            //Left Corners
            temp = state[13];
            state[13] = state[63];
            state[63] = state[35];
            state[35] = temp;

            //Front Edges
            temp = state[19];
            state[19] = state[24];
            state[24] = state[21];
            state[21] = temp;

            //Outer Edges
            temp = state[16];
            state[16] = state[65];
            state[65] = state[32];
            state[32] = temp;

            //Front Triangles
            temp = state[20];
            state[20] = state[23];
            state[23] = state[25];
            state[25] = temp;

            //Outer Right Top Triangles
            temp = state[28];
            state[28] = state[15];
            state[15] = state[66];
            state[66] = temp;

            //Outer Left Top Triangles
            temp = state[12];
            state[12] = state[64];
            state[64] = state[33];
            state[33] = temp;
        }
    }

    /**
     * Applies an Fs move "times" times
     *
     * @param times
     */
    public void doFs(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //Top Triangles
            temp = state[6];
            state[6] = state[61];
            state[61] = state[41];
            state[41] = temp;

            //Left Triangles
            temp = state[10];
            state[10] = state[69];
            state[69] = state[30];
            state[30] = temp;

            //UR Edges
            temp = state[7];
            state[7] = state[57];
            state[57] = state[42];
            state[42] = temp;

            //RU Edges
            temp = state[29];
            state[29] = state[14];
            state[14] = state[70];
            state[70] = temp;

            //UL Edges
            temp = state[5];
            state[5] = state[60];
            state[60] = state[37];
            state[37] = temp;

            //LU Edges
            temp = state[11];
            state[11] = state[68];
            state[68] = state[34];
            state[34] = temp;
        }
    }

    /**
     * Applies a B move "times" times
     *
     * @param times
     */
    public void doB(int times) {
        int temp;
        for (int i = 0; i < times; i++) {
            //UBL Corners
            temp = state[0];
            state[0] = state[36];
            state[36] = state[58];
            state[58] = temp;

            //Back Corners
            temp = state[49];
            state[49] = state[45];
            state[45] = state[53];
            state[53] = temp;

            //Left Bottom Corners
            temp = state[54];
            state[54] = state[4];
            state[4] = state[44];
            state[44] = temp;

            //LUB Corners
            temp = state[9];
            state[9] = state[31];
            state[31] = state[71];
            state[71] = temp;

            //Back Edges
            temp = state[47];
            state[47] = state[50];
            state[50] = state[52];
            state[52] = temp;

            //Outer Edges
            temp = state[2];
            state[2] = state[39];
            state[39] = state[55];
            state[55] = temp;

            //Back Triangles
            temp = state[46];
            state[46] = state[51];
            state[51] = state[48];
            state[48] = temp;

            //UBR Triangles
            temp = state[3];
            state[3] = state[43];
            state[43] = state[56];
            state[56] = temp;

            //UBL Triangles
            temp = state[1];
            state[1] = state[38];
            state[38] = state[59];
            state[59] = temp;
        }
    }



}
