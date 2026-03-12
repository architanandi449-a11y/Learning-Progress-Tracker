package learningtracker;

import java.util.*;

public class DataChangeNotifier {
    private static final List<DataChangeListener> listeners = new ArrayList<>();
    
    public static void addListener(DataChangeListener listener) {
        listeners.add(listener);
    }
    
    public static void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }
    
    public static void notifyDataChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }
    
    public interface DataChangeListener {
        void onDataChanged();
    }
}
