package com.samsungsds.analyst.code.main.result;

import static com.samsungsds.analyst.code.util.CSVUtil.*;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.samsungsds.analyst.code.findbugs.FindBugsResult;
import com.samsungsds.analyst.code.main.CliParser;
import com.samsungsds.analyst.code.main.MeasuredResult;
import com.samsungsds.analyst.code.main.detailed.Duplication;
import com.samsungsds.analyst.code.main.detailed.Inspection;
import com.samsungsds.analyst.code.main.detailed.MartinMetrics;
import com.samsungsds.analyst.code.pmd.ComplexityResult;
import com.samsungsds.analyst.code.pmd.PmdResult;
import com.samsungsds.analyst.code.sonar.DuplicationResult;

public class TextOutputFile extends AbstractOutputFile {
	MeasuredResult result;
	
	CSVSeperatedOutput csvOutput;

	@Override
	protected void open(MeasuredResult result) {
		this.result = result;
		
		csvOutput =  new CSVSeperatedOutput(result);
	}
	
	@Override
	protected void writeSeparator() {
		writer.println(";===============================================================================");
	}
	
	@Override
	protected void writeProjectInfo(CliParser cli, MeasuredResult result) {
		writer.println("[Project]");
		writer.println("Target = " + result.getProjectDirectory());
		writer.println("Source = " + cli.getSrc());
		writer.println("Binary = " + cli.getBinary());
		writer.println("Encoding = " + cli.getEncoding());
		writer.println("JavaVersion = " + cli.getJavaVersion());
		writer.println("Datetime = " + new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date()));
		if (cli.getRuleSetFileForPMD() != null && !cli.getRuleSetFileForPMD().equals("")) {
			writer.println("PMD = " + cli.getRuleSetFileForPMD());
		}
		if (cli.getRuleSetFileForFindBugs() != null && !cli.getRuleSetFileForFindBugs().equals("")) {
			writer.println("FindBugs = " + cli.getRuleSetFileForFindBugs());
		}
		if (!"".equals(cli.getIncludes())) {
			writer.println("includes = " + cli.getIncludes());
		}
		if (!"".equals(cli.getExcludes())) {
			writer.println("excludes = " + cli.getExcludes());
		}
		writer.println("mode = " + result.getIndividualModeString());
		if (result.isDetailAnalysis()) {
			writer.println("detailAnalysis = true");
		}
		if (result.isSeperatedOutput()) {
			writer.println("seperatedOutput = true");
		}
		writer.println("version = " + result.getVersion());
		writer.println("engineVersion = " + result.getEngineVersion());
		writer.println();
		writer.println();
	}

	@Override
	protected void writeSummary(MeasuredResult result) {
		writer.println("[Summary]");
		if (result.getIndividualMode().isCodeSize()) {
			writer.println("Files = " + result.getFiles());
			writer.println("Directories = " + result.getDirectories());
			writer.println("Classes = " + result.getClasses());
			writer.println("Functions = " + result.getFunctions());
			writer.println("lines = " + result.getLines());
			writer.println("CommentLines = " + result.getCommentLines());
			writer.println("Ncloc = " + result.getNcloc());
			writer.println("Statements = " + result.getStatements());
			writer.println();
		}
		if (result.getIndividualMode().isDuplication()) {
			writer.println("DuplicatedLines = " + result.getDuplicatedLines());
			writer.println();
		}
		if (result.getIndividualMode().isComplexity()) {
			writer.println("ComplexityFunctions = " + result.getComplexityFunctions());
			writer.println("ComplexityTotal = " + result.getComplexitySum());
			writer.println("ComplexityOver10 = " + result.getComplexityOver10());
			writer.println("ComplexityOver15 = " + result.getComplexityOver15());
			writer.println("ComplexityOver20 = " + result.getComplexityOver20());
			writer.println("ComplexityEqualOrOver50 = " + result.getComplexityEqualOrOver50());
			writer.println();
		}
		if (result.getIndividualMode().isPmd()) {
			writer.println("PMDViolations = " + result.getPmdCountAll());
			writer.println("PMD1Priority = " + result.getPmdCount(1));
			writer.println("PMD2Priority = " + result.getPmdCount(2));
			writer.println("PMD3Priority = " + result.getPmdCount(3));
			writer.println("PMD4Priority = " + result.getPmdCount(4));
			writer.println("PMD5Priority = " + result.getPmdCount(5));
			writer.println();
		}
		if (result.getIndividualMode().isFindBugs()) {
			writer.println("FindBugs = " + result.getFindBugsCountAll());
			writer.println("FindBugs1Priority = " + result.getFindBugsCount(1));
			writer.println("FindBugs2Priority = " + result.getFindBugsCount(2));
			writer.println("FindBugs3Priority = " + result.getFindBugsCount(3));
			writer.println("FindBugs4Priority = " + result.getFindBugsCount(4));
			writer.println("FindBugs5Priority = " + result.getFindBugsCount(5));
			writer.println();
		}
		if (result.getIndividualMode().isFindSecBugs()) {
			writer.println("FindSecBugs = " + result.getFindSecBugsCountAll());
			writer.println();
		}
		if (result.getIndividualMode().isDependency()) {
			writer.println("AcyclicDependecies = " + result.getAcyclicDependencyCount());
			writer.println();
		}
		writer.println("TechnicalDebt(Total) = " + result.getTechnicalDebt().getTotalDebt() + "MH");
		writer.println("TechnicalDebt(Duplication) = " + result.getTechnicalDebt().getDuplicationDebt() + "MH");
		writer.println("TechnicalDebt(Violation) = " + result.getTechnicalDebt().getViolationDebt() + "MH");
		writer.println("TechnicalDebt(Complexity) = " + result.getTechnicalDebt().getComplexityDebt() + "MH");
		writer.println("TechnicalDebt(AcyclicDependency) = " + result.getTechnicalDebt().getAcyclicDependencyDebt() + "MH");
		writer.println();
		writer.println();
	}

	@Override
	protected void writeDuplication(List<DuplicationResult> list) {
		if (result.isSeperatedOutput()) {
			csvOutput.writeDuplication(list);
		} else {
			writer.println("[Duplication]");
			writer.println("; path, start line, end line, duplicated path, duplicated start line, duplicated end line");
			
			int count = 0;
			synchronized (list) {
				for (DuplicationResult result : list) {
					writer.print(++count + " = ");
					writer.print(getStringsWithComma(result.getPath(), getString(result.getStartLine()), getString(result.getEndLine())));
					writer.print(",");
					writer.print(getStringsWithComma(result.getDuplicatedPath(), getString(result.getDuplicatedStartLine()), getString(result.getDuplicatedEndLine())));
					writer.println();
				}
			}
			writer.println();
			writer.println("total = " + count);
			writer.println();
			writer.println();
		}
		
		if (result.isDetailAnalysis()) {
			writeTopDuplication(result.getTopDuplicationList());
		}
	}

	private void writeTopDuplication(List<Duplication> topDuplicationList) {
		writer.println("[TopDuplication]");
		writer.println("; path, start line, end line, count, total duplicated lines");
		
		int count = 0;
		synchronized (topDuplicationList) {
			for (Duplication result : topDuplicationList) {
				writer.print(++count + " = ");
				writer.print(getStringsWithComma(result.getPath(), getString(result.getStartLine()), getString(result.getEndLine())));
				writer.print(",");
				writer.print(result.getCount());
				writer.print(",");
				writer.print(result.getTotalDuplicatedLines());
				writer.println();
			}
		}
		writer.println();
		writer.println("total = " + count);
		writer.println();
		writer.println();
	}

	@Override
	protected void writeComplexity(List<ComplexityResult> list) {
		if (result.isSeperatedOutput()) {
			csvOutput.writeComplexity(list);
			return;
		}
		
		writer.println("[Complexity]");
		writer.println("; path, line, method, complexity");
		writer.println("; only 20 over methods"); 

		int count = 0;
		synchronized (list) {
			Collections.sort(list, (r1, r2) -> (r2.getComplexity() - r1.getComplexity()));

			for (ComplexityResult result : list) {
				if (result.getComplexity() <= 20) {
					break;
				}
				writer.print(++count + " = ");
				writer.print(getStringsWithComma(result.getPath(), getString(result.getLine()), result.getMethodName(), getString(result.getComplexity())));
				writer.println();
			}
		}
		writer.println();
		writer.println("total = " + count);
		writer.println();
		writer.println();
	}

	@Override
	protected void writePmd(List<PmdResult> list) {
		if (result.isSeperatedOutput()) {
			csvOutput.writePmd(list);
		} else {
			writer.println("[PMD]");
			writer.println("; path, line, rule, priority, description");
			
			int count = 0;
			synchronized (list) {
				for (PmdResult result : list) {
					writer.print(++count + " = ");
					writer.print(getStringsWithComma(result.getPath(), getString(result.getLine()), result.getRule(), getString(result.getPriority()), result.getDescription()));
					writer.println();
				}
			}
			
			writer.println();
			writer.println("total = " + count);
			writer.println();
			writer.println();
		}
		
		if (result.isDetailAnalysis()) {
			writeTopInspection(result.getTopPmdList(), "TopPmdList");
		}
	}

	private void writeTopInspection(List<Inspection> topList, String topName) {
		writer.println("[" + topName + "]");
		writer.println("; rule, count");
		
		int count = 0;
		synchronized (topList) {
			for (Inspection result : topList) {
				writer.print(++count + " = ");
				writer.print(getStringsWithComma(result.getRule()));
				writer.print(",");
				writer.print(result.getCount());
				writer.println();
			}
		}
		writer.println();
		writer.println("total = " + count);
		writer.println();
		writer.println();
		
	}

	@Override
	protected void writeFindBugs(List<FindBugsResult> list) {
		writeFindBugsAndFindSecBugs(list, "FindBugs");
		
		if (result.isDetailAnalysis()) {
			writeTopInspection(result.getTopFindBugsList(), "TopFindBugsList");
		}
	}
	
	@Override
	protected void writeFindSecBugs(List<FindBugsResult> list) {
		writeFindBugsAndFindSecBugs(list, "FindSecBugs");
	}
	
	private void writeFindBugsAndFindSecBugs(List<FindBugsResult> list, String title) {
		if (result.isSeperatedOutput()) {
			csvOutput.writeFindBugsAndFindSecBugs(list, title);
			return;
		}
		writer.println("[" + title + "]");
		writer.println("; package, file, start line, end line, pattern key, pattern, priority, class, field, local var, method, message");
		
		int count = 0;
		synchronized (list) {
			for (FindBugsResult result : list) {
				writer.print(++count + " = ");
				writer.print(getStringsWithComma(result.getPackageName(), result.getFile(), getString(result.getStartLine()), getString(result.getEndLine()), result.getPatternKey(), result.getPattern()));
				writer.print(", ");
				writer.print(getStringsWithComma(getString(result.getPriority()), result.getClassName(), result.getField(), result.getLocalVariable(), result.getMethod(), result.getMessage()));
				writer.println();
			}
		}

		writer.println();
		writer.println("total = " + count);
		writer.println();
		writer.println();
	}

	@Override
	protected void writeAcyclicDependencies(List<String> list) {
		writer.println("[AcyclicDependencies]");
		
		int count = 0;
		synchronized (list) {
			for (String dependency : list) {
				writer.print(++count + " = ");
				writer.print(dependency);
				writer.println();
			}
		}
		
		writer.println();
		writer.println("total = " + count);
		writer.println();
		writer.println();
		
		if (result.isDetailAnalysis()) {
			writeTopMartinMetrics(result.getTopMartinMetrics());
		}
	}

	private void writeTopMartinMetrics(List<MartinMetrics> topMartinMetrics) {
		writer.println("[TopMartinMetrics]");
		writer.println("; package, Ca, Ce, A, I, D");
		
		int count = 0;
		synchronized (topMartinMetrics) {
			for (MartinMetrics result : topMartinMetrics) {
				writer.print(++count + " = ");
				writer.print(result.getPackageName());
				writer.print(",");
				writer.print(result.getAfferentCoupling());
				writer.print(",");
				writer.print(result.getEfferentCoupling());
				writer.print(",");
				writer.print(result.getAbstractness());
				writer.print(",");
				writer.print(result.getInstability());
				writer.print(",");
				writer.print(result.getDistance());
				writer.println();
			}
		}
		writer.println();
		writer.println("total = " + count);
		writer.println();
		writer.println();
	}

	@Override
	protected void close(PrintWriter writer) {
		// no-op
	}
}
