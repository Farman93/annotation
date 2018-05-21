package com.farman.annotator;

import java.util.*;

/**
 * Created by must on 25.02.2018.
 */
public class Annotator {

    public static class Data {
        public int min;
        public int max;
        public int found;
        public float score;
        public String title;
        public int titleInd;

        public Data(int min, int max, int found) {
            this.min = min;
            this.max = max;
            this.found = found;
        }
    }

    /**
     * Метод выполняющий сопоставление статей из списков queryString и not_tokenized
     *
     * @param text - текст для аннотирования (в токенизированном виде, токены соединены пробелами)
     *
     * @param titles - названия статей, которыми аннотируется текст
     *
     * @param definitionsTokenized - статьи, которыми аннотируется текст (в токенизированном виде, токены соединены пробелами)
     *
     * @return список ссылок на статьи с указанием номера слова в исходном токенизированном тексте
     * */

    public static List<Data> annotate(String text, List<String> titles, List<String> definitionsTokenized) {
        return annotate(text, titles, definitionsTokenized, "", new HashMap<>(), -0.1f, 1f, 20,
                35, 0, 1f, 0.01f, 150, 0.99f);
    }

    public static List<Data> annotate(String text, List<String> titles, List<String> definitionsTokenized, String currentTitle,
                                      Map<String, List<String>> aliases, float minScore, float minRatio, int deltaLeft, int deltaRight,
                                      int windowSize, float minRatioNear, float singleShingleCoefficient, int defLength, float notFullCoef) {
        String[] toks = text.split(" ");
        Map<Integer, List<Data>> wordToTitle = new HashMap<>();
        boolean isRedirected = false;
        int redirectCount = 0;
        int currentAliasId = 0;
        for (int titleId = 0; titleId < titles.size(); titleId++) {
            String title = titles.get(titleId);
            if (isRedirected) {
                if (currentAliasId < aliases.get(title).size()) {
                    title = aliases.get(title).get(currentAliasId);
                }
                redirectCount++;
                currentAliasId++;
                if (currentAliasId >= aliases.get(titles.get(titleId)).size()) {
                    isRedirected = false;
                    currentAliasId = 0;
                }
            } else if (aliases.get(title) != null) {
                isRedirected = true;
            }
            if (title == null || title.equalsIgnoreCase(currentTitle)) {
                isRedirected = false;
                continue;
            }
            List<List<Integer>> indexes = new ArrayList<>();
            for (String wordInTitle: title.split(" ")) {
                List<Integer> ind = new ArrayList<>();
                for (int i = 0; i < toks.length; i++) {
                    if (wordInTitle.equals(toks[i])) {
                        ind.add(i);
                    }
                }
                indexes.add(ind);
            }
            int window = indexes.size() + windowSize;
            List<Data> pretendents = new ArrayList<>();
            for (Integer ind: indexes.get(0)) {
                boolean found = false;
                if (indexes.size() > 1) {
                    for (Integer pretendent: indexes.get(1)) {
                        if (Math.abs(pretendent - ind) < window) {
                            pretendents.add(new Data(Math.min(ind, pretendent), Math.max(ind, pretendent), 2));
                            found = true;
                        }
                    }
                }
                if (!found) {
                    pretendents.add(new Data(ind, ind, 1));
                }
            }
            List<Integer> pretendentsAbsentCount = new ArrayList<>();
            for (int i = 0; i < pretendents.size(); i++) {
                pretendentsAbsentCount.add(2 - pretendents.get(i).found);
            }

            if (!pretendents.isEmpty()) {
                for (int i = 2; i < indexes.size(); i++) {
                    if (pretendents.isEmpty()) {
                        break;
                    }
                    List<Integer> currentIndexes = indexes.get(i);
                    for (int j = 0; j < pretendents.size(); j++) {
                        Data index = pretendents.get(j);
                        boolean isPresent = false;
                        for (Integer indexCurrent: currentIndexes) {
                            if (Math.abs(index.min - indexCurrent) <= window && Math.abs(index.max - indexCurrent) <= window) {
                                isPresent = true;
                                if (indexCurrent < index.min) {
                                    index.min = indexCurrent;
                                } else {
                                    if (indexCurrent > index.max) {
                                        index.max = indexCurrent;
                                    }
                                }
                                index.found++;
                                break;
                            }
                        }
                        if (!isPresent) {
                            pretendentsAbsentCount.set(j, pretendentsAbsentCount.get(j) + 1);
                        }
                    }
                }
            }
            List<Data> newPretendents = new ArrayList<>();
            for (int i = 0; i < pretendents.size(); i++) {
                if (pretendents.get(i).found >= title.split(" ").length) {
                    newPretendents.add(pretendents.get(i));
                }
            }
            for (Data pretendent: newPretendents) {
                int min = Math.max(pretendent.min - deltaLeft, 0);
                int max = Math.min(pretendent.max + deltaRight, toks.length - 1);
                String[] selectedToks = new String[max - min];
                System.arraycopy(toks, min, selectedToks, 0, pretendent.min - min);
                if (pretendent.max < max) {
                    System.arraycopy(toks, pretendent.max + 1, selectedToks, pretendent.min - min, max - pretendent.max);
                }
                String[] defPartsTmp = definitionsTokenized.get(titleId).split(" ");
                String[] defParts = Arrays.copyOfRange(defPartsTmp, 0, Math.min(defLength, defPartsTmp.length));
                float score = GeneralUtils.getScoresForTwoTexts(selectedToks, defParts);
                score += singleShingleCoefficient * GeneralUtils.getScoresForTwoTexts(selectedToks, defParts, 1);
                pretendent.score = score * (1 - notFullCoef * (title.split(" ").length - pretendent.found));
                pretendent.title = titles.get(titleId);
                pretendent.titleInd = titleId;
                for (int i = pretendent.min; i <= pretendent.max; i++) {
                    if (wordToTitle.get(i) != null) {
                        wordToTitle.get(i).add(pretendent);
                    } else {
                        wordToTitle.put(i, new ArrayList<>(Arrays.asList(pretendent)));
                    }
                }
            }
            if (isRedirected) {
                titleId--;
            }
        }
        if (currentTitle.equalsIgnoreCase("пустой множество")) {
            System.out.println("RedirectCount = " + redirectCount);
        }
        for (List<Data> values: wordToTitle.values()) {
            values.sort((o1, o2) -> {
                if (o1.score < o2.score) {
                    return 1;
                }
                if (o1.score > o2.score) {
                    return -1;
                }
                return 0;
            });
        }
        List<Data> annotations = new ArrayList<>();
        Set<String> foundTitles = new HashSet<>();
        for (Map.Entry<Integer, List<Data>> entry: wordToTitle.entrySet()) {
            Data data = entry.getValue().get(0);
            Data bestData = data;
            if (data.score > minScore) {
                if (entry.getValue().size() > 1) {
                    float currentRatio = data.score / entry.getValue().get(1).score;
                    for (int i = data.min; i <= data.max; i++) {
                        List<Data> near = wordToTitle.get(i);
                        if (near.size() > 1) {
                            if (bestData.found <= near.get(0).found && currentRatio < (near.get(0).score / near.get(1).score) && (near.get(0).score / near.get(1).score) > minRatio && bestData.score < minRatioNear * near.get(0).score) {
                                bestData = near.get(0);
                            }
                        } else {
                            if (bestData.found <= near.get(0).found && bestData.score < minRatioNear * near.get(0).score) {
                                bestData = near.get(0);
                            }
                        }
                    }
                    if ((bestData != data || currentRatio > minRatio) && !annotations.contains(bestData)) {
                        if (!foundTitles.contains(bestData.title)) {
                            annotations.add(bestData);
                            foundTitles.add(bestData.title);
                        }
                    }
                } else {
                    for (int i = data.min; i <= data.max; i++) {
                        List<Data> near = wordToTitle.get(i);
                        if (near.size() > 1) {
                            if (bestData.found <= near.get(0).found && (near.get(0).score / near.get(1).score) > minRatio && bestData.score < minRatioNear * near.get(0).score) {
                                bestData = near.get(0);
                            }
                        } else {
                            if (bestData.found <= near.get(0).found && bestData.score < minRatioNear * near.get(0).score) {
                                bestData = near.get(0);
                            }
                        }
                    }
                    if (bestData.found >= bestData.title.split(" ").length && !annotations.contains(bestData)) {
                        if (!foundTitles.contains(bestData.title)) {
                            annotations.add(bestData);
                            foundTitles.add(bestData.title);
                        }
                    }
                }
            }
        }

        return annotations;
    }
}
