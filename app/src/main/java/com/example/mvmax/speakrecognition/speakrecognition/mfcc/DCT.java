package com.example.mvmax.speakrecognition.speakrecognition.mfcc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public final class DCT {

    private final int[][] f;
    private int[][] g;
    private int[][] inv;

    public static void main(final String[] args) {

        final int[][] fm = new int[8][8];

        if (args.length != 1) {
            return;
        }

        final File f = new File(args[0]);
        if (!f.canRead()) {
            return;
        }
        try {
            @SuppressWarnings("resource") final BufferedReader br = new BufferedReader(new FileReader(f));
            for (int i = 0; i < 8; i++) {
                final String line = br.readLine();
                final StringTokenizer tok = new StringTokenizer(line, ", ");
                if (tok.countTokens() != 8) {
                    throw new IOException("Error");
                }
                for (int j = 0; j < 8; j++) {
                    final String numstr = tok.nextToken();
                    final int num = Integer.parseInt(numstr);
                    fm[i][j] = num;
                }
            }
            br.close();
        } catch (final FileNotFoundException e) {
            return;
        } catch (final IOException e) {
            return;
        } catch (final NumberFormatException e) {
            return;
        }

        final DCT dct = new DCT(fm);
        dct.transform();
        dct.inverse();
    }

    private DCT(final int[][] f) {
        this.f = f;
    }

    private void transform() {
        g = new int[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                double ge = 0.0;
                for (int x = 0; x < 8; x++) {
                    for (int y = 0; y < 8; y++) {
                        final double cg1 = (2.0 * (double) x + 1.0) * (double) i * Math.PI / 16.0;
                        final double cg2 = (2.0 * (double) y + 1.0) * (double) j * Math.PI / 16.0;

                        ge += ((double) f[x][y]) * Math.cos(cg1) * Math.cos(cg2);

                    }
                }
                final double ci = ((i == 0) ? 1.0 / Math.sqrt(2.0) : 1.0);
                final double cj = ((j == 0) ? 1.0 / Math.sqrt(2.0) : 1.0);
                ge *= ci * cj * 0.25;
                g[i][j] = (int) Math.round(ge);
            }
        }
    }

    private void inverse() {
        inv = new int[8][8];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                double ge = 0.0;
                for (int i = 0; i < 8; i++) {
                    final double cg1 = (2.0 * (double) x + 1.0) * (double) i * Math.PI / 16.0;
                    final double ci = ((i == 0) ? 1.0 / Math.sqrt(2.0) : 1.0);
                    for (int j = 0; j < 8; j++) {
                        final double cg2 = (2.0 * (double) y + 1.0) * (double) j * Math.PI / 16.0;
                        final double cj = ((j == 0) ? 1.0 / Math.sqrt(2.0) : 1.0);
                        final double cij4 = ci * cj * 0.25;
                        ge += cij4 * Math.cos(cg1) * Math.cos(cg2) * (double) g[i][j];
                    }
                }
                inv[x][y] = (int) Math.round(ge);
            }
        }
    }
}