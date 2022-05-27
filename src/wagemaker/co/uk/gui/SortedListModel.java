package wagemaker.co.uk.gui;

import java.text.Collator;
import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.AbstractListModel;


@SuppressWarnings("rawtypes")
public class SortedListModel extends AbstractListModel {

    private static final long serialVersionUID = 1L;

    private TreeSet model;

    public SortedListModel() {
        model = new TreeSet<Object>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                String str1 = o1.toString();
                String str2 = o2.toString();
                Collator collator = Collator.getInstance();
                int result = collator.compare(str1, str2);
                return result;
            }
        });
    }

    
    public int getSize() {
        return model.size();
    }


    public Object getElementAt(int index) {
        return model.toArray()[index];
    }


    @SuppressWarnings("unchecked")
	public void addElement(Object element) {
        if (model.add(element)) {
            fireContentsChanged(this, 0, getSize());
        }
    }


    public void clear() {
        model.clear();
        fireContentsChanged(this, 0, getSize());
    }


    public boolean contains(Object element) {
        return model.contains(element);
    }


    public boolean removeElement(Object element) {
        boolean removed = model.remove(element);
        if (removed) {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }
    
}