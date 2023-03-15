package com.example.eventory.Logic;

import com.example.eventory.R;
import com.example.eventory.ViewAllActivity;
import com.example.eventory.adapters.TagAdapter;
import com.example.eventory.models.CardModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Filter extends ViewAllActivity {


    public void filter(String text) {
        filteredList.clear();

        for (CardModel event : allEvents)
            if (event.getName().toLowerCase().contains(text.toLowerCase()))
                filteredList.add(event);


        adapter.filterList(filteredList);
        setFounds();
    }

    public void filterByTag(String tag, boolean selected) {

        if (selected) {
            for (CardModel event : allEvents)
                if (event.getTags()!=null && event.getTags().contains(tag))
                    filteredList.add(event);

        } else {
            filteredList.removeIf(item -> item.getTags().contains(tag) &&
                    !TagAdapter.selected_tags.stream()
                            .anyMatch(selectedTag -> item.getTags().contains(selectedTag)));
        }

        adapter.filterList(filteredList);
        setFounds();
    }


    public void filterByTags(TagAdapter tagAdapter){
        filteredList.clear();
        if(!TagAdapter.selected_tags.isEmpty()) {
            for (String tag : TagAdapter.selected_tags) {
                filterByTag(tag, true);
            }
        }
        else reset();
    }

    public void filterByLocation(List<String> locations) {

        if (!locations.isEmpty()) {
            for (CardModel event : allEvents)
                if (!locations.contains(event.getLocation()))
                    filteredList.remove(event);

            adapter.filterList(filteredList);
            setFounds();
        }

    }
    public static ArrayList<CardModel> location(String location) {
        ArrayList<CardModel> events = new ArrayList<>();
            for (CardModel event : allEvents)
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

        adapter.filterList(filteredList);

        setFounds();
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


        adapter.filterList(filteredList);

        setFounds();
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

        adapter.filterList(filteredList);

        setFounds();
    }


    public static void setFounds(){
        int founds = adapter.getItemCount();
        if(founds == 0)
            found.setText(R.string.noResults);
        else if(founds == 1)
            found.setText(R.string.found);
        else found.setText(adapter.getItemCount() + " founds");

    }

    public void reset(){
        filteredList.clear();
        filteredList.addAll(allEvents);
        adapter.filterList(filteredList);
        setFounds();
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
