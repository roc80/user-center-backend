package com.yupi.usercenter.algorithm;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lipeng
 * @description 编辑距离算法
 * @since 2025/6/7 8:53
 */
public class EditDistance {

    /**
     * levenshtein 编辑距离算法，DP实现
     *
     * @param s source str
     * @param t target str
     * @return min step num
     */
    public static int levenshteinDistance(String s, String t) {
        if (s == null || t == null) {
            return -1;
        }
        int n = s.length();
        int m = t.length();
        if (n == 0 && m == 0) {
            return 0;
        }
        int[][] distance = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            distance[i][0] = i;
        }
        for (int i = 0; i <= m; i++) {
            distance[0][i] = i;
        }
        // 状态转移： d[i][j] = min(d[i - 1][j], d[i, j - 1], d[i - 1, j - 1]) + 1
        // 如果d[i - 1, j - 1] 和 d[i, j] 相等，那么不用 + 1
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (s.charAt(i - 1) == t.charAt(j - 1)) {
                    distance[i][j] = distance[i - 1][j - 1];
                } else {
                    int temp = Math.min(distance[i - 1][j], distance[i][j - 1]);
                    int min = Math.min(temp, distance[i - 1][j - 1]);
                    distance[i][j] = min + 1;
                }
            }
        }
        return distance[n][m];
    }

    public static int levenshteinDistance(List<String> sList, List<String> tList) {
        if (sList == null || tList == null) {
            return -1;
        }
        List<String> nonNullSourceList = sList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<String> nonNullTargetList = tList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        int n = nonNullSourceList.size();
        int m = nonNullTargetList.size();
        if (n == 0 && m == 0) {
            return 0;
        }
        int[][] distance = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            distance[i][0] = i;
        }
        for (int i = 0; i <= m; i++) {
            distance[0][i] = i;
        }
        // 状态转移： d[i][j] = min(d[i - 1][j], d[i, j - 1], d[i - 1, j - 1]) + 1
        // 如果d[i - 1, j - 1] 和 d[i, j] 相等，那么不用 + 1
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (nonNullSourceList.get(i - 1).equals(nonNullTargetList.get(j - 1))) {
                    distance[i][j] = distance[i - 1][j - 1];
                } else {
                    int temp = Math.min(distance[i - 1][j], distance[i][j - 1]);
                    int min = Math.min(temp, distance[i - 1][j - 1]);
                    distance[i][j] = min + 1;
                }
            }
        }
        return distance[n][m];
    }
}
