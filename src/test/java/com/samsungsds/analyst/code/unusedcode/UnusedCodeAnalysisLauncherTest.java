package com.samsungsds.analyst.code.unusedcode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.samsungsds.analyst.code.main.MeasuredResult;
import com.samsungsds.analyst.code.unusedcode.type.CAMethod;

public class UnusedCodeAnalysisLauncherTest {
	
	private static String testInstanceKey = "0";
	
	private UnusedCodeAnalysisLauncher unusedCodeAnalysisLauncher;
	
	private MeasuredResult measuredResult;
	
	@Before	public void 
	setUp() throws IOException {
		unusedCodeAnalysisLauncher = spy(new UnusedCodeAnalysisLauncher());
		
		setOptionsForTest();
		setMeasuredResultForTest();
	}
	
	private void 
	setOptionsForTest() {
		
	}
	
	private void
	setMeasuredResultForTest() throws IOException {
		testInstanceKey = Integer.toString(Integer.parseInt(testInstanceKey)+1);//테스트 메서드를 수행할때마다 다른 instance 키를 생성한다.
		measuredResult = MeasuredResult.getInstance(testInstanceKey);
		measuredResult.setProjectDirectory(new File(".").getCanonicalPath());
		measuredResult.initialize(false, false);
	}
	
	@Test public void
	should_detect_2_unused_fields_when_launcher_analysis_UClass() {
		String src = "./src";
		String binary = "./target/classes/com/samsungsds/analyst/code/test/UClass.class";
		
		unusedCodeAnalysisLauncher.setTargetSrc(src);
		unusedCodeAnalysisLauncher.setTargetBinary(binary);
		
		unusedCodeAnalysisLauncher.run(testInstanceKey);
		
		int actualUnusedFieldCount = 0;
		List<UnusedCodeResult> unusedCodeList = measuredResult.getUnusedCodeList();
		for (UnusedCodeResult unusedCodeResult : unusedCodeList) {
			if(unusedCodeResult.getType().equals(UnusedCodeAnalysisLauncher.UNUSED_CODE_TYPE_FIELD)) {
				actualUnusedFieldCount++;
			}
		}
		
		assertThat(actualUnusedFieldCount, is(2));
	}
	
	@Test public void
	should_detect_2_unused_methods_when_launcher_analysis_UClass() {
		String src = "./src";
		String binary = "./target/classes/com/samsungsds/analyst/code/test/UClass.class";
		
		unusedCodeAnalysisLauncher.setTargetSrc(src);
		unusedCodeAnalysisLauncher.setTargetBinary(binary);
		
		unusedCodeAnalysisLauncher.run(testInstanceKey);
		
		int actualUnusedMethodCount = 0;
		List<UnusedCodeResult> unusedCodeList = measuredResult.getUnusedCodeList();
		for (UnusedCodeResult unusedCodeResult : unusedCodeList) {
			if(unusedCodeResult.getType().equals(UnusedCodeAnalysisLauncher.UNUSED_CODE_TYPE_METHOD)) {
				actualUnusedMethodCount++;
			}
		}
		
		assertThat(actualUnusedMethodCount, is(3));
	}
	
	@Test public void
	should_detect_1_unused_constant_when_launcher_analysis_UClass() {
		String src = "./src";
		String binary = "./target/classes/com/samsungsds/analyst/code/test/UClass.class";
		
		unusedCodeAnalysisLauncher.setTargetSrc(src);
		unusedCodeAnalysisLauncher.setTargetBinary(binary);
		
		unusedCodeAnalysisLauncher.run(testInstanceKey);
		
		int actualUnusedConstantCount = 0;
		List<UnusedCodeResult> unusedCodeList = measuredResult.getUnusedCodeList();
		for (UnusedCodeResult unusedCodeResult : unusedCodeList) {
			if(unusedCodeResult.getType().equals(UnusedCodeAnalysisLauncher.UNUSED_CODE_TYPE_CONSTANT)) {
				actualUnusedConstantCount++;
			}
		}
		
		assertThat(actualUnusedConstantCount, is(1));
	}
	
	@Test public void
	sholud_detect_1_unused_class_when_launcher_analysis_unusedcode_package(){
		String src = "./src";
		String binary = "./target/classes/com/samsungsds/analyst/code/unusedcode";
		
		unusedCodeAnalysisLauncher.setTargetSrc(src);
		unusedCodeAnalysisLauncher.setTargetBinary(binary);
		
		unusedCodeAnalysisLauncher.run(testInstanceKey);
		
		int actualUnusedClassCount = 0;
		List<UnusedCodeResult> unusedCodeList = measuredResult.getUnusedCodeList();
		for (UnusedCodeResult unusedCodeResult : unusedCodeList) {
			if(unusedCodeResult.getType().equals(UnusedCodeAnalysisLauncher.UNUSED_CODE_TYPE_CLASS)) {
				actualUnusedClassCount++;
			}
		}
		
		assertThat(actualUnusedClassCount, is(1));
	}
	
	@Test public void
	should_detect_1_unused_field_when_lancher_analysis_unusedcode_package_with_filter() {
		String src = "./src";
		String binary = "./target/classes/com/samsungsds/analyst/code/unusedcode";
		
		unusedCodeAnalysisLauncher.setTargetSrc(src);
		unusedCodeAnalysisLauncher.setTargetBinary(binary);
		
		measuredResult.setExcludeFilters("*ClassVisitor*");

		unusedCodeAnalysisLauncher.run(testInstanceKey);
		
		int actualUnusedCount = 0;
		List<UnusedCodeResult> unusedCodeList = measuredResult.getUnusedCodeList();
		for (UnusedCodeResult unusedCodeResult : unusedCodeList) {
			if(unusedCodeResult.getType().equals(UnusedCodeAnalysisLauncher.UNUSED_CODE_TYPE_CONSTANT)) {
				actualUnusedCount++;
			}
		}
		
		assertThat(actualUnusedCount, is(1));
	}
	
	@Test public void
	should_parsed_in_int_and_string_parameterTypes_when_parse_desc(){
		String desc = "(ILjava/lang/String;)V";
		
		CAMethod method = new CAMethod();
		method.setDesc(desc);
		
		String[] expected = {"int", "java.lang.String"};
		assertThat(method.getParameterTypes(), is(expected));
	}
}
