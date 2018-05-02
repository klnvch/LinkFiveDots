package by.klnvch.link5dots.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import by.klnvch.link5dots.models.Dot;

public class DotsArrayUtils {
    private static final int N = 20;
    private static final int M = 20;

    private static Dot[][] net = new Dot[N][M];

    private static void clearNet() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < M; j++)
                net[i][j] = new Dot(i, j);
    }

    @Nullable
    public static ArrayList<Dot> findWinningLine(@Nullable List<Dot> dots) {
        if (dots == null || dots.size() < 9) return null;

        clearNet();
        for (Dot dot : dots) {
            net[dot.getX()][dot.getY()] = dot;
        }

        final Dot lastDot = dots.get(dots.size() - 1);

        final int[][] directions = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}};

        for (int[] d : directions) {
            final ArrayList<Dot> result = getDotsInLine(lastDot, d[0], d[1]);
            if (result.size() >= 5) {
                return result;
            }
        }

        return null;
    }

    @NonNull
    private static ArrayList<Dot> getDotsInLine(@NonNull Dot dot, int dx, int dy) {

        final int x = dot.getX();
        final int y = dot.getY();

        final ArrayList<Dot> result = new ArrayList<>();
        result.add(dot);

        // one direction, add before
        for (int k = 1; k < 5; k++) {
            final int i = x + dx * k;
            final int j = y + dy * k;
            if (isInBound(i, j) && net[i][j].getType() == dot.getType()) {
                result.add(net[i][j]);
            } else {
                break;
            }
        }
        // another direction, add after
        for (int k = 1; k < 5; k++) {
            final int i = x - dx * k;
            final int j = y - dy * k;
            if (isInBound(i, j) && net[i][j].getType() == dot.getType()) {
                result.add(0,net[i][j]);
            } else {
                break;
            }
        }

        return result;
    }

    private static boolean isInBound(int x, int y) {
        return x >= 0 && y >= 0 && x < N && y < M;
    }
}