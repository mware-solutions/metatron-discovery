package app.metatron.discovery.spec.druid.ingestion.io;

import javax.validation.constraints.NotNull;
import java.util.List;

public class TsvInputFormat implements InputFormat {
    @NotNull
    List<String> columns;

    String delimiter;

    Integer skipHeaderRows;

    String listDelimiter;

    public TsvInputFormat() {
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public Integer getSkipHeaderRows() {
        return skipHeaderRows;
    }

    public void setSkipHeaderRows(Boolean skipHeaderRows) {
        this.skipHeaderRows = skipHeaderRows ? 1 : 0;
    }

    public String getListDelimiter() {
        return listDelimiter;
    }

    public void setListDelimiter(String listDelimiter) {
        this.listDelimiter = listDelimiter;
    }
}
