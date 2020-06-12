package app.metatron.discovery.spec.druid.ingestion.input;

import javax.validation.constraints.NotNull;

public class LocalInputSource implements InputSource {
    @NotNull
    String type;

    @NotNull
    String filter;

    @NotNull
    String baseDir;

    public LocalInputSource() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}
