package com.farman.annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class MappingTest {
    static List<String> queryString = new ArrayList<>();
    static List<String> not_tokenized = new ArrayList<>();
    static int docs = 0;
    static final String RUSSIAN = "russian";
    static final String ENGLISH = "english";
    static List<String> titlesEn = new ArrayList<>();
    static List<String> titlesRu = new ArrayList<>();
    static List<String> titlesRuTokenized = new ArrayList<>();

    public static void main(String args[]) {
        not_tokenized = getDocuments(ENGLISH, false);
        queryString = getDocuments(RUSSIAN, true);

        Set<String> titles = new HashSet<>();
        for (int i = 0; i < titlesEn.size(); i++)
            if (!not_tokenized.get(i).contains("#REDIRECT"))
                titles.add(Mapper.getTokenized2(titlesEn.get(i).toLowerCase()));
        for (String str: titlesRu)
            titlesRuTokenized.add(Mapper.getTokenized2(str));

        float [] parts = new float[]{/*0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, */0.7f, 0.8f, 0.9f, 0.99f};
        String result = "";
        for(float part: parts) {
            List<Integer> res = Mapper.map(queryString, not_tokenized, part);
            System.out.println(part);
            int containing = 0;
            int found = 0;
            int ignored = 0;
            for (int i = 0; i < queryString.size(); i++) {
                if (titles.contains(Mapper.getTokenized2(titlesRu.get(i).toLowerCase())) && !queryString.get(i).trim().startsWith("см.") && !GeneralUtils.containsTwise(titlesRuTokenized, Mapper.getTokenized2(titlesRu.get(i)))) {
                    containing++;
                    if (res.get(i) != -1) {
                        if (Mapper.getTokenized2(titlesRu.get(i)).equalsIgnoreCase(Mapper.getTokenized2(titlesEn.get(res.get(i))))) {
                            found++;
                        }
                    } else {
                        ignored++;
                    }
                }
            }
            System.out.println("Containing = " + containing);
            System.out.println("Found = " + found);
            System.out.println("Точность = " + 100.0f * found / (containing - ignored));
            System.out.println("Полнота = " + 100.0f * found / (containing));
            result += part + ";" +  (1.0f * found / (containing - ignored)) + ";" + (1.0f * found / (containing)) + "\n";
        }
        System.out.println(result);
    }

    public static List<String> getDocuments(String lang, boolean isQuery) {
        List<String> result = new ArrayList<>();
        try {
            String filePath = AnnotationTest.class.getClassLoader().getResource("math").getPath() + "/";
            if (lang.equals(RUSSIAN))
                filePath += "russianText.txt";
            else
                filePath += "englishTranslationSaved2.txt";

            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = in.readLine()) != null) {
                String [] parts = line.split(" --:-- ");
                if (lang.equals(ENGLISH))
                    titlesEn.add(parts[0]);
                else
                    titlesRu.add(parts[0]);
                result.add(parts[1]);
            }
            if (!isQuery) {
                docs = result.size();
                GeneralUtils.docs = result.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
