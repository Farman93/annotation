package com.farman.annotator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AnnotationTest {
    private static Set<String> categories = new HashSet<>();
    private static List<Page> pages;
    private static List<String> titles = new ArrayList<>();
    private static List<String> titlesTokenized = new ArrayList<>();
    private static Map<String, List<String>> aliases = new HashMap<>();

    public static void main(String[] args) {
        System.out.println(Calendar.getInstance().getTime());
        getCategories();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            AnnotationTest.PageHandler pageHandler = new AnnotationTest.PageHandler();
            File folder = new File(AnnotationTest.class.getClassLoader().getResource("wiki").getPath());
            for (final File fileEntry : folder.listFiles()) {
                File file = new File(AnnotationTest.class.getClassLoader().getResource("wiki").getPath() + "/" + fileEntry.getName());
                parser.parse(file, pageHandler);
                break;
            }
            int linksCount = 0;
            pages = pageHandler.getPages();
            System.out.println("pages.size() = " + pages.size());
            for (Page page: pages) {
                StringBuilder sb = new StringBuilder();
                String text = page.getText();
                try {
                    int lastEnd = 0;
                    int indStart = text.indexOf("[[");
                    while (indStart >= 0) {
                        int indEnd = text.indexOf("]]", indStart + 2);
                        String link = text.substring(indStart + 2, indEnd);
                        String[] linkParts = link.split("\\|");
                        if (titles.contains(linkParts[0].toLowerCase())) {
                            page.getLinks().add(new Link(linkParts[0], GeneralUtils.getTokenized(linkParts[0]), (indStart + 2) / text.length()));
                            linksCount++;
                        }
                        sb.append(text.substring(lastEnd, indStart));
                        sb.append(" ");
                        sb.append(linkParts[linkParts.length - 1]);
                        sb.append(" ");
                        indStart = text.indexOf("[[", indEnd + 2);
                        lastEnd = indEnd + 2;
                    }
                    if (lastEnd < text.length()) {
                        sb.append(text.substring(lastEnd));
                    }
                } catch (Exception e) {
                }
                page.setPlainText(GeneralUtils.getTokenized(sb.toString()));
            }
            System.out.println("linksCount = " + linksCount);
            for (String title: titles) {
                titlesTokenized.add(GeneralUtils.getTokenized(title));
            }
            List<Score> scores = new ArrayList<>();
            float weightedAvgPrecision = 0;
            float weightedAvgRecall = 0;
            int weightedPrecisionCount = 0;
            int weightedRecallCount = 0;
            for (int i = 0; i < pages.size(); i++) {
                Page page = pages.get(i);
                page.setAnnotations(Annotator.annotate(page.getPlainText(), titlesTokenized, pages.stream().map(page1 -> page1.getPlainText()).collect(Collectors.toList())));
                Score score = getScore(page);
                if (!Float.isNaN(score.getPrecision()) && score.getPrecisionCount() > 0) {
                    weightedAvgPrecision += score.getPrecision() * score.getPrecisionCount();
                    weightedPrecisionCount += score.getPrecisionCount();
                }
                if (!Float.isNaN(score.getRecall()) && score.getRecallCount() > 0) {
                    weightedAvgRecall += score.getRecall() * score.getRecallCount();
                    weightedRecallCount += score.getRecallCount();
                }
                if (i % 500 == 0 && i > 0) {
                    System.out.print(i + " ");
                }
                Collections.sort(pages.get(i).getAnnotations(), new Comparator<Annotator.Data>() {
                    @Override
                    public int compare(Annotator.Data o1, Annotator.Data o2) {
                        return o1.min - o2.min;
                    }
                });
                scores.add(score);
            }
            weightedAvgPrecision /= weightedPrecisionCount;
            weightedAvgRecall /= weightedRecallCount;

            System.out.println(String.format("Точность = %s, полнота = %s. ", weightedAvgPrecision, weightedAvgRecall));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static Score getScore(Page page) {
        int positive = page.getLinks().size();
        int selected = page.getAnnotations().size();
        int truePositive = 0;
        for (Link link: page.getLinks()) {
            for (Annotator.Data data: page.getAnnotations()) {
                if (titlesTokenized.get(data.titleInd).equals(link.getTitleTokenized())) {
                    truePositive++;
                }
            }
        }
        float precision = 1.0f * truePositive / selected;
        if (selected == 0) {
            precision = 0;
        }
        float recall = 1.0f * truePositive / positive;
        return new Score(precision, recall, positive, selected);
    }

    private static void getCategories() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File file = new File(AnnotationTest.class.getClassLoader().getResource("categories.xml").getPath());
            Document document = documentBuilder.parse(file);
            NodeList items = document.getElementsByTagName("a");
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                categories.add(item.getTextContent());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static class PageHandler extends DefaultHandler {

        private ArrayList<Page> pages = new ArrayList<>();
        private Page page;
        private StringBuilder stringBuilder;
        private boolean idSet = false;
        private int count = 0;

        PageHandler(){
            super();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            stringBuilder = new StringBuilder();

            if (qName.equals("page")){
                page = new Page();
                idSet = false;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            if (page != null){

                if (qName.equals("title")){
                    page.setTitle(stringBuilder.toString().toLowerCase());
                } else if (qName.equals("id")){
                    if (!idSet){

                        page.setId(Integer.parseInt(stringBuilder.toString()));
                        idSet = true;
                    }
                } else if (qName.equals("text")){

                    String articleText = stringBuilder.toString();
                    String newTitle = "";
                    if (articleText.indexOf("{{Перенаправление|") >= 0) {
                        int indexStart = articleText.indexOf("{{Перенаправление|") + 18;
                        newTitle = articleText.substring(indexStart, articleText.indexOf("}}", indexStart));
                        String currentTitle = GeneralUtils.getTokenized(page.getTitle());
                        aliases.putIfAbsent(currentTitle, new ArrayList<>());
                        aliases.get(currentTitle).add(GeneralUtils.getTokenized(newTitle));
                    }



                    if (articleText.indexOf("#перенаправление [[") >= 0) {
                        int indexStart = articleText.indexOf("#перенаправление [[") + 19;
                        newTitle = articleText.substring(indexStart, articleText.indexOf("]]", indexStart));
                        if (!newTitle.contains("|")) {
                            String currentTitle = GeneralUtils.getTokenized(page.getTitle());
                            newTitle = GeneralUtils.getTokenized(newTitle);
                            aliases.putIfAbsent(newTitle, new ArrayList<>());
                            aliases.get(newTitle).add(currentTitle);
                            if (articleText.indexOf("#перенаправление [[") < 5) {
                                page.setText(null);
                                return;
                            }
                        }
                    }
                    if (articleText.indexOf("#REDIRECT [[") >= 0) {
                        int indexStart = articleText.indexOf("#REDIRECT [[") + 12;
                        newTitle = articleText.substring(indexStart, articleText.indexOf("]]", indexStart));
                        if (!newTitle.contains("|")) {
                            String currentTitle = GeneralUtils.getTokenized(page.getTitle());
                            newTitle = GeneralUtils.getTokenized(newTitle);
                            aliases.putIfAbsent(newTitle, new ArrayList<>());
                            aliases.get(newTitle).add(currentTitle);
                            if (articleText.indexOf("#перенаправление [[") < 5) {
                                page.setText(null);
                                return;
                            }
                        }
                    }
                    boolean isMath = false;
                    int indStart = articleText.indexOf("[[Категория:");
                    try {
                        while (indStart >= 0) {
                            String category = articleText.substring(indStart + 12, articleText.indexOf("]]", indStart + 12));
                            if (categories.contains(category)) {
                                isMath = true;
                                break;
                            }
                            indStart = articleText.indexOf("[[Категория:", indStart + 12);
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                    }
                    if (!isMath) {
                        page.setText(null);
                        return;
                    }


                    articleText = articleText.replaceAll("(?s)<ref(.+?)</ref>", " "); //remove references
                    articleText = articleText.replaceAll("(?s)<math(.+?)</math>", " "); //remove references
                    articleText = articleText.replaceAll("(?s)\\{\\{(.+?)\\}\\}", " "); //remove links underneath headings
                    articleText = articleText.replaceAll("(?s)== См. также ==.+", " "); //remove everything after see also
                    articleText = articleText.replaceAll("(?s)== Литература ==.+", " "); //remove everything after see also
                    articleText = articleText.replaceAll("(?s)== Ссылки ==.+", " "); //remove everything after see also
                    articleText = articleText.replaceAll("(?s)== Примечания ==.+", " "); //remove everything after see also
                    articleText = articleText.replaceAll("\\n", " "); //remove new lines
                    articleText = articleText.replaceAll("́", ""); //remove all non alphanumeric except dashes and spaces
                    articleText = articleText.replaceAll("[^а-яёА-Яa-zA-Z0-9- \\s\\[\\]()—:|]", " "); //remove all non alphanumeric except dashes and spaces
                    articleText = articleText.trim().replaceAll(" +", " "); //convert all multiple spaces to 1 space

                    Pattern pattern = Pattern.compile("([\\S]+\\s*){1,75}"); //get first 75 words of text
                    Matcher matcher = pattern.matcher(articleText);
                    matcher.find();
                    page.setText(articleText);

                } else if (qName.equals("page")) {
                    count++;
                    if (page.getText() != null && !page.getTitle().startsWith("Категория:")) {
                        pages.add(page);
                        titles.add(page.getTitle());
                        if (pages.size() % 1000 == 0) {
                            System.out.println(pages.size() + " - " + titles.size() + ", aliases.size() = " + aliases.size() + "     " + count);
                        }
                    }
                    page = null;
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            stringBuilder.append(ch, start, length);
        }

        public ArrayList<Page> getPages() {
            return pages;
        }
    }


}
