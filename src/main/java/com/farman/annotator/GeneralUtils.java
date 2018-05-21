package com.farman.annotator;

import com.github.stagirs.lingvo.morpho.MorphoAnalyst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by must on 16.11.2017.
 */
public class GeneralUtils {

    private static final int SHINGLE_LEN = 2;

    static int docs = 0;

    static List<HashSet<Integer>> shingles = new ArrayList<>();

    private static final String STOP_SYMBOLS[] = {".", ",", "!", "?", ":", ";", "-", "—", "\\", "/", "*", "(", ")", "+", "@",
            "#", "$", "%", "^", "&", "=", "'", "\"", "[", "]", "{", "}", "|"};
    private static final String STOP_WORDS_RU[] = {"это", "как", "так", "и", "в", "над", "к", "до", "не", "на", "но", "за",
            "то", "с", "ли", "а", "во", "от", "со", "для", "о", "же", "ну", "вы",
            "бы", "что", "кто", "он", "она"};

    public static float getScoresForTwoTexts(String[] toksFirst, String[] toksSecond, int shingleLength){


        ArrayList<Integer> shinglesFirst = new ArrayList<>();
        int shinglesNumber = toksFirst.length - shingleLength;

        //Create all shingles
        for (int i = 0; i <= shinglesNumber; i++) {
            String shingle = "";

            //Create one shingle
            for (int j = 0; j < shingleLength; j++) {
                shingle = shingle + toksFirst[i+j] + " ";
            }

            shinglesFirst.add(shingle.hashCode());
        }

        return compare(shinglesFirst, genShingle(toksSecond, shingleLength));
    }

    public static float getScoresForTwoTexts(String[] toksFirst, String[] toksSecond){
        return getScoresForTwoTexts(toksFirst, toksSecond, SHINGLE_LEN);
    }

    public static List<Integer> getScoresShingles(String[] toks, int count, double min_score, boolean expanded, List<Float> result, int shingleLength){
        List<Integer> scores = new ArrayList<>();


        ArrayList<Integer> shinglesThis = new ArrayList<>();
        int shinglesNumber = toks.length - shingleLength;

        //Create all shingles
        for (int i = 0; i <= shinglesNumber; i++) {
            String shingle = "";

            //Create one shingle
            for (int j = 0; j < shingleLength; j++) {
                shingle = shingle + toks[i+j] + " ";
            }

            shinglesThis.add(shingle.hashCode());
        }

        List<Float> lst = new ArrayList<>();
        for (int i = 0; i < docs; i++){
            lst.add(compare(shinglesThis, shingles.get(i)));
        }

        int id[] = new int[docs];
        float val[] = new float[docs];
        for (int i = 0; i < docs; i++){
            id[i] = i;
            val[i] = lst.get(i);
        }
        for (int i = val.length - 1; i > 0; i--){
            for (int j = 0; j < i; j++){
                if (val[j] < val[j + 1]){
                    float tmp = val[j];
                    val[j] = val[j + 1];
                    val[j + 1] = tmp;
                    int tmp_id = id[j];
                    id[j] = id[j + 1];
                    id[j + 1] = tmp_id;
                }
            }
        }
        int i = 0;
        while (i < docs && i < count && val[i] > min_score){
            scores.add(id[i]);
            i++;
        }
        result.add(val[0]);
        result.add(val[1]);
        return scores;
    }

    public static List<Integer> getScoresShingles(String[] toks, int count, double min_score, boolean expanded, List<Float> result){
        return getScoresShingles(toks, count, min_score, expanded, result, SHINGLE_LEN);
    }

    public static HashSet<Integer> genShingle(String [] words, int shingleLength) {
        HashSet<Integer> shingles = new HashSet<>();

        int shinglesNumber = words.length - shingleLength;

        //Create all shingles
        for (int i = 0; i <= shinglesNumber; i++) {
            String shingle = "";

            //Create one shingle
            for (int j = 0; j < shingleLength; j++) {
                shingle = shingle + words[i+j] + " ";
            }

            shingles.add(shingle.hashCode());
        }

        return shingles;
    }

    public static HashSet<Integer> genShingle(String [] words) {
        return genShingle(words, SHINGLE_LEN);
    }

    /**
     * Метод сравнивает две последовательности шинглов
     *
     * @param textShingles1New первая последовательность шинглов
     * @param textShingles2New вторая последовательность шинглов
     * @return процент сходства шинглов
     */
    public static float compare(ArrayList<Integer> textShingles1New, HashSet<Integer> textShingles2New) {
        if (textShingles1New == null || textShingles2New == null) return 0.0f;

        int textShingles1Number = textShingles1New.size();
        int textShingles2Number = textShingles2New.size();

        float similarShinglesNumber = 0;

        for (int i = 0; i < textShingles1Number; i++) {
            if (textShingles2New.contains(textShingles1New.get(i)))
                similarShinglesNumber += 1;
        }

        return ((similarShinglesNumber / ((textShingles1Number + textShingles2Number) / 2.0f)) * 100);
    }

    public static boolean containsTwise(List<String> list, String value) {
        int count = 0;
        for (String str: list) {
            if (str.equalsIgnoreCase(value)) {
                count++;
                if (count > 1)
                    return true;
            }
        }
        return false;
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

}
