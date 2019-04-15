/*
Copyright 2018 Samsung SDS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.samsungsds.analyst.code.api.impl;

import com.samsungsds.analyst.code.api.*;
import com.samsungsds.analyst.code.main.App;
import com.samsungsds.analyst.code.main.CliParser;
import com.samsungsds.analyst.code.util.IOAndFileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class JavaScriptCodeAnalystImpl extends AbstractCodeAnalystImpl {
	private static final Logger LOGGER = LogManager.getLogger(JavaScriptCodeAnalystImpl.class);

	private String outputFilePath;

	@Override
	public String analyze(String where, ArgumentInfo argument, TargetFileInfo targetFile) {
		return analyze(where, argument, targetFile, false);
	}

	public String analyze(String where, ArgumentInfo argument, AbstractFileInfo targetFile, boolean withSeparatedOutput) {
		checkDirectoryAndArgument(where, argument);

		argument.getMode().setLanguageType(Language.JAVASCRIPT);

		String[] arguments = getArguments(where, argument, targetFile, withSeparatedOutput);

		System.out.println("* Arguments : " + getArgumentsString(arguments));

		if (withSeparatedOutput) {
			System.out.println(" - with separated output option");
		}

		CliParser cli = new CliParser(arguments, Language.JAVASCRIPT);

		cli.setInstanceKey(getUniqueId());

		LOGGER.info("Instance Key : {}", cli.getInstanceKey());

    	App app = new App();

    	for (ProgressObserver observer : observerList) {
    		app.addProgressObserver(observer);
    	}

    	try {
    		app.process(cli);
    	} catch (Throwable ex) {
    		LOGGER.error("Error", ex);
    		throw ex;
    	} finally {
    		app.cleanup(cli.getInstanceKey());
    	}

    	if (app.hasParsingError()) {
    		throw new RuntimeException(app.getParsingErrorMessage());
    	} else {
    		return outputFilePath;
    	}
	}

	@Override
	public ResultInfo analyzeWithSeparatedResult(String where, ArgumentInfo argument, TargetFileInfo targetFile) {
		String resultFile = analyze(where, argument, targetFile, true);

		ResultInfo info = new ResultInfo();
		info.setOutputFile(resultFile);

		String fileWithoutExt = IOAndFileUtils.getFilenameWithoutExt(new File(resultFile));

		info.setDuplicationFile(fileWithoutExt + "-duplication.json");
		info.setComplexityFile(fileWithoutExt + "-complexity.json");
		info.setSonarJsFile(fileWithoutExt + "-sonarjs.json");

		return info;
	}

	@Override
	public String analyzeWebResource(String where, WebArgumentInfo webArgument, WebTargetFileInfo webTargetFile, boolean includeCssAndHtml) {
		throw new UnsupportedOperationException("AnalyzeWebResource mode not supported in JavaScript language mode.");
	}

	private String[] getArguments(String where, ArgumentInfo argument, AbstractFileInfo targetFile, boolean withSeperatedOutput) {
		List<String> argumentList = new ArrayList<>();

		argumentList.add("--language");
		argumentList.add("javascript");
		
		argumentList.add("--project");
		argumentList.add(argument.getProject());

		argumentList.add("--src");
		argumentList.add(argument.getSrc());

		if (argument.isDebug()) {
			argumentList.add("--debug");
		}
		
		argumentList.add("--encoding");
		argumentList.add(argument.getEncoding());

		if (isValidated(argument.getSonarRuleFile())) {
			argumentList.add("-sonar");
			argumentList.add(argument.getSonarRuleFile());
		}
		
		argumentList.add("--output");
		argumentList.add(outputFilePath = getOutputFile(where, "json"));
		
		argumentList.add("--format");
		argumentList.add("json");
		
		argumentList.add("--timeout");
		argumentList.add(Integer.toString(argument.getTimeout()));

		String includeString;
		if (isValidated(argument.getInclude())) {
			includeString = String.join(",", argument.getInclude(), getIncludeString(targetFile));
		} else {
			includeString = getIncludeString(targetFile);
		}

		if (!includeString.equals("")) {
			argumentList.add("-include");
			argumentList.add(includeString);
		}
		
		if (isValidated(argument.getExclude())) {
			argumentList.add("-exclude");
			argumentList.add(argument.getExclude());
		}
		
		argumentList.add("--mode");
		argumentList.add(getModeParameter(argument.getMode()));
		
		if (withSeperatedOutput) {
			argumentList.add("-seperated");
		}

		if (argument.isDetailAnalysis()) {
			argumentList.add("--analysis");
		}

		if (argument.isSaveCatalog()) {
			argumentList.add("-catalog");
		}
		
		return argumentList.toArray(new String[0]);
	}
	
	private String getIncludeString(AbstractFileInfo targetFile) {
		StringBuilder builder = new StringBuilder();
		
		for (String file : targetFile.getFiles()) {
			if (builder.length() != 0) {
				builder.append(",");
			}
			builder.append(file);
		}
		
		System.out.println("* Target file patterns : " + builder.toString());
		
		return builder.toString();
	}

	private void checkDirectoryAndArgument(String where, ArgumentInfo argument) {
		File dir = new File(where);
		
		if (!dir.exists() || !dir.isDirectory()) {
			throw new IllegalArgumentException("Check target directory : " + where);
		} 
		
		if (isNotValidated(argument.getProject())) {
			throw new IllegalArgumentException("Project directory is needed...");
		}

		if (isNotValidated(argument.getSrc())) {
			throw new IllegalArgumentException("Source directory is needed...");
		}

		if (argument.getMode() == null) {
			throw new IllegalArgumentException("Analysis Mode is needed...");
		}
	}
	
	private String getModeParameter(AnalysisMode mode) {
		StringBuilder parameter = new StringBuilder();
		
		if (mode.isCodeSize()) {
			addAnalysisItem(parameter, "code-size");
		}
		
		if (mode.isDuplication()) {
			addAnalysisItem(parameter, "duplication");
		}
		
		if (mode.isComplexity()) {
			addAnalysisItem(parameter, "complexity");
		}
		
		return parameter.toString();
	}

}
