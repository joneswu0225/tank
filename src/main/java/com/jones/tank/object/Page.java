package com.jones.tank.object;

import com.jones.tank.entity.query.Query;
import lombok.Data;

import java.util.List;

@Data
public class Page<T> {
    private Long currentPage = Long.valueOf(1);
    private Long pageSize = Long.valueOf(20);
    private Long total;
    private List<T> content;

    public Page() {
    }

    public Page(Long currentPage, Long pageSize, Long totalNum, List<T> content) {
        currentPage = Long.valueOf(currentPage.intValue() < 1 ? 1 : currentPage.intValue());
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = totalNum;
        this.content = content;
    }

    public Page(Query query, Long totalNum, List<T> content) {
        this(Long.valueOf(query.getPage()), Long.valueOf(query.getSize()), totalNum, content);
    }

    public Page(Query query, Long totalNum) {
        this(Long.valueOf(query.getPage()), Long.valueOf(query.getSize()), totalNum, null);
    }

}

