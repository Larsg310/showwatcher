package nl.larsgerrits.showwatcher.property;

import java.util.HashSet;
import java.util.Set;

public class Property<V>
{
    private Set<IPropertyChangeListener<V>> listeners = new HashSet<>();
    
    private V value;
    
    public Property(V defaultValue)
    {
        value = defaultValue;
    }
    
    public void set(V newValue)
    {
        listeners.forEach(l -> l.onValueChanged(value, newValue));
        this.value = newValue;
    }
    
    public V get()
    {
        return value;
    }
    
    public void addChangeListener(IPropertyChangeListener<V> listener)
    {
        listeners.add(listener);
    }
}
