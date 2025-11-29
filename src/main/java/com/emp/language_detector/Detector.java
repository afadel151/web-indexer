package com.emp.language_detector;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;

public class Detector {

    //load all languages
    TextObjectFactory textObjectFactory;
    LanguageDetector languageDetector;

    public Detector() {
        try {
            List<LdLocale> languages = Arrays.asList(LdLocale.fromString("fr"), LdLocale.fromString("en"));
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readBuiltIn(languages);

            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                    .withProfiles(languageProfiles)
                    .build();

            textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
        } catch (IOException e) {
                System.out.println(e.getMessage());
        }
    }

    public String detect(String input) {
        TextObject textObject = textObjectFactory.forText(input);
        Optional<LdLocale> lang = languageDetector.detect(textObject);
        return lang.isPresent() ? lang.get().getLanguage() : "x";
    }
}
