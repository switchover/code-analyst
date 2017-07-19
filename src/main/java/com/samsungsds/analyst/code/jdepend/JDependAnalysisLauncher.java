package com.samsungsds.analyst.code.jdepend;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.samsungsds.analyst.code.jdepend.framework.JDepend;
import com.samsungsds.analyst.code.jdepend.framework.JavaPackage;
import com.samsungsds.analyst.code.jdepend.framework.PackageComparator;
import com.samsungsds.analyst.code.main.MeasuredResult;

public class JDependAnalysisLauncher implements JDependAnalysis {
	private static final Logger LOGGER = LogManager.getLogger(JDependAnalysisLauncher.class);
	
	private final JDepend analyzer = new JDepend();
	private final NumberFormat formatter = NumberFormat.getInstance();
	private final List<String> includePackageList = new ArrayList<>();
	
	private String directory = null;
	
	public JDependAnalysisLauncher() {
		formatter.setMaximumFractionDigits(2);
	}
	
	@Override
	public void addIncludePackage(String packageName) {
		includePackageList.add(packageName);
	}
	
	@Override
	public void setTarget(String directory) {
		this.directory = directory;
	}

	@Override
	public void run() {
		
		try {
			analyzer.addDirectory(directory);
		} catch (IOException ioe) {
			throw new IllegalArgumentException(ioe);
		}
		
		Collection<JavaPackage> packages = analyzer.analyze();
		
		ArrayList<JavaPackage> packageList = new ArrayList<>(packages);

        Collections.sort(packageList, new PackageComparator(PackageComparator.byName()));
		
		ResultIncludeFilter filter = getResultIncludeFilter();
		
		checkResult(packageList, filter);
	}

	public ResultIncludeFilter getResultIncludeFilter() {
		ResultIncludeFilter filter = new ResultIncludeFilter();
		
		for (String packageName : includePackageList) {
			filter.addIncludePackage(packageName);
		}
		
		return filter;
	}

	public void checkResult(ArrayList<JavaPackage> packageList, ResultIncludeFilter filter) {
		List<JavaPackage> list = new ArrayList<>();
		
		LOGGER.info("Acyclic Dependencies:");
		for (JavaPackage jPackage : packageList) {
			if (!filter.include(jPackage.getName())) {
				continue;
			}
			
	        jPackage.collectCycle(list);

	        if (!jPackage.containsCycle()) {
	            continue;
	        }
	        
	        JavaPackage cyclePackage = (JavaPackage) list.get(list.size() - 1);
	        String cyclePackageName = cyclePackage.getName();

	        int i = 0;
	        
			StringBuilder print = new StringBuilder();
			
	        for (JavaPackage pkg : list) {
	            i++;

	            if (i == 1) {
	                print.append(pkg.getName());
	                LOGGER.info("{}", pkg.getName());
	                LOGGER.info("{}|", tab());
	            } else {
	                if (pkg.getName().equals(cyclePackageName)) {
	                    print.append(" |-> " + pkg.getName());
	                    LOGGER.info("{}|-> {}", tab(), pkg.getName());
	                } else {
	                	print.append(" | " + pkg.getName());
	                	LOGGER.info("{}|   {}", tab(), pkg.getName());
	                }
	            }
	        }
	        
	        MeasuredResult.getInstance().addAcyclicDependency(print.toString());
	        
	        list.clear();
        }
	}
	
	protected String tab() {
		return "    ";
	}
}