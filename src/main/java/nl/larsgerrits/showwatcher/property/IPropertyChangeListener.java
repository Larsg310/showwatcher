package nl.larsgerrits.showwatcher.property;

@FunctionalInterface
public interface IPropertyChangeListener<V>
{
    void onValueChanged(V oldValue, V newValue);
}
