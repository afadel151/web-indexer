package com.emp.web_indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emp.web_indexer.models.HtmlToken;

public class HtmlTokenizer {

    private final String delimiters = " <>#=,.'':;!?-_()[]{}\"\\/\n\r\t";
    private final Set<String> textTags;

    public HtmlTokenizer() {
        // Define which tags should have their text content tokenized
        textTags = new HashSet<>();
        textTags.add("title");
        textTags.add("h1");
        textTags.add("h2");
        textTags.add("h3");
        textTags.add("h4");
        textTags.add("h5");
        textTags.add("h6");
        textTags.add("p");
        textTags.add("td");
        textTags.add("th");
        textTags.add("div");
        textTags.add("span");
        textTags.add("a");
        textTags.add("li");
        textTags.add("ul");
        textTags.add("ol");
        textTags.add("strong");
        textTags.add("em");
        textTags.add("b");
        textTags.add("i");
        textTags.add("blockquote");
        textTags.add("code");
        textTags.add("pre");
    }

    public List<HtmlToken> tokenize(String text) {
        boolean insideTag = false;
        boolean insideTextTag = false;
        String currentTag = null;
        StringBuilder tagBuffer = new StringBuilder();

        List<HtmlToken> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int position = 0;

        // rm script and style
        text = text.replaceAll("(?is)<script.*?>.*?</script>", " ");
        text = text.replaceAll("(?is)<style.*?>.*?</style>", " ");

        for (char c : text.toCharArray()) {
            if (c == '<') {
                if (current.length() > 0 && insideTextTag) 
                {
                    tokens.add(new HtmlToken(position++, currentTag, current.toString().toLowerCase()));
                    current.setLength(0);
                }
                insideTag = true;
                tagBuffer.setLength(0);
                continue;
            }

            if (c == '>') {
                insideTag = false;
                String rawTag = tagBuffer.toString().trim();

                // check  closing tag
                if (rawTag.startsWith("/")) {
                    String closingTagName = rawTag.substring(1).trim().toLowerCase();
                    if (textTags.contains(closingTagName) && closingTagName.equals(currentTag)) {
                        insideTextTag = false;
                        currentTag = null;
                    }
                } else {
                    // extract tag name )
                    int spaceIndex = rawTag.indexOf(' ');
                    String tagName;
                    if (spaceIndex > 0) {
                        tagName = rawTag.substring(0, spaceIndex).toLowerCase();
                    } else {
                        tagName = rawTag.toLowerCase();
                    }

                    // check self-closing tag
                    boolean selfClosing = tagName.endsWith("/")
                            || rawTag.endsWith("/")
                            || tagName.equals("img")
                            || tagName.equals("br")
                            || tagName.equals("hr")
                            || tagName.equals("meta")
                            || tagName.equals("link")
                            || tagName.equals("input");

                    if (selfClosing) {
                        if (tagName.endsWith("/")) {
                            tagName = tagName.substring(0, tagName.length() - 1);
                        }
                        insideTextTag = false;
                        currentTag = null;
                    } else if (textTags.contains(tagName)) {
                        insideTextTag = true;
                        currentTag = tagName;
                    } else {
                        insideTextTag = false;
                        currentTag = null;
                    }
                }
                continue;
            }

            if (insideTag) {
                tagBuffer.append(c);
                continue;
            }

            if (insideTextTag) {
                if (isDelimiter(c)) {
                    if (current.length() > 0) {
                        tokens.add(new HtmlToken(position++, currentTag, current.toString().toLowerCase()));
                        current.setLength(0);
                    }
                } else {
                    current.append(c);
                }
            }
        }

        if (current.length() > 0 && insideTextTag) {
            tokens.add(new HtmlToken(position, currentTag, current.toString().toLowerCase()));
        }

        return tokens;
    }

    private boolean isDelimiter(char c) {
        return delimiters.indexOf(c) != -1;
    }

    public void addTextTag(String tagName) {
        textTags.add(tagName.toLowerCase());
    }

    public boolean isTextTag(String tagName) {
        return textTags.contains(tagName.toLowerCase());
    }
}
