package com.farman.annotator;

import com.github.stagirs.lingvo.morpho.MorphoAnalyst;

import java.util.*;

public class Mapper {
    static List<String> titlesRuTokenized = new ArrayList<>();

    private static final String STOP_SYMBOLS[] = {".", ",", "!", "?", ":", ";", "-", "\\", "/", "*", "(", ")", "+", "@",
            "#", "$", "%", "^", "&", "=", "'", "\"", "[", "]", "{", "}", "|"};
    private static final String STOP_WORDS_RU[] = {"это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за",
            "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну", "вы",
            "бы", "что", "кто", "он", "она"};


    public static List<Integer> map(List<String> queryString, List<String> not_tokenized, List<String> titlesRu, List<String> titlesEn, float part) {

        tokenize(not_tokenized);
        Set<String> titles = new HashSet<>();
        for (int i = 0; i < titlesEn.size(); i++)
            if (!not_tokenized.get(i).contains("#REDIRECT"))
                titles.add(getTokenized2(titlesEn.get(i).toLowerCase()));
        for (String str: titlesRu)
            titlesRuTokenized.add(getTokenized2(str));

        List<Integer> scores;
        List<Float> points = new ArrayList<>();
        System.out.println(part);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < queryString.size(); i++) {
            String[] toks = getTokenized(queryString.get(i)).split(" ");
            points.clear();
            scores = GeneralUtils.getScoresShingles(toks, 10000, 0.00000000001, false, points);

            if (points.get(1) / points.get(0) < part) {
                result.add(scores.get(0));
            } else {
                result.add(-1);
            }
        }
        return result;
    }


    protected static String getTokenized(String s){
        s = s.toLowerCase();
        for (String stopSymbol : STOP_SYMBOLS) {
            s = s.replace(stopSymbol, " ");
        }

        s = s.replaceAll("  ", " ");
        for (String stopWord : STOP_WORDS_RU) {
            s = s.replace(" " + stopWord + " ", " ");
        }
        StringTokenizer st = new StringTokenizer(s);
        List<String> words = new ArrayList<>();
        while (st.hasMoreTokens()) {
            words.add(st.nextToken());
        }
        return String.join(" ", MorphoAnalyst.normalize(words));
    }

    protected static String getTokenized2(String s){
        s = s.toLowerCase();
        for (String stopSymbol : STOP_SYMBOLS) {
            s = s.replace(stopSymbol, " ");
        }

        s = s.replaceAll("  ", " ");
        for (String stopWord : STOP_WORDS_RU) {
            s = s.replace(" " + stopWord + " ", " ");
        }
        StringTokenizer st = new StringTokenizer(s);
        List<String> words = new ArrayList<>();
        while (st.hasMoreTokens()) {
            words.add(st.nextToken());
        }
        return String.join(" ", MorphoAnalyst.normalize(words));
    }

    private static List<String> tokenize(List<String> not_tokenized){
        List<String> tokenized = new ArrayList<>();
        for (String s: not_tokenized){
            String tokenizedStr = getTokenized(s);
            tokenized.add(tokenizedStr);
            GeneralUtils.shingles.add(GeneralUtils.genShingle(tokenizedStr.split(" ")));
        }
        return  tokenized;
    }

}
