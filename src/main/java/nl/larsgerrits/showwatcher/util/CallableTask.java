package nl.larsgerrits.showwatcher.util;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;

public class CallableTask<T> extends Task<T>
{
    private final Callable<T> callable;
    
    public CallableTask(Callable<T> callable)
    {
        this.callable = callable;
    }
    
    @Override
    protected T call() throws Exception
    {
        return callable.call();
    }
}
