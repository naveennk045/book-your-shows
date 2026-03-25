package org.bookyourshows.dto.seat;

import java.util.ArrayList;
import java.util.List;

public class SeatCreateRequest {

    private Integer rowNo;
    private List<SeatSegments> segments;

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public List<SeatSegments> getSegments() {
        return new ArrayList<>(segments);
    }

    public void setSegments(List<SeatSegments> seatSegments) {
        this.segments = seatSegments;
    }
}
