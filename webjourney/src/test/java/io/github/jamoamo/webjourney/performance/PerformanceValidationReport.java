/*
 * The MIT License
 *
 * Copyright 2024 James Amoore.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.jamoamo.webjourney.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Report containing validation results for performance test thresholds.
 *
 * @author James Amoore
 */
public class PerformanceValidationReport
{
    private final String testName;
    private final List<String> violations;
    private final List<String> warnings;
    
    public PerformanceValidationReport(String testName)
    {
        this.testName = testName;
        this.violations = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    /**
     * Adds a threshold violation to the report.
     *
     * @param violation Description of the violation
     */
    public void addViolation(String violation)
    {
        violations.add(violation);
    }
    
    /**
     * Adds a performance warning to the report.
     *
     * @param warning Description of the warning
     */
    public void addWarning(String warning)
    {
        warnings.add(warning);
    }
    
    /**
     * Checks if the validation passed (no violations).
     *
     * @return true if no violations detected, false otherwise
     */
    public boolean isPassed()
    {
        return violations.isEmpty();
    }
    
    /**
     * Gets the test name.
     *
     * @return Test name
     */
    public String getTestName()
    {
        return testName;
    }
    
    /**
     * Gets all threshold violations.
     *
     * @return List of violation descriptions
     */
    public List<String> getViolations()
    {
        return new ArrayList<>(violations);
    }
    
    /**
     * Gets all performance warnings.
     *
     * @return List of warning descriptions
     */
    public List<String> getWarnings()
    {
        return new ArrayList<>(warnings);
    }
    
    /**
     * Generates a formatted validation report.
     *
     * @return Formatted report string
     */
    public String generateReport()
    {
        StringBuilder report = new StringBuilder();
        report.append("=== Performance Validation Report ===\n");
        report.append("Test: ").append(testName).append("\n");
        report.append("Status: ").append(isPassed() ? "PASSED" : "FAILED").append("\n");
        
        if (!violations.isEmpty())
        {
            report.append("\nViolations:\n");
            for (String violation : violations)
            {
                report.append("  - ").append(violation).append("\n");
            }
        }
        
        if (!warnings.isEmpty())
        {
            report.append("\nWarnings:\n");
            for (String warning : warnings)
            {
                report.append("  - ").append(warning).append("\n");
            }
        }
        
        if (isPassed() && warnings.isEmpty())
        {
            report.append("\nAll performance thresholds met successfully.\n");
        }
        
        return report.toString();
    }
    
    @Override
    public String toString()
    {
        return "PerformanceValidationReport{" +
            "testName='" + testName + '\'' +
            ", passed=" + isPassed() +
            ", violations=" + violations.size() +
            ", warnings=" + warnings.size() +
            '}';
    }
} 