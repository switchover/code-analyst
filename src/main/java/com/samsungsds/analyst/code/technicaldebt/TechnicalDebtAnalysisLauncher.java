package com.samsungsds.analyst.code.technicaldebt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.samsungsds.analyst.code.findbugs.FindBugsResult;
import com.samsungsds.analyst.code.main.MeasuredResult;
import com.samsungsds.analyst.code.pmd.PmdResult;
import com.samsungsds.analyst.code.util.IOAndFileUtils;

public class TechnicalDebtAnalysisLauncher implements TechnicalDebtAnalysis {

	private static final Logger LOGGER = LogManager.getLogger(TechnicalDebtAnalysisLauncher.class);

	private static final double COST_TO_FIX_ONE_BLOCK = 2;
	private static final double COST_TO_FIX_ONE_VULNERABILITY_ISSUE = 0.62;
	private static final double COST_TO_SPLIT_A_METHOD = 1;
	private static final double COST_TO_CUT_AN_EDGE_BETWEEN_TWO_FILES = 4;

	private static final String PMD_EFFORT_XML_FILE = "/statics/PmdEffort.xml";
	private static final String FINDBUGS_EFFORT_XML_FILE = "/statics/FindBugsEffort.xml";

	private static Map<String, Double> pmdEffortMap = new HashMap<>();
	private static Map<String, Double> findBugsEffortMap = new HashMap<>();

	private final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();;
	private MeasuredResult measuredResult;
	private double technicalDebt;
	private double duplicationDebt;
	private double violationDebt;
	private double complexityDebt;
	private double acyclicDependencyDebt;

	@Override
	public void run(String instanceKey) {
		measuredResult = MeasuredResult.getInstance(instanceKey);
		calculateTechnicalDebt();
		measuredResult.setTechnicalDebtResult(new TechnicalDebtResult(technicalDebt, duplicationDebt, violationDebt, complexityDebt, acyclicDependencyDebt));
	}

	private void calculateTechnicalDebt() {
		calculateDuplicationDebt();
		calculateViolationDebt();
		calculateComplexityDebt();
		calculateAcyclicDependencyDebt();
		calculateTotalDebt();
	}

	private void calculateDuplicationDebt() {
		duplicationDebt = roundDecimal(measuredResult.getDulicationList().size() * COST_TO_FIX_ONE_BLOCK);
		LOGGER.info("TechnicalDebt(duplication): " + duplicationDebt);
	}

	private void calculateViolationDebt() {
		effortXMLParse();
		violationDebt += calculatePmdDebt();
		violationDebt += calculateFindBugsDebt();
		violationDebt += calculateFindSecBugsDebt();
		LOGGER.info("TechnicalDebt(violation): " + violationDebt);
	}

	private void effortXMLParse() {
		DebtXMLParserHandler debtXMLParserHandler;
		try {
			saxParserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			SAXParser saxParser = saxParserFactory.newSAXParser();
			debtXMLParserHandler = new DebtXMLParserHandler(pmdEffortMap);
			saxParser.parse(IOAndFileUtils.saveResourceFile(PMD_EFFORT_XML_FILE, "pmd_effort", ".xml"), debtXMLParserHandler);
			debtXMLParserHandler = new DebtXMLParserHandler(findBugsEffortMap);
			saxParser.parse(IOAndFileUtils.saveResourceFile(FINDBUGS_EFFORT_XML_FILE, "findbugs_effort", ".xml"), debtXMLParserHandler);
		} catch (ParserConfigurationException e) {
			LOGGER.info("Parser Configuration Exception", e);
		} catch (SAXException e) {
			LOGGER.info("SAX Exception", e);
		} catch (IOException e) {
			LOGGER.info("IO Exception", e);
		}
	}

	private double calculatePmdDebt() {
		double result = 0;
		for (PmdResult pmdResult : measuredResult.getPmdList()) {
			result += pmdEffortMap.get(pmdResult.getRule());
		}
		return roundDecimal(result);
	}

	private double calculateFindBugsDebt() {
		double result = 0;
		for (FindBugsResult findBugsResult : measuredResult.getFindBugsList()) {
			result += findBugsEffortMap.get(findBugsResult.getPatternKey());
		}
		return roundDecimal(result);
	}

	private double calculateFindSecBugsDebt() {
		return roundDecimal(measuredResult.getFindSecBugsCountAll() * COST_TO_FIX_ONE_VULNERABILITY_ISSUE);
	}

	private void calculateComplexityDebt() {
		complexityDebt = roundDecimal(measuredResult.getComplexityOver20() * COST_TO_SPLIT_A_METHOD);
		LOGGER.info("TechnicalDebt(complexity): " + complexityDebt);
	}

	private void calculateAcyclicDependencyDebt() {
		acyclicDependencyDebt = roundDecimal(measuredResult.getAcyclicDependencyCount() * COST_TO_CUT_AN_EDGE_BETWEEN_TWO_FILES);
		LOGGER.info("TechnicalDebt(acyclicDependency): " + acyclicDependencyDebt);
	}

	private void calculateTotalDebt() {
		technicalDebt += duplicationDebt;
		technicalDebt += violationDebt;
		technicalDebt += complexityDebt;
		technicalDebt += acyclicDependencyDebt;
		technicalDebt = roundDecimal(technicalDebt);
		LOGGER.debug("TechnicalDebt(total): " + technicalDebt);
	}

	private double roundDecimal(double decimal) {
		DecimalFormat decimalFormat = new DecimalFormat("0.##");
		return Double.parseDouble(decimalFormat.format(decimal));
	}

}
