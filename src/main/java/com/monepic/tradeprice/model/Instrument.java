package com.monepic.tradeprice.model;

import java.util.Objects;

public class Instrument {

    private String symbol, description;

    public Instrument() {}

    public Instrument(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String getSymbol() { return symbol; }

    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Instrument)) {
            return false;
        }
        Instrument that = (Instrument) obj;
        return Objects.equals(symbol, that.symbol)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, description);
    }

    @Override
    public String toString() {
        return "Instrument{" +
                "symbol='" + symbol + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
