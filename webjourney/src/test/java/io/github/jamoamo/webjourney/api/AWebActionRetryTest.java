package io.github.jamoamo.webjourney.api;

import io.github.jamoamo.webjourney.ActionResult;
import io.github.jamoamo.webjourney.BaseJourneyActionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

public class AWebActionRetryTest
{

    @Test
    public void testExecuteAction_SuccessFirstAttempt() throws Exception
    {
        TestAction action = spy(new TestAction(true));
        IJourneyContext context = mock(IJourneyContext.class);
        when(context.getActionRetryPolicy()).thenReturn(new ActionRetryPolicy());

        ActionResult result = action.executeAction(context);

        Assertions.assertEquals(ActionResult.SUCCESS, result);
        verify(action, times(1)).executeActionImpl(context);
    }

    @Test
    public void testExecuteAction_RetrySuccess() throws Exception
    {
        TestAction action = spy(new TestAction(false, true)); // Fails once, then succeeds
        IJourneyContext context = mock(IJourneyContext.class);
        ActionRetryPolicy policy = new ActionRetryPolicy(1, 0, Collections.emptyList());
        when(context.getActionRetryPolicy()).thenReturn(policy);

        ActionResult result = action.executeAction(context);

        Assertions.assertEquals(ActionResult.SUCCESS, result);
        verify(action, times(2)).executeActionImpl(context);
    }

    @Test
    public void testExecuteAction_RetryFail() throws Exception
    {
        TestAction action = spy(new TestAction(false, false, false)); // Fails 3 times
        IJourneyContext context = mock(IJourneyContext.class);
        ActionRetryPolicy policy = new ActionRetryPolicy(1, 0, Collections.emptyList());
        when(context.getActionRetryPolicy()).thenReturn(policy);

        Assertions.assertThrows(BaseJourneyActionException.class, () ->
        {
            action.executeAction(context);
        });

        verify(action, times(2)).executeActionImpl(context); // 1 initial + 1 retry
    }

    @Test
    public void testExecuteAction_NoRetryPolicy() throws Exception
    {
        TestAction action = spy(new TestAction(false));
        IJourneyContext context = mock(IJourneyContext.class);
        when(context.getActionRetryPolicy()).thenReturn(null);

        Assertions.assertThrows(BaseJourneyActionException.class, () ->
        {
            action.executeAction(context);
        });

        verify(action, times(1)).executeActionImpl(context);
    }

    @Test
    public void testExecuteAction_RetryableException_Matches() throws Exception
    {
        // Policy retries on BaseJourneyActionException
        ActionRetryPolicy policy = new ActionRetryPolicy(1, 0, List.of(BaseJourneyActionException.class));

        // Action throws BaseJourneyActionException on first attempt, succeeds on second
        TestAction action = spy(new TestAction(false, true));

        IJourneyContext context = mock(IJourneyContext.class);
        when(context.getActionRetryPolicy()).thenReturn(policy);

        ActionResult result = action.executeAction(context);

        Assertions.assertEquals(ActionResult.SUCCESS, result);
        verify(action, times(2)).executeActionImpl(context);
    }

    @Test
    public void testExecuteAction_RetryableException_NoMatch() throws Exception
    {
        // Policy retries ONLY on IllegalArgumentException
        ActionRetryPolicy policy = new ActionRetryPolicy(1, 0, List.of(IllegalArgumentException.class));

        // Action throws BaseJourneyActionException (which is NOT IllegalArgumentException)
        TestAction action = spy(new TestAction(false, true));

        IJourneyContext context = mock(IJourneyContext.class);
        when(context.getActionRetryPolicy()).thenReturn(policy);

        // Should throw immediately on first failure because exception type doesn't match policy
        Assertions.assertThrows(BaseJourneyActionException.class, () ->
        {
            action.executeAction(context);
        });

        verify(action, times(1)).executeActionImpl(context);
    }

    @Test
    public void testExecuteAction_RuntimeException_Retry() throws Exception
    {
        // Policy retries on RuntimeException
        ActionRetryPolicy policy = new ActionRetryPolicy(1, 0, List.of(RuntimeException.class));

        // Custom action that throws RuntimeException
        AWebAction action = spy(new RuntimeExceptionAction());

        IJourneyContext context = mock(IJourneyContext.class);
        when(context.getActionRetryPolicy()).thenReturn(policy);

        // Should fail eventually but retry once
        Assertions.assertThrows(RuntimeException.class, () ->
        {
            action.executeAction(context);
        });

        verify(action, times(2)).executeActionImpl(context);
    }

    static class TestAction extends AWebAction
    {
        private final boolean[] outcomes;
        private int attempt = 0;

        TestAction(boolean... outcomes)
        {
            this.outcomes = outcomes;
        }

        @Override
        public ActionResult executeActionImpl(IJourneyContext context) throws BaseJourneyActionException
        {
            if (attempt >= outcomes.length)
            {
                throw new BaseJourneyActionException("Failed (out of outcomes)", this, new RuntimeException("Simulated failure"));
            }
            boolean success = outcomes[attempt++];
            if (success)
            {
                return ActionResult.SUCCESS;
            }
            else
            {
                throw new BaseJourneyActionException("Failed", this, new RuntimeException("Simulated failure"));
            }
        }

        @Override
        protected String getActionName()
        {
            return "Test Action";
        }
    }

    static class RuntimeExceptionAction extends AWebAction
    {
        @Override
        public ActionResult executeActionImpl(IJourneyContext context)
        {
            throw new RuntimeException("Simulated Runtime Exception");
        }

        @Override
        protected String getActionName()
        {
            return "Runtime Exception Action";
        }
    }
}
