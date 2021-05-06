package ru.komiss77.utils.inventory;

import java.util.Arrays;

public class Pagination {
    
    private int currentPage;
    private ClickableItem[] items;
    private int entriesPerPage;
    
    public Pagination() {
        this.items = new ClickableItem[0];
        this.entriesPerPage = 5;
    }
    
    public ClickableItem[] getPageItems() {
        return Arrays.copyOfRange(this.items, this.currentPage * this.entriesPerPage, (this.currentPage + 1) * this.entriesPerPage);
    }
    
    public int getPage() {
        return this.currentPage;
    }
    
    public Pagination page(final int page) {
        this.currentPage = page;
        return this;
    }
    
    public boolean isFirst() {
        return this.currentPage == 0;
    }
    
    public boolean isLast() {
        return this.currentPage >= (int)Math.ceil(this.items.length / (double)this.entriesPerPage) - 1;
    }
    
    public Pagination first() {
        this.currentPage = 0;
        return this;
    }
    
    public Pagination previous() {
        if (!this.isFirst()) {
            --this.currentPage;
        }
        return this;
    }
    
    public Pagination next() {
        if (!this.isLast()) {
            ++this.currentPage;
        }
        return this;
    }
    
    public Pagination last() {
        this.currentPage = this.items.length / this.entriesPerPage;
        return this;
    }
    
    public Pagination addToIterator(final SlotIterator iterator) {
        final ClickableItem[] pageItems = this.getPageItems();
        for (int length = pageItems.length, i = 0; i < length; ++i) {
            iterator.next().set(pageItems[i]);
            if (iterator.ended()) {
                break;
            }
        }
        return this;
    }
    
    public Pagination setItems(final ClickableItem... items) {
        this.items = items;
        return this;
    }
    
    public Pagination setItemsPerPage(final int entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
        return this;
    }
}
