package com.yupi.usercenter.service;

import com.yupi.usercenter.algorithm.EditDistance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class UserServiceTest {


    @Test
    void testEditDistance() {
        String str1 = "horse";
        String str2 = "ros";
        Assertions.assertEquals(3, EditDistance.levenshteinDistance(str1, str2));

        String str3 = "abcxyz";
        String str4 = "xyza";
        Assertions.assertEquals(4, EditDistance.levenshteinDistance(str3, str4));


        List<String> sList = Arrays.asList("Java", "男", "大一");
        Assertions.assertEquals(1, EditDistance.levenshteinDistance(sList, Arrays.asList("Java", "女", "大一")));
        Assertions.assertEquals(1, EditDistance.levenshteinDistance(sList, Arrays.asList("Java", "男")));

        Assertions.assertEquals(2, EditDistance.levenshteinDistance(sList, Arrays.asList("Java", "女", "大二")));

        Assertions.assertEquals(3, EditDistance.levenshteinDistance(sList, Arrays.asList("Python", "女", "大三")));
        Assertions.assertEquals(3, EditDistance.levenshteinDistance(sList, Collections.singletonList("C++")));

        Assertions.assertEquals(4, EditDistance.levenshteinDistance(sList, Arrays.asList("TypeScript", "女", "大二", "Dance")));
    }
}