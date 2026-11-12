package io.github.jamoamo.webjourney;

import io.github.jamoamo.webjourney.annotation.Retry;
import io.github.jamoamo.webjourney.annotation.form.Form;
import io.github.jamoamo.webjourney.annotation.form.TextField;

/**
 * A form that retries its completion, for tests.
 *
 * @author James Amoore
 */
@Form(submit = "//form[@name='scorecard_oracle_form']//table//tbody//input[@type='submit']")
@Retry(maxRetries = 2, delayMs = 1)
public class RetryingInputForm
{
	 @TextField(xPath = "//input[@name='match']")
	 private String matchId;

	 public RetryingInputForm(Integer testId)
	 {
		  this.matchId = "t" + testId;
	 }

	 public String getMatchId()
	 {
		  return matchId;
	 }

	 public void setMatchId(String matchId)
	 {
		  this.matchId = matchId;
	 }
}
