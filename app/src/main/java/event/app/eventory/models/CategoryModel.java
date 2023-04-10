package event.app.eventory.models;

import java.util.List;

public class CategoryModel {

    private String category;
    private List<CardModel> cardModelList;

    public CategoryModel(String category, List<CardModel> cardModelList) {
        this.category = category;
        this.cardModelList = cardModelList;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<CardModel> getCardModelList() {
        return cardModelList;
    }

    public void setCardModelList(List<CardModel> cardModelList) {
        this.cardModelList = cardModelList;
    }
}
