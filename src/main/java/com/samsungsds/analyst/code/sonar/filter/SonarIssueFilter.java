package com.samsungsds.analyst.code.sonar.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SonarIssueFilter {
    private static final Logger LOGGER = LogManager.getLogger(SonarIssueFilter.class);
    private final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();;

    private int excludedRules = 0;
    private int excludedJavaRules = 0;
    private int excludedJSRules = 0;

    public Set<String> parse(String ruleSetFileForSonar) {
        Set<String> filters = new HashSet<>();

        SonarIssueFilterHandler filterHandler;
        try {
            saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            filterHandler = new SonarIssueFilterHandler(filters);
            saxParser.parse(ruleSetFileForSonar, filterHandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("XML Parser Exception", e);
            throw new IllegalArgumentException(e);
        }

        excludedRules = filterHandler.getExcludedRules();
        excludedJavaRules = filterHandler.getExcludedJavaRules();
        excludedJSRules = filterHandler.getExcludedJSRules();

        return filters;
    }

    /**
     * @deprecated use getExcludedJavaRules() or getExcludedJSRules()
     */
    @Deprecated
    public int getExcludedRules() {
        return excludedRules;
    }

    public int getExcludedJavaRules() {
        return excludedJavaRules;
    }

    public int getExcludedJSRules() {
        return excludedJSRules;
    }
}