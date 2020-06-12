/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.metatron.discovery.spec.druid.ingestion;

import app.metatron.discovery.query.druid.Aggregation;
import app.metatron.discovery.spec.druid.ingestion.granularity.GranularitySpec;
import app.metatron.discovery.spec.druid.ingestion.parser.CsvStreamParser;
import app.metatron.discovery.spec.druid.ingestion.parser.DimensionsSpec;
import app.metatron.discovery.spec.druid.ingestion.parser.Parser;
import app.metatron.discovery.spec.druid.ingestion.parser.TimestampSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by kyungtaak on 2016. 6. 17..
 */
public class DataSchema {

    @NotNull
    String dataSource;

    @NotNull
    @JsonProperty("timestampSpec")
    TimestampSpec timestampSpec;

    @NotNull
    @JsonProperty("dimensionsSpec")
    DimensionsSpec dimensionsSpec;

    @NotNull
    List<Aggregation> metricsSpec;

    @NotNull
    GranularitySpec granularitySpec;

    /**
     * Perform type casting at the time of parsing row, if true.
     */
    Boolean enforceType = true;

    /**
     * Fill in verification conditions
     */
    List<Validation> validations;

    /**
     * Fill in the alternative conditions
     */
    List<Evaluation> evaluations;

    @JsonIgnore
    Parser parser;

    public DataSchema() {
    }

    public void addMetrics(Aggregation aggregation) {

        if (metricsSpec == null) {
            metricsSpec = Lists.newArrayList();
        }
        if (aggregation != null) {
            metricsSpec.add(aggregation);
        }
    }

    public void addValidation(Validation validation) {
        if (validations == null) {
            validations = Lists.newArrayList();
        }
        validations.add(validation);
    }

    public void addEvaluation(Evaluation evaluation) {
        if (evaluations == null) {
            evaluations = Lists.newArrayList();
        }
        evaluations.add(evaluation);
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void setParser(Parser parser) {
        CsvStreamParser csvStreamParser = (CsvStreamParser) parser;
        this.timestampSpec = csvStreamParser.getTimestampSpec();
        this.dimensionsSpec = csvStreamParser.getDimensionsSpec();
        this.parser = parser;
    }

    public List<Aggregation> getMetricsSpec() {
        return metricsSpec;
    }

    public void setMetricsSpec(List<Aggregation> metricsSpec) {
        this.metricsSpec = metricsSpec;
    }

    public GranularitySpec getGranularitySpec() {
        return granularitySpec;
    }

    public void setGranularitySpec(GranularitySpec granularitySpec) {
        this.granularitySpec = granularitySpec;
    }

    public Boolean getEnforceType() {
        return enforceType;
    }

    public void setEnforceType(Boolean enforceType) {
        this.enforceType = enforceType;
    }

    public List<Validation> getValidations() {
        return validations;
    }

    public void setValidations(List<Validation> validations) {
        this.validations = validations;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public Parser getParser() {
        return parser;
    }
}
