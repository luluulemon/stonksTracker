package com.visa.ssf.stonksTracker.Model;

public class Mover {
    /*
     *  "change": 0.04364569961489098,
        "description": "Micron Technology, Inc. - Common Stock",
        "direction": "up",
        "last": 65.04,
        "symbol": "MU",
        "totalVolume": 16194051
     */

     private String symbol;
     private String description;
     private Float change;
     private Float last;
     private String direction;
     private Integer totalVolume;
     private String indices;

     public String getIndices() {
         return indices;
     }
 
     public void setIndices(String indices) {
         this.indices = indices;
     }
     
    public Integer getTotalVolume() {
        return totalVolume;
    }
    public void setTotalVolumne(Integer totalVolume) {
        this.totalVolume = totalVolume;
    }
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Float getChange() {
        return change;
    }
    public void setChange(Float change) {
        this.change = change;
    }
    public Float getLast() {
        return last;
    }
    public void setLast(Float last) {
        this.last = last;
    }
    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }

}
