package event.app.eventory.Logic;

import android.util.Log;

import event.app.eventory.MapFragment;
import event.app.eventory.R;
import event.app.eventory.ViewAllActivity;
import event.app.eventory.adapters.TagAdapter;
import event.app.eventory.models.CardModel;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class Filter extends ViewAllActivity {

    public HashSet<CardModel> filteredList;

    public Filter(HashSet<CardModel> filteredList){
        this.filteredList = filteredList;
    }
   // public static HashSet<String> filtered_address_set = new HashSet<>(ContainerActivity.locations_set);
    //if adapte != null check


    public void filter(String text) {
        filteredList.clear();

        for (CardModel event : allEvents)
            if (event.getName().toLowerCase().contains(text.toLowerCase()))
                filteredList.add(event);


        if(adapter != null) {
            adapter.filterList(filteredList);
            setFounds();
        }
    }

    public void filterByTag(String tag, boolean selected, TagAdapter tagAdapter) {

        if (selected) {
            for (CardModel event : allEvents)
                if (event.getTags()!=null && event.getTags().contains(tag))
                    filteredList.add(event);

        } else {
            filteredList.removeIf(item -> item.getTags().contains(tag) &&
                    !tagAdapter.selected_tags.stream()
                            .anyMatch(selectedTag -> item.getTags().contains(selectedTag)));
        }

        if(adapter != null) {
            adapter.filterList(filteredList);
            setFounds();
        }
    }


    public void filterByTags(TagAdapter tagAdapter){
        filteredList.clear();
        if(!tagAdapter.selected_tags.isEmpty()) {
            for (String tag : tagAdapter.selected_tags) {
                filterByTag(tag, true, tagAdapter);
            }
        }
        else reset();
    }

    public void filterByLocations(List<String> locations) {

        if (!locations.isEmpty()) {
            for (CardModel event : allEvents)
                if (!locations.contains(event.getLocation()))
                    filteredList.remove(event);

            if(adapter != null) {
                adapter.filterList(filteredList);
                setFounds();
            }
        }

    }
    public static ArrayList<CardModel> filterByLocation(String location) {

        ArrayList<CardModel> events = new ArrayList<>();
            for (CardModel event : allEvents)
                if (location.equals(event.getLocation()))
                    events.add(event);

        return events;
    }

    public ArrayList<CardModel> location(String location) {

         if(filteredList.isEmpty()) filteredList.addAll(allEvents);
        ArrayList<CardModel> events = new ArrayList<>();
        for (CardModel event : filteredList)
            if (location.equals(event.getLocation()))
                events.add(event);

        return events;
    }




    public void filterByPrice(int startPrice, int endPrice){

        for (CardModel event : allEvents) {
            ArrayList<Integer> prices = event.getPrices();
            if (event.getPrices() != null)
                if (!prices.isEmpty())
                    if (!(prices.get(0) <= endPrice &&
                            prices.get(prices.size() - 1) >= startPrice))
                        filteredList.remove(event);

        }

        if(adapter != null) {
            adapter.filterList(filteredList);
            setFounds();
        }
    }

    //FIXME events with many dates that include the given date
    public void filterByDate(Date date){

        for (CardModel event : allEvents)
            if (event.getDates() != null) {
                boolean hasMatchingDate = false;
                for (Date eventDate: event.getDates()) {
                    if(reset_time(eventDate).equals(reset_time(date)))
                        hasMatchingDate = true;
                        break;
                }
                if (!hasMatchingDate) filteredList.remove(event);
            }


        if(adapter != null) {
            adapter.filterList(filteredList);
            setFounds();
        }
    }

    public void filterByDate(Date startDate, Date endDate) {

        for (CardModel event : allEvents) {
            boolean hasMatchingDate = false;

            if (event.getDates() != null) {
                for (Date date : event.getDates()) {
                    if (date.compareTo(startDate) >= 0 && reset_time(date).compareTo(endDate) <= 0) {
                        hasMatchingDate = true;
                        break;
                    }
                }
                if (!hasMatchingDate) filteredList.remove(event);
            }
        }

        if(adapter != null) {
            adapter.filterList(filteredList);
            setFounds();
        }
    }


    public void filterMarkers(String tag, boolean selected){
        for (Marker marker: MapFragment.markers) {
            Log.e("filter marker", marker.getTag()+"");
            if(marker.getTag().equals(tag)){
                if(selected) marker.setVisible(true);
                else marker.setVisible(false);

            }
        }
    }




    public void setFounds(){
        int founds;
        if(ViewAllActivity.filteredList == filteredList) {
            founds = adapter.getItemCount();
            if (founds == 0)
                found.setText(R.string.noResults);
            else if (founds == 1)
                found.setText(R.string.found);
            else found.setText(adapter.getItemCount() + " founds");
        }

    }

    public void reset(){
        filteredList.clear();
        filteredList.addAll(allEvents);
        if(adapter != null) {
            adapter.filterList(filteredList);
            setFounds();
        }
    }

    public Date reset_time(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        return date;
    }

     public Comparator<CardModel> priceComparator = new Comparator<CardModel>() {
        @Override
        public int compare(CardModel e1, CardModel e2) {
            if(e1.getPrices().isEmpty() && e2.getPrices().isEmpty()) return 0;
            if(e1.getPrices().isEmpty()) return -1;
            if(e2.getPrices().isEmpty() ) return 1;
            return e1.getPrices().get(0).compareTo(e2.getPrices().get(0));
        }
    };



}
